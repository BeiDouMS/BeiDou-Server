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
            qm.sendNext("Now, you will undergo a test that will determine whether you're fit or not. All you have to do is take on the most powerful monster on this island, #o0100134#s. About #r50#k of them would suffice, but...");
            break;
        case 1:
            qm.sendAcceptDecline("We can't have you wipe out the entire population of #o0100134#s, since they aren't many of them out there. How about 5 of them? You're here to train, not to destroy the ecosystem.");
            break;
        case 2:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendOk("Oh, is 5 not enough? If you feel the need to train further, please feel free to slay more than that. If you slay all of them, I'll just have to look the other way even if it breaks my heart, since they will have been sacrificed for a good cause...");
            } else { // ACCEPT
                if (!qm.isQuestStarted(21018)) {
                    qm.forceStartQuest();
                }
                qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
            }
            qm.dispose();
            break;
        default:
            qm.dispose();
            break;
    }
}