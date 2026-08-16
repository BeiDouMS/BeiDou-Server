function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim == null) {
        return false;
    }

    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (GameConfig.getServerBoolean("use_enable_stage_skip") && eim.getPlayerCount() == 1) {
        if (eim.getIntProperty("glpq5") < 5) {
            eim.setIntProperty("glpq5", 5);
            try {
                eim.showClearEffect(610030500, "5pt", 2);
                eim.giveEventPlayersStageReward(5);
            } catch (err) {
            }
        }
        pi.playPortalSound();
        pi.warp(610030600, 0);
        return true;
    }

    if (eim.getIntProperty("glpq5") < 5) {
        pi.playerMessage(5, "传送门尚未开启。");
        return false;
    }

    pi.playPortalSound();
    pi.warp(610030600, 0);
    return true;
}
