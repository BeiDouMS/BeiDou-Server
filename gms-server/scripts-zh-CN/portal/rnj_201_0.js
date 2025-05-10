function enter(pi) {
    if (pi.getMap().getReactorByName("rnj31_out").getState() == 1) {
        pi.playPortalSound();
        pi.warp(926100200, 1);
        return true;
    } else {
        pi.playerMessage(5, "传送门尚未开启。");
        return false;
    }
}