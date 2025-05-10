function enter(pi) {
    var map = pi.getPlayer().getMap();
    if (pi.getPortal().getName() == "female00") {
        if (pi.getPlayer().getGender() == 1) {
            pi.playPortalSound();
            pi.warp(map.getId(), "female01");
            return true;
        } else {
            pi.message("此传送门通向女生区域，请使用另一侧的男生传送门");
            return false;
        }
    } else {
        if (pi.getPlayer().getGender() == 0) {
            pi.playPortalSound();
            pi.warp(map.getId(), "male01");
            return true;
        } else {
            pi.message("此传送门通向男生区域，请使用另一侧的女生传送门");
            return false;
        }
    }
}