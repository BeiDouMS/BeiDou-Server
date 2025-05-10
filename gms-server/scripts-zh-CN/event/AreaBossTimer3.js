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

const Point = Java.type('java.awt.Point');
const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
const PacketCreator = Java.type('org.gms.util.PacketCreator');
const LoggerFactory = Java.type('org.slf4j.LoggerFactory');
var log = null;
var channel = null;
var isinit = false;

var MapID = 220050200;
var BossID = 5220003;
var BossName = "提莫";
/**刷新时间，分钟;  Generation time in minutes*/
var BossTime = 180;
/**指定Boss刷新的XY坐标位置; Specify the XY coordinate position for Boss refresh*/
var point = new Point(Math.floor((Math.random() * 1400) - 700), 1030);
var BossNotice= "嘀嗒...嘀嗒...！时间精灵提莫在轻声提醒。";

const methodName = "start";     //指定当前事件刷新Boss的函数，无需改动
/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Zeno Spawner
 -- Edited by --------------------------------------------------------------------------------------
 ThreeStep - based on xQuasar's King Clang spawner
 **/
function init() {
    channel = em.getChannelServer().getId();
    log = LoggerFactory.getLogger(em.getName());
  
    scheduleNew();
}

function scheduleNew() {
    setupTask = em.schedule(methodName, 0);    //服务器启动时生成。每指定时间，服务器事件会检查boss是否存在，如果不存在，会立即生成boss。
}

function cancelSchedule() {
    if (setupTask != null) {
        setupTask.cancel(true);
    }
}

function start() {
    var graysPrairie = em.getChannelServer().getMapFactory().getMap(MapID);
    var Timer = em.getBossTime(BossTime * 60 * 1000);  //转为毫秒并加载时间倍率修正

    if (graysPrairie.getMonsterById(BossID) != null) {
        em.schedule(methodName, Timer);
        return;
    }
    const BossObj = LifeFactory.getMonster(BossID);
    BossName = BossObj.getName() || BossName;
    try {
        graysPrairie.spawnMonsterOnGroundBelow(BossObj, point);
        if(isinit) {
            log.info(`[事件脚本-野外BOSS] ${em.getName()} 已在频道 ${channel} 的 ${graysPrairie.getMapName()}(${MapID}) ${point.x} , ${point.y}) 生成 ${BossName}(${BossID})，检测间隔：${Timer / 60 / 1000} 分钟`);
        } else {
            isinit = true;
        }
    } catch (e) {
        console.error(`[事件脚本-野外BOSS] ${em.getName()} 在频道 ${channel} 的 ${graysPrairie.getMapName()}(${MapID}) ${point.x} , ${point.y}) 生成 ${BossName}(${BossID}) 时出错`,e);
    }
    graysPrairie.broadcastMessage(PacketCreator.serverNotice(6, `[野外BOSS] ${BossName}  ${BossNotice}`));     //聊天框输出当前地图范围的Boss登场消息

    em.schedule(methodName, Timer);
}

// ---------- FILLER FUNCTIONS ----------

function dispose() {}

function setup(eim, leaderid) {}

function monsterValue(eim, mobid) {return 0;}

function disbandParty(eim, player) {}

function playerDisconnected(eim, player) {}

function playerEntry(eim, player) {}

function monsterKilled(mob, eim) {}

function scheduledTimeout(eim) {}

function afterSetup(eim) {}

function changedLeader(eim, leader) {}

function playerExit(eim, player) {}

function leftParty(eim, player) {}

function clearPQ(eim) {}

function allMonstersDead(eim) {}

function playerUnregistered(eim, player) {}

