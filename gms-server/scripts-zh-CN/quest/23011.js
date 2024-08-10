var status = -1;

function end(mode, type, selection) {
	if (mode == 0 && type == 0) {
		status--;
	} else if (mode == -1) {
		qm.dispose();
		return;
	} else {
		status++;
	}
	if (status == 0) {
		qm.sendYesNo("所以你最终决定成为一名战斗法师，嗯？好吧，你还是可以改变主意的.停止我们的对话，放弃这个任务，和另一个教师谈谈.所以，你确定你想成为一名战斗法师？我不想教你除非你百分之百确定...");
	} else if (status == 1) {
		if (mode == 0) {
			qm.sendNext("做决定前要仔细考虑.");
		} else {
			if (!qm.isQuestCompleted(23011)) {
				qm.gainItem(1382100);
				qm.gainItem(1142242);
				qm.forceCompleteQuest();
				qm.changeJobById(3200);
				qm.showItemGain(1382100, 1142242);
			}
			qm.sendNext("好吧，好吧。欢迎加入反抗军，孩子。从现在起，你将扮演一个战斗法师的角色，一个凶猛的魔术师随时准备带领你的队伍进入战斗.");
		}
	} else if (status == 2) {
		qm.sendNextPrev("但别到处宣扬你是个战斗法师，嗯？不需要引诱黑法师来追你。从现在起，我将是你的老师.如果有人问，你就说是一个普通学生来拜访我，而不是作为抵抗军的一员.我时不时地给你上特别的课.你最好不要在课堂上睡着，听到了吗？");
	} else if (status == 3) {
		qm.dispose();
	}
}