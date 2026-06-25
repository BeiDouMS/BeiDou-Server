var MEDAL_ID = 1142133;

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
    qm.sendOk("Reach level 200 as an Aran, then speak with Dalair to receive #b#t1142133##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    var player = qm.getPlayer();
    if (player.getLevel() < 200 || ((player.getJob().getId() / 100) | 0) != 21) {
        qm.sendOk("You must be a level 200 or higher Aran to receive this medal.");
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
    qm.sendOk("Your lost power, your memories, and your name have all found their way back to you. The legend of Aran is no longer a story buried in ice.\r\n\r\nPlease accept #b#t1142133##k. This honor belongs to the hero who returned.");
    qm.dispose();
}
