function act() {
    var eim = rm.getEventInstance();
    if (eim != null) {
        eim.dropMessage(6, "魔法师印记已激活！");
        eim.setIntProperty("glpq4", eim.getIntProperty("glpq4") + 1);
        if (eim.getIntProperty("glpq4") == 5) { //all 5 done
            eim.dropMessage(6, "安特利昂已为你开启下一传送门！前进吧！");

            eim.showClearEffect(610030400, "4pt", 2);
            eim.giveEventPlayersStageReward(4);
        }
    }
}