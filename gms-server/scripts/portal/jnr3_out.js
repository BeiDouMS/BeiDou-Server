function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim == null) {
        return false;
    }

    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (GameConfig.getServerBoolean("use_enable_stage_skip") && eim.getPlayerCount() == 1) {
        if (eim.getIntProperty("statusStg4") < 1) {
            eim.setIntProperty("statusStg4", 1);
            try {
                eim.showClearEffect();
                eim.giveEventPlayersStageReward(4);
                pi.getMap().killAllMonsters();
            } catch (err) {
            }
        }
        try {
            var door = pi.getMap().getReactorByName("jnr3_out3");
            if (door != null) {
                door.forceHitReactor(1);
            }
        } catch (err) {
        }
        pi.playPortalSound();
        pi.warp(926110203, 0);
        return true;
    }

    if (pi.getMap().getReactorByName("jnr3_out3").getState() == 1) {
        pi.playPortalSound();
        pi.warp(926110203, 0); //next
        return true;
    }

    pi.playerMessage(5, "The door is not opened yet.");
    return false;
}
