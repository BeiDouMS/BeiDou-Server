/**
 *
 * @author Arnah, Ronan
 */

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.forceCompleteQuest();

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> 没有找到.");
    qm.earnTitle("<" + medalname + "> 已领取奖励.");
    qm.dispose();
}

function end(mode, type, selection) {
    qm.forceCompleteQuest();

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> 没有找到.");
    qm.earnTitle("<" + medalname + "> 已领取奖励.");
    qm.dispose();
}