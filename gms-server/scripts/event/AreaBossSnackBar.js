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
 Snack Bar Spawner
 -- Edited by --------------------------------------------------------------------------------------
 Ronan - based on xQuasar's King Clang spawner

 **/
function init() {
    // 8220008 = 未激活形态「未知的小吃店」；受击后消失并生成激活形态 8220009「小吃店」。
    // 按原版逻辑登记未激活形态 8220008（start() 仅刷 8220008，激活转化由客户端战斗触发）。
    // 中文层（scripts-zh-CN）登记的是激活形态 8220009，英文层此处保持原版未激活形态。
    em.setProperty("bossMobId", 8220008);
    em.setProperty("bossMapId", 105090310);
    scheduleNew();
}

function scheduleNew() {
    setupTask = em.schedule("start", 0);    //spawns upon server start. Each 3 hours an server event checks if boss exists, if not spawns it instantly.
}

function cancelSchedule() {
    if (setupTask != null) {
        setupTask.cancel(true);
    }
}

function start() {
    var snackBarMap = em.getChannelServer().getMapFactory().getMap(105090310);

    if (snackBarMap.getMonsterById(8220008) != null || snackBarMap.getMonsterById(8220009) != null) {
        em.schedule("start", 3 * 60 * 60 * 1000);
        return;
    }

    var setPos = [[-626, -604], [735, -600]];
    var rndPos = setPos[Math.floor(Math.random() * setPos.length)];

    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    const PacketCreator = Java.type('org.gms.util.PacketCreator');

    var snackBar = LifeFactory.getMonster(8220008);
    snackBarMap.spawnMonsterOnGroundBelow(snackBar, new Point(rndPos[0], rndPos[1]));
    snackBarMap.broadcastMessage(PacketCreator.serverNotice(6, "Slowly, a suspicious food stand opens up on a strangely remote place."));
    em.schedule("start", 3 * 60 * 60 * 1000);
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

