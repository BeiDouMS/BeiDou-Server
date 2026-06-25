/*
    Author : kevintjuh93
    Map(s):         Aran Training Map 2
    Description:    Quest - Help Kid
    Quest ID :      21000
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            status -= 2;
        } else {
            qm.sendNext("不，战神……我们不能丢下那个孩子。我知道这个请求很过分，但请你再考虑一下。拜托了！");
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("糟了！森林里好像还有一个孩子！战神，真的很抱歉，你能去救那个孩子吗？我知道你受伤了，但我已经没有别人可以拜托了！");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.sendNext("#b那个孩子很可能迷失在森林深处！#k我们必须在黑魔法师找到我们之前逃走。请冲进森林，把孩子带回来！");
    } else if (status == 2) {
        qm.sendNextPrev("别慌，战神。如果你想查看任务状态，可以按 #bQ#k 打开任务窗口。");
    } else if (status == 3) {
        qm.sendNextPrev("拜托你了，战神！我不能再眼睁睁看着别人被黑魔法师夺走生命！");
    } else if (status == 4) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
        qm.dispose();
    }
}

function end(mode, type, selection) {
    qm.forceCompleteQuest();
    qm.dispose();
}
