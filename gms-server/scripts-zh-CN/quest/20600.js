/*
 * Cygnus Skill - Training Never ends
 */

var status = -1;

function start(mode, type, selection) {
    status++;

    if (status == 0) {
    	qm.sendAcceptDecline("#h0#. 你达到100级后一直在放松训练吗？我们都知道你有多强大，但训练还没有完成。看看这些骑士指挥官。他们日夜训练，为可能遇到黑魔法师做准备。");
    } else if (status == 1) {
        if (mode == 1) {
            qm.forceStartQuest();
        }
        qm.dispose();
    }
}

// function end(mode, type, selection) {
//     qm.dispose();
// }