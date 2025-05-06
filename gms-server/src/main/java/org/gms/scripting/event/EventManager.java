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

import org.gms.client.Character;
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;
import org.gms.net.server.guild.Guild;
import org.gms.net.server.world.Party;
import org.gms.net.server.world.PartyCharacter;
import org.gms.net.server.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.scripting.event.scheduler.EventScriptScheduler;
import org.gms.server.Marriage;
import org.gms.server.ThreadManager;
import org.gms.server.expeditions.Expedition;
import org.gms.server.life.LifeFactory;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapleMap;
import org.gms.server.quest.Quest;
import org.gms.exception.EventInstanceInProgressException;

import javax.script.Invocable;
import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * 事件管理器，负责管理游戏中的各种事件实例
 * @author Matze
 * @author Ronan
 */
public class EventManager {
    private static final Logger log = LoggerFactory.getLogger(EventManager.class);
    private Invocable iv;  // 可调用的脚本引擎
    private Channel cserv;  // 频道服务器
    private World wserv;  // 世界服务器
    private Server server;  // 主服务器
    private final EventScriptScheduler ess = new EventScriptScheduler();  // 事件脚本调度器
    private final Map<String, EventInstanceManager> instances = new HashMap<>();  // 事件实例映射表
    private final Map<String, Integer> instanceLocks = new HashMap<>();  // 实例锁定映射表
    private final Queue<Integer> queuedGuilds = new LinkedList<>();  // 排队中的公会队列
    private final Map<Integer, Integer> queuedGuildLeaders = new HashMap<>();  // 排队公会及其会长映射
    private final List<Boolean> openedLobbys;  // 已开启的大厅列表
    private final List<EventInstanceManager> readyInstances = new LinkedList<>();  // 准备就绪的实例队列
    private Integer readyId = 0, onLoadInstances = 0;  // 准备ID和加载中的实例数
    private final Properties props = new Properties();  // 属性配置
    private final String name;  // 事件名称
    private final Lock lobbyLock = new ReentrantLock();  // 大厅锁
    private final Lock queueLock = new ReentrantLock();  // 队列锁
    private final Lock startLock = new ReentrantLock();  // 启动锁

    private final Set<Integer> playerPermit = new HashSet<>();  // 玩家许可集合
    private final Semaphore startSemaphore = new Semaphore(7);  // 启动信号量

    private static final int maxLobbys = 8;     // 一个事件管理器最多支持同时运行的大厅数量

    /**
     * 构造函数
     * @param cserv 频道服务器
     * @param iv 可调用的脚本引擎
     * @param name 事件名称
     */
    public EventManager(Channel cserv, Invocable iv, String name) {
        this.server = Server.getInstance();
        this.iv = iv;
        this.cserv = cserv;
        this.wserv = server.getWorld(cserv.getWorld());
        this.name = name;

        this.openedLobbys = new ArrayList<>();
        for (int i = 0; i < maxLobbys; i++) {
            this.openedLobbys.add(false);
        }
    }

    /**
     * 检查事件管理器是否已释放
     * @return 是否已释放
     */
    private boolean isDisposed() {
        return onLoadInstances <= -1000;
    }

    /**
     * 取消事件管理器（确保在没有玩家在线时调用）
     */
    public void cancel() {
        ess.dispose();

        try {
            iv.invokeFunction("cancelSchedule", (Object) null);
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        Collection<EventInstanceManager> eimList;
        synchronized (instances) {
            eimList = getInstances();
            instances.clear();
        }

        for (EventInstanceManager eim : eimList) {
            eim.dispose(true);
        }

        List<EventInstanceManager> readyEims;
        queueLock.lock();
        try {
            readyEims = new ArrayList<>(readyInstances);
            readyInstances.clear();
            onLoadInstances = Integer.MIN_VALUE / 2;
        } finally {
            queueLock.unlock();
        }

        for (EventInstanceManager eim : readyEims) {
            eim.dispose(true);
        }

        props.clear();
        cserv = null;
        wserv = null;
        server = null;
        iv = null;
    }

    /**
     * 将对象列表转换为整数列表
     * @param objects 对象列表
     * @return 整数列表
     */
    private List<Integer> convertToIntegerList(List<Object> objects) {
        List<Integer> intList = new ArrayList<>();

        for (Object object : objects) {
            intList.add((Integer) object);
        }

        return intList;
    }

    /**
     * 获取大厅延迟时间
     * @return 延迟时间（毫秒）
     */
    public long getLobbyDelay() {
        return GameConfig.getServerLong("event_lobby_delay");
    }

    /**
     * 获取最大大厅数量
     * @return 最大大厅数量
     */
    private int getMaxLobbies() {
        try {
            return (int) iv.invokeFunction("getMaxLobbies");
        } catch (ScriptException | NoSuchMethodException ex) { // 如果没有定义大厅范围
            return maxLobbys;
        }
    }

    /**
     * 调度事件方法
     * @param methodName 方法名
     * @param delay 延迟时间
     * @return 事件调度未来对象
     */
    public EventScheduledFuture schedule(String methodName, long delay) {
        return schedule(methodName, null, delay);
    }

    /**
     * 调度事件方法（带事件实例）
     * @param methodName 方法名
     * @param eim 事件实例管理器
     * @param delay 延迟时间
     * @return 事件调度未来对象
     */
    public EventScheduledFuture schedule(final String methodName, final EventInstanceManager eim, long delay) {
        Runnable r = () -> {
            try {
                iv.invokeFunction(methodName, eim);
            } catch (ScriptException | NoSuchMethodException ex) {
                log.error("eim（"+eim+"），methodName（"+methodName+"），Event script schedule（事件脚本时间表）", ex);
            }
        };

        ess.registerEntry(r, delay);
        return new EventScheduledFuture(r, ess);
    }

    /**
     * 在指定时间戳调度事件
     * @param methodName 方法名
     * @param timestamp 时间戳
     * @return 事件调度未来对象
     */
    public EventScheduledFuture scheduleAtTimestamp(final String methodName, long timestamp) {
        Runnable r = () -> {
            try {
                iv.invokeFunction(methodName, (Object) null);
            } catch (ScriptException | NoSuchMethodException ex) {
                log.error("Event script scheduleAtTimestamp（事件脚本调度时间戳）", ex);
            }
        };

        ess.registerEntry(r, timestamp - server.getCurrentTime());
        return new EventScheduledFuture(r, ess);
    }

    /**
     * 获取世界服务器
     * @return 世界服务器
     */
    public World getWorldServer() {
        return wserv;
    }

    /**
     * 获取频道服务器
     * @return 频道服务器
     */
    public Channel getChannelServer() {
        return cserv;
    }

    /**
     * 获取可调用的脚本引擎
     * @return 可调用的脚本引擎
     */
    public Invocable getIv() {
        return iv;
    }

    /**
     * 获取指定名称的事件实例
     * @param name 实例名称
     * @return 事件实例管理器
     */
    public EventInstanceManager getInstance(String name) {
        return instances.get(name);
    }

    /**
     * 获取所有事件实例
     * @return 事件实例集合
     */
    public Collection<EventInstanceManager> getInstances() {
        synchronized (instances) {
            return new LinkedList<>(instances.values());
        }
    }

    /**
     * 创建新的事件实例
     * @param name 实例名称
     * @return 事件实例管理器
     * @throws EventInstanceInProgressException 如果实例已存在
     */
    public EventInstanceManager newInstance(String name) throws EventInstanceInProgressException {
        EventInstanceManager ret = getReadyInstance();

        if (ret == null) {
            ret = new EventInstanceManager(this, name);
        } else {
            ret.setName(name);
        }

        synchronized (instances) {
            if (instances.containsKey(name)) {
                throw new EventInstanceInProgressException(name, this.getName());
            }

            instances.put(name, ret);
        }
        return ret;
    }

    /**
     * 创建新的婚姻实例
     * @param name 实例名称
     * @return 婚姻实例
     * @throws EventInstanceInProgressException 如果实例已存在
     */
    public Marriage newMarriage(String name) throws EventInstanceInProgressException {
        Marriage ret = new Marriage(this, name);

        synchronized (instances) {
            if (instances.containsKey(name)) {
                throw new EventInstanceInProgressException(name, this.getName());
            }

            instances.put(name, ret);
        }
        return ret;
    }

    /**
     * 释放指定名称的实例
     * @param name 实例名称
     */
    public void disposeInstance(final String name) {
        ess.registerEntry(() -> {
            freeLobbyInstance(name);

            synchronized (instances) {
                instances.remove(name);
            }
        }, SECONDS.toMillis(GameConfig.getServerLong("event_lobby_delay")));
    }

    /**
     * 设置属性
     * @param key 属性键
     * @param value 属性值
     */
    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    /**
     * 设置整数属性
     * @param key 属性键
     * @param value 属性值
     */
    public void setIntProperty(String key, int value) {
        setProperty(key, value);
    }

    /**
     * 设置属性（整数）
     * @param key 属性键
     * @param value 属性值
     */
    public void setProperty(String key, int value) {
        props.setProperty(key, value + "");
    }

    /**
     * 获取属性
     * @param key 属性键
     * @return 属性值
     */
    public String getProperty(String key) {
        return props.getProperty(key);
    }

    /**
     * 获取整数属性
     * @param key 属性键
     * @return 属性值
     */
    public int getIntProperty(String key) {
        return Integer.parseInt(props.getProperty(key));
    }

    /**
     * 设置大厅锁定状态
     * @param lobbyId 大厅ID
     * @param lock 是否锁定
     */
    private void setLockLobby(int lobbyId, boolean lock) {
        lobbyLock.lock();
        try {
            openedLobbys.set(lobbyId, lock);
        } finally {
            lobbyLock.unlock();
        }
    }

    /**
     * 启动大厅实例
     * @param lobbyId 大厅ID
     * @return 是否成功启动
     */
    private boolean startLobbyInstance(int lobbyId) {
        lobbyLock.lock();
        try {
            if (lobbyId < 0) {
                lobbyId = 0;
            } else if (lobbyId >= maxLobbys) {
                lobbyId = maxLobbys - 1;
            }

            if (!openedLobbys.get(lobbyId)) {
                openedLobbys.set(lobbyId, true);
                return true;
            }

            return false;
        } finally {
            lobbyLock.unlock();
        }
    }

    /**
     * 释放大厅实例
     * @param lobbyName 大厅名称
     */
    private void freeLobbyInstance(String lobbyName) {
        Integer i = instanceLocks.get(lobbyName);
        if (i == null) {
            return;
        }

        instanceLocks.remove(lobbyName);
        if (i > -1) {
            setLockLobby(i, false);
        }
    }

    /**
     * 获取事件名称
     * @return 事件名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取可用的大厅实例ID
     * @return 大厅ID，如果没有可用则返回-1
     */
    private int availableLobbyInstance() {
        int maxLobbies = getMaxLobbies();

        if (maxLobbies > 0) {
            for (int i = 0; i < maxLobbies; i++) {
                if (startLobbyInstance(i)) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * 获取内部脚本异常消息
     * @param a 异常对象
     * @return 异常消息
     */
    private String getInternalScriptExceptionMessage(Throwable a) {
        if (!(a instanceof ScriptException)) {
            return null;
        }

        while (true) {
            Throwable t = a;
            a = a.getCause();

            if (a == null) {
                return t.getMessage();
            }
        }
    }

    /**
     * 创建事件实例
     * @param name 实例名称
     * @param args 参数
     * @return 事件实例管理器
     * @throws ScriptException 脚本异常
     * @throws NoSuchMethodException 方法不存在异常
     */
    private EventInstanceManager createInstance(String name, Object... args) throws ScriptException, NoSuchMethodException {
        return (EventInstanceManager) iv.invokeFunction(name, args);
    }

    /**
     * 注册事件实例
     * @param eventName 事件名称
     * @param lobbyId 大厅ID
     */
    private void registerEventInstance(String eventName, int lobbyId) {
        Integer oldLobby = instanceLocks.get(eventName);
        if (oldLobby != null) {
            setLockLobby(oldLobby, false);
        }

        instanceLocks.put(eventName, lobbyId);
    }

    /**
     * 启动远征队实例
     * @param exped 远征队
     * @return 是否成功启动
     */
    public boolean startInstance(Expedition exped) {
        return startInstance(-1, exped);
    }

    /**
     * 启动远征队实例（指定大厅）
     * @param lobbyId 大厅ID
     * @param exped 远征队
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, Expedition exped) {
        return startInstance(lobbyId, exped, exped.getLeader());
    }

    /**
     * 启动远征队实例（指定大厅和队长）
     * @param lobbyId 大厅ID
     * @param exped 远征队
     * @param leader 队长
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, Expedition exped, Character leader) {
        if (this.isDisposed()) {
            return false;
        }

        try {
            if (!playerPermit.contains(leader.getId()) && startSemaphore.tryAcquire(7777, MILLISECONDS)) {
                playerPermit.add(leader.getId());

                startLock.lock();
                try {
                    try {
                        if (lobbyId == -1) {
                            lobbyId = availableLobbyInstance();
                            if (lobbyId == -1) {
                                return false;
                            }
                        } else {
                            if (!startLobbyInstance(lobbyId)) {
                                return false;
                            }
                        }

                        EventInstanceManager eim;
                        try {
                            eim = createInstance("setup", leader.getClient().getChannel());
                            registerEventInstance(eim.getName(), lobbyId);
                        } catch (ScriptException | NullPointerException e) {
                            String message = getInternalScriptExceptionMessage(e);
                            if (message != null && !message.startsWith(EventInstanceInProgressException.EIIP_KEY)) {
                                throw e;
                            }

                            if (lobbyId > -1) {
                                setLockLobby(lobbyId, false);
                            }
                            return false;
                        }

                        eim.setLeader(leader);

                        exped.start();
                        eim.registerExpedition(exped);

                        eim.startEvent();
                    } catch (ScriptException | NoSuchMethodException ex) {
                        log.error("Event script startInstance（事件脚本startInstance）", ex);
                    }

                    return true;
                } finally {
                    startLock.unlock();
                    playerPermit.remove(leader.getId());
                    startSemaphore.release();
                }
            }
        } catch (InterruptedException ie) {
            playerPermit.remove(leader.getId());
        }

        return false;
    }

    /**
     * 启动玩家实例
     * @param chr 玩家
     * @return 是否成功启动
     */
    public boolean startInstance(Character chr) {
        return startInstance(-1, chr);
    }

    /**
     * 启动玩家实例（指定大厅）
     * @param lobbyId 大厅ID
     * @param leader 队长
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, Character leader) {
        return startInstance(lobbyId, leader, leader, 1);
    }

    /**
     * 启动玩家实例（指定大厅、玩家、队长和难度）
     * @param lobbyId 大厅ID
     * @param chr 玩家
     * @param leader 队长
     * @param difficulty 难度
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, Character chr, Character leader, int difficulty) {
        if (this.isDisposed()) {
            return false;
        }

        try {
            if (!playerPermit.contains(leader.getId()) && startSemaphore.tryAcquire(7777, MILLISECONDS)) {
                playerPermit.add(leader.getId());

                startLock.lock();
                try {
                    try {
                        if (lobbyId == -1) {
                            lobbyId = availableLobbyInstance();
                            if (lobbyId == -1) {
                                return false;
                            }
                        } else {
                            if (!startLobbyInstance(lobbyId)) {
                                return false;
                            }
                        }

                        EventInstanceManager eim;
                        try {
                            eim = createInstance("setup", difficulty, (lobbyId > -1) ? lobbyId : leader.getId());
                            registerEventInstance(eim.getName(), lobbyId);
                        } catch (ScriptException | NullPointerException e) {
                            String message = getInternalScriptExceptionMessage(e);
                            if (message != null && !message.startsWith(EventInstanceInProgressException.EIIP_KEY)) {
                                throw e;
                            }

                            if (lobbyId > -1) {
                                setLockLobby(lobbyId, false);
                            }
                            return false;
                        }
                        eim.setLeader(leader);

                        if (chr != null) {
                            eim.registerPlayer(chr);
                        }

                        eim.startEvent();
                    } catch (ScriptException | NoSuchMethodException ex) {
                        log.error("Event script startInstance（事件脚本startInstance）", ex);
                    }

                    return true;
                } finally {
                    startLock.unlock();
                    playerPermit.remove(leader.getId());
                    startSemaphore.release();
                }
            }
        } catch (InterruptedException ie) {
            playerPermit.remove(leader.getId());
        }

        return false;
    }

    /**
     * 启动队伍实例（PQ）
     * @param party 队伍
     * @param map 地图
     * @return 是否成功启动
     */
    public boolean startInstance(Party party, MapleMap map) {
        return startInstance(-1, party, map);
    }

    /**
     * 启动队伍实例（PQ，指定大厅）
     * @param lobbyId 大厅ID
     * @param party 队伍
     * @param map 地图
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, Party party, MapleMap map) {
        return startInstance(lobbyId, party, map, party.getLeader().getPlayer());
    }

    /**
     * 启动队伍实例（PQ，指定大厅和队长）
     * @param lobbyId 大厅ID
     * @param party 队伍
     * @param map 地图
     * @param leader 队长
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, Party party, MapleMap map, Character leader) {
        if (this.isDisposed()) {
            return false;
        }

        try {
            if (!playerPermit.contains(leader.getId()) && startSemaphore.tryAcquire(7777, MILLISECONDS)) {
                playerPermit.add(leader.getId());

                startLock.lock();
                try {
                    try {
                        if (lobbyId == -1) {
                            lobbyId = availableLobbyInstance();
                            if (lobbyId == -1) {
                                return false;
                            }
                        } else {
                            if (!startLobbyInstance(lobbyId)) {
                                return false;
                            }
                        }

                        EventInstanceManager eim;
                        try {
                            eim = createInstance("setup", (Object) null);
                            registerEventInstance(eim.getName(), lobbyId);
                        } catch (ScriptException | NullPointerException e) {
                            String message = getInternalScriptExceptionMessage(e);
                            if (message != null && !message.startsWith(EventInstanceInProgressException.EIIP_KEY)) {
                                throw e;
                            }

                            if (lobbyId > -1) {
                                setLockLobby(lobbyId, false);
                            }
                            return false;
                        }

                        eim.setLeader(leader);

                        eim.registerParty(party, map);
                        party.setEligibleMembers(null);

                        eim.startEvent();
                    } catch (ScriptException | NoSuchMethodException ex) {
                        log.error("Event script startInstance（事件脚本startInstance）", ex);
                    }

                    return true;
                } finally {
                    startLock.unlock();
                    playerPermit.remove(leader.getId());
                    startSemaphore.release();
                }
            }
        } catch (InterruptedException ie) {
            playerPermit.remove(leader.getId());
        }

        return false;
    }

    /**
     * 启动队伍实例（PQ，带难度）
     * @param party 队伍
     * @param map 地图
     * @param difficulty 难度
     * @return 是否成功启动
     */
    public boolean startInstance(Party party, MapleMap map, int difficulty) {
        return startInstance(-1, party, map, difficulty);
    }

    /**
     * 启动队伍实例（PQ，指定大厅和难度）
     * @param lobbyId 大厅ID
     * @param party 队伍
     * @param map 地图
     * @param difficulty 难度
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, Party party, MapleMap map, int difficulty) {
        return startInstance(lobbyId, party, map, difficulty, party.getLeader().getPlayer());
    }

    /**
     * 启动队伍实例（PQ，指定大厅、难度和队长）
     * @param lobbyId 大厅ID
     * @param party 队伍
     * @param map 地图
     * @param difficulty 难度
     * @param leader 队长
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, Party party, MapleMap map, int difficulty, Character leader) {
        if (this.isDisposed()) {
            return false;
        }

        try {
            if (!playerPermit.contains(leader.getId()) && startSemaphore.tryAcquire(7777, MILLISECONDS)) {
                playerPermit.add(leader.getId());

                startLock.lock();
                try {
                    try {
                        if (lobbyId == -1) {
                            lobbyId = availableLobbyInstance();
                            if (lobbyId == -1) {
                                return false;
                            }
                        } else {
                            if (!startLobbyInstance(lobbyId)) {
                                return false;
                            }
                        }

                        EventInstanceManager eim;
                        try {
                            eim = createInstance("setup", difficulty, (lobbyId > -1) ? lobbyId : party.getLeaderId());
                            registerEventInstance(eim.getName(), lobbyId);
                        } catch (ScriptException | NullPointerException e) {
                            String message = getInternalScriptExceptionMessage(e);
                            if (message != null && !message.startsWith(EventInstanceInProgressException.EIIP_KEY)) {
                                throw e;
                            }

                            if (lobbyId > -1) {
                                setLockLobby(lobbyId, false);
                            }
                            return false;
                        }

                        eim.setLeader(leader);

                        eim.registerParty(party, map);
                        party.setEligibleMembers(null);

                        eim.startEvent();
                    } catch (ScriptException | NoSuchMethodException ex) {
                        log.error("Event script startInstance（事件脚本启动实例）", ex);
                    }

                    return true;
                } finally {
                    startLock.unlock();
                    playerPermit.remove(leader.getId());
                    startSemaphore.release();
                }
            }
        } catch (InterruptedException ie) {
            playerPermit.remove(leader.getId());
        }

        return false;
    }

    /**
     * 启动非PQ事件实例
     * @param eim 事件实例管理器
     * @param ldr 队长名称
     * @return 是否成功启动
     */
    public boolean startInstance(EventInstanceManager eim, String ldr) {
        return startInstance(-1, eim, ldr);
    }

    /**
     * 启动非PQ事件实例（指定队长）
     * @param eim 事件实例管理器
     * @param ldr 队长
     * @return 是否成功启动
     */
    public boolean startInstance(EventInstanceManager eim, Character ldr) {
        return startInstance(-1, eim, ldr.getName(), ldr);
    }

    /**
     * 启动非PQ事件实例（指定大厅）
     * @param lobbyId 大厅ID
     * @param eim 事件实例管理器
     * @param ldr 队长名称
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, EventInstanceManager eim, String ldr) {
        return startInstance(-1, eim, ldr, eim.getEm().getChannelServer().getPlayerStorage().getCharacterByName(ldr));
    }

    /**
     * 启动非PQ事件实例（指定大厅和队长）
     * @param lobbyId 大厅ID
     * @param eim 事件实例管理器
     * @param ldr 队长名称
     * @param leader 队长
     * @return 是否成功启动
     */
    public boolean startInstance(int lobbyId, EventInstanceManager eim, String ldr, Character leader) {
        if (this.isDisposed()) {
            return false;
        }

        try {
            if (!playerPermit.contains(leader.getId()) && startSemaphore.tryAcquire(7777, MILLISECONDS)) {
                playerPermit.add(leader.getId());

                startLock.lock();
                try {
                    try {
                        if (lobbyId == -1) {
                            lobbyId = availableLobbyInstance();
                            if (lobbyId == -1) {
                                return false;
                            }
                        } else {
                            if (!startLobbyInstance(lobbyId)) {
                                return false;
                            }
                        }

                        if (eim == null) {
                            if (lobbyId > -1) {
                                setLockLobby(lobbyId, false);
                            }
                            return false;
                        }
                        registerEventInstance(eim.getName(), lobbyId);
                        eim.setLeader(leader);

                        iv.invokeFunction("setup", eim);
                        eim.setProperty("leader", ldr);

                        eim.startEvent();
                    } catch (ScriptException | NoSuchMethodException ex) {
                        log.error("Event script startInstance（事件脚本启动实例）", ex);
                    }

                    return true;
                } finally {
                    startLock.unlock();
                    playerPermit.remove(leader.getId());
                    startSemaphore.release();
                }
            }
        } catch (InterruptedException ie) {
            playerPermit.remove(leader.getId());
        }

        return false;
    }

    /**
     * 获取符合条件的队伍成员
     * @param party 队伍
     * @return 符合条件的成员列表
     */
    public List<PartyCharacter> getEligibleParty(Party party) {
        if (party == null) {
            return new ArrayList<>();
        }
        try {
            Object o = iv.invokeFunction("getEligibleParty", party.getPartyMembersOnline());

            if (o instanceof PartyCharacter[] partyChrs) {
                final List<PartyCharacter> eligibleParty = new ArrayList<>(Arrays.asList(partyChrs));
                party.setEligibleMembers(eligibleParty);
                return eligibleParty;
            }
        } catch (ScriptException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * 清除PQ
     * @param eim 事件实例管理器
     */
    public void clearPQ(EventInstanceManager eim) {
        try {
            iv.invokeFunction("clearPQ", eim);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error("Event script clearPQ（事件脚本清除PQ）", ex);
        }
    }

    /**
     * 清除PQ并传送到指定地图
     * @param eim 事件实例管理器
     * @param toMap 目标地图
     */
    public void clearPQ(EventInstanceManager eim, MapleMap toMap) {
        try {
            iv.invokeFunction("clearPQ", eim, toMap);
        } catch (ScriptException | NoSuchMethodException ex) {
            log.error("Event script clearPQ（事件脚本清除PQ）", ex);
        }
    }

    /**
     * 获取怪物对象
     * @param mid 怪物ID
     * @return 怪物对象
     */
    public Monster getMonster(int mid) {
        return (LifeFactory.getMonster(mid));
    }

    /**
     * 通知公会准备就绪
     * @param guildId 公会ID
     */
    private void exportReadyGuild(Integer guildId) {
        Guild mg = server.getGuild(guildId);
        String callout = "[公会任务] 您的公会已成功报名参加频道 " + this.getChannelServer().getId() + " 的" +
                "【家族对抗赛】，当前已进入战略准备阶段。3分钟后将禁止新成员加入任务。" +
                " 请前往勇士之都挖掘现场寻找NPC双了解更多详情。";

        mg.dropMessage(6, callout);
    }

    /**
     * 通知公会队列位置
     * @param guildId 公会ID
     * @param place 队列位置
     */
    private void exportMovedQueueToGuild(Integer guildId, int place) {
        Guild mg = server.getGuild(guildId);
        String callout = "[公会任务] 您的公会已成功报名参加频道 " + this.getChannelServer().getId() + " 的" +
                "【家族对抗赛】，当前在等待队列中排名第 " + GameConstants.ordinal(place) + " 位。";

        mg.dropMessage(6, callout);
    }

    /**
     * 获取下一个排队的公会
     * @return 公会ID和会长ID列表
     */
    private List<Integer> getNextGuildQueue() {
        synchronized (queuedGuilds) {
            Integer guildId = queuedGuilds.poll();
            if (guildId == null) {
                return null;
            }

            wserv.removeGuildQueued(guildId);
            Integer leaderId = queuedGuildLeaders.remove(guildId);

            int place = 1;
            for (Integer i : queuedGuilds) {
                exportMovedQueueToGuild(i, place);
                place++;
            }

            List<Integer> list = new ArrayList<>(2);
            list.add(guildId);
            list.add(leaderId);
            return list;
        }
    }

    /**
     * 检查队列是否已满
     * @return 是否已满
     */
    public boolean isQueueFull() {
        synchronized (queuedGuilds) {
            return queuedGuilds.size() >= GameConfig.getServerInt("event_max_guild_queue");
        }
    }

    /**
     * 获取队列大小
     * @return 队列大小
     */
    public int getQueueSize() {
        synchronized (queuedGuilds) {
            return queuedGuilds.size();
        }
    }

    /**
     * 添加公会到队列
     * @param guildId 公会ID
     * @param leaderId 会长ID
     * @return 添加结果（-1=已在队列，0=队列已满，1=成功加入，2=成功并立即开始）
     */
    public byte addGuildToQueue(Integer guildId, Integer leaderId) {
        if (wserv.isGuildQueued(guildId)) {
            return -1;
        }

        if (!isQueueFull()) {
            boolean canStartAhead;
            synchronized (queuedGuilds) {
                canStartAhead = queuedGuilds.isEmpty();

                queuedGuilds.add(guildId);
                wserv.putGuildQueued(guildId);
                queuedGuildLeaders.put(guildId, leaderId);

                int place = queuedGuilds.size();
                exportMovedQueueToGuild(guildId, place);
            }

            if (canStartAhead) {
                if (!attemptStartGuildInstance()) {
                    synchronized (queuedGuilds) {
                        queuedGuilds.add(guildId);
                        wserv.putGuildQueued(guildId);
                        queuedGuildLeaders.put(guildId, leaderId);
                    }
                } else {
                    return 2;
                }
            }

            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 尝试启动公会实例
     * @return 是否成功启动
     */
    public boolean attemptStartGuildInstance() {
        Character chr = null;
        List<Integer> guildInstance = null;
        while (chr == null) {
            guildInstance = getNextGuildQueue();
            if (guildInstance == null) {
                return false;
            }

            chr = cserv.getPlayerStorage().getCharacterById(guildInstance.get(1));
        }

        if (startInstance(chr)) {
            exportReadyGuild(guildInstance.get(0));
            return true;
        } else {
            return false;
        }
    }

    /**
     * 强制开始任务
     * @param chr 玩家
     * @param id 任务ID
     * @param npcid NPC ID
     */
    public void startQuest(Character chr, int id, int npcid) {
        try {
            Quest.getInstance(id).forceStart(chr, npcid);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 强制完成任务
     * @param chr 玩家
     * @param id 任务ID
     * @param npcid NPC ID
     */
    public void completeQuest(Character chr, int id, int npcid) {
        try {
            Quest.getInstance(id).forceComplete(chr, npcid);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取运输时间
     * @param travelTime 旅行时间
     * @return 运输时间
     */
    public int getTransportationTime(int travelTime) {
        return this.getWorldServer().getTransportationTime(travelTime);
    }

    /**
     * 填充EIM队列
     */
    private void fillEimQueue() {
        ThreadManager.getInstance().newTask(new EventManagerTask());  // 调用新线程填充准备就绪的实例队列
    }

    /**
     * 获取准备就绪的实例
     * @return 事件实例管理器
     */
    private EventInstanceManager getReadyInstance() {
        queueLock.lock();
        try {
            if (readyInstances.isEmpty()) {
                fillEimQueue();
                return null;
            }

            EventInstanceManager eim = readyInstances.remove(0);
            fillEimQueue();

            return eim;
        } finally {
            queueLock.unlock();
        }
    }

    /**
     * 实例化队列中的实例
     */
    private void instantiateQueuedInstance() {
        int nextEventId;
        queueLock.lock();
        try {
            if (this.isDisposed() || readyInstances.size() + onLoadInstances >= Math.ceil((double) maxLobbys / 3.0)) {
                return;
            }

            onLoadInstances++;
            nextEventId = readyId;
            readyId++;
        } finally {
            queueLock.unlock();
        }

        EventInstanceManager eim = new EventInstanceManager(this, "sampleName" + nextEventId);
        queueLock.lock();
        try {
            if (this.isDisposed()) {  // 事件管理器已释放
                return;
            }

            readyInstances.add(eim);
            onLoadInstances--;
        } finally {
            queueLock.unlock();
        }

        instantiateQueuedInstance();    // 持续填充队列直到达到阈值
    }

    /**
     * 事件管理器任务类
     */
    private class EventManagerTask implements Runnable {
        @Override
        public void run() {
            instantiateQueuedInstance();
        }
    }
}