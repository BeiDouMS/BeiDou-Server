function enter(pi) {
    var eim = pi.getEventInstance();
    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (eim != null && GameConfig.getServerBoolean("use_enable_stage_skip") && eim.getPlayerCount() == 1) {
        pi.playPortalSound();
        pi.warp(610030550, 0);
        return true;
    }

    if (pi.getPlayer().getJob().getJobNiche() == 5) {
        pi.playPortalSound();
        pi.warp(610030550, 0);
        return true;
    }

    pi.playerMessage(5, "※ 仅限海盗职业进入该传送门！");
    return false;
}
