/*
 * Cygnus Skill -
 */

var status = -1;

function start(mode, type, selection) {
    status++;

    if (status == 0) {
    	qm.sendAcceptDecline("你一直在掌握你的技能吗？我相信你已经掌握了所有的技能，这意味着。。。是你学习新技能的时候了，对吧？");
    } else if (status == 1) {
	if (mode == 0) {
	    qm.sendOk("好吧，你现在所做的并不能让你看起来像个谦虚的人。你这样做看起来很自满，这不是好事.");
	} else {
	    qm.forceStartQuest();
            qm.dispose();
        }
    } else if (status == 2) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.dispose();
}