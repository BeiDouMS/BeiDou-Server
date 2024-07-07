function enter(pi) {
    if (!pi.haveItem(4031890)) {
        pi.getPlayer().dropMessage(6, "你需要持有霍夫卡才能激活这个机器。");
        return false;
    }

    pi.playPortalSound();
    pi.warp(221000300, "earth00");
    return true;
}