// 事件实例化变量
var isPq = true;
var minPlayers = 1, maxPlayers = 5;
var minLevel = 1, maxLevel = 255;
var entryMap = 925020002;
var exitMap = 910000000;
var recruitMap = 910000000;
var clearMap = 910000000;

var minMapId = 925020002;
var maxMapId = 925020002;

var eventTime = 1;                  // 分钟
const maxLobbies = 1;

var BossID = 9001007;               // 稻草人
var PosX = 4, PosY = 7;

var BossDropList = [2000005];
var BossDropCount = [5];
var BossDropChance = [0.4];

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
    if (itemSet.length > 0) eim.setExclusiveItems(itemSet);
}

function setEventRewards(eim) {
    var itemSet = [2000005];
    var itemQty = [5];
    eim.setEventRewards(1, itemSet, itemQty);
    eim.setEventClearStageExp([]);
}

function getEligibleParty(party) {
    var eligible = [];
    var hasLeader = false;
    if (party.size() > 0) {
        var partyList = party.toArray();
        for (var i = 0; i < party.size(); i++) {
            var ch = partyList[i];
            if (ch.getMapId() === recruitMap && ch.getLevel() >= minLevel && ch.getLevel() <= maxLevel) {
                if (ch.isLeader()) hasLeader = true;
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
    let eim = em.newInstance(em.getName() + lobbyid);
    eim.setProperty("level", level);

    respawnStages(eim);
    eim.startEventTimer(eventTime * 60000);
    setEventRewards(eim);
    setEventExclusives(eim);

    if (BossID != null && BossID > 0) {
        const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
        let mob = LifeFactory.getMonster(BossID);
        if (mob != null) {
            let map = eim.getMapInstance(entryMap);
            map.killAllMonsters();
            mob.setStartingHp(2100000000);  // 设置血量（根据需要）
            map.spawnMonsterOnGroundBelow(mob, new java.awt.Point(PosX, PosY));
        }
    }

    // 处理掉落列表
    var dropList = BossDropList.slice();
    BossDropList = [];
    for (let i = 0; i < dropList.length; i++) {
        const chance = BossDropChance[i] * 10000;
        for (let j = 0; j < BossDropCount[i]; j++) {
            if (Math.random() * 10000 < chance) {
                BossDropList.push(dropList[i]);
            }
        }
    }
    return eim;
}

function afterSetup(eim) {}
function respawnStages(eim) {}

function playerEntry(eim, player) {
    var map = eim.getMapInstance(entryMap);
    player.changeMap(map, map.getPortal(0));
    //开启伤害记录
    const GameConfig = Java.type('org.gms.config.GameConfig');
    if(GameConfig.getServerBoolean("damage_ranking")) {
        eim.startDamageRecording();
        player.dropMessage(6, "当前副本已开启伤害统计。");
    }
}

// ---------- 伤害通报 ----------
function scheduledTimeout(eim) {
    // 先输出伤害排名
    eim.broadcastDamageRanking();
    // 再发送失败提示（可选）
    eim.dropMessage(5, "讨伐结束！时间到！");
    end(eim);
}

function monsterKilled(mob, eim) {
    try {
        if (eim.isEventCleared()) return;
        if (mob.getId() === BossID) {
            // 输出伤害排名
            eim.broadcastDamageRanking();

            // 掉落物品
            var mapObj = mob.getMap();
            var dropper = eim.getPlayers().get(0);
            mapObj.spawnItemDropList(BossDropList, mob, dropper, mob.getPosition());

            // 清除事件
            clearPQ(eim);
        }
    } catch (err) {
        console.error(err);
    }
}

// ---------- 其他事件生命周期函数 ----------
function playerUnregistered(eim, player) {}
function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, 0);
}
function playerLeft(eim, player) {
    if (!eim.isEventCleared()) playerExit(eim, player);
}

function changedMap(eim, player, mapid) {
    // 如果玩家离开了事件地图（即地图ID不再是入口地图）
    if (mapid !== entryMap) {
        // 1. 输出伤害排名通报
        eim.broadcastDamageRanking();
        // 2. 结束整个事件，将所有玩家传送回自由市场
        end(eim);
    }
}

function changedLeader(eim, leader) {
    var mapid = leader.getMapId();
    if (!eim.isEventCleared() && (mapid < minMapId || mapid > maxMapId)) {
        end(eim);
    }
}
function playerDead(eim, player) {}
function playerRevive(eim, player) {
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
    if (!eim.isEventCleared()) end(eim);
}
function monsterValue(eim, mobId) { return 1; }

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
    eim.startEventTimer(2 * 60000);
}
function allMonstersDead(eim) {}
function cancelSchedule(eim) {}
function dispose(eim) {}