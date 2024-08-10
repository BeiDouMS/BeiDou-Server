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
		qm.sendAcceptDecline("你的 #b父亲#k 今天早上他去农场时忘了带饭盒. 你能把这个饭盒带去给你父亲吗 #b#m100030300##k, 蜂蜜?");
	} else if (status == 1) {
		if (mode == 0 && type == 15) {//decline
			qm.sendNext("好孩子听妈妈的话。现在，埃文，做个好孩子，再和我说话.");
			qm.dispose();
		} else {
			if (!qm.isQuestStarted(22003)) {
				if (!qm.haveItem(4032448)) {
					qm.gainItem(4032448, true);
				}
				qm.forceStartQuest();
			}
			qm.sendNext("嘻嘻，我的埃文真是个好孩子! 出门后#b头上的血就要流出来了#k. 快去找你父亲. 你父亲肯定都饿坏了.");
		}
	} else if (status == 2) {
		qm.sendNextPrev("如果你不小心弄丢了饭盒，请回来找我。我再给他做份午饭.");
	} else if (status == 3) {
		qm.sendImage("UI/tutorial/evan/5/0");
		qm.dispose();
	}
}