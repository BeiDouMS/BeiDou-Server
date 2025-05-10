function enter(pi) {
    if (pi.getPlayer().getJob().getJobNiche() == 2) {
        pi.playPortalSound();
        pi.warp(610030521, 0);
        return true;
    } else {
        pi.playerMessage(5, "※ 仅限魔法师职业进入该传送门！");
        return false;
    }
}