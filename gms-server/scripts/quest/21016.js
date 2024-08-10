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
        qm.sendAcceptDecline("“我们继续你的基础训练吗？在接受之前，请确保你已经正确装备好你的剑，并且你的技能和药水触手可及。”");
    } else if (status == 1) {
        if (mode == 0) {
            qm.sendNext("“你还没准备好猎取#o0100132#s吗？只有在完全准备好的情况下才能继续。未经充分准备就投入战斗是最糟糕的事情。”");
            qm.dispose();
        } else {
            qm.forceStartQuest();
            qm.sendNext("“好的。这次，让你打败#r#o0100132#s#k，它们比#o0100131#s稍微强大一些。前往#b#m140020100##k并打败#r15#k只它们。那应该能帮助你增强力量。好的！我们开始吧！”", 1);
        }
    } else if (status == 2) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
        qm.dispose();
    }
}