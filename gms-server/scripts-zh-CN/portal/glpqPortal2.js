function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim != null) {
        pi.playPortalSound();
        pi.warp(610030300, 0);

        if (eim.getIntProperty("glpq3") < 5 || eim.getIntProperty("glpq3_p") < 5) {
            if (eim.getIntProperty("glpq3_p") == 5) {
                pi.mapMessage(6, "尚未激活所有封印石！请确保全部激活后再进入下一阶段。");
            } else {
                eim.setIntProperty("glpq3_p", eim.getIntProperty("glpq3_p") + 1);

                if (eim.getIntProperty("glpq3") == 5 && eim.getIntProperty("glpq3_p") == 5) {
                    pi.mapMessage(6, "【圣石安特利昂】已开启传送门！请继续前进！");

                    eim.showClearEffect(610030300, "3pt", 2);
                    eim.giveEventPlayersStageReward(3);
                } else {
                    pi.mapMessage(6, "一位冒险者已通过！还需" + (5 - eim.getIntProperty("glpq3_p")) + "人。");
                }
            }
        } else {
            pi.getPlayer().dropMessage(6, "底部传送门已经开启！请从该处继续前进！");
        }
        return true;
    }
    return false;
}
