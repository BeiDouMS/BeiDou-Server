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
 * @event: Zakum Battle
 */

var isPq = true;
var minPlayers = 6, maxPlayers = 30;
var minLevel = 50, maxLevel = 255;
var entryMap = 280030000;
var exitMap = 211042400;
var recruitMap = 211042400;
var clearMap = 211042400;

var minMapId = 280030000;
var maxMapId = 280030000;

var eventTime = 120;     // 120 minutes

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
    updateGateState(1);
}

function setup(channel) {
    var eim = em.newInstance("Zakum" + channel);
    eim.setProperty("canJoin", 1);
    eim.setProperty("defeatedBoss", 0);

    var level = 1;
    eim.getInstanceMap(280030000).resetPQ(level);

    eim.startEventTimer(eventTime * 60000);
    setEventRewards(eim);
    setEventExclusives(eim);

    return eim;
}

/**
 * 处理玩家进入远征副本事件 - 当玩家进入远征副本时触发
 * @param {ExpeditionInstanceManager} eim - 远征副本实例管理器
 * @param {Player} player - 进入副本的玩家对象
 * @returns {void}
 * @description 当玩家进入副本时发送系统消息，并将玩家传送到副本入口地图
 */
function playerEntry(eim, player) {
    // 发送玩家进入副本的系统提示消息
    eim.dropMessage(5, "[远征队] " + player.getName() + " 已进入副本地图。");

    // 获取副本入口地图实例
    var map = eim.getMapInstance(entryMap);

    // 将玩家传送到入口地图的第一个传送点
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    end(eim);
}

/**
 * 处理玩家切换地图事件 - 当玩家在远征副本中切换地图时触发
 * @param {ExpeditionInstanceManager} eim - 远征副本实例管理器
 * @param {Player} player - 触发事件的玩家对象
 * @param {number} mapid - 玩家切换到的地图ID
 * @returns {void}
 * @description 当玩家切换到副本允许范围外的地图时，执行玩家移除逻辑
 */
function changedMap(eim, player, mapid) {
    // 检查地图ID是否超出副本允许范围
    if (mapid < minMapId || mapid > maxMapId) {
        // 检查是否因玩家退出导致队伍不满足最低人数要求
        partyPlayersCheck(eim, player);
    }
}

function changedLeader(eim, leader) {}

function playerDead(eim, player) {}

/**
 * 处理玩家复活事件 - 当玩家在远征副本中复活时触发
 * @param {ExpeditionInstanceManager} eim - 远征副本实例管理器
 * @param {Player} player - 触发事件的玩家对象
 * @returns {void}
 */
function playerRevive(eim, player) {
    partyPlayersCheck(eim, player);
}

/**
 * 处理玩家断线事件 - 当玩家在远征副本中断开连接时触发
 * @param {ExpeditionInstanceManager} eim - 远征副本实例管理器
 * @param {Player} player - 触发事件的玩家对象
 * @returns {void}
 */
function playerDisconnected(eim, player) {
    partyPlayersCheck(eim, player);
}

function leftParty(eim, player) {}

function disbandParty(eim) {}

function monsterValue(eim, mobId) {
    return 1;
}

function playerUnregistered(eim, player) {
    if (eim.isEventCleared()) {
        em.completeQuest(player, 100200, 2030010);
    }
}

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
    updateGateState(0);
}

function isZakum(mob) {
    var mobid = mob.getId();
    return (mobid == 8800002);
}

function monsterKilled(mob, eim) {
    if (isZakum(mob)) {
        eim.setIntProperty("defeatedBoss", 1);
        eim.showClearEffect(mob.getMap().getId());
        eim.clearPQ();

        mob.getMap().broadcastZakumVictory();
    }
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function updateGateState(newState) {    // thanks Conrad for noticing missing gate update
    em.getChannelServer().getMapFactory().getMap(211042300).getReactorById(2118002).forceHitReactor(newState);
}

function dispose(eim) {
    if (!eim.isEventCleared()) {
        updateGateState(0);
    }
}
/**
 * 检测队伍人数是否满足最低人数要求
 * @param {ExpeditionInstanceManager} eim - 远征副本实例管理器
 * @param {Player} player - 触发事件的玩家对象
 * @returns {void}
 */
function partyPlayersCheck(eim, player) {
    if (eim.isExpeditionTeamLackingNow(true, minPlayers, player)) {
        eim.unregisterPlayer(player);
        eim.dropMessage(5, "[远征队] 队长已退出远征或者队伍人数不足最低要求，无法继续。");
        end(eim);
        return false;
    } else {
        eim.dropMessage(5, "[远征队] " + player.getName() + " 已离开副本。");
        eim.unregisterPlayer(player);
        return true;
    }
}