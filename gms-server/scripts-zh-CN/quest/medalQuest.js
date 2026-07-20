/**
 *
 * @author Arnah, Ronan
 */

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.forceCompleteQuest();

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> 尚未编写专用脚本。");
    qm.earnTitle("<" + medalname + "> 奖励已获得。");
    qm.dispose();
}

function end(mode, type, selection) {
    qm.forceCompleteQuest();

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> 尚未编写专用脚本。");
    qm.earnTitle("<" + medalname + "> 奖励已获得。");
    qm.dispose();
}
