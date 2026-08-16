function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim == null) {
        return false;
    }

    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (GameConfig.getServerBoolean("use_enable_stage_skip") && eim.getPlayerCount() == 1) {
        if (eim.getIntProperty("glpq4") < 5) {
            eim.setIntProperty("glpq4", 5);
            eim.setIntProperty("glpq_s", 777);
            try {
                pi.getMap().killAllMonsters();
                eim.showClearEffect(610030400, "4pt", 2);
                eim.giveEventPlayersStageReward(4);
            } catch (err) {
            }
        }
        pi.playPortalSound();
        pi.warp(610030500, 0);
        return true;
    }

    if (eim.getIntProperty("glpq4") < 5) {
        pi.playerMessage(5, "The portal is not opened yet.");
        return false;
    }

    pi.playPortalSound();
    pi.warp(610030500, 0);
    return true;
}
