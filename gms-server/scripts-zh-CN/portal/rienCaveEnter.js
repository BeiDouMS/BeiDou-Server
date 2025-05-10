function enter(pi) {
    if (pi.isQuestStarted(21201) || pi.isQuestStarted(21302)) { //aran first job
        pi.playPortalSound();
        pi.warp(140030000, 1);
        return true;
    } else {
        pi.playerMessage(5, "传送门的作用似乎被某种力量封印了！");
        return false;
    }

}