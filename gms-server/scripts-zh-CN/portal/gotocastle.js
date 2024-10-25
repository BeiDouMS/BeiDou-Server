function enter(pi) {
    if (pi.isQuestCompleted(2324)) {
        pi.playPortalSound();
        pi.warp(106020501, 0);
        return true;
    } else {
        pi.playerMessage(5, "前路布满荆棘，无法通过！");
        return false;
    }
}
