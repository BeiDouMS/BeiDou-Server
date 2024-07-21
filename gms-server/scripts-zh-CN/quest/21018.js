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
        qm.sendNext("来，让我测试一下，你至今为止的基础体力训练结果。测试方法很简单。这座岛上有一种最强悍凶猛的怪兽，叫#r#o0100134##k，你只要击退它就可以！要是能击退#r50#k只就最好了……");
    } else if (status == 1) {
        qm.sendAcceptDecline("不过#o0100134#的数量本来就不多，杀掉那么多恐怕不利生态平衡的保持，你消灭5只就差不多了。你看，这训练与自然环境之间是多么滴和谐啊！真是完美啊……");
    } else if (status == 2) {
        if (mode == 0 && type == 15) {
            qm.sendNext("哦，5只不够吗？如果你觉得需要更多的训练，请随意多击杀一些。如果你把它们全部消灭，尽管这会让我心痛，我也只能选择视而不见，因为它们将为英雄复苏而牺牲……");
            qm.dispose();
        } else {
            qm.forceStartQuest();
            qm.sendNext("#o0100134#在岛的较深处。村子左边的路一直走，就能看到#b#m140010200##k，请去那里消灭#r5只#o0100134#s#k。");
        }
    } else if (status == 3) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
        qm.dispose();
    }
}