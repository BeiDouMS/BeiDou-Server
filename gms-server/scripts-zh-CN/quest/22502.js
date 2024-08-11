var status = -1;

function start(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
		qm.sendAcceptDecline("蜥蜴不会喜欢 #b#t4032452##k, 像牛一样? 附近有很多 #b干草堆#k , 所以试着喂它.");
	} else if (status == 1) {
		if (mode == 0) {
			qm.sendNext("嗯，除非你努力，否则你永远不会知道。不管你信不信，那只蜥蜴大到可以爬上枫树。它可能吃干草.");
		} else {
			qm.forceStartQuest();
			qm.sendImage("UI/tutorial/evan/12/0");
		}
                qm.dispose();
        }
}
