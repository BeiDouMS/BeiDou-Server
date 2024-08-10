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
		qm.sendAcceptDecline("很奇怪。小鸡们表现得很滑稽。他们过去孵化的方式更多 #t4032451#s. 你认为狐狸和它有关系吗？如果是的话，我们最好快点做点什么.");
	} else if (status == 1) {
		if (mode == 0) {//decline
			qm.sendNext("我滴个妈呀... 你害怕 #o9300385#es? 别告诉任何人你和我有关系。太丢人了.");
			qm.dispose();
		} else {
			qm.forceStartQuest();
			qm.sendNext("真的吗? 让我们去打败那些狐狸. 继续前进，先击败 #r10 #o9300385#es#k 在 #b#m100030103##k 里. 我会跟着你，照顾好你留下的东西。现在，快去 #m100030103#!");
		}
	} else if (status == 2) {
		qm.sendImage("UI/tutorial/evan/10/0");
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
		qm.sendNext("你打败了 #o9300385#es?");
	} else if (status == 1) {
		qm.sendNextPrev("#b打败狐狸后发生了什么?", 2);
	} else if (status == 2) {
		qm.sendNextPrev("噢，那个? 哈哈哈. 我确实追过他们，但我想确保他们追不上你. 我可不想让你被 #o9300385# 吃掉. 所以我就绕过它们.");
	} else if (status == 3) {
		qm.sendNextPrev("#b你确定你不是因为害怕狐狸才躲起来的?", 2);
	} else if (status == 4) {
		qm.sendNextPrev("什么？不可能！我什么都不怕!");
	} else if (status == 5) {
		qm.sendNextPrev("#b小心啊! #o9300385# 就在你身后!", 2);
	} else if (status == 6) {
		qm.sendNextPrev("再见了! 母亲!");	
	} else if (status == 7) {
		qm.sendNextPrev("#b...", 2);	
	} else if (status == 8) {
		qm.sendNextPrev("...");	
	} else if (status == 9) {
		qm.sendNextPrev("你这个小鬼！我是你哥哥。别惹我！你知道，你弟弟的心脏很虚弱。别那样吓我!");	
	} else if (status == 10) {
		qm.sendNextPrev("#b(这就是为什么我不想喊你哥哥...)", 2);
	} else if (status == 11) {
		qm.sendNextPrev("哼！不管怎样，我很高兴你能打败 #o9300385#es. 作为奖励，我会给你一个冒险家很久以前给我的东西。给你. \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i1372043# 1 #t1372043# \r\n#i2022621# 25 #t2022621# \r\n#i2022622# 25 #t2022622#s \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 910 exp");
	} else if (status == 12) {
		if (!qm.isQuestCompleted(22008)) {
			qm.gainItem(1372043, true);
			qm.gainItem(2022621, 25, true);
			qm.gainItem(2022622, 25, true);
			qm.forceCompleteQuest();
			qm.gainExp(910);
		}
		qm.sendNextPrev("#b这是魔术师使用的武器。这是一根魔杖，你可能并不需要它，但如果你随身携带它，它会让你看起来很重要。哈哈哈哈.");
	} else if (status == 13) {
		qm.sendPrev("不管怎么说，狐狸数量增加了，对吧？有多奇怪？为什么它们一天比一天多？我们真的应该调查清楚.");
	} else if (status == 14) {
                qm.dispose();
        }
}