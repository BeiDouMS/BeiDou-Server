function enter(pi) {
    if (pi.getPlayer().getJob().getJobNiche() == 1) {
        pi.playPortalSound();
        pi.warp(610030510, 0);
        return true;
    } else {
        pi.playerMessage(5, "※ 仅限战士职业进入该传送门！");
        return false;
    }
}