/* @author RonanLana */

function enter(pi) {
    if (!pi.haveItem(4000381)) {
        pi.playerMessage(5, "你尚未持有白色精华。");
        return false;
    } else {
        var em = pi.getEventManager("LatanicaBattle");

        if (pi.getParty() == null) {
            pi.playerMessage(5, "你当前未加入队伍，请创建队伍后再挑战BOSS。");
            return false;
        } else if (!pi.isLeader()) {
            pi.playerMessage(5, "你的队伍队长必须进入传送门才能开始战斗。");
            return false;
        } else {
            var eli = em.getEligibleParty(pi.getParty());
            if (eli.size() > 0) {
                if (!em.startInstance(pi.getParty(), pi.getPlayer().getMap(), 1)) {
                    pi.playerMessage(5, "你的队伍队长必须进入传送门才能开始战斗。");
                    return false;
                }
            } else {  //this should never appear
                pi.playerMessage(5, "你暂时无法开始这场战斗，可能是因为队伍人数不符合要求、部分队员未满足挑战条件或不在当前地图。若组队遇到困难，请尝试使用队伍搜索功能。");
                return false;
            }

            pi.playPortalSound();
            return true;
        }
    }
}