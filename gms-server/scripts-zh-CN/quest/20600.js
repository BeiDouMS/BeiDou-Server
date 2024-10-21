/*
 * Cygnus Skill - Training Never ends
 */

var status = -1;

function start(mode, type, selection) {
    status++;

    if (status == 0) {
        qm.sendAcceptDecline("#h0#，达到100级之后，你是不是就疏于修炼了呢？虽然你确实变强了，但修炼是不能停止的。你必须以骑士团长们作为榜样。他们为了对付黑魔法师，一刻都没有停止修炼。");
    } else if (status == 1) {
        if (mode == 1) {
            qm.forceStartQuest();
        }
        qm.dispose();
    }
}