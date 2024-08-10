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
		qm.sendNext("呃。这样还不行。我还需要别的东西。没有植物。没有肉。什么，你不知道？但你是主人，你也比我大。你一定知道什么对我有好处!");
	} else if (status == 1) {
		qm.sendNextPrev("#b但...我不觉得。这和年龄无关...", 2);
	} else if (status == 2) {
		qm.sendAcceptDecline("既然你年纪大了，你在这个世界上也必须更有经验。你知道的比我多是有道理的。哦，很好。我会问比你大的人，师父!");
	} else if (status == 3) {
		if (mode == 0) {
			qm.sendNext("我自己想办法找到答案是没有用的. 我最好去找 #b比主人更老更聪明的人#k!");
		} else {
			qm.forceStartQuest();
			qm.sendNext("#b#b(你已经问过父亲一次了，貌似你没有更好的办法。是时候再去找他一次了!)");
		}
	} else if (status == 4) {
                qm.dispose();
        }
}