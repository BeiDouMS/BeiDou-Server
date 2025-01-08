// 渡轮相关地图变量
var Orbis_btf; // 候船室<开往魔法密林>
var Boat_to_Orbis; // 开往天空之城
var Orbis_Boat_Cabin; // 船仓<开往天空之城>
var Orbis_docked; // 码头<开往魔法密林>

var Ellinia_btf; // 候船室<开往天空之城>
var Boat_to_Ellinia; // 开往魔法密林
var Ellinia_Boat_Cabin; // 船仓<开往魔法密林>
var Ellinia_docked; // 魔法密林码头

var Orbis_Station; // 天空之城售票处

// 时间设置（以毫秒为单位）
var closeTime = 4 * 60 * 1000; // 关闭登船入口的时间
var beginTime = 5 * 60 * 1000; // 船只启航前的准备时间
var rideTime = 10 * 60 * 1000; // 到达目的地所需的时间
var invasionStartTime = 3 * 60 * 1000; // 蝙蝠魔船只接近的时间
var invasionDelayTime = 1 * 60 * 1000; // 蝙蝠魔船只接近的时间延迟
var invasionDelay = 5 * 1000; // 生成蝙蝠魔的时间延迟

function init() {
    // 初始化函数，用于设置时间和获取地图实例。
    closeTime = em.getTransportationTime(closeTime);
    beginTime = em.getTransportationTime(beginTime);
    rideTime = em.getTransportationTime(rideTime);
    invasionStartTime = em.getTransportationTime(invasionStartTime);
    invasionDelayTime = em.getTransportationTime(invasionDelayTime);

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
    // 设置属性，并安排关闭入口和起飞的任务
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.setProperty("haveBalrog", "false");

    // 安排关闭入口和起飞的时间点
    em.schedule("stopentry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopentry() {
    // 关闭入口后清除船舱内的对象（例如箱子）
    em.setProperty("entry", "false");
    Orbis_Boat_Cabin.clearMapObjects();   // 清除船舱内的对象
    Ellinia_Boat_Cabin.clearMapObjects();
}

function takeoff() {
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
}

function arrived() {
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

function cancelSchedule() {}

// ---------- 辅助函数 ----------
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