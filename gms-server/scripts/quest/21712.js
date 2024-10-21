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
            qm.sendNext("#t4032315#... #rThis puppet is making a strange noise#k. You can't hear it with your ears, of course, since it can only be heard by the #o1210102#s. I believe it's this noise that changed the personality of the #o1210102#s.");
            break;
        case 1:
            qm.sendAcceptDecline("The #o1210102#s that have been affected by the noise have turned cynical. They've started fighting the non-affected #o1210102#s, which has made all #o1210102#s prepare for combat. #bThe reason for all these changes in the #o1210102#s is this puppet#k! Do you understand?");
            break;
        case 2:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("You still don't understand what's going on? I'll explain it to you again if you talk to me one more time.");
                qm.dispose();
                break;
            }
            // ACCEPT
            if (!qm.isQuestStarted(21712)) {
                qm.forceStartQuest();
            }
            qm.sendNext("I wonder what triggered this in the first place. There is no way this puppet was naturally created, which means someone planned this. I should keep an eye on the #o1210102#s.", 9);
            break;
        case 3:
            qm.sendPrev("#b(You were able to find out what caused the changes in the #o1210102#s. You should report to #p1002104# and deliver the information you've gathered.)#k", 3);
            break;
        default:
            qm.dispose();
            break;
    }
}
