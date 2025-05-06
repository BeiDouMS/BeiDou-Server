function act() {
    var eim = rm.getEventInstance();
    if (eim != null) {
        eim.dropMessage(6, "一件武器已归还至宗师圣器！");
        eim.setIntProperty("glpq5", eim.getIntProperty("glpq5") + 1);
        if (eim.getIntProperty("glpq5") == 5) { //all 5 done
            eim.dropMessage(6, "安特利昂已为你开启下一传送门！前进吧！");

            eim.showClearEffect(610030500, "5pt", 2);
            eim.giveEventPlayersStageReward(5);
        }
    }
}