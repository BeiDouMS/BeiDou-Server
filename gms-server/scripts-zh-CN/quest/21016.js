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
            qm.sendAcceptDecline("开始基础体力锻炼吧？准备好了？再确认一下剑是否装备好了？技能和药水是否已经拖到了快捷栏中？");
            break;
        case 1:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("还没做好打猎#o0100132#的准备吗？做好充分的准备再出发比较好。可别到时候因为准备不足而挂了，就划不来了。");
            } else { // ACCEPT
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