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
        qm.sendAcceptDecline("Shall we continue with your Basic Training? Before accepting, please make sure you have properly equipped your sword and your skills and potions are readily accessible.");
    } else if (status == 1) {
        if (mode == 0) {
            qm.sendOk("Are you not ready to hunt the #o0100132#s yet? Always proceed if and only if you are fully ready. There's nothing worse than engaging in battles without sufficient preparation.");
        } else {
            qm.forceStartQuest();
            qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow3");
        }
        qm.dispose();
    } else if (status == 2) {

    }
}