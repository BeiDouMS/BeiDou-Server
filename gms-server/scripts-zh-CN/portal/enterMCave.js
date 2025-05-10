function enter(pi) {
    if (pi.isQuestStarted(21201)) { // 二转
        for (var i = 108000700; i < 108000709; i++) {
            if (pi.getPlayerCount(i) > 0 && pi.getPlayerCount(i + 10) > 0) {
                continue;
            }

            pi.playPortalSound();
            pi.warp(i, "out00");
            pi.setQuestProgress(21202, 21203, 0);
            return true;
        }
        pi.message("由于太多玩家正在回忆，镜子暂时空白。请稍后再试。");
        return false;
    } else if (pi.isQuestStarted(21302) && !pi.isQuestCompleted(21303)) { // 三转
        if (pi.getPlayerCount(108010701) > 0 || pi.getPlayerCount(108010702) > 0) {
            pi.message("由于太多玩家正在回忆，镜子暂时空白。请稍后再试。");
            return false;
        } else {
            var map = pi.getClient().getChannelServer().getMapFactory().getMap(108010702);
            spawnMob(-210, 454, 9001013, map);

            pi.playPortalSound();
            pi.setQuestProgress(21303, 21203, 1);
            pi.warp(108010701, "out00");
            return true;
        }
    } else {
        pi.message("你已经通过测试，无需再次使用镜子。");
        return false;
    }
}

function spawnMob(x, y, id, map) {
    if (map.getMonsterById(id) != null) {
        return;
    }

    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    var mob = LifeFactory.getMonster(id);
    map.spawnMonsterOnGroundBelow(mob, new Point(x, y));
}