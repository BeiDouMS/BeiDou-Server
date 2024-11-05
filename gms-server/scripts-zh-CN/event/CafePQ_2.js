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
 * @event: Cafe PQ 2
 */

var isPq = true;
var minPlayers = 3, maxPlayers = 6;
var minLevel = 21, maxLevel = 120;
var entryMap = 191000000;
var exitMap = 193000000;
var recruitMap = 193000000;

var minMapId = 191000000;
var maxMapId = 191000001;

var eventMaps = [191000000, 191000001];

var eventTime = 45;         // 45 minutes
var couponsNeeded = 350;    // total of coupons to complete the event

const maxLobbies = 1;

const GameConfig = Java.type('org.gms.config.GameConfig');
minPlayers = GameConfig.getServerBoolean("use_enable_solo_expeditions") ? 1 : minPlayers;  //如果解除远征队人数限制，则最低人数改为1人
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
    var itemSet = [4001007];
    eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
    var itemSet, itemQty, evLevel, expStages;

    evLevel = 1;    //Rewards at clear PQ
    itemSet = [4001009];
    itemQty = [1];
    eim.setEventRewards(evLevel, itemSet, itemQty);

    expStages = [15000];    //bonus exp given on CLEAR stage signal
    eim.setEventClearStageExp(expStages);
}

function getEligibleParty(party) {      //selects, from the given party, the team that is allowed to attempt this event
    var eligible = [];
    var hasLeader = false;

    if (party.size() > 0) {
        var partyList = party.toArray();

        for (var i = 0; i < party.size(); i++) {
            var ch = partyList[i];

            if (ch.getMapId() == recruitMap && ch.getLevel() >= minLevel && ch.getLevel() <= maxLevel) {
                if (ch.isLeader()) {
                    hasLeader = true;
                }
                eligible.push(ch);
            }
        }
    }

    if (!(hasLeader && eligible.length >= minPlayers && eligible.length <= maxPlayers)) {
        eligible = [];
    }
    return Java.to(eligible, Java.type('org.gms.net.server.world.PartyCharacter[]'));
}

function setup(level, lobbyid) {
    var eim = em.newInstance("Lan2_" + lobbyid);
    eim.setProperty("level", level);
    eim.setProperty("stage", "0");

    eim.setIntProperty("couponsNeeded", couponsNeeded);

    var i;
    for (i = 0; i < eventMaps.length; i++) {
        var mapObj = eim.getInstanceMap(eventMaps[i]);
        mapObj.resetPQ(level);
        mapObj.toggleDrops();
        mapObj.instanceMapForceRespawn();
    }

    respawnStages(eim);
    eim.startEventTimer(eventTime * 60000);
    setEventRewards(eim);
    setEventExclusives(eim);
    return eim;
}

function afterSetup(eim) {}

function respawnStages(eim) {
    var i;
    for (i = 0; i < eventMaps.length; i++) {
        eim.getInstanceMap(eventMaps[i]).instanceMapRespawn();
    }

    eim.schedule("respawnStages", 15 * 1000);
}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
}

function scheduledTimeout(eim) {
    end(eim);
}

function playerUnregistered(eim, player) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 0);
}

function playerLeft(eim, player) {
    if (!eim.isEventCleared()) {
        playerExit(eim, player);
    }
}

function changedMap(eim, player, mapid) {
    if (mapid < minMapId || mapid > maxMapId) {
        if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
            eim.unregisterPlayer(player);
            end(eim);
        } else {
            eim.unregisterPlayer(player);
        }
    }
}

function changedLeader(eim, leader) {
    var mapid = leader.getMapId();
    if (!eim.isEventCleared() && (mapid < minMapId || mapid > maxMapId)) {
        end(eim);
    }
}

function playerDead(eim, player) {}

function playerRevive(eim, player) { // player presses ok on the death pop up.
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
        eim.unregisterPlayer(player);
        end(eim);
    } else {
        eim.unregisterPlayer(player);
    }
}

function playerDisconnected(eim, player) {
    if (eim.isEventTeamLackingNow(true, minPlayers, player)) {
        eim.unregisterPlayer(player);
        end(eim);
    } else {
        eim.unregisterPlayer(player);
    }
}

function leftParty(eim, player) {
    if (eim.isEventTeamLackingNow(false, minPlayers, player)) {
        end(eim);
    } else {
        playerLeft(eim, player);
    }
}

function disbandParty(eim) {
    if (!eim.isEventCleared()) {
        end(eim);
    }
}

function monsterValue(eim, mobId) {
    return 1;
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
    eim.giveEventPlayersStageReward(1);

    var i;
    for (i = 0; i < eventMaps.length; i++) {
        eim.getInstanceMap(eventMaps[i]).killAllMonstersNotFriendly();
        eim.showClearEffect(eventMaps[i]);
    }
}

function getDroppedQuantity(mob) {
    if (mob.getLevel() > 65) {
        return 5;
    } else if (mob.getLevel() > 40) {
        return 2;
    } else {
        return 1;
    }
}

function monsterKilled(mob, eim) {
    try {
        if (eim.isEventCleared()) {
            return;
        }

        var mapObj = mob.getMap();
        const Item = Java.type('org.gms.client.inventory.Item');
        var itemObj = new Item(4001007, 0, getDroppedQuantity(mob));
        var dropper = eim.getPlayers().get(0);

        mapObj.spawnItemDrop(mob, dropper, itemObj, mob.getPosition(), true, false);

    } catch (err) {
    } // PQ not started yet
}

function allMonstersDead(eim) {}

function cancelSchedule() {}

function dispose(eim) {}
