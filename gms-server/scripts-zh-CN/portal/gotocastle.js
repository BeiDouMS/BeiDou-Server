function enter(pi) {
    if (pi.isQuestCompleted(2321)) {
        pi.playPortalSound();
        pi.warp(pi.isQuestCompleted(2324) ? 106020501 : 106020500, 0);
        return true;
    } else {
        pi.playerMessage(5, "前路布满荆棘，无法通过！");
        return false;
    }
}