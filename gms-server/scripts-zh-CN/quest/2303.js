/* ===========================================================
        @author Resonance
	NPC Name: 		Maple Administrator
	Description: 	Quest -  Kingdom of Mushroom in Danger
=============================================================
Version 1.0 - Script Done.(17/7/2010)
Version 2.0 - Script Reworked by Ronan - (16/11/2018)
=============================================================
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            if (status != 3) {
                qm.sendOk("我有个请求，你愿意听我说说吗？");
                qm.dispose();
            } else {
                if (qm.canHold(4032375, 1)) {
                    qm.sendNext("你是谁？看你好像是冒险岛世界的冒险家。我们王国目前正面临巨大的危机，需要一位勇士来拯救我们。如果你没什么事的话，希望你能帮助我们。");
                } else {
                    qm.sendOk("请在其他栏中留至少一个位置。");
                    qm.dispose();
                }
            }

            status++;
        } else {
            if (mode == 1) {
                status++;
            } else {
                status--;
            }

            if (status == 0) {
                qm.sendAcceptDecline("现在你的强大了许多，我有一件事情想找你帮忙，你是否愿意听听？");
            } else if (status == 1) {
                qm.sendNext("故事发生在蘑菇王国，具体的事情我也不太清楚。但是好像很紧急。");
            } else if (status == 2) {
                qm.sendNext("我不知道事情的细节，所以想找你帮帮忙，你可能会需要更多的时间帮助蘑菇王国，我送你一封信，请你把它交给#b警卫队长#k。\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v4032375# #t4032375#");
            } else if (status == 3) {
                qm.sendYesNo("如果你现在想去蘑菇城堡的话，我可以送你去。你确定要去吗？");
            } else if (status == 4) {
                if (qm.canHold(4032375, 1)) {
                    if (!qm.haveItem(4032375, 1)) {
                        qm.gainItem(4032375, 1);
                    }

                    qm.warp(106020000, 0);
                    qm.forceStartQuest();
                } else {
                    qm.sendOk("请在其他栏中留至少一个位置。");
                }

                qm.dispose();

            } else if (status == 5) {
                if (!qm.haveItem(4032375, 1)) {
                    qm.gainItem(4032375, 1);
                }

                qm.forceStartQuest();
                qm.dispose();

            }
        }
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
            if (!qm.haveItem(4032375, 1)) {
                qm.sendNext("你想做什么？");
                qm.dispose();
                return;
            }

            qm.sendNext("嗯？那是#b转职官的推荐书#k吗？你是来拯救我们蘑菇王国的人吗？");
        } else if (status == 1) {
            qm.sendNextPrev("好吧，既然你有转职教官的推荐信，我想你是一个很棒的人，很抱歉我没有自我介绍，我是包围蘑菇城堡的卫兵，正如你所看到的，这里是我们暂时的藏身之地，我们的情况很糟糕，尽管如此，欢迎你来到蘑菇王国！");
        } else if (status == 2) {
            qm.gainItem(4032375, -1);
            qm.gainExp(6000);
            qm.forceCompleteQuest();
            qm.forceStartQuest(2312);
            qm.dispose();
        }
    }
}
