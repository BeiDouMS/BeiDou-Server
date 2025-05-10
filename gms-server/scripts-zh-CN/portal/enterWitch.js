function enter(pi) {
    if (pi.isQuestCompleted(20404)) {
        var warpMap;

        if (pi.isQuestCompleted(20407)) {
            warpMap = 924010200;
        } else if (pi.isQuestCompleted(20406)) {
            warpMap = 924010100;
        } else {
            warpMap = 924010000;
        }

        pi.playPortalSound();
        pi.warp(warpMap, 1);
        return true;


    } else {
        pi.playerMessage(5, "这里阴森森的...还是别进去了！");
        return false;
    }
}