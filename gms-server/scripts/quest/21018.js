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
        qm.sendNext("Now, you will undergo a test that will determine whether you're fit or not. All you have to do is take on the most powerful monster on this island, #o0100134#s. About #r50#k of them would suffice, but...");
    } else if (status == 1) {
        qm.sendAcceptDecline("We can't have you wipe out the entire population of #o0100134#s, since they aren't many of them out there. How about 5 of them? You're here to train, not to destroy the ecosystem.");
    } else if (status == 2) {
        if (mode == 0) {
            qm.sendOk("Oh, is 5 not enough? If you feel the need to train further, please feel free to slay more than that. If you slay all of them, I'll just have to look the other way even if it breaks my heart, since they will have been sacrificed for a good cause...");
        } else {
            qm.forceStartQuest();
            qm.showInfo("Effect/OnUserEff.img/guideEffect/aranTutorial/tutorialArrow1");
        }
        qm.dispose();
    }
}