var status = -1;

function start(mode, type, selection) {
    if (mode == -1) { // END CHAT
        qm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            qm.sendNext("你似乎在回想什么。这个长矛果然认出了你。那么你肯定就是#b使用长矛的英雄，战神#k了。你想起什么其他的了吗？有关长矛的技能之类……", 8);
            break
        case 1:
            qm.sendNextPrev("#b（说技能倒是想起来了几个。）#k", 2);
            break;
        case 2:
            qm.sendNextPrev("虽然数量不多，不过也已经很不容易了。现在让我们集中精力来恢复过去的技能吧。虽然你失忆了，但毕竟是以前曾经烂熟于心的东西，要恢复起来应该很快。", 8);
            break;
        case 3:
            qm.sendNextPrev("怎么恢复过去的技能？", 2);
            break;
        case 4:
            qm.sendAcceptDecline("那个......办法只有一个。就是修炼!修炼!修炼!只有不停地修炼才能找回曾经忘却的身体感觉!");
            break;
        case 5:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("什么？你不愿意？打算一个人修炼？有人指导的效果可比自己慢慢摸索的效果好很多哦。再说，你也该学习学习如何与人打交道了。");
                qm.dispose();
                break;
            }
            // ACCEPT
            qm.sendNext("武器要是能使得更熟练就好了。送你一支#b长矛#k，希望你在修炼的时候能够进步得更快。带着这支长矛……");
            if (!qm.isQuestStarted(21700) && !qm.isQuestCompleted(21700)) {
                qm.gainItem(1442000, 1);
                qm.forceStartQuest();
            }
            break;
        case 6:
            qm.sendPrev("如果你离开这里，从村子的左边出去，在里恩修炼场入口可以见到长期研究英雄技能的#b#p1202006##k，让他来帮助你。");
            break;
        default:
            qm.dispose();
            break;
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
