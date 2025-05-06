function enter(pi) {
    if (!pi.haveItem(4031890)) {
        pi.getPlayer().dropMessage(6, "需要霍夫卡才能激活此传送门。");
        return false;
    }

    pi.playPortalSound();
    pi.warp(221000300, "earth00");
    return true;
}