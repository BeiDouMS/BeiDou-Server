function enter(pi) {
    if (pi.getPlayer().getJob().getJobNiche() == 3) {
        pi.playPortalSound();
        pi.warp(610030540, 0);
        return true;
    } else {
        pi.playerMessage(5, "只有弓箭手才能进入此传送门。");
        return false;
    }
}