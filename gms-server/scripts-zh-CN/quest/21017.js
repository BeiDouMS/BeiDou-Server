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
            qm.sendNext("现在，身体筋骨差不多舒展开了吧？这种时候还要进一步加强训练强度才能保证锻炼出过硬的基础体力。来吧，继续基础体力的锻炼吧。", 8);
            break;
        case 1:
            qm.sendNextPrev("这次去#b#m140020200##k消灭#r#o0100133##k试试看。大概消灭#r20只#k就行，将会对你的体力增长大有帮助。快去快去……咦？你有什么要说的吗？", 8);
            break;
        case 2:
            qm.sendNextPrev("……为什么要消灭的怪兽数量越来越多了？", 2);
            break;
        case 3:
            qm.sendNextPrev("是要越来越多。难道说20只还不够吗？要不消灭100只试试？哦，这还不够，索性不如像林中之城的谁那样，一口气消灭999只怪兽试试……", 8);
            break;
        case 4:
            qm.sendNextPrev("不，不用了，这样就够了。", 2);
            break;
        case 5:
            qm.sendAcceptDecline("哎呦哎呦，用不着这么谦虚。我充★分★理解英雄大人渴望赶紧变得厉害起来的心情。真不愧是英雄大人……");
            break;
        case 6:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("#b（怀着沉重的心情，没有说出拒绝的话。现在临阵脱逃也不可能……镇定一下情绪，再说两句。）", 2);
                qm.dispose();
                break;
            }
            // ACCEPCT
            if (!qm.isQuestStarted(21017)) {
                qm.forceStartQuest();
            }
            qm.sendNext("#b（再这么说下去，搞不好真得让我去消灭999只怪兽了，赶紧接受任务得了。）", 2);
            break;
        case 7:
            qm.sendNextPrev("那就拜托你消灭20只#o0100133#。", 8);
            break;
        case 8:
            qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}