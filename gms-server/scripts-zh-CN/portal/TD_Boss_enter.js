/* @author RonanLana */

function enter(pi) {
    var stage = ((Math.floor(pi.getMapId() / 100)) % 10) - 1;
    var em = pi.getEventManager("TD_Battle" + stage);
    if (em == null) {
        pi.playerMessage(5, "TD战场第" + stage + "阶段发生意外错误，当前不可用。");
        return false;
    }

    if (pi.getParty() == null) {
        pi.playerMessage(5, "您当前未加入队伍，请创建队伍来挑战首领。");
        return false;
    } else if (!pi.isLeader()) {
        pi.playerMessage(5, "必须由队长进入传送门才能开始战斗。");
        return false;
    } else {
        var eli = em.getEligibleParty(pi.getParty());
        if (eli.size() > 0) {
            if (!em.startInstance(pi.getParty(), pi.getPlayer().getMap(), 1)) {
                pi.playerMessage(5, "首领战斗已经开始，您暂时无法进入该区域。");
                return false;
            }
        } else {
            pi.playerMessage(5, "您的队伍至少需要2名成员才能挑战首领。");
            return false;
        }

        pi.playPortalSound();
        return true;
    }
}
