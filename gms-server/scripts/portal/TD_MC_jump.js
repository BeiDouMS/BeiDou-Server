function enter(pi) {
    if (!pi.isQuestCompleted(2324)) {
        pi.playerMessage(5, "The castle wall is still covered in thorns.");
        return false;
    }

    pi.playPortalSound();
    pi.warp(106020501, 0);
    return true;
}
