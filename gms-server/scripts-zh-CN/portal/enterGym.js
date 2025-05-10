function enter(pi) {
    if (pi.isQuestStarted(21701)) {
        pi.playPortalSound();
        pi.warp(914010000, 1);
        return true;
    } else if (pi.isQuestStarted(21702)) {
        pi.playPortalSound();
        pi.warp(914010100, 1);
        return true;
    } else if (pi.isQuestStarted(21703)) {
        pi.playPortalSound();
        pi.warp(914010200, 1);
        return true;
    } else {
        pi.playerMessage(5, "只有正在接受小企企普奥的修炼指导时，才能进入企鹅训练场。");
        return false;
    }
}