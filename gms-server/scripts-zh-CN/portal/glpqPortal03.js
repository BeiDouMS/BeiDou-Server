function enter(pi) {
    if (pi.getPlayer().getJob().getJobNiche() == 4) {
        pi.playPortalSound();
        pi.warp(610030530, 0);
        return true;
    } else {
        pi.playerMessage(5, "※ 仅限飞侠职业进入该传送门！");
        return false;
    }
}