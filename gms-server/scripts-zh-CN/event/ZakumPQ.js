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
 * @event: Zakum PQ
 */

/** 是否为远征队(Party Quest)模式 */
var isPq = true;
/** minPlayers - 队伍最小人数 , maxPlayers - 队伍最大人数 */
var minPlayers = 6, maxPlayers = 6;
/** minLevel - 最低等级限制 , maxLevel - 最高等级限制 */
var minLevel = 50, maxLevel = 255;
/** entryMap - 入口地图ID , exitMap - 退出地图ID */
var entryMap = 280010000, exitMap = 211042300;
/** recruitMap - 招募地图ID , clearMap - 清除地图ID */
var recruitMap = 211042300, clearMap = 211042300;
/** minMapId - 最小地图ID , maxMapId - 最大地图ID */
var minMapId = 280010000, maxMapId = 280011006;
/** eventTime - 事件时间限制(分钟) */
var eventTime = 30;
/** maxLobbies - 最大大厅数量 */
const maxLobbies = 1;

// 从游戏配置获取是否启用单人远征队设置
const GameConfig = Java.type('org.gms.config.GameConfig');
minPlayers = GameConfig.getServerBoolean("use_enable_solo_expeditions") ? 1 : minPlayers;

/**
 * 初始化事件，设置事件要求
 */
function init() {
    setEventRequirements();
}

/**
 * 获取最大大厅数量
 * @returns {number} 最大大厅数
 */
function getMaxLobbies() {
    return maxLobbies;
}

/**
 * 设置事件参与要求并更新到事件管理器
 */
function setEventRequirements() {
    var reqStr = "";

    // 组队人数要求
    reqStr += "\r\n   组队人数: ";
    reqStr += maxPlayers - minPlayers >= 1 ? minPlayers + " ~ " + maxPlayers : minPlayers;

    // 等级要求
    reqStr += "\r\n   等级要求: ";
    reqStr += maxLevel - minLevel >= 1 ? minLevel + " ~ " + maxLevel : minLevel;

    // 时间限制
    reqStr += "\r\n   时间限制: " + eventTime + " 分钟";

    em.setProperty("party", reqStr);
}

/**
 * 设置事件专属物品
 * @param {EventInstanceManager} eim - 事件实例管理器
 */
function setEventExclusives(eim) {
    var itemSet = [4001015, 4001016, 4001018];
    eim.setExclusiveItems(itemSet);
}

/**
 * 设置事件奖励
 * @param {EventInstanceManager} eim - 事件实例管理器
 */
function setEventRewards(eim) {
    var evLevel = 1;    // 清除PQ时的奖励等级
    itemSet = [];
    itemQty = [];
    eim.setEventRewards(evLevel, itemSet, itemQty);  // 空物品奖励

    expStages = [];
    eim.setEventClearStageExp(expStages);         // 空经验奖励
}

/**
 * 从队伍中筛选符合条件的成员
 * @param {Party} party - 玩家队伍
 * @returns {PartyCharacter[]} 符合条件的队伍成员数组
 */
function getEligibleParty(party) {
    var eligible = [];
    var hasLeader = false;

    if (party.size() > 0) {
        var partyList = party.toArray();

        for (var i = 0; i < party.size(); i++) {
            var ch = partyList[i];
            // 检查玩家是否在招募地图且等级符合要求
            if (ch.getMapId() == recruitMap && ch.getLevel() >= minLevel && ch.getLevel() <= maxLevel) {
                if (ch.isLeader()) hasLeader = true;
                eligible.push(ch);
            }
        }
    }
    // 必须有队长且人数符合要求
    if (!(hasLeader && eligible.length >= minPlayers && eligible.length <= maxPlayers)) {
        eligible = [];
    }
    return Java.to(eligible, Java.type('org.gms.net.server.world.PartyCharacter[]'));
}

/**
 * 设置事件实例
 * @param {number} level - 事件等级
 * @param {number} lobbyid - 大厅ID
 * @returns {EventInstanceManager} 创建的事件实例管理器
 */
function setup(level, lobbyid) {
    var eim = em.newInstance("PreZakum" + lobbyid);
    eim.setProperty("level", level);
    eim.setProperty("gotDocuments", 0);
    // 重置所有PQ地图
    eim.getInstanceMap(280010000).resetPQ(level);
    eim.getInstanceMap(280010010).resetPQ(level);
    eim.getInstanceMap(280010011).resetPQ(level);
    eim.getInstanceMap(280010020).resetPQ(level);
    eim.getInstanceMap(280010030).resetPQ(level);
    eim.getInstanceMap(280010031).resetPQ(level);
    eim.getInstanceMap(280010040).resetPQ(level);
    eim.getInstanceMap(280010041).resetPQ(level);
    eim.getInstanceMap(280010050).resetPQ(level);
    eim.getInstanceMap(280010060).resetPQ(level);
    eim.getInstanceMap(280010070).resetPQ(level);
    eim.getInstanceMap(280010071).resetPQ(level);
    eim.getInstanceMap(280010080).resetPQ(level);
    eim.getInstanceMap(280010081).resetPQ(level);
    eim.getInstanceMap(280010090).resetPQ(level);
    eim.getInstanceMap(280010091).resetPQ(level);
    eim.getInstanceMap(280010100).resetPQ(level);
    eim.getInstanceMap(280010101).resetPQ(level);
    eim.getInstanceMap(280010110).resetPQ(level);
    eim.getInstanceMap(280010120).resetPQ(level);
    eim.getInstanceMap(280010130).resetPQ(level);
    eim.getInstanceMap(280010140).resetPQ(level);
    eim.getInstanceMap(280010150).resetPQ(level);
    eim.getInstanceMap(280011000).resetPQ(level);
    eim.getInstanceMap(280011001).resetPQ(level);
    eim.getInstanceMap(280011002).resetPQ(level);
    eim.getInstanceMap(280011003).resetPQ(level);
    eim.getInstanceMap(280011004).resetPQ(level);
    eim.getInstanceMap(280011005).resetPQ(level);
    eim.getInstanceMap(280011006).resetPQ(level);

    respawnStages(eim);

    eim.startEventTimer(eventTime * 60000);  // 启动事件计时器
    setEventRewards(eim);                   // 设置奖励
    setEventExclusives(eim);                // 设置专属物品
    return eim;
}

// 空函数 - 事件设置后执行
function afterSetup(eim) {}

// 空函数 - 重生阶段
function respawnStages(eim) {}

/**
 * 玩家进入事件处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {Player} player - 玩家对象
 */
function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

/**
 * 事件超时处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 */
function scheduledTimeout(eim) {
    end(eim);
}

// 空函数 - 玩家取消注册
function playerUnregistered(eim, player) {}

/**
 * 玩家退出事件处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {Player} player - 玩家对象
 */
function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 0);
}

/**
 * 玩家离开事件处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {Player} player - 玩家对象
 */
function playerLeft(eim, player) {
    if (!eim.isEventCleared()) playerExit(eim, player);
}

/**
 * 玩家切换地图处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {Player} player - 玩家对象
 * @param {number} mapid - 地图ID
 */
function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        eim.unregisterPlayer(player);
        if (eim.isEventTeamLackingNow(true, minPlayers, player)) end(eim);
    }
}

/**
 * 队长变更处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {Player} leader - 新队长对象
 */
function changedLeader(eim, leader) {
    var mapid = leader.getMapId();
    if (!eim.isEventCleared() && (mapid < minMapId || mapid > maxMapId)) end(eim);
}

// 空函数 - 玩家死亡处理
function playerDead(eim, player) {}

/**
 * 玩家复活处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {Player} player - 玩家对象
 */
function playerRevive(eim, player) {
    eim.unregisterPlayer(player);
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) end(eim);
}

/**
 * 玩家断线处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {Player} player - 玩家对象
 */
function playerDisconnected(eim, player) {
    eim.unregisterPlayer(player);
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) end(eim);
}

/**
 * 玩家离开队伍处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {Player} player - 玩家对象
 */
function leftParty(eim, player) {
    if (eim.isEventTeamLackingNow(false, minPlayers, player)) end(eim);
    else playerLeft(eim, player);
}

/**
 * 解散队伍处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 */
function disbandParty(eim) {
    if (!eim.isEventCleared()) end(eim);
}

/**
 * 获取怪物价值
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {number} mobId - 怪物ID
 * @returns {number} 怪物价值(固定为1)
 */
function monsterValue(eim, mobId) {
    return 1;
}

/**
 * 结束事件处理
 * @param {EventInstanceManager} eim - 事件实例管理器
 */
function end(eim) {
    var party = eim.getPlayers();
    for (var i = 0; i < party.size(); i++) playerExit(eim, party.get(i));
    eim.dispose();
}

/**
 * 给予随机事件奖励
 * @param {EventInstanceManager} eim - 事件实例管理器
 * @param {Player} player - 玩家对象
 */
function giveRandomEventReward(eim, player) {
    eim.giveEventReward(player);
}

/**
 * 清除PQ状态
 * @param {EventInstanceManager} eim - 事件实例管理器
 */
function clearPQ(eim) {
    eim.stopEventTimer();
    eim.setEventCleared();
}

// 空函数 - 怪物被击杀处理
function monsterKilled(mob, eim) {}

// 空函数 - 所有怪物死亡处理
function allMonstersDead(eim) {}

// 空函数 - 取消计划
function cancelSchedule() {}

// 空函数 - 释放资源
function dispose(eim) {}