function enter(pi) {
    if (pi.getPlayer().getMapId() != 777777777) {
        const Server = Java.type('org.gms.net.server.Server');
        if (!Server.getInstance().canEnterDeveloperRoom()) {
            pi.message("这个房间你无法进入");
            return false;
        }

        pi.getPlayer().saveLocation("DEVELOPER");
        pi.playPortalSound();
        pi.warp(777777777, "out00");
    } else {
        try {
            var toMap = pi.getPlayer().getSavedLocation("DEVELOPER");
            pi.playPortalSound();
            pi.warp(toMap, "in00");
        } catch (err) {
            pi.playPortalSound();
            pi.warp(100000000, 0);
        }
    }

    return true;
}