function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim == null) {
        return false;
    }

    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (eim.getIntProperty("statusStg3") != 3
            && GameConfig.getServerBoolean("use_enable_stage_skip")
            && eim.getPlayerCount() == 1) {
        eim.setIntProperty("statusStg3", 3);
        try {
            eim.showClearEffect();
            eim.giveEventPlayersStageReward(3);
            pi.getMap().killAllMonsters();
            var door = pi.getMap().getReactorByName("rnj2_door");
            if (door != null) {
                door.forceHitReactor(1);
            }
        } catch (err) {
        }
    }

    if (eim.getIntProperty("statusStg3") == 3) {
        pi.playPortalSound();
        pi.warp(926100200, 0); //next
        return true;
    }

    pi.playerMessage(5, "传送门尚未开启。");
    return false;
}
