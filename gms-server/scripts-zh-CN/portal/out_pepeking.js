function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        eim.stopEventTimer();
        eim.dispose();
    }

    var questProgress = pi.getQuestProgressInt(2330, 3300005) + pi.getQuestProgressInt(2330, 3300006) + pi.getQuestProgressInt(2330, 3300007); //3 Yetis
    if (questProgress == 3 && !pi.hasItem(4032388)) {
        if (pi.canHold(4032388)) {
            pi.getPlayer().message("你已经拿到了结婚礼堂的钥匙。企鹅国王肯定是把它丢掉了。");
            pi.gainItem(4032388, 1);

            pi.playPortalSound();
            pi.warp(106021400, 2);
            return true;
        } else {
            pi.getPlayer().message("请确保背包其它物品栏还有可用空间。");
            return false;
        }
    } else {
        pi.playPortalSound();
        pi.warp(106021400, 2);
        return true;
    }
}
