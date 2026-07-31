function enter(pi) {
    if (!pi.isQuestCompleted(2324)) {
        pi.playerMessage(5, "城墙上仍然长满了荆棘。");
        return false;
    }

    pi.playPortalSound();
    pi.warp(106020501, 0);
    return true;
}
