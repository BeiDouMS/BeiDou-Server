function enter(pi) {
    var mapid = pi.getPlayer().getMap().getId();

    if (mapid == 103040410 && pi.isQuestCompleted(2287)) {
        pi.playPortalSound();
        pi.warp(103040420, "right00");
        return true;
    } else if (mapid == 103040420 && pi.isQuestCompleted(2288)) {
        pi.playPortalSound();
        pi.warp(103040430, "right00");
        return true;
    } else if (mapid == 103040410 && pi.isQuestStarted(2287)) {
        pi.playPortalSound();
        pi.warp(103040420, "right00");
        return true;
    } else if (mapid == 103040420 && pi.isQuestStarted(2288)) {
        pi.playPortalSound();
        pi.warp(103040430, "right00");
        return true;
    } else {
        if (mapid == 103040440 || mapid == 103040450) {
            pi.playPortalSound();
            pi.warp(mapid + 10, "right00");
            return true;
        }
        pi.getPlayer().dropMessage(5, "您无法进入该区域");
        return false;
    }
}