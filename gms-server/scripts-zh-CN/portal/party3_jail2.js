function enter(pi) {
    if (pi.getEventInstance().getIntProperty("statusStg8") == 1) {
        pi.playPortalSound();
        pi.warp(920010920, 0);
        return true;
    } else {
        pi.playerMessage(5, "当前无法使用仓库，因为精灵的力量仍在塔内生效。");
        return false;
    }
}