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
            qm.sendNext("What did #p1032112# say?", 8);
            break;
        case 1:
            qm.sendNextPrev("#b(You tell her what #p1032112# observed.)#k", 2);
            break;
        case 2:
            qm.sendAcceptDecline("A kid with a puppet? That seems very suspicious. I am sure that kid is the reason the Green Mushrooms have suddenly turned violent.");
            break;
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                qm.sendNext("What? I don't think there are any suspects besides that kid. Please think again.");
                qm.dispose();
                break;
            }
            // ACCEPT
            if (!qm.isQuestStarted(21716)) {
                qm.forceStartQuest();
            }
            qm.sendNext("How dare this kid wreak havoc in the South Forest. Who knows how long it will take to restore the forest... I'll have to devote most of my time cleaning up the mess.", 2);
            break;
        case 4:
            qm.sendPrev("#b(You were able to find out what caused the changes in the Green Mushrooms. You should report #p1002104# and deliver the information you've collected.)#k", 2);
            break;
        default:
            qm.dispose();
            break;
    }
}