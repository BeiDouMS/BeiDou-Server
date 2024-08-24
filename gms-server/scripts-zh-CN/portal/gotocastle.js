function enter(pi) {
    if (pi.isQuestCompleted(2324)) {
        pi.playPortalSound();
        pi.warp(106020501, 0);
        return true;
    } else {
        pi.playerMessage(5, "前路布满荆棘，需要尖刺消除剂清理……");
        return false;
    }
}
