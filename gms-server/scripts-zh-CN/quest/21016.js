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
        qm.sendAcceptDecline("开始基础体力锻炼吧？准备好了？再确认一下剑是否装备好了？技能和药水是否已经托到了快捷栏中？");
    } else if (status == 1) {
        if (mode == 0) {
            qm.sendNext("你还没有准备好去猎捕 #b#o0100132##k 吗？只有在你完全准备好的情况下才可以进行。在没有充分准备的情况下参与战斗会糟糕的。");
            qm.dispose();
        } else {
            qm.forceStartQuest();
            qm.sendNext("很好。下面要去打猎的#r#o0100132#s#k，是比#o0100131#s更厉害一些的怪兽。去#b#m140020100##k抓#r15只#k，这将有助于你的体力提高。体力就是冒险动力的来源！快出去吧！", 1);
        }
    } else if (status == 2) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
        qm.dispose();
    }
}