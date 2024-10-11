var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
	if (status == 4) {
	    qm.sendNext("不？你是说你可以自己训练吗？我只是想告诉你，如果你和教练一起训练，你会得到更好的成绩。你不能一个人生活在这个世界上。你必须学会和别人相处.");
	    qm.dispose();
	    return;
	}
	status--;
    }
    if (status == 0) {
    	qm.sendNext("你似乎在回想什么。这个长矛果然认出了你。那么你肯定就是#b使用长矛的英雄，战神#k了。你想起什么其他的了吗？有关长矛的技能之类......");
    } else if (status == 1) {
    	qm.sendNextPrev("#b（说技能倒是想起来了几个。）#k", 2);
    } else if (status == 2) {
    	qm.sendNextPrev("虽然数量不多，不过也已经很不容易了。现在让我们集中精力来恢复过去的技能吧。虽然你失忆了，但毕竟是以前曾经烂熟于心的东西，要恢复起来应该很快。");
    } else if (status == 3) {
    	qm.sendNextPrev('怎么恢复过去的技能？', 2);
    } else if (status == 4) {
    	qm.sendAcceptDecline("那个......办法只有一个。就是修炼！修炼！修炼！只有不停地修炼才能找回曾经忘却的身体感觉！");
    } else if (status == 5) {
		qm.sendNext("武器要是能使得更熟练就好了。送你一支#b长矛#k，希望你在修炼的时候能够进步得更快。带着这支长矛......");
		if (!qm.isQuestStarted(21700) && !qm.isQuestCompleted(21700)) {
			qm.gainItem(1442000,1);
			qm.forceStartQuest();
		}
    } else if (status == 6) {
            qm.sendPrev("如果你离开这里，从村子的左边出去，在里恩修炼场入口可以见到长期研究英雄技能的#b#p1202006##k，让他来帮助你。");
    } else if (status == 7) {
        qm.dispose();
    }
}


function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (qm.getPlayer().getRemainingAp() > 0) {
                qm.sendNext("请完成属性的分配。");
                qm.dispose();
                return;
            }
            qm.sendNext("看来你已经完成属性的分配了，让我们开始真正的修炼吧。");
        } else if (status == 1) {
            qm.gainExp(100);
            qm.forceCompleteQuest();
            qm.forceStartQuest(21701);
            qm.dispose();
        }
    }
}
