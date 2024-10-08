function enter(pi) {
    if (pi.isQuestActive(21739)) {
        var mapobj1 = pi.getWarpMap(920030000);
        var mapobj2 = pi.getWarpMap(920030001);

        if (mapobj1.countPlayers() == 0 && mapobj2.countPlayers() == 0) {
            mapobj1.resetPQ(1);
            mapobj2.resetPQ(1);
            mapobj2.destroyNPC(1204010);
            
            pi.playPortalSound();
            pi.warp(920030000, 2);
            return true;
        } else {
            pi.message("目标地图有人，请稍后再尝试进入。");
            return false;
        }
    } else {
        pi.playPortalSound();
        pi.warp(200060001, 2);
        return true;
    }
}