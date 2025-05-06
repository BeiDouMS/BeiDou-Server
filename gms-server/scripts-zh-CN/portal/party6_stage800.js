function enter(pi) {
    pi.removeAll(4001162);
    pi.removeAll(4001163);
    pi.removeAll(4001164);
    pi.removeAll(4001169);
    pi.removeAll(2270004);

    var spring = pi.getMap().getReactorById(3008000);  // thanks Chloek3, seth1 for noticing fragments not being awarded properly
    if (spring != null && spring.getState() > 0) {
        if (!pi.canHold(4001198, 1)) {
            pi.playerMessage(5, "进入传送门前请给背包的 其它栏 空出至少1个空格子。");
            return false;
        }

        pi.gainItem(4001198, 1);
    }

    pi.playPortalSound();
    pi.warp(300030100, 0);
    return true;
}