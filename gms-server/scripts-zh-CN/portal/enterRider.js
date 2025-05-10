function enter(pi) {
    if (pi.isQuestStarted(21610) && pi.haveItem(4001193, 1) == 0) {
        var em = pi.getEventManager("Aran_2ndmount");
        if (em == null) {
            pi.message("抱歉，第二坐骑(斯卡德)任务目前不可用。");
            return false;
        } else {
            var em = pi.getEventManager("Aran_2ndmount");
            if (!em.startInstance(pi.getPlayer())) {
                pi.message("当前地图有其他玩家，请稍后再试。");
                return false;
            } else {
                pi.playPortalSound();
                return true;
            }
        }
    } else {
        pi.playerMessage(5, "只有参加第二骑狼任务的玩家才能进入此场地。");
        return false;
    }
}