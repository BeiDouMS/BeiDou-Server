function enter(pi) {
    if (pi.hasItem(4032120) || pi.hasItem(4032121) || pi.hasItem(4032122) || pi.hasItem(4032123) || pi.hasItem(4032124)) {
        pi.playerMessage(5, "你已经有了资格的证物。");
        return false;
    }
    if (pi.isQuestStarted(20601) || pi.isQuestStarted(20602) || pi.isQuestStarted(20603) || pi.isQuestStarted(20604) || pi.isQuestStarted(20605)) {
        if (pi.getPlayerCount(913010200) == 0) {
            var map = pi.getMap(913010200);
            map.killAllMonsters();
            pi.playPortalSound();
            pi.warp(913010200, 0);
            pi.spawnMonster(9300289, 0, 0);
            return true;
        } else {
            pi.playerMessage(5, "已经有人在尝试击败Boss，请你稍后再来。");
            return false;
        }
    } else {
        pi.playerMessage(5, "你必须达到100级且正在进行技能训练方可进入第3演武场。");
        return false;
    }
}