var MEDAL_ID = 1142129;

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
    qm.sendOk("Reach level 10 as an Aran, then speak with the Medal Master to receive #b#t1142129##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    var player = qm.getPlayer();
    if (player.getLevel() < 10 || ((player.getJob().getId() / 100) | 0) != 21) {
        qm.sendOk("You must be a level 10 or higher Aran to receive this medal.");
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
    qm.sendOk("The ice that sealed you has finally begun to melt. Even without all of your memories, your courage has already answered for who you are.\r\n\r\nPlease accept #b#t1142129##k. This medal marks the first step of the hero who has awakened again.");
    qm.dispose();
}
