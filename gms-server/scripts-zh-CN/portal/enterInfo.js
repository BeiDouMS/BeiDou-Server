function enter(pi) {
    if (pi.isQuestActive(21733) && pi.getQuestProgressInt(21733, 21762) != 2) {
        var mapObj = pi.getWarpMap(910400000);
        if (mapObj.countPlayers() == 0) {
            pi.playPortalSound();
            pi.warp(910400000);
            return true;
        } else {
            pi.message("目标地图有人，请稍后再尝试进入。");
            return false;
        }
    }

    pi.playPortalSound();
    pi.warp(104000004, 1);
    return true;
}