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
		qm.sendNext("最近农场上的#o1210100#有点奇怪。经常无缘无故地发脾气，做出一些烦人的事情。我对此很担心，所以今天很早就出来了。果然有一只#o1210100#钻过了篱笆，逃到外面去了.");
	} else if (status == 1) {
		qm.sendAcceptDecline("在找到#o1210100#之前，必须先把坏了的篱笆修好。还好坏得不是太严重，只要有几个#t4032498#应该就能修好了。小不点，要是你能帮我搜集#b3个#t4032498##k就好了……");
	} else if (status == 2) {
		if (mode == 0) {//decline
			qm.sendNext("嗯……#p1013101#的话，应该就能帮我了.");
			qm.dispose();
		} else {
			qm.forceStartQuest();
			qm.sendNext("哦，真是谢谢你。#b#t4032498##k可以从周围的#r#o0130100##k身上搜集到。它们虽然不是很强，但不小心的话，可能会遇到危险。你一定要好好使用技能道具.");
		}
	} else if (status == 3) {
		qm.sendImage("UI/tutorial/evan/6/0");
		qm.dispose();
	}
}

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
		qm.sendNext("哦，#t4032498#搜集到了吗？真了不起。我应该给你什么作为奖赏呢……对了，我有那个东西。 \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i3010097# 1 #t3010097# \r\n#i2022621# 15 #t2022621#s \r\n#i2022622# 15 #t2022622#s \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 210 exp");
	} else if (status == 1) {
		if (!qm.isQuestCompleted(22004)) {
			qm.gainItem(3010097, true);
			qm.forceCompleteQuest();
			qm.gainExp(210);
		}
		qm.sendNextPrev("好了，我用修理篱笆剩下的木板做了一把椅子。虽然不太好看，但却很结实。就给你用吧.");
	} else if (status == 2) {
		qm.sendImage("UI/tutorial/evan/7/0");
		qm.dispose();
	}
}