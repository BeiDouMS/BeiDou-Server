function enter(pi) {
    if (pi.isQuestCompleted(22015)) {
        pi.playPortalSound();
        pi.warp(100030300, 2);
    } else {
        pi.playerMessage(5, "请救救小猪！");//not gms like
    }
    return true;
}