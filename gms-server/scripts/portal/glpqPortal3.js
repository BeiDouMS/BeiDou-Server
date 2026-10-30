function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim == null) {
        return false;
    }

    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (GameConfig.getServerBoolean("use_enable_stage_skip") && eim.getPlayerCount() == 1) {
        if (eim.getIntProperty("glpq3") < 5 || eim.getIntProperty("glpq3_p") < 5) {
            eim.setIntProperty("glpq3", 5);
            eim.setIntProperty("glpq3_p", 5);
            try {
                eim.showClearEffect(610030300, "3pt", 2);
                eim.giveEventPlayersStageReward(3);
            } catch (err) {
            }
        }
        pi.playPortalSound();
        pi.warp(610030400, 0);
        return true;
    }

    if (eim.getIntProperty("glpq3") < 5 || eim.getIntProperty("glpq3_p") < 5) {
        pi.playerMessage(5, "The portal is not opened yet.");
        return false;
    }

    pi.playPortalSound();
    pi.warp(610030400, 0);
    return true;
}
