function enter(pi) {
    try {
        var eim = pi.getEventInstance();
        if (eim != null && eim.getProperty("stage2") === "3") {
            pi.playPortalSound();
            pi.warp(925100200, 0); //next
            return true;
        } else {
            pi.playerMessage(5, "传送门尚未开启。");
            return false;
        }
    } catch (e) {
        pi.playerMessage(5, "传送门故障，请联系管理员处理");
    }

    return false;
}