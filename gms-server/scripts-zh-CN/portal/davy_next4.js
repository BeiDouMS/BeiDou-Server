function enter(pi) {
    if (pi.getMap().getReactorByName("sMob1").getState() >= 1 && pi.getMap().getReactorByName("sMob2").getState() >= 1 && pi.getMap().getReactorByName("sMob3").getState() >= 1 && pi.getMap().getReactorByName("sMob4").getState() >= 1 && pi.getMap().getMonsters().size() == 0) {
        var eim = pi.getEventInstance();

        if (eim.getProperty("spawnedBoss") == null) {
            var level = parseInt(eim.getProperty("level"));
            var chests = parseInt(eim.getProperty("openedChests"));
            var boss;

            const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
            if (chests == 0) {
                boss = LifeFactory.getMonster(9300119);
            }//lord pirate
            else if (chests == 1) {
                boss = LifeFactory.getMonster(9300105);
            }//angry lord pirate
            else {
                boss = LifeFactory.getMonster(9300106);
            }                   //enraged lord pirate

            boss.changeDifficulty(level, true);

            const Point = Java.type('java.awt.Point');
            pi.getMap(925100500).spawnMonsterOnGroundBelow(boss, new Point(777, 140));
            eim.setProperty("spawnedBoss", "true");
        }

        pi.playPortalSound();
        pi.warp(925100500, 0);
        return true;
    } else {
        pi.playerMessage(5, "传送门尚未开启。");
        return false;
    }
}