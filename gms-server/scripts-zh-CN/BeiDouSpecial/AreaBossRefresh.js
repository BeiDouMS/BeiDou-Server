const Date = Java.type('java.util.Date');
const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
const Server = Java.type('org.gms.net.server.Server');
const SimpleDateFormat = Java.type('java.text.SimpleDateFormat');

function start() {
    levelStart();
}

function levelStart() {
    var bossEntries = loadBossEntries();
    if (bossEntries.length === 0) {
        cm.sendOkLevel("Dispose", "暂时没有可查询的野外 BOSS。请确认服务器已完成启动。");
        return;
    }

    var text = "#e#b<野外 BOSS 刷新查询>#k#n\r\n\r\n请选择要查询的 BOSS：\r\n";
    for (var i = 0; i < bossEntries.length; i++) {
        text += "#L" + i + "##b" + bossEntries[i].bossName + "#k（" + bossEntries[i].mapName + "）#l\r\n";
    }
    cm.sendNextSelectLevel("ShowStatus", text);
}

function levelShowStatus(selection) {
    var bossEntries = loadBossEntries();
    if (selection < 0 || selection >= bossEntries.length) {
        cm.sendOkLevel("Dispose", "选择无效，请重新打开查询界面。");
        return;
    }
    cm.sendOkLevel("Dispose", buildRefreshStatus(bossEntries[selection]));
}

function levelDispose() {
    cm.dispose();
}

function loadBossEntries() {
    var entries = [];
    var managers = cm.getClient().getChannelServer().getEventSM().getEventManagers();
    for (var i = 0; i < managers.size(); i++) {
        var manager = managers.get(i);
        if (!manager.getName().startsWith("AreaBoss")) {
            continue;
        }

        var bossMobId = manager.getProperty("bossMobId");
        var bossMapId = manager.getProperty("bossMapId");
        if (bossMobId == null || bossMapId == null) {
            continue;
        }

        var parsedBossMobId = parseInt(bossMobId);
        var parsedBossMapId = parseInt(bossMapId);
        var monster = LifeFactory.getMonster(parsedBossMobId);
        if (monster == null) {
            continue;
        }
        var map = cm.getClient().getChannelServer().getMapFactory().getMap(parsedBossMapId);
        entries.push({
            eventName: String(manager.getName()),
            bossMobId: parsedBossMobId,
            bossMapId: parsedBossMapId,
            bossName: String(monster.getName()),
            mapName: map == null ? String(parsedBossMapId) : String(map.getMapName())
        });
    }

    entries.sort(function (left, right) {
        if (left.bossName === right.bossName) {
            if (left.mapName === right.mapName) {
                return 0;
            }
            return left.mapName < right.mapName ? -1 : 1;
        }
        return left.bossName < right.bossName ? -1 : 1;
    });
    return entries;
}

function buildRefreshStatus(boss) {
    var dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
    var channels = Server.getInstance().getAllChannels();
    var text = "#e#b" + boss.bossName + "#k#n（" + boss.mapName + "）\r\n\r\n";
    var found = false;

    for (var i = 0; i < channels.size(); i++) {
        var channel = channels.get(i);
        var manager = findEventManager(channel, boss.eventName);
        if (manager == null) {
            continue;
        }
        found = true;

        var map = channel.getMapFactory().getMap(boss.bossMapId);
        var prefix = "世界 " + (channel.getWorld() + 1) + " / 频道 " + channel.getId() + "：";
        if (map != null && map.getMonsterById(boss.bossMobId) != null) {
            text += prefix + "#g已刷新#k\r\n";
            continue;
        }

        var nextScheduledTime = manager.getNextScheduledTime();
        if (nextScheduledTime == null) {
            text += prefix + "#r尚未初始化#k\r\n";
            continue;
        }
        text += prefix + "#r" + dateFormat.format(new Date(nextScheduledTime)) + "#k\r\n";
    }

    if (!found) {
        text += "\r\n#r当前没有频道加载此 BOSS 事件。#k";
    }
    return text;
}

function findEventManager(channel, eventName) {
    var managers = channel.getEventSM().getEventManagers();
    for (var i = 0; i < managers.size(); i++) {
        var manager = managers.get(i);
        if (String(manager.getName()) === eventName) {
            return manager;
        }
    }
    return null;
}
