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
package org.gms.server.maps;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.config.GameConfig;
import org.gms.net.packet.Packet;
import org.gms.net.server.services.task.channel.OverallService;
import org.gms.net.server.services.type.ChannelServices;
import org.gms.scripting.reactor.ReactorScriptManager;
import org.gms.server.TimerManager;
import org.gms.server.partyquest.GuardianSpawnPoint;
import org.gms.util.PacketCreator;
import org.gms.util.Pair;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Lerk
 * @author Ronan
 */
/**
 * 反应器对象类，继承自AbstractMapObject
 */
public class Reactor extends AbstractMapObject {
    private final int rid;  // 反应器ID
    private final ReactorStats stats;  // 反应器状态统计
    private byte state;  // 当前状态
    private byte evstate;  // 事件状态
    private int delay;  // 延迟时间
    private MapleMap map;  // 所属地图
    private String name;  // 名称
    private boolean alive;  // 是否存活
    private boolean shouldCollect;  // 是否应该收集物品
    private boolean attackHit;  // 是否被攻击击中
    private ScheduledFuture<?> timeoutTask = null;  // 超时任务
    private Runnable delayedRespawnRun = null;  // 延迟重生任务
    private GuardianSpawnPoint guardian = null;  // 守卫生成点
    private byte facingDirection = 0;  // 面向方向
    private final Lock reactorLock = new ReentrantLock(true);  // 反应器锁
    private final Lock hitLock = new ReentrantLock(true);  // 击中锁

    /**
     * 构造函数
     * @param stats 反应器状态统计
     * @param rid 反应器ID
     */
    public Reactor(ReactorStats stats, int rid) {
        this.evstate = (byte) 0;  // 初始化事件状态为0
        this.stats = stats;  // 设置状态统计
        this.rid = rid;  // 设置反应器ID
        this.alive = true;  // 初始状态为存活
    }

    /**
     * 设置是否应该收集物品
     * @param collect 是否收集
     */
    public void setShouldCollect(boolean collect) {
        this.shouldCollect = collect;  // 设置收集标志
    }

    /**
     * 获取是否应该收集物品
     * @return 是否收集
     */
    public boolean getShouldCollect() {
        return shouldCollect;  // 返回收集标志
    }

    /**
     * 锁定反应器
     */
    public void lockReactor() {
        reactorLock.lock();  // 获取反应器锁
    }

    /**
     * 解锁反应器
     */
    public void unlockReactor() {
        reactorLock.unlock();  // 释放反应器锁
    }

    /**
     * 锁定击中相关操作
     */
    public void hitLockReactor() {
        hitLock.lock();  // 获取击中锁
        reactorLock.lock();  // 获取反应器锁
    }

    /**
     * 解锁击中相关操作
     */
    public void hitUnlockReactor() {
        reactorLock.unlock();  // 释放反应器锁
        hitLock.unlock();  // 释放击中锁
    }

    /**
     * 设置当前状态
     * @param state 状态值
     */
    public void setState(byte state) {
        this.state = state;  // 设置状态
    }

    /**
     * 获取当前状态
     * @return 状态值
     */
    public byte getState() {
        return state;  // 返回当前状态
    }

    /**
     * 设置事件状态
     * @param substate 事件状态值
     */
    public void setEventState(byte substate) {
        this.evstate = substate;  // 设置事件状态
    }

    /**
     * 获取事件状态
     * @return 事件状态值
     */
    public byte getEventState() {
        return evstate;  // 返回事件状态
    }

    /**
     * 获取反应器状态统计
     * @return 状态统计对象
     */
    public ReactorStats getStats() {
        return stats;  // 返回状态统计
    }

    /**
     * 获取反应器ID
     * @return 反应器ID
     */
    public int getId() {
        return rid;  // 返回反应器ID
    }

    /**
     * 设置延迟时间
     * @param delay 延迟时间(毫秒)
     */
    public void setDelay(int delay) {
        this.delay = delay;  // 设置延迟时间
    }

    /**
     * 获取延迟时间
     * @return 延迟时间(毫秒)
     */
    public int getDelay() {
        return delay;  // 返回延迟时间
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.REACTOR;  // 返回对象类型为反应器
    }

    /**
     * 获取反应器类型
     * @return 反应器类型
     */
    public int getReactorType() {
        return stats.getType(state);  // 返回当前状态的类型
    }

    /**
     * 检查是否最近被攻击击中
     * @return 是否被击中
     */
    public boolean isRecentHitFromAttack() {
        return attackHit;  // 返回攻击击中标志
    }

    /**
     * 设置所属地图
     * @param map 地图对象
     */
    public void setMap(MapleMap map) {
        this.map = map;  // 设置地图
    }

    /**
     * 获取所属地图
     * @return 地图对象
     */
    public MapleMap getMap() {
        return map;  // 返回地图
    }

    /**
     * 获取反应物品
     * @param index 物品索引
     * @return 物品ID和数量对
     */
    public Pair<Integer, Integer> getReactItem(byte index) {
        return stats.getReactItem(state, index);  // 返回指定状态的物品
    }

    /**
     * 检查是否存活
     * @return 是否存活
     */
    public boolean isAlive() {
        return alive;  // 返回存活状态
    }

    /**
     * 检查是否活跃
     * @return 是否活跃
     */
    public boolean isActive() {
        return alive && stats.getType(state) != -1;  // 存活且状态类型有效
    }

    /**
     * 设置存活状态
     * @param alive 是否存活
     */
    public void setAlive(boolean alive) {
        this.alive = alive;  // 设置存活状态
    }

    @Override
    public void sendDestroyData(Client client) {
        client.sendPacket(makeDestroyData());  // 发送销毁数据包
    }

    /**
     * 创建销毁数据包
     * @return 数据包
     */
    public final Packet makeDestroyData() {
        return PacketCreator.destroyReactor(this);  // 生成反应器销毁包
    }

    @Override
    public void sendSpawnData(Client client) {
        if (this.isAlive()) {
            client.sendPacket(makeSpawnData());  // 如果存活则发送生成数据
        }
    }

    /**
     * 创建生成数据包
     * @return 数据包
     */
    public final Packet makeSpawnData() {
        return PacketCreator.spawnReactor(this);  // 生成反应器生成包
    }

    /**
     * 重置反应器动作
     * @param newState 新状态
     */
    public void resetReactorActions(int newState) {
        setState((byte) newState);  // 设置新状态
        cancelReactorTimeout();  // 取消超时任务
        setShouldCollect(true);  // 设置可收集
        refreshReactorTimeout();  // 刷新超时

        if (map != null) {
            map.searchItemReactors(this);  // 搜索物品反应器
        }
    }

    /**
     * 强制击中反应器
     * @param newState 新状态
     */
    public void forceHitReactor(final byte newState) {
        this.lockReactor();  // 锁定反应器
        try {
            this.resetReactorActions(newState);  // 重置动作
            map.broadcastMessage(PacketCreator.triggerReactor(this, (short) 0));  // 广播触发消息
        } finally {
            this.unlockReactor();  // 解锁反应器
        }
    }

    /**
     * 尝试强制击中反应器(弱信号)
     * @param newState 新状态
     */
    private void tryForceHitReactor(final byte newState) {
        if (!reactorLock.tryLock()) {
            return;  // 如果无法获取锁则直接返回
        }

        try {
            this.resetReactorActions(newState);  // 重置动作
            map.broadcastMessage(PacketCreator.triggerReactor(this, (short) 0));  // 广播触发消息
        } finally {
            reactorLock.unlock();  // 释放锁
        }
    }

    /**
     * 取消反应器超时
     */
    public void cancelReactorTimeout() {
        if (timeoutTask != null) {
            timeoutTask.cancel(false);  // 取消任务
            timeoutTask = null;  // 清空引用
        }
    }

    /**
     * 刷新反应器超时
     */
    private void refreshReactorTimeout() {
        int timeOut = stats.getTimeout(state);  // 获取超时时间
        if (timeOut > -1) {
            final byte nextState = stats.getTimeoutState(state);  // 获取超时后状态

            timeoutTask = TimerManager.getInstance().schedule(() -> {
                timeoutTask = null;  // 清空任务引用
                tryForceHitReactor(nextState);  // 尝试强制击中
            }, timeOut);  // 调度超时任务
        }
    }

    /**
     * 延迟击中反应器
     * @param c 客户端
     * @param delay 延迟时间
     */
    public void delayedHitReactor(final Client c, long delay) {
        TimerManager.getInstance().schedule(() -> hitReactor(c), delay);  // 延迟执行击中
    }

    /**
     * 击中反应器
     * @param c 客户端
     */
    public void hitReactor(Client c) {
        hitReactor(false, 0, (short) 0, 0, c);  // 默认参数调用
    }

    /**
     * 击中反应器(完整参数)
     * @param wHit 是否为武器击中
     * @param charPos 角色位置
     * @param stance 姿态
     * @param skillid 技能ID
     * @param c 客户端
     */
    public void hitReactor(boolean wHit, int charPos, short stance, int skillid, Client c) {
        try {
            if (!this.isActive()) {
                return;  // 如果不活跃则直接返回
            }

            if (hitLock.tryLock()) {
                this.lockReactor();  // 锁定反应器
                try {
                    cancelReactorTimeout();  // 取消超时
                    attackHit = wHit;  // 设置击中标志

                    Character player = c.getPlayer();
                    if (GameConfig.getServerBoolean("use_debug") && player.isGM()) {
                        player.dropMessage(5, "击中反应器 " + this.getId() + " 位置 " + charPos + " , 姿态 " + stance + " , 技能ID " + skillid + " , 状态 " + state + " 状态大小 " + stats.getStateSize(state));  // GM调试信息
                    }
                    ReactorScriptManager.getInstance().onHit(c, this);  // 调用击中脚本

                    int reactorType = stats.getType(state);
                    if (reactorType < 999 && reactorType != -1) {  // 类型2=只能从右侧击中(沼泽植物), 00是左侧空中 02是左侧地面
                        if (!(reactorType == 2 && (stance == 0 || stance == 2))) {  // 获取下一状态
                            for (byte b = 0; b < stats.getStateSize(state); b++) {  // 遍历状态
                                List<Integer> activeSkills = stats.getActiveSkills(state, b);
                                if (activeSkills != null) {
                                    if (!activeSkills.contains(skillid)) {
                                        continue;  // 技能不匹配则跳过
                                    }
                                }

                                this.state = stats.getNextState(state, b);  // 设置下一状态
                                byte nextState = stats.getNextState(state, b);
                                boolean isInEndState = nextState < this.state;
                                if (isInEndState) {  // 反应器结束状态
                                    if (reactorType < 100) {  // 反应器破坏
                                        if (delay > 0) {
                                            map.destroyReactor(getObjectId());  // 延迟销毁
                                        } else {  // 正常触发
                                            map.broadcastMessage(PacketCreator.triggerReactor(this, stance));  // 广播触发
                                        }
                                    } else {  // 最终步骤物品触发
                                        map.broadcastMessage(PacketCreator.triggerReactor(this, stance));  // 广播触发
                                    }

                                    ReactorScriptManager.getInstance().act(c, this);  // 执行脚本动作
                                } else {  // 反应器未完全破坏
                                    map.broadcastMessage(PacketCreator.triggerReactor(this, stance));  // 广播触发
                                    if (state == stats.getNextState(state, b)) {  // 当前状态=下一状态,循环反应器
                                        ReactorScriptManager.getInstance().act(c, this);  // 执行脚本动作
                                    }

                                    setShouldCollect(true);  // 刷新物品掉落反应器的可收集性
                                    refreshReactorTimeout();  // 刷新超时
                                    if (stats.getType(state) == 100) {
                                        map.searchItemReactors(this);  // 搜索物品反应器
                                    }
                                }
                                break;  // 跳出循环
                            }
                        }
                    } else {
                        state++;  // 状态自增
                        map.broadcastMessage(PacketCreator.triggerReactor(this, stance));  // 广播触发
                        if (this.getId() != 9980000 && this.getId() != 9980001) {
                            ReactorScriptManager.getInstance().act(c, this);  // 执行脚本动作(特殊ID除外)
                        }

                        setShouldCollect(true);  // 设置可收集
                        refreshReactorTimeout();  // 刷新超时
                        if (stats.getType(state) == 100) {
                            map.searchItemReactors(this);  // 搜索物品反应器
                        }
                    }
                } finally {
                    this.unlockReactor();  // 解锁反应器
                    hitLock.unlock();  // 解锁击中(感谢MiLin发现非封装解锁)
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  // 打印异常
        }
    }

    /**
     * 销毁反应器
     * @return 是否成功销毁
     */
    public boolean destroy() {
        if (reactorLock.tryLock()) {
            try {
                boolean alive = this.isAlive();
                // 反应器既不存活也不在延迟重生中，允许移除地图对象
                if (alive) {
                    this.setAlive(false);  // 设置死亡
                    this.cancelReactorTimeout();  // 取消超时

                    if (this.getDelay() > 0) {
                        this.delayedRespawn();  // 延迟重生
                    }
                } else {
                    return !this.inDelayedRespawn();  // 返回是否不在延迟重生中
                }
            } finally {
                reactorLock.unlock();  // 解锁
            }
        }

        map.broadcastMessage(PacketCreator.destroyReactor(this));  // 广播销毁消息
        return false;  // 返回失败
    }

    /**
     * 重生反应器
     */
    private void respawn() {
        this.lockReactor();  // 锁定
        try {
            this.resetReactorActions(0);  // 重置动作
            this.setAlive(true);  // 设置存活
        } finally {
            this.unlockReactor();  // 解锁
        }

        map.broadcastMessage(this.makeSpawnData());  // 广播生成消息
    }

    /**
     * 延迟重生
     */
    public void delayedRespawn() {
        Runnable r = () -> {
            delayedRespawnRun = null;  // 清空任务引用
            respawn();  // 执行重生
        };

        delayedRespawnRun = r;  // 设置任务

        OverallService service = (OverallService) map.getChannelServer().getServiceAccess(ChannelServices.OVERALL);
        service.registerOverallAction(map.getId(), r, this.getDelay());  // 注册全局动作
    }

    /**
     * 强制延迟重生
     * @return 是否成功
     */
    public boolean forceDelayedRespawn() {
        Runnable r = delayedRespawnRun;

        if (r != null) {
            OverallService service = (OverallService) map.getChannelServer().getServiceAccess(ChannelServices.OVERALL);
            service.forceRunOverallAction(map.getId(), r);  // 强制运行全局动作
            return true;  // 返回成功
        } else {
            return false;  // 返回失败
        }
    }

    /**
     * 检查是否在延迟重生中
     * @return 是否在延迟重生中
     */
    public boolean inDelayedRespawn() {
        return delayedRespawnRun != null;  // 返回任务是否存在
    }

    /**
     * 获取区域矩形
     * @return 矩形区域
     */
    public Rectangle getArea() {
        return new Rectangle(getPosition().x + stats.getTL().x, getPosition().y + stats.getTL().y, stats.getBR().x - stats.getTL().x, stats.getBR().y - stats.getTL().y);  // 计算并返回区域
    }

    /**
     * 获取名称
     * @return 名称
     */
    public String getName() {
        return name;  // 返回名称
    }

    /**
     * 设置名称
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;  // 设置名称
    }

    /**
     * 获取守卫生成点
     * @return 守卫生成点
     */
    public GuardianSpawnPoint getGuardian() {
        return guardian;  // 返回守卫
    }

    /**
     * 设置守卫生成点
     * @param guardian 守卫生成点
     */
    public void setGuardian(GuardianSpawnPoint guardian) {
        this.guardian = guardian;  // 设置守卫
    }

    /**
     * 设置面向方向
     * @param facingDirection 方向
     */
    public final void setFacingDirection(final byte facingDirection) {
        this.facingDirection = facingDirection;  // 设置方向
    }

    /**
     * 获取面向方向
     * @return 方向
     */
    public final byte getFacingDirection() {
        return facingDirection;  // 返回方向
    }
}