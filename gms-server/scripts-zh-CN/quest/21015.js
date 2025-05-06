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
	qm.sendNext("那么，说明就到此结束，现在进入下一阶段。下一个阶段是什么呢？刚刚才跟您说过不是吗？要修炼变强盗你可以除掉黑魔法师的程度。");
    } else if (status == 1) {
	qm.sendNextPrev("您虽然过去是英雄，可是那已经是几百年前的事了。就算不是黑魔法师的诅咒，待在冰雪当中这么长的时间，身体一定会变得很僵硬吧！首先先松开僵硬的身体。您觉得如何？");
    } else if (status == 2) {
	qm.sendYesNo("体力就是战力！英雄的基础就是体力！ ... 您没听过这些话吗？当然要先做#b基础体力锻炼#k ... 啊！ 您丧失记忆所以什么都忘了。不知道也没关系。那么现在就进入基础体力锻炼吧！");
    } else if (status == 3) {
        if (mode == 0) {
            qm.sendNext("你还犹豫什么？你可是英雄啊！机不可失，时不再来！来吧，我们上！");
            qm.dispose();
        } else {
            qm.forceStartQuest();
            qm.sendNext("虽然里恩岛的居民大多是企鹅，但这座岛上也有怪物。如果你前往位于城镇右侧的#b#m140020000##k，你会发现#o0100131#s。请击败#r10只#o0100131#s#k。我相信这对你来说轻而易举，毕竟就连这里最菜的企鹅都能打败它们。", 1);
        }
    } else if (status == 4) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
        qm.dispose();
    }
}