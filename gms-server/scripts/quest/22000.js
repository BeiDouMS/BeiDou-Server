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
		qm.sendNext("醒了吗，小不点?");
	} else if (status == 1) {
		qm.sendNextPrev("#b嗯……妈妈也醒了吗？", 2);
	} else if (status == 2) {
		qm.sendNextPrev("嗯……但是你怎么好象没睡着呢？昨天晚上打了一夜的雷。所以才没睡好吗?");
	} else if (status == 3) {
		qm.sendNextPrev("#b不，不是因为那个，是因为做了一个奇怪的梦.", 2);
	} else if (status == 4) {
		qm.sendNextPrev("奇怪的梦，梦见什么呢?");
	} else if (status == 5) {
		qm.sendNextPrev("#b嗯……", 2);
	} else if (status == 6) {
		qm.sendNextPrev("#b(说明梦见在迷雾中遇到龙的事情.)", 2);
	} else if (status == 7) {
		qm.sendAcceptDecline("呵呵呵呵，龙？怎么会梦到这个呢？没被吃掉，真是太好了。你做了个有趣的梦，去告诉 #p1013101# 吧。他一定会很高兴的.");
	} else if (status == 8) {
		if (mode == 0) {//decline
			qm.sendNext("嗯？不想告诉 #p1013101#吗? 真是，兄弟之间应该好好相处嘛.");//guess
			qm.dispose();//get the message xd
		} else {//accept
			qm.forceStartQuest();
			qm.sendNext("#b#p1013101##k 去 #b#m100030102##k 给猎犬喂饭了。从家里出去就能见到他了.");
		}
	} else if (status == 9) {
		qm.sendImage("UI/tutorial/evan/1/0");
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
		qm.sendNext("哦，起来啦，小不点？大清早的，怎么这么大的黑眼圈啊？晚上没睡好吗？什么？做了奇怪的梦？什么梦啊？嗯？梦见遇到了龙？");
	} else if (status == 1) {
		qm.sendNextPrev("哈哈哈哈～龙？不得了。居然梦到了龙！但是梦里有狗吗？哈哈哈哈～ \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 20 exp");
	} else if (status == 2) {
		qm.forceCompleteQuest();
		qm.gainExp(20);
		qm.sendImage("UI/tutorial/evan/2/0");
		qm.dispose();
	}	
}
	