function enter(pi) {
    if (pi.getPlayer().getJob().getJobNiche() == 5) {
        pi.playPortalSound();
        pi.warp(610030550, 0);
        return true;
    } else {
        pi.playerMessage(5, "※ 仅限海盗职业进入该传送门！")
        return false;
    }
}