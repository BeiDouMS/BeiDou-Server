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
            qm.sendNext("It seems like you're warmed up now. This is when rigorous training can really help you build a strong foundation. Let's proceed with the Basic Training, shall we?", 8);
            break;
        case 1:
            qm.sendNextPrev("Go defeat some #r#o0100133#s#k in #b#m140020200##k this time. I think about  #r20#k should do it. Go on ahead and... Hm? Do you have something you'd like to say?", 8);
            break;
        case 2:
            qm.sendNextPrev("Isn't the number getting bigger and bigger?", 2);
            break;
        case 3:
            qm.sendNextPrev("Of course it is. What, are you not happy with 20? Would you like to defeat 100 of them instead? Oh, how about 999 of them? Someone in Sleepywood would be able to do it easily. After all, we are training...", 8);
            break;
        case 4:
            qm.sendNextPrev("Oh no, no, no. Twenty is plenty.", 2);
            break;
        case 5:
            qm.sendAcceptDecline("You don't have to be so modest. I understand your desire to quickly become the hero you once were. This sort of attitude is what makes you a hero.");
            break;
        case 6:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("#b(You declined out of fear, but it's not like you can run away like this. Take a big breath, calm down, and try again.)#k", 2);
                qm.dispose();
                break;
            }
            // ACCEPCT
            if (!qm.isQuestStarted(21017)) {
                qm.forceStartQuest();
            }
            qm.sendNext("#b(You accepted, thinking you might end up having to 999 of them if you let her keep talking.)#k", 2);
            break;
        case 7:
            qm.sendNextPrev("Please go ahead and slay 20 #o0100133#s.", 8);
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