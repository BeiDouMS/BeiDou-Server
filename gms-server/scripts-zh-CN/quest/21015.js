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
            qm.sendNext("What are you so hesitant about? You're a hero! You gotta strike while the iron is hot! Come on, let's do this!");
            qm.dispose();
        } else {
            qm.forceStartQuest();
            qm.sendNext("The population of Rien may be mostly Penguins, but even this island has monsters. You'll find #o0100131#s if you go to #b#m140020000##k, located on the right side of the town. Please defeat #r10 of those #o0100131#s#k. I'm sure you'll have no trouble defeating the #o0100131#s that even the slowest penguins here can defeat.", 1);
        }
    } else if (status == 4) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
        qm.dispose();
    }
}