function enter(pi) {
    if (!pi.haveItem(4031890)) {
        pi.getPlayer().dropMessage(6, "需要霍夫卡才能激活此传送门。");
        return false;
    }

    pi.playPortalSound();
    pi.warp(120000101, "earth01");
    return true;
}