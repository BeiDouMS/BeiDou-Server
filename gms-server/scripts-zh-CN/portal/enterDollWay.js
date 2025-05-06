function enter(pi) {
    if (pi.isQuestCompleted(20730) || pi.isQuestCompleted(21734)) {  // puppeteer defeated, newfound secret path
        pi.playPortalSound();
        pi.warp(105070300, 3);
        return true;
    } else if (pi.isQuestStarted(21734)) {
        pi.playPortalSound();
        pi.warp(910510100, 0);
        return true;
    } else {
        pi.message("一股不祥的力量阻止你通过此处。");
        return false;
    }
}