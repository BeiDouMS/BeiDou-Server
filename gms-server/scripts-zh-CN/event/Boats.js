// 渡轮相关地图变量
/** 候船室<开往魔法密林> */var Orbis_btf;
/** 开往天空之城的船 */var Boat_to_Orbis;
/** 船仓<开往天空之城> */var Orbis_Boat_Cabin;
/** 码头<开往魔法密林> */var Orbis_docked;
/** 候船室<开往天空之城> */var Ellinia_btf;
/** 开往魔法密林的船 */var Boat_to_Ellinia;
/** 船仓<开往魔法密林> */var Ellinia_Boat_Cabin;
/** 魔法密林码头 */var Ellinia_docked;
/** 天空之城售票处 */var Orbis_Station;

// 时间设置（以毫秒为单位），以下变量会被getTransportationTime()函数改变时间倍率而重新赋值
/** 关闭登船入口的时间 (4分钟) */ var closeTime = 4 * 60 * 1000;
/** 船只启航前的准备时间 (5分钟) */ var beginTime = 5 * 60 * 1000;
/** 到达目的地所需的时间 (10分钟) */ var rideTime = 10 * 60 * 1000;
/** 蝙蝠魔船只接近的时间 (3分钟) */ var invasionStartTime = 3 * 60 * 1000;
/** 蝙蝠魔船只接近的时间延迟 (1分钟) */ var invasionDelayTime = 1 * 60 * 1000;
/** 生成蝙蝠魔的时间延迟 (5秒) */ var invasionDelay = 5 * 1000;

const PacketCreator = Java.type('org.gms.util.PacketCreator');  //获取封包数据实例

function init() {
    console.log("当前函数名:", getCurrentFunctionName());
    // 初始化函数，用于设置时间和获取地图实例。
    closeTime = em.getTransportationTime(closeTime);
    beginTime = em.getTransportationTime(beginTime);
    rideTime = em.getTransportationTime(rideTime);
    invasionStartTime = em.getTransportationTime(invasionStartTime);
    invasionDelayTime = em.getTransportationTime(invasionDelayTime);
    console.log(closeTime/1000,beginTime/1000,rideTime/1000,invasionStartTime/1000,invasionDelayTime/1000);
    // 获取地图实例
    Orbis_btf = em.getChannelServer().getMapFactory().getMap(200000112);    //候船室<开往魔法密林>
    Ellinia_btf = em.getChannelServer().getMapFactory().getMap(101000301);  //候船室<开往天空之城>
    Boat_to_Orbis = em.getChannelServer().getMapFactory().getMap(200090010);    //开往天空之城
    Boat_to_Ellinia = em.getChannelServer().getMapFactory().getMap(200090000);  //开往魔法密林
    Orbis_Boat_Cabin = em.getChannelServer().getMapFactory().getMap(200090011); //船仓<开往天空之城>
    Ellinia_Boat_Cabin = em.getChannelServer().getMapFactory().getMap(200090001);   //船仓<开往魔法密林>
    Ellinia_docked = em.getChannelServer().getMapFactory().getMap(101000300);   //魔法密林码头
    Orbis_Station = em.getChannelServer().getMapFactory().getMap(200000100);    //天空之城售票处
    Orbis_docked = em.getChannelServer().getMapFactory().getMap(200000111);     //码头<开往魔法密林>

    // 设置码头状态为已停靠
    Ellinia_docked.setDocked(true);
    Orbis_docked.setDocked(true);

    // 安排新的周期性任务
    scheduleNew();
}

function scheduleNew() {
    console.log("scheduleNew");
    // 设置属性，并安排关闭入口和起飞的任务
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.setProperty("haveBalrog", "false");

    // 安排关闭入口和起飞的时间点
    em.schedule("stopentry", closeTime);
    em.schedule("takeoff", beginTime);
    // setClock(Ellinia_docked,closeTime);
    // setClock(Orbis_docked,closeTime);
}

function stopentry() {
    // 关闭入口后清除船舱内的对象（例如箱子）
    em.setProperty("entry", "false");
    Orbis_Boat_Cabin.clearMapObjects();   // 清除船舱内的对象
    Ellinia_Boat_Cabin.clearMapObjects();
}

function takeoff() {
    console.log("takeoff");

    // 玩家被传送至船上，广播船只离开的消息
    Orbis_btf.warpEveryone(Boat_to_Ellinia.getId());
    Ellinia_btf.warpEveryone(Boat_to_Orbis.getId());
    Ellinia_docked.broadcastShip(false); // 广播魔法密林头船只离开消息
    Orbis_docked.broadcastShip(false);   // 广播天空之城码头船只离开消息

    // 设置码头状态为未停靠
    em.setProperty("docked", "false");

    // 随机决定是否会有蝙蝠魔船只接近
    if (Math.random() < 0.42) {
        em.schedule("approach", invasionStartTime + Math.trunc(Math.random() * invasionDelayTime));
    }

    // 安排到达目的地的时间点
    em.schedule("arrived", rideTime);
    // em.startInstance(1,);
    // eim.startEventTimer(rideTime);  // 启动事件计时器
    // setClock(Boat_to_Ellinia,rideTime);
    // setClock(Boat_to_Orbis,rideTime);
}

function arrived() {
    console.log("arrived");
    // end();
    // 玩家到达目的地后被传送至对应站点或码头
    Boat_to_Orbis.warpEveryone(Orbis_Station.getId(), 0);
    Orbis_Boat_Cabin.warpEveryone(Orbis_Station.getId(), 0);
    Boat_to_Ellinia.warpEveryone(Ellinia_docked.getId(), 1);
    Ellinia_Boat_Cabin.warpEveryone(Ellinia_docked.getId(), 1);

    // 播放船只到达的消息并重置蝙蝠魔状态
    Orbis_docked.broadcastShip(true);
    Ellinia_docked.broadcastShip(true);
    Boat_to_Orbis.broadcastEnemyShip(false);
    Boat_to_Ellinia.broadcastEnemyShip(false);
    Boat_to_Orbis.killAllMonsters();
    Boat_to_Ellinia.killAllMonsters();
    em.setProperty("haveBalrog", "false");

    // 安排下一个周期性任务
    scheduleNew();
}

function approach() {
    console.log("approach");
    // 处理蝙蝠魔船只接近的情况
    if (Math.floor(Math.random() * 10) < 10) {
        em.setProperty("haveBalrog", "true");
        Boat_to_Orbis.broadcastEnemyShip(true);
        Boat_to_Ellinia.broadcastEnemyShip(true);

        // 更改背景音乐
        const PacketCreator = Java.type('org.gms.util.PacketCreator');
        Boat_to_Orbis.broadcastMessage(PacketCreator.musicChange("Bgm04/ArabPirate"));
        Boat_to_Ellinia.broadcastMessage(PacketCreator.musicChange("Bgm04/ArabPirate"));

        // 安排蝙蝠魔出现的时间点
        em.schedule("invasion", invasionDelay);
    }
}

function invasion() {
    console.log("invasion");
    // 生成偶遇蝙蝠魔
    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');

    var map1 = Boat_to_Ellinia;
    var pos1 = new java.awt.Point(-538, 143);
    map1.spawnMonsterOnGroundBelow(LifeFactory.getMonster(8150000), pos1);
    map1.spawnMonsterOnGroundBelow(LifeFactory.getMonster(8150000), pos1);

    var map2 = Boat_to_Orbis;
    var pos2 = new java.awt.Point(339, 148);
    map2.spawnMonsterOnGroundBelow(LifeFactory.getMonster(8150000), pos2);
    map2.spawnMonsterOnGroundBelow(LifeFactory.getMonster(8150000), pos2);
}

/**
 * 取消预定的事件/任务调度
 */
function cancelSchedule() {
    console.log("当前函数名:", getCurrentFunctionName());
}

function setClock(MapObj,Time) {
    console.log("setClock",MapObj,Time);
    const players = MapObj.getCharacters(); //获取当前地图的所有角色
    console.log(players);
    if (players.length > 0){
        players.map(player => { //遍历角色
            player.sendPacket(PacketCreator.getClock(Time));    //发送创建倒计时封包
        });
    }
}

// ---------- 辅助函数 ----------

/**
 * 释放/清理资源
 */
function dispose() {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 初始化副本实例（Event Instance Management）
 * @param {Object} eim - 副本实例对象
 * @param {number} leaderid - 队伍领袖ID
 */
function setup(level, lobbyid) {
    console.log("当前函数名:", getCurrentFunctionName());
    var eim = em.newInstance("Boats_takeoff");
    var Maplist = [Boat_to_Ellinia,Boat_to_Orbis,Orbis_Boat_Cabin,Ellinia_Boat_Cabin];
    Maplist.map(obj => eim.getInstanceMap(obj.getId()).resetPQ(1));
    eim.startEventTimer(rideTime);  // 启动事件计时器
    return eim;
}

/**
 * 获取指定怪物的数值（经验/掉落等）
 * @param {Object} eim - 副本实例对象
 * @param {number} mobid - 怪物ID
 * @return {number} 怪物数值
 */
function monsterValue(eim, mobid) {return 0;}

/**
 * 解散队伍
 * @param {Object} eim - 副本实例对象
 * @param {Object} player - 玩家对象
 */
function disbandParty(eim, player) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 玩家断开连接处理
 * @param {Object} eim - 副本实例对象
 * @param {Object} player - 玩家对象
 */
function playerDisconnected(eim, player) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 玩家进入副本处理
 * @param {Object} eim - 副本实例对象
 * @param {Object} player - 玩家对象
 */
function playerEntry(eim, player) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 怪物被击杀处理
 * @param {Object} mob - 怪物对象
 * @param {Object} eim - 副本实例对象
 */
function monsterKilled(mob, eim) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 预定超时触发
 * @param {Object} eim - 副本实例对象
 */
function scheduledTimeout(eim) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 副本设置完成后执行
 * @param {Object} eim - 副本实例对象
 */
function afterSetup(eim) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 队伍领袖变更处理
 * @param {Object} eim - 副本实例对象
 * @param {Object} leader - 新领袖对象
 */
function changedLeader(eim, leader) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 玩家退出副本处理
 * @param {Object} eim - 副本实例对象
 * @param {Object} player - 玩家对象
 */
function playerExit(eim, player) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 玩家离开队伍处理
 * @param {Object} eim - 副本实例对象
 * @param {Object} player - 玩家对象
 */
function leftParty(eim, player) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 清理副本任务（Party Quest）
 * @param {Object} eim - 副本实例对象
 */
function clearPQ(eim) {
    console.log("当前函数名:", getCurrentFunctionName());
    eim.stopEventTimer();
    eim.setEventCleared();
}

/**
 * 所有怪物被击杀处理
 * @param {Object} eim - 副本实例对象
 */
function allMonstersDead(eim) {
    console.log("当前函数名:", getCurrentFunctionName());
}

/**
 * 玩家注销/取消注册处理
 * @param {Object} eim - 副本实例对象
 * @param {Object} player - 玩家对象
 */
function playerUnregistered(eim, player) {
    console.log("当前函数名:", getCurrentFunctionName());
}
function end(eim) {
    eim.dispose();
}
function getCurrentFunctionName() {
    const stack = new Error().stack;
    const stackLines = stack.split('\n');
    // 不同浏览器/环境格式不同，可能需要调整
    const callerLine = stackLines[2]; // 通常是调用者的行
    const functionName = callerLine.match(/at (.*?) /)?.[1] || 'anonymous';
    return functionName;
}