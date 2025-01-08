// 事件实例化变量
var isPq = true; // 是否为PQ（Party Quest）类型事件。
var minPlayers, maxPlayers; // 该事件实例允许的队伍成员数量范围。
var minLevel, maxLevel;     // 合格队伍成员的等级范围。
var entryMap;               // 事件启动时玩家进入的初始地图。
var exitMap;                // 玩家未能完成事件时被传送至此地图。
var recruitMap;             // 玩家必须在此地图上才能开始此事件。
var clearMap;               // 玩家成功完成事件后被传送至此地图。

var minMapId;               // 事件发生在此地图ID区间内。若玩家超出此范围则立即从事件中移除。
var maxMapId;

var eventTime;              // 事件的最大允许时间，以分钟计。

const maxLobbies = 7;       // 并发活跃大厅的最大数量。

function init() {
    // 在ChannelServer加载后执行初始化操作。
}

function getMaxLobbies() {
    return maxLobbies;
}

function setEventRequirements() {
    // 设置在招募区域显示的关于事件的要求信息。
}

function setEventExclusives(eim) {
    // 设置仅在事件实例中存在的物品，并在事件结束时从库存中移除这些物品。
}

function setEventRewards(eim) {
    // 设置所有可能的奖励，随机给予玩家作为事件结束时的奖品。
}

function getEligibleParty(party) {
    // 从给定的队伍中选择符合资格的团队尝试此事件。
}

function setup(eim, leaderid) {
    // 当调用时设置事件实例，例如：开始PQ。
}

function afterSetup(eim) {
    // 事件实例初始化完毕且所有玩家分配完成后，但在玩家进入之前触发。
}

function respawnStages(eim) {
    // 定义事件内部允许重生的地图。此函数应在末尾创建一个新的任务，在指定的重生率后再次调用自身。
}

function playerEntry(eim, player) {
    // 将玩家传送到事件地图等操作。
}

function playerUnregistered(eim, player) {
    // 在玩家即将注销前对其进行某些操作。
}

function playerExit(eim, player) {
    // 在解散事件实例前对玩家进行某些操作。
}

function playerLeft(eim, player) {
    // 在玩家离开队伍前对其进行某些操作。
}

function changedMap(eim, player, mapid) {
    // 当玩家更换地图时根据mapid执行的操作。
}

function changedLeader(eim, leader) {
    // 如果队伍领袖变更时执行的操作。
}

function scheduledTimeout(eim) {
    // 当事件超时而未完成时触发。
}

function timeOut(eim) {
    if (eim.getPlayerCount() > 0) {
        var pIter = eim.getPlayers().iterator();
        while (pIter.hasNext()) {
            var player = pIter.next();
            player.dropMessage(6, "你已经没有时间完成这个事件！");
            playerExit(eim, player);
        }
    }
    eim.dispose(); // 解散事件实例。
}

function monsterKilled(mob, eim) {
    // 当敌对怪物死亡时触发。
}

function monsterValue(eim, mobid) {
    // 当注册的怪物被击杀时调用。
    // 返回此玩家获得的积分 - “保存点数”
}

function friendlyKilled(mob, eim) {
    // 当友好怪物死亡时触发。
}

function allMonstersDead(eim) {
    // 当调用unregisterMonster(Monster mob)或怪物被击杀后触发。
    // 只有当剩余怪物数量为0时触发。
}

function playerDead(eim, player) {
    // 当玩家死亡时触发。
}

function monsterRevive(mob, eim) {
    // 当敌对怪物复活时触发。
}

function playerRevive(eim, player) {
    // 当玩家复活时触发。
    // 参数返回true/false。
}

function playerDisconnected(eim, player) {
    // 返回0 - 正常注销玩家并在玩家数量为零时解散实例。
    // 返回大于0的值 - 正常注销玩家并在玩家数量等于或低于该值时解散实例。
    // 返回小于0的值 - 正常注销玩家并在玩家数量等于或低于该值时解散实例，如果是队长则踢出所有人。
}

function end(eim) {
    // 当队伍未能完成事件实例时触发。
}

function giveRandomEventReward(eim, player) {
    // 从奖励池中随机选择一个奖励给予玩家。
}

function clearPQ(eim) {
    // 当队伍成功完成事件实例时触发。
}

function leftParty(eim, player) {
    // 当玩家离开队伍时触发。
}

function disbandParty(eim, player) {
    // 当队伍解散时触发。
}

function removePlayer(eim, player) {
    // 当NPCConversationManager.removePlayerFromInstance()方法被调用时触发。
}

function registerCarnivalParty(eim, carnivalparty) {
    // 当嘉年华PQ开始时触发。目前未使用。
}

function onMapLoad(eim, player) {
    // 当玩家更换地图时触发。目前未使用。
}

function cancelSchedule() {
    // 结束正在进行的任务调度。
}

function dispose() {
    // 结束事件实例。
}