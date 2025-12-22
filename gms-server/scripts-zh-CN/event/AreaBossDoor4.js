/*
 *  通用野外BOSS事件脚本模板 (Standalone Area Boss Script Template)
 *  
 *  功能：
 *  1. 自动生成指定 BOSS。
 *  2. 监听 BOSS 死亡事件。
 *  3. 死亡后依指定时间准确重置刷新。
 *  4. [新增] 只有当玩家进入地图且冷却时间已到时才生成 (Lazy Spawn)。
 *
 *  使用说明：
 *  - 自动更新脚本生成
 */

// ============================================================================
//                              配置区域 (Configuration)
// ============================================================================

// 地图 ID (BOSS 生成的地图)
const MapID = 677000012;

// BOSS ID (要生成的怪物 ID)
const BossID = 9400633;

// BOSS 名称 (默认名称，若数据库有读取到会自动更新)
var BossName = "牛魔王";

// 刷新冷却时间 (单位：分钟)
const BossTime = 180;

// BOSS 生成坐标 (X, Y)
const SpawnPoint = new java.awt.Point(842, 0);

// BOSS 出现时的全服公告内容
const BossNotice = "玛巴斯现世之时，黄金狮鬃在硫磺风暴中燃烧成几何光轮，其足印所踏之处自动浮现精密齿轮构成的所罗门封印‌";

// ============================================================================
//                            核心逻辑 (Core Logic)
// ============================================================================

const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
const PacketCreator = Java.type('org.gms.util.PacketCreator');
const LoggerFactory = Java.type('org.slf4j.LoggerFactory');

var log = null;
var channel = null;

// 事件实例名称 (自动获取，无需修改)
const EventName = "AreaBossDoor4";

function init() {
    channel = em.getChannelServer().getId();
    log = LoggerFactory.getLogger(em.getName());
    em.registerMapListener(MapID);
    scheduleNew(); // [新系统] 注册地图监听器，避免绑定玩家 EIM
}

function scheduleNew() {
    setupEventInstance();

    var eim = getOrCreateEventInstance();
    // 标记：冷却已就绪 (服务器重启默认为就绪)
    eim.setProperty("canSpawn", "true");

    // 关键修正：必须启动事件，monsterKilled 等监听器才会生效
    try { eim.startEvent(); } catch (e) { }

    // 尝试生成
    // 如果地图已经有人，立即生成
    // 如果地图没人，不做任何事，等待玩家进入
    checkAndSpawn(eim);
}

function cancelSchedule() {
    var eim = em.getInstance(EventName);
    if (eim != null) {
        eim.dispose();
    }
}

// 辅助函数：获取或创建实例
function getOrCreateEventInstance() {
    var eim = em.getInstance(EventName);
    if (eim == null) {
        try {
            eim = em.newInstance(EventName);
        } catch (e) {
            eim = em.getInstance(EventName);
        }
    }
    return eim;
}

function setupEventInstance() {
    var eim = getOrCreateEventInstance();
    var map = em.getChannelServer().getMapFactory().getMap(MapID);
    // 绑定地图到EIM
    map.setEventInstance(eim);
}

// 核心监测函数：替代 playerEntry 的轮询机制
function checkAndSpawn(eim) {
    if (eim == null) return;
    
    var canSpawn = eim.getProperty("canSpawn");
    // 如果不能刷（已经刷了，或正在CD），直接停止监测
    if (canSpawn != "true") {
        return;
    }

    var map = em.getChannelServer().getMapFactory().getMap(MapID);

    // 1. 检查人数
    if (map.getCharacters().size() > 0) {
        // 有人，嘗試生成
        spawnBoss(eim, map);
    }
}


function spawnBoss(eim, map) {
    try {
        // 双重保险：检查 BOSS 是否已存在
        if (map.getMonsterById(BossID) != null) {
            eim.setProperty("canSpawn", "false");
            return;
        }

        // 准备 BOSS 对象
        var bossMob = LifeFactory.getMonster(BossID);
        if (bossMob.getName()) {
            BossName = bossMob.getName();
        }

        // 生成 BOSS
        map.spawnMonsterOnGroundBelow(bossMob, SpawnPoint);

        // 标记为不可刷新
        eim.setProperty("canSpawn", "false");

        

        // 发送公告
        map.broadcastMessage(PacketCreator.serverNotice(6, `[野外BOSS] ${BossName}  ${BossNotice}`));

    } catch (e) {
        if (log) log.error(`[野外BOSS] ${EventName} 生成失败`, e);
    }
}

// ============================================================================
//                            事件回调 (Event Hooks)
// ============================================================================

/**
 * [新系统] 当玩家进入监听地图时触发
 */
function onMapPlayerEnter(em, player, mapId) {
    if (mapId != MapID) return;
    
    var eim = em.getInstance(EventName);
    if (eim != null) {
        
        checkAndSpawn(eim);
    }
}

function monsterKilled(mob, eim) {
    // 判断死亡的怪物是否为目标 BOSS
    if (mob.getId() == BossID) {
        var nextRespawnTime = em.getBossTime(BossTime * 60 * 1000);

        if (log) log.info(`[野外BOSS] ${BossName}(${BossID}) 被击杀, ${nextRespawnTime / 60000} 分钟后刷新`);

        // 设定倒数计时，时间到后执行 cooldownFinished
        em.schedule("cooldownFinished", nextRespawnTime);
    }
}

// 计时器回调：冷却结束
function cooldownFinished() {
    var eim = getOrCreateEventInstance();
    // 标记冷却结束
    eim.setProperty("canSpawn", "true");

    // 启动监测
    
    checkAndSpawn(eim);
}

// 必须保留的空函数
function playerEntry(eim, player) { }
function dispose() { }
function setup(eim, leaderid) { }
function monsterValue(eim, mobid) { return 0; }
function disbandParty(eim, player) { }
function playerDisconnected(eim, player) { }
function scheduledTimeout(eim) { }
function afterSetup(eim) { }
function changedLeader(eim, leader) { }
function playerExit(eim, player) { }
function leftParty(eim, player) { }
function clearPQ(eim) { }
function allMonstersDead(eim) { }
function playerUnregistered(eim, player) { }
function playerRevive(eim, player) { return true; }