var MEDAL_ID = 1142141;

function finishIfAlreadyAwarded() {
    if (qm.isQuestCompleted(qm.getQuest())) {
        qm.sendOk("You have already received #b#t" + MEDAL_ID + "##k, and this achievement is already recorded in your journey.");
        qm.dispose();
        return true;
    }
    if (qm.haveItemWithId(MEDAL_ID, true)) {
        qm.forceCompleteQuest();
        qm.earnTitle(qm.getMedalName());
        qm.sendOk("You have already received #b#t" + MEDAL_ID + "##k, and this achievement is already recorded in your journey.");
        qm.dispose();
        return true;
    }
    return false;
}

function start(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("Defeat 10,000 monsters on the Abandoned Subway Platform, then speak with Mr. Lim or Dalair to receive #b#t1142141##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    var player = qm.getPlayer();
    var count = qm.getQuestProgressInt(29931, 7662);
    if (count < 10000) {
        qm.sendOk("Please keep eliminating monsters on the Abandoned Subway Platform.\r\nProgress: #r" + count + "#k / 10000");
        qm.dispose();
        return;
    }
    awardMedal();
}

function awardMedal() {
    if (!qm.haveItem(MEDAL_ID)) {
        if (!qm.canHold(MEDAL_ID)) {
            qm.sendOk("Please make 1 empty slot in your Equip inventory.");
            qm.dispose();
            return;
        }
        qm.gainItem(MEDAL_ID, 1);
    }
    qm.forceCompleteQuest();
    qm.earnTitle(qm.getMedalName());
    qm.sendOk("The abandoned platform is safer because you refused to abandon it. Every monster you drove back helped the subway breathe again.\r\n\r\nPlease accept #b#t1142141##k. Kerning City's rails will remember your work.");
    qm.dispose();
}
