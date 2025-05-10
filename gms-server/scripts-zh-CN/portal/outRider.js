function enter(pi) {
    if (pi.canHold(4001193, 1)) {
        pi.gainItem(4001193, 1);
        pi.playPortalSound();
        pi.warp(211050000, 4);
        return true;
    } else {
        pi.playerMessage(5, "请在背包中的其它栏目预留至少1个空位以领取通关奖励");
        return false;
    }
}