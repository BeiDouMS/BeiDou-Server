function enter(pi) {
    var eim = pi.getEventInstance();

    if (eim.isEventCleared()) {
        if (pi.canHold(4001198, 1)) {
            pi.gainItem(4001198, 1);
            pi.playPortalSound();
            pi.warp(930000800);
            pi.playerMessage(5, "将石头人的毒株投入净化之泉");
            return true;
        }else{
            pi.playerMessage(5, "背包需空出1格来接收奖励。");
        }
       
    } else {
        pi.playerMessage(5, "请先消灭石头人");
        return false;
    }
}
