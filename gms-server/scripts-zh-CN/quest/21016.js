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
        qm.sendAcceptDecline("开始基础体力锻炼吧？准备好了？再确认一下剑是否装备好了？技能和药水是否已经拖到了快捷栏中？");
    } else if (status == 1) {
        if (mode == 0) {
            qm.sendOk("还没做好打猎#o0100132#的准备吗？最好在出发前做好万全的准备。别因为准备不充分而中途挂掉。");
        } else {
            qm.forceStartQuest();
            qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
        }
        qm.dispose();
    }
}