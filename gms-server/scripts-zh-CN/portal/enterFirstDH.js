var mapp = -1;
var map = 0;

function enter(pi) {
    if (pi.getQuestStatus(20701) == 1) {
        map = 913000000;
    } else if (pi.getQuestStatus(20702) == 1) {
        map = 913000100;
    } else if (pi.getQuestStatus(20703) == 1) {
        map = 913000200;
    }
    if (map > 0) {
        if (pi.getPlayerCount(map) == 0) {
            var mapp = pi.getMap(map);
            mapp.resetPQ();

            pi.playPortalSound();
            pi.warp(map, 0);
            return true;
        } else {
            pi.playerMessage(5, "已经有其他人比你先使用了演武场，请稍后再试！");
            return false;
        }
    } else {
        pi.playerMessage(5, "只有参加Kiku的适应训练，才能进入1号演武场。");
        return false;
    }
}