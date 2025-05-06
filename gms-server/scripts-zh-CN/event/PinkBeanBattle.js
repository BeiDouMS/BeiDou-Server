/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
 * @author: Ronan
 * @event: Pink Bean Battle
 */

var isPq = true;
var minPlayers = 6, maxPlayers = 30;
var minLevel = 120, maxLevel = 255;
var entryMap = 270050100;
var exitMap = 270050300;
var recruitMap = 270050000;
var clearMap = 270050300;

var minMapId = 270050100;
var maxMapId = 270050300;

var eventTime = 140;     // 140 minutes

const maxLobbies = 1;

const GameConfig = Java.type('org.gms.config.GameConfig');
minPlayers = GameConfig.getServerBoolean("use_enable_solo_expeditions") ? 1 : minPlayers;  //如果解除远征队人数限制，则最低人数改为1人
if(GameConfig.getServerBoolean("use_enable_party_level_limit_lift")) {  //如果解除远征队等级限制，则最低1级，最高999级。
    minLevel = 1 , maxLevel = 999;
}

function init() {
    setEventRequirements();
}

function getMaxLobbies() {
    return maxLobbies;
}

function setEventRequirements() {
    var reqStr = "";

    reqStr += "\r\n   组队人数: ";
    if (maxPlayers - minPlayers >= 1) {
        reqStr += minPlayers + " ~ " + maxPlayers;
    } else {
        reqStr += minPlayers;
    }

    reqStr += "\r\n   等级要求: ";
    if (maxLevel - minLevel >= 1) {
        reqStr += minLevel + " ~ " + maxLevel;
    } else {
        reqStr += minLevel;
    }

    reqStr += "\r\n   时间限制: ";
    reqStr += eventTime + " 分钟";

    em.setProperty("party", reqStr);
}

function setEventExclusives(eim) {
    var itemSet = [];
    eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
    var itemSet, itemQty, evLevel, expStages, mesoStages;

    evLevel = 1;    //Rewards at clear PQ
    itemSet = [];
    itemQty = [];
    eim.setEventRewards(evLevel, itemSet, itemQty);

    expStages = [];    //bonus exp given on CLEAR stage signal
    eim.setEventClearStageExp(expStages);

    mesoStages = [];    //bonus meso given on CLEAR stage signal
    eim.setEventClearStageMeso(mesoStages);
}

function afterSetup(eim) {
    eim.dropMessage(5, "第一波攻击将在15秒后开始，请做好准备。");
    eim.schedule("startWave", 15 * 1000);
}

function setup(channel) {
    var eim = em.newInstance("PinkBean" + channel);
    eim.setProperty("canJoin", 1);
    eim.setProperty("defeatedBoss", 0);
    eim.setProperty("fallenPlayers", 0);

    eim.setProperty("stage", 1);
    eim.setProperty("channel", channel);

    var level = 1;
    eim.getInstanceMap(270050100).resetPQ(level);
    eim.getInstanceMap(270050200).resetPQ(level);
    eim.getInstanceMap(270050300).resetPQ(level);

    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    var mob = LifeFactory.getMonster(8820000);
    mob.disableDrops();
    eim.getInstanceMap(270050100).spawnMonsterOnGroundBelow(mob, new Point(0, -42));

    eim.startEventTimer(eventTime * 60000);
    setEventRewards(eim);
    setEventExclusives(eim);

    return eim;
}

function playerEntry(eim, player) {
    eim.dropMessage(5, "[远征队] " + player.getName() + " 已进入地图。");
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    end(eim);
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        if (eim.isExpeditionTeamLackingNow(true, minPlayers, player)) {
            eim.unregisterPlayer(player);
            eim.dropMessage(5, "[远征队] 队长已退出远征队或队伍人数不足最低要求，无法继续。");
            end(eim);
        } else {
            eim.dropMessage(5, "[远征队] " + player.getName() + " 已离开远征队。");
            eim.unregisterPlayer(player);
        }
    }
}

function changedLeader(eim, leader) {}

function playerDead(eim, player) {
    var count = eim.getIntProperty("fallenPlayers");
    count = count + 1;

    eim.setIntProperty("fallenPlayers", count);

    if (count == 5) {
        eim.dropMessage(5, "[远征队] 太多队员阵亡，品克缤现在被视为不可战胜，远征结束。");
        end(eim);
    } else if (count == 4) {
        eim.dropMessage(5, "[远征队] 品克缤变得比以往更强大，大家进入背水一战模式！");
    } else if (count == 3) {
        eim.dropMessage(5, "[远征队] 伤亡人数开始失控，请小心战斗。");
    }
}

function playerRevive(eim, player) {
    return true;
}

function monsterRevive(eim, mob) {
    if (isPinkBean(mob)) {
        mob.enableDrops();
    }
}

function playerDisconnected(eim, player) {
    if (eim.isExpeditionTeamLackingNow(true, minPlayers, player)) {
        eim.unregisterPlayer(player);
        eim.dropMessage(5, "[远征队] 队长已退出远征队或队伍人数不足最低要求，无法继续。");
        end(eim);
    } else {
        eim.dropMessage(5, "[远征队] " + player.getName() + " 已离开远征队。");
        eim.unregisterPlayer(player);
    }
}

function leftParty(eim, player) {}

function disbandParty(eim) {}

function monsterValue(eim, mobId) {
    return 1;
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 0);
}

function end(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) {
        playerExit(eim, party.get(i));
    }
    eim.dispose();
}

function giveRandomEventReward(eim, player) {
    eim.giveEventReward(player);
}

function clearPQ(eim) {
    eim.stopEventTimer();
    eim.setEventCleared();
}

function isPinkBean(mob) {
    var mobid = mob.getId();
    return (mobid == 8820001);
}

function isJrBoss(mob) {
    var mobid = mob.getId();
    return (mobid >= 8820002 && mobid <= 8820006);
}

function noJrBossesLeft(map) {
    return map.countMonster(8820002, 8820006) == 0;
}

function spawnJrBoss(mobObj, gotKilled) {
    if (gotKilled) {
        spawnid = mobObj.getId() + 17;

    } else {
        mobObj.getMap().killMonster(mobObj.getId());
        spawnid = mobObj.getId() - 17;
    }

    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    var mob = LifeFactory.getMonster(spawnid);
    mobObj.getMap().spawnMonsterOnGroundBelow(mob, mobObj.getPosition());
}

function monsterKilled(mob, eim) {
    if (isPinkBean(mob)) {
        eim.setIntProperty("defeatedBoss", 1);
        eim.showClearEffect(mob.getMap().getId());
        mob.getMap().killAllMonsters();
        eim.clearPQ();

        var ch = eim.getIntProperty("channel");
        mob.getMap().broadcastPinkBeanVictory(ch);
    } else if (isJrBoss(mob)) {
        if (noJrBossesLeft(mob.getMap())) {
            var stage = eim.getIntProperty("stage");

            if (stage == 5) {
                var iid = 4001193;
                const Item = Java.type('org.gms.client.inventory.Item');
                var itemObj = new Item(iid, 0, 1);
                var mapObj = eim.getMapFactory().getMap(270050100);
                var reactObj = mapObj.getReactorById(2708000);
                var dropper = eim.getPlayers().get(0);
                mapObj.spawnItemDrop(dropper, dropper, itemObj, reactObj.getPosition(), true, true);


                eim.dropMessage(6, "随着最后的守护者倒下，品克缤失去了无敌状态。真正的战斗现在开始！");
            } else {
                stage++;
                eim.setIntProperty("stage", stage);

                eim.dropMessage(5, "下一波攻击将在15秒后开始，请做好准备。");
                eim.schedule("startWave", 15 * 1000);
            }
        }
    }
}

function startWave(eim) {
    var mapObj = eim.getMapInstance(270050100);
    var stage = eim.getProperty("stage");

    for (var i = 1; i <= stage; i++) {
        spawnJrBoss(mapObj.getMonsterById(8820019 + (i % 5)), false);
    }
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
