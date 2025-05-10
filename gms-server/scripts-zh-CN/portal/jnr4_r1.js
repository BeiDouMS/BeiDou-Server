function enter(pi) {
    var eim = pi.getEventInstance();
    var area = eim.getIntProperty("statusStg5");
    var reg = 0;

    if ((area >> reg) % 2 == 0) {
        area |= (1 << reg);
        eim.setIntProperty("statusStg5", area);

        pi.playPortalSound();
        pi.warp(926110301 + reg, 0); //next
        return true;
    } else {
        pi.playerMessage(5, "该房间正在被探索中。");
        return false;
    }
}