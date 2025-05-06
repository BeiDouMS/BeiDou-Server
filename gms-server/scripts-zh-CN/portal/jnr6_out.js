function enter(pi) {
    if (pi.getMap().getReactorByName("jnr6_out").getState() == 1) {
        pi.playPortalSound();
        pi.warp(926110300, 0);
        return true;
    } else {
        pi.playerMessage(5, "传送门尚未开启。");
        return false;
    }
}