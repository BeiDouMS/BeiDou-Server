function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim == null) {
        return false;
    }
    var map = pi.getMap(925100202);
    map.killAllMonsters();
    map.restoreMapSpawnPoints();
    map.instanceMapForceRespawn();

    pi.playPortalSound();
    pi.warp(925100202, 0);
    return true;
}