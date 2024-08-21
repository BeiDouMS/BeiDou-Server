var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        if (qm.getQuestProgress(3114) == 42) {
            qm.sendNext("Is it done? Thank you, adventurer. Thanks to your help, I can sleep peacefully...");
        } else {
            qm.sendNext("You haven't finished yet.");
            qm.dispose();
        }
    } else if (status == 1) {
        qm.forceCompleteQuest();
        qm.gainFame(20);
        qm.sendNext("Goodbye, I hope we can meet again.");
        qm.dispose();
    }
}
