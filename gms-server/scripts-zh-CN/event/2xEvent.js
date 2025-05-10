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
/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 2x EXP Event Script
 -- Author --------------------------------------------------------------------------------------
 Twdtwd
 **/
/*
    该文件是OdinMS Maple Story服务器的一部分
    版权所有 (C) 2008 Patrick Huy <patrick.huy@frz.cc>
                   Matthias Butz <matze@odinms.de>
                   Jan Christian Meyer <vimes@odinms.de>

    该程序是自由软件：你可以根据自由软件基金会发布的GNU Affero通用公共许可协议第三版重新分发和/或修改它。
    你不得在任何其他版本的GNU Affero通用公共许可协议下使用、修改或分发此程序。

    该程序是基于它可以是有用的前提下发的，
    但是没有任何形式的担保；甚至没有默示的保证
    商业性或者适用于特定目的。有关详细信息，请参阅
    GNU Affero通用公共许可协议。

    你应该已经收到了一份GNU Affero通用公共许可协议的副本，
    如果没有，请参见<http://www.gnu.org/licenses/>。
*/
/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 2倍经验活动脚本
 -- 作者 --------------------------------------------------------------------------------------
 Twdtwd
 **/

var timer1;
var timer2;
var timer3;
var timer4;

function init() {
    /*
        if(em.getChannelServer().getId() == 1) { // 仅在频道1运行。
        // 澳大利亚东部标准时间(AEST)
        timer1 = em.scheduleAtTimestamp("start", 1428220800000); // 在给定的时间戳启动活动
        timer2 = em.scheduleAtTimestamp("stop", 1428228000000); // 在给定的时间戳停止活动
        // 美国东部夏令时(EDT)
        timer1 = em.scheduleAtTimestamp("start", 1428271200000);
        timer2 = em.scheduleAtTimestamp("stop", 1428278400000);
    }
        */
}

function cancelSchedule() {
    if (timer1 != null) {
        timer1.cancel(true); // 取消定时器1
    }
    if (timer2 != null) {
        timer2.cancel(true); // 取消定时器2
    }
    if (timer3 != null) {
        timer3.cancel(true); // 取消定时器3
    }
    if (timer4 != null) {
        timer4.cancel(true); // 取消定时器4
    }
}
/**
 * 开始双倍经验活动
 */
function start() {
    const Server = Java.type('org.gms.net.server.Server');
    const PacketCreator = Java.type('org.gms.util.PacketCreator');
    let world = Server.getInstance().getWorld(em.getChannelServer().getWorld());
    let ExpRate = world.getExpRate();   //获取当前经验倍率
    world.setExpRate(ExpRate * 2); // 将经验倍率调整为双倍经验
    world.broadcastPacket(PacketCreator.serverNotice(6, "BOSS扫描器检测到即将到来的复活节兔子袭击！GM团队已激活紧急经验池，在接下来的两小时内获得的经验值将翻倍！"));
}
/**
 * 结束双倍经验活动
 */
function stop() {
    const Server = Java.type('org.gms.net.server.Server');
    const PacketCreator = Java.type('org.gms.util.PacketCreator');
    var world = Server.getInstance().getWorld(em.getChannelServer().getWorld());
    world.setExpRate(4); // 将经验值恢复到原来的4倍（正常情况下）
    world.broadcastPacket(PacketCreator.serverNotice(6, "很遗憾，紧急经验池(EXP)能量已耗尽需要重新充能，经验倍率已恢复正常。"));
}

// ---------- 预留函数(空实现) ----------

/**
 * 清理函数
 */
function dispose() {}

/**
 * 设置副本
 * @param {Object} eim 副本实例
 * @param {number} leaderid 队长ID
 */
function setup(eim, leaderid) {}

/**
 * 获取怪物价值
 * @param {Object} eim 副本实例
 * @param {number} mobid 怪物ID
 * @return {number} 总是返回0
 */
function monsterValue(eim, mobid) {return 0;}

/**
 * 解散队伍
 * @param {Object} eim 副本实例
 * @param {Object} player 玩家对象
 */
function disbandParty(eim, player) {}

/**
 * 玩家断开连接
 * @param {Object} eim 副本实例
 * @param {Object} player 玩家对象
 */
function playerDisconnected(eim, player) {}

/**
 * 玩家进入副本
 * @param {Object} eim 副本实例
 * @param {Object} player 玩家对象
 */
function playerEntry(eim, player) {}

/**
 * 怪物被击杀
 * @param {Object} mob 怪物对象
 * @param {Object} eim 副本实例
 */
function monsterKilled(mob, eim) {}

/**
 * 副本超时
 * @param {Object} eim 副本实例
 */
function scheduledTimeout(eim) {}

/**
 * 副本设置完成后
 * @param {Object} eim 副本实例
 */
function afterSetup(eim) {}

/**
 * 队长变更
 * @param {Object} eim 副本实例
 * @param {Object} leader 新队长对象
 */
function changedLeader(eim, leader) {}

/**
 * 玩家退出副本
 * @param {Object} eim 副本实例
 * @param {Object} player 玩家对象
 */
function playerExit(eim, player) {}

/**
 * 玩家离开队伍
 * @param {Object} eim 副本实例
 * @param {Object} player 玩家对象
 */
function leftParty(eim, player) {}

/**
 * 清理副本任务
 * @param {Object} eim 副本实例
 */
function clearPQ(eim) {}

/**
 * 所有怪物被击杀
 * @param {Object} eim 副本实例
 */
function allMonstersDead(eim) {}

/**
 * 玩家取消注册
 * @param {Object} eim 副本实例
 * @param {Object} player 玩家对象
 */
function playerUnregistered(eim, player) {}