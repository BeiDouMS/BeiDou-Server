function enter(pi) {
    if (pi.getEventInstance().getIntProperty("statusStg3") == 3) {
        pi.playPortalSound();
        pi.warp(926110200, 0); //next
        return true;
    } else {
        pi.playerMessage(5, "传送门尚未开启。");
        return false;
    }
}