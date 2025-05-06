function enter(pi) {
    var react = pi.getMap().getReactorByName("mob0");

    if (react.getState() < 1) {
        react.forceHitReactor(1);

        var eim = pi.getEventInstance();
        eim.setIntProperty("glpq1", 1);

        pi.getEventInstance().dropMessage(5, "传送装置开始散发出奇异能量，原先被封锁的隐藏路径现在开启了。");
        pi.playPortalSound();
        pi.warp(610030100, 0);

        pi.getEventInstance().showClearEffect();
        eim.giveEventPlayersStageReward(1);
        return true;
    }

    pi.getEventInstance().dropMessage(5, "由于上次传送的影响，传送装置出现故障。请寻找其他路径通过。");
    return false;
}