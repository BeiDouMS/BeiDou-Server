/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gms.scripting.event;

import org.gms.net.server.channel.Channel;
import org.slf4j.LoggerFactory;
import org.gms.scripting.AbstractScriptManager;
import org.gms.scripting.SynchronizedInvocable;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件脚本管理器，负责加载、初始化和执行 JavaScript 事件脚本
 * @author Matze
 */
public class EventScriptManager extends AbstractScriptManager {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EventScriptManager.class); // SLF4J 日志实例
    private static final String INJECTED_VARIABLE_NAME = "em"; // 注入到 JS 引擎的变量名
    private static EventEntry fallback; // 后备事件（如默认事件）
    private final Map<String, EventEntry> events = new ConcurrentHashMap<>(); // 存储事件名与事件实体的映射
    private boolean active = false; // 管理器是否激活

    /**
     * 事件实体类，封装 JS 调用接口和事件管理器
     */
    private static class EventEntry {
        public Invocable iv; // 可调用的 JS 引擎接口
        public EventManager em; // 事件管理器

        public EventEntry(Invocable iv, EventManager em) {
            this.iv = iv; // 初始化 JS 调用接口
            this.em = em; // 初始化事件管理器
        }
    }

    /**
     * 构造函数，加载并初始化所有事件脚本
     * @param channel 游戏频道（上下文）
     * @param scripts 事件脚本名称数组
     */
    public EventScriptManager(final Channel channel, String[] scripts) {
        for (String script : scripts) {
            if (!script.isEmpty()) {
                events.put(script, initializeEventEntry(script, channel)); // 加载并存储每个脚本
            }
        }

        init(); // 初始化所有事件
        fallback = events.remove("0_EXAMPLE"); // 移除并保留后备事件
    }

    /**
     * 获取指定事件的事件管理器
     * @param event 事件名称
     * @return 对应的事件管理器，若不存在则返回后备事件
     */
    public EventManager getEventManager(String event) {
        EventEntry entry = events.get(event); // 查找事件
        if (entry == null) {
            return fallback.em; // 返回后备事件
        }
        return entry.em; // 返回找到的事件
    }

    /**
     * 检查事件管理器是否激活
     * @return true 表示已激活
     */
    public boolean isActive() {
        return active; // 返回激活状态
    }

    /**
     * 初始化所有事件脚本，调用其 JS 中的 init() 函数
     */
    public final void init() {
        for (EventEntry entry : events.values()) {
            try {
                entry.iv.invokeFunction("init", (Object) null); // 调用 JS 的 init 方法
            } catch (Exception ex) {
                log.error("Error on script（事件脚本初始化出错）: {}", entry.em.getName(), ex); // 记录错误日志
            }
        }

        active = events.size() > 1; // 如果事件数 >1，标记为激活状态
    }

    /**
     * 重新加载所有事件脚本
     */
    private void reloadScripts() {
        Set<Entry<String, EventEntry>> eventEntries = new HashSet<>(events.entrySet()); // 复制当前事件集合
        if (eventEntries.isEmpty()) {
            return; // 无事件时直接返回
        }

        Channel channel = eventEntries.iterator().next().getValue().em.getChannelServer(); // 获取频道上下文
        for (Entry<String, EventEntry> entry : eventEntries) {
            String script = entry.getKey();
            events.put(script, initializeEventEntry(script, channel)); // 重新加载每个脚本
        }
    }

    /**
     * 初始化单个事件脚本的入口
     * @param script 脚本名称
     * @param channel 游戏频道
     * @return 事件实体（包含 JS 引擎和事件管理器）
     */
    private EventEntry initializeEventEntry(String script, Channel channel) {
        ScriptEngine engine = getInvocableScriptEngine("event/" + script + ".js"); // 获取 JS 引擎
        Invocable iv = SynchronizedInvocable.of((Invocable) engine); // 包装为线程安全的调用接口
        EventManager eventManager = new EventManager(channel, iv, script); // 创建事件管理器
        engine.put(INJECTED_VARIABLE_NAME, eventManager); // 向 JS 引擎注入变量 "em"
        return new EventEntry(iv, eventManager); // 返回事件实体
    }

    /**
     * 重新加载所有事件脚本（外部调用入口）
     */
    public void reload() {
        cancel(); // 取消当前所有事件
        reloadScripts(); // 重新加载脚本
        init(); // 重新初始化
    }

    /**
     * 取消所有事件执行
     */
    public void cancel() {
        active = false; // 标记为未激活
        for (EventEntry entry : events.values()) {
            entry.em.cancel(); // 调用每个事件的取消方法
        }
    }

    /**
     * 销毁事件管理器，清理资源
     */
    public void dispose() {
        if (events.isEmpty()) {
            return; // 无事件时直接返回
        }

        Set<EventEntry> eventEntries = new HashSet<>(events.values()); // 复制事件集合
        events.clear(); // 清空映射表

        active = false; // 标记为未激活
        for (EventEntry entry : eventEntries) {
            entry.em.cancel(); // 取消所有事件
        }
    }
}