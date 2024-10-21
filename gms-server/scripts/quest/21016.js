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
            qm.sendAcceptDecline("Shall we continue with your Basic Training? Before accepting, please make sure you have properly equipped your sword and your skills and potions are readily accessible.");
            break;
        case 1:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("Are you not ready to hunt the #o0100132#s yet? Always proceed if and only if you are fully ready. There's nothing worse than engaging in battles without sufficient preparation.");
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