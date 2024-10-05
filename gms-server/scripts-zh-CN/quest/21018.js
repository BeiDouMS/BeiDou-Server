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
        qm.sendNext("来，让我测试一下你至今为止的基础体力训练成果。测试方法很简单。这座岛上有一种最强悍凶猛的怪兽，叫#o0100134#，你只要击退它就可以！要是能击退#r50只#k就最好了...");
    } else if (status == 1) {
        qm.sendAcceptDecline("不过#o0100134#的数量本来就不多，杀掉那么多恐怕不利生态平衡的保持，你消灭5只就差不多了。你看，这训练与自然环境之间是多么地和谐啊！真是完美啊...");
    } else if (status == 2) {
        if (mode == 0) {
            qm.sendOk("哎哟，难道说你觉得5只太少了？如果你愿意加强锻炼，多消灭一些也没关系的。既然是英雄大人的心愿，我就睁一只眼闭一只眼吧，虽然很可惜那些怪兽...");
        } else {
            qm.forceStartQuest();
            qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
        }
        qm.dispose();
    }
}