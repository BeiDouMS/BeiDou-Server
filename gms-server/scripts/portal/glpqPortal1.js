function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim == null) {
        return false;
    }

    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (GameConfig.getServerBoolean("use_enable_stage_skip") && eim.getPlayerCount() == 1) {
        if (eim.getIntProperty("glpq2") < 5) {
            eim.setIntProperty("glpq2", 5);
            try {
                eim.showClearEffect(610030200, "2pt", 2);
                eim.giveEventPlayersStageReward(2);
            } catch (err) {
            }
        }
        pi.playPortalSound();
        pi.warp(610030300, 0);
        return true;
    }

    if (eim.getIntProperty("glpq2") == 5) {
        pi.playPortalSound();
        pi.warp(610030300, 0);
        return true;
    }

    pi.playerMessage(5, "The portal is not activated yet!");
    return false;
}
