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
            qm.sendAcceptDecline("糟糕！有个孩子被留在森林里了！我们不能丢下孩子就这么逃走！战神……请你救救孩子吧！你伤得这么重，还要你去战斗，我们心里也很过意不去……但只有你能够救那个孩子啊！");
            break;
        case 1:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("不行，战神……要是抛下孩子我们自己逃掉……就算能活下去也没什么意义！我知道这个要求对你而言很勉强，不过还是请你再考虑考虑。");
                qm.dispose();
                break;
            }
            if (!qm.isQuestStarted(21000) && !qm.isQuestCompleted(21000)) {
                qm.forceStartQuest();
            }
            qm.sendNext("#b孩子可能在森林的深处#k！必须在黑魔法师找到我们之前，启动方舟，所以必须尽快救出孩子才行！");
            break;
        case 2:
            qm.sendNextPrev("关键是不要慌张，战神。如果你要查看任务状态，按#bQ键#k就能在任务栏中查看。");
            break;
        case 3:
            qm.sendNextPrev("拜托了，战神！救救孩子吧！我们不能再有人因为黑魔法师而牺牲了！");
            break;
        case 4:
            qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}