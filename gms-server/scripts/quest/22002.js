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
		qm.sendNext("你喂过 #p1013102#了吗? 埃文，你现在应该吃早餐了. 今天的早餐是 #t2022620#. 我把它带来了。嘻嘻。如果你不同意我自己吃 #p1013102#.");
	} else if (status == 1) {
		qm.sendAcceptDecline("给，我给你弄了个 #b三明治#k, so #b你吃完后再跟妈妈说。她说她有话要跟你说.");
	} else if (status == 2) {
		if (mode == 0) {//decline
			qm.sendNext("哦，什么？你不吃早饭吗？早餐是一天中最重要的一餐！如果你改变主意再跟我说一次。如果你不吃，我就自己吃.");
			qm.dispose();
		} else {
			qm.gainItem(2022620, true);
			qm.forceStartQuest();
			qm.sendNext("#b(妈妈有话要说吗? 把你的 #t2022620# 吃了，然后回屋去.)#k");
		}
	} else if (status == 3) {
		qm.sendImage("UI/tutorial/evan/3/0");
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
		qm.sendNext("你吃早餐了吗，埃文？那么，你能帮我个忙吗?  \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i1003028# 1 #t1003028#  \r\n#i2022621# 5 #t2022621#s \r\n#i2022622# 5 #t2022622# \r\n#fUI/UIWindow.img/QuestIcon/8/0# 60 exp");
	} else if (status == 1) {
		qm.forceCompleteQuest();
                qm.gainItem(1003028, 1, true);
		qm.gainItem(2022621, 5, true);
		qm.gainItem(2022622, 5, true);
		qm.gainExp(60);
		qm.sendImage("UI/tutorial/evan/4/0");
		qm.dispose();
	}
}