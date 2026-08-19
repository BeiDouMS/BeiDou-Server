function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim == null) {
        return false;
    }

    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (GameConfig.getServerBoolean("use_enable_stage_skip") && eim.getPlayerCount() == 1) {
        if (eim.getIntProperty("statusStg6") == 0) {
            eim.setIntProperty("statusStg6", 1);
            try {
                eim.showClearEffect();
                eim.giveEventPlayersStageReward(6);
            } catch (err) {
            }
        }
        pi.playPortalSound();
        pi.warp(926100400, 0);
        return true;
    }

    var area = eim.getIntProperty("statusStg5");
    var reg = 3;

    if ((area >> reg) % 2 == 0) {
        area |= (1 << reg);
        eim.setIntProperty("statusStg5", area);

        pi.playPortalSound();
        pi.warp(926100301 + reg, 0); //next
        return true;
    } else {
        pi.playerMessage(5, "This room is already being explored.");
        return false;
    }
}
