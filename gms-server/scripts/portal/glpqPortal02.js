function enter(pi) {
    var eim = pi.getEventInstance();
    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (eim != null && GameConfig.getServerBoolean("use_enable_stage_skip") && eim.getPlayerCount() == 1) {
        pi.playPortalSound();
        pi.warp(610030521, 0);
        return true;
    }

    if (pi.getPlayer().getJob().getJobNiche() == 2) {
        pi.playPortalSound();
        pi.warp(610030521, 0);
        return true;
    }

    pi.playerMessage(5, "Only Magicians may enter this portal!");
    return false;
}
