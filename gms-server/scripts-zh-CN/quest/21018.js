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
        qm.sendNext("现在，你将接受一个测试，以确定你是否合格。你要做的就是挑战这个岛上最强大的怪物，#o0100134#s。大约#r50#k只就可以了，但是...");
    } else if (status == 1) {
        qm.sendAcceptDecline("“我们不能让你消灭所有#o0100134#s，因为它们数量不多。消灭5个怎么样？你是来训练的，不是来破坏生态系统的。”");
    } else if (status == 2) {
        if (mode == 0 && type == 15) {
            qm.sendNext("“哦，五只还不够吗？如果你觉得需要进一步训练，请随意多杀几只。如果你杀掉全部，我只能忍痛不看，因为他们是为了一个好的目的而被牺牲的……”");
            qm.dispose();
        } else {
            qm.forceStartQuest();
            qm.sendNext("#o0100134#s 可以在岛屿的更深处找到。继续向左走，直到到达#b#m140010200##k，并击败#r5 #o0100134#s#k.");
        }
    } else if (status == 3) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
        qm.dispose();
    }
}