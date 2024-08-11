/*
	NPC Name: 		Kisan
	Description: 		Quest - Cygnus tutorial helper
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        if (status == 2) {
	    qm.sendNext("你不想？它甚至不是那么难，你会得到特殊的设备作为奖励！好吧，好好想想如果你改变主意了就告诉我。");
            qm.dispose();
            return;
        }
        status--;
    }
    if (status == 0) {
    	qm.sendNext("有许多方法打猎，但最基本的方法是用你的 #b普通攻击#k. 所有你需要的是在你的手的武器，因为它只是摆动你的武器在怪物一件简单的事情。");
    } else if (status == 1) {
    	qm.sendNextPrev("请按 #bCtrl#k 使用你的普通攻击. 通常下 Ctrl 位于 #b键盘的左下角#k, 但你并不需要我告诉你对不对？ 发现Ctrl 并尝试攻击！");
    } else if (status == 2) {
    	qm.sendAcceptDecline("现在，你已经尝试过了，我们一定要测试它。在这方面，你可以找到最薄弱 #r#o100120##k 在耶雷弗, 这是您的最佳选择。尝试狩猎 #r1只#k. 当你回来我给你的奖励。.");
    } else if (status == 3) {
        qm.forceStartQuest();
        qm.guideHint(4);
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
    	qm.sendNext("啊，看来你成功地猎到了 #o100120#. P很简单，对吧？经常攻击可能很容易使用，但它们相当弱。不过，别担心。#p1102006# 会教会你如何使用更强大的技能。等等，你走之前让我给你个应得的奖励。");
    } else if (status == 1) {
    	qm.sendPrev("这个装备是给贵族用的。比你现在穿的酷多了，不是吗？跟着你左边的箭去见我弟弟 #b#p1102006##k. 你走之前换一套新的贵族服装怎么样？ \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i1002869# #t1002869# - 1 \r\n#i1052177# #t1052177# - 1 \r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 30 exp");
    } else if (status == 2) {
        qm.gainItem(1002869, 1);
        qm.gainItem(1052177, 1);
        qm.forceCompleteQuest();
        qm.gainExp(30);
        qm.guideHint(6);
        qm.dispose();
    }
}