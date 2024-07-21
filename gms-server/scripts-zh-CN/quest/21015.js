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
        qm.sendNext("好了，说明到这里就告一段落，我们要进入下一阶段了。下一阶段是什么？刚才我已经说过了。就是不断的磨练自己直到你拥有足以战胜黑魔法师的实力。");
    } else if (status == 1) {
        qm.sendNextPrev("虽然在几年前你确实是英雄，但这毕竟是很久以前的事情了。就算没有黑魔法师的诅咒，在冰块里封冻了那么久，身体筋骨什么的也没那么灵活了吧？首先要做些准备活动。想知道是怎么样的准备活动？");
    } else if (status == 2) {
        qm.sendAcceptDecline("身体是革命的本钱。英雄也要从基本体力开始训练！……那句话你也知道吧？当然要从#b 基本体力锻炼#k开始练起……啊，你可能不记的了。不过也没关系。尝试一下你就明白了。现在就开始基础体力锻炼吧？");
    } else if (status == 3) {
        if (mode == 0) {
            qm.sendNext("你还犹豫什么？你可是英雄啊！要趁热打铁！来吧，我们一起上！");
            qm.dispose();
        } else {
            qm.forceStartQuest();
            //qm.sendNext("The population of Rien may be mostly Penguins, but even this island has monsters. You'll find #o0100131#s if you go to #b#m140020000##k, located on the right side of the town. Please defeat #r10 of those #o0100131#s#k. I'm sure you'll have no trouble defeating the #o0100131#s that even the slowest penguins here can defeat.", 1);
            qm.sendNextS("在这个几乎全是企鹅的岛上，也有几只怪兽。去村子右边的#b#m140020000##k，就能看到许多#o0100131#。请消灭#r10只#o0100131##k。我们这些笨拙的企鹅用喙都能抓到的#o0100131#，你总不能还抓不到吧？", 1);
        }
    } else if (status == 4) {
        qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
        qm.dispose();
    }
}