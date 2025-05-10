function enter(pi) {
    if (pi.getMap().getReactorByName("jnr31_out").getState() == 1) {
        pi.playPortalSound();
        pi.warp(926110200, 1);
        return true;
    } else {
        pi.playerMessage(5, "传送门尚未开启。");
        return false;
    }
}