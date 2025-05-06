function enter(pi) {
    if (pi.isQuestCompleted(2238)) {
        pi.playPortalSound();
        pi.warp(105100101, "in00");
        return true;
    } else {
        pi.message("一股神秘的力量阻止你进入。");
        return false;
    }
}