function enter(pi) {
    if (pi.getMap().countMonsters() == 0) {
        if (pi.canHold(4001193, 1)) {
            pi.gainItem(4001193, 1);
            pi.playPortalSound();
            pi.warp(140010210, 0);
            return true;
        } else {
            pi.playerMessage(5, "请在背包中的其它栏目预留至少1个空位以领取通关奖励");
            return false;
        }
    } else {
        pi.playerMessage(5, "离场前需消灭所有狼群");
        return false;
    }
}