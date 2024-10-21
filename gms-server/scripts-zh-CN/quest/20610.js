/*
 * Cygnus Skill -
 */

var status = -1;

function start(mode, type, selection) {
    status++;

    if (status == 0) {
        qm.sendAcceptDecline("你在这段时间学了很多技能吗？应该不少吧...现在你想学习#b新技能#k吗？");
    } else if (status == 1) {
        if (mode == 0) {
            qm.sendOk("你不是没有野心，而是没有上进心。这可不太好。");
        } else {
            qm.forceStartQuest();
            qm.dispose();
        }
    } else if (status == 2) {
        qm.dispose();
    }
}
