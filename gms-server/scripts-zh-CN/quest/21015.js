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
            qm.sendNext("好了，说明到这里就告一段落，我们要进入下一阶段了。下一阶段是什么？刚才我已经说过了。就是不断地磨练自己，直到你拥有足以战胜黑魔法师的实力。");
            break;
        case 1:
            qm.sendNextPrev("虽然在几百年前你确实是英雄，但这毕竟是很久以前的事情了。就算没有黑魔法师的诅咒，在冰块里封冻了那么久，身体筋骨什么的也没那么灵活了吧？首先要做些准备活动。想知道是怎么样的准备活动？");
            break;
        case 2:
            qm.sendAcceptDecline("体力是革命的本钱。英雄也要从基础体力开始训练！……那句话你也知道吧？当然要从#b基础体力锻炼#k开始练起……啊，你可能不记得了。不过也没关系。尝试一下你就明白了。现在就开始基础体力锻炼吧？");
            break;
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("别觉得自己原来是英雄就了不起了。冰冻三尺非一日之寒。要想变得足够厉害，就赶紧开始磨练自己吧。");
            } else { // ACCEPCT
                qm.forceStartQuest();
                qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
            }
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}