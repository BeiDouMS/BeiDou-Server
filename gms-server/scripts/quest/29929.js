var MEDAL_ID = 1140001;

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
    qm.sendOk("Reach level 13 with a male character, then speak with Dalair to receive #b#t1140001##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    var player = qm.getPlayer();
    if (player.getLevel() < 13 || player.getGender() != 0) {
        qm.sendOk("A male character of level 13 or higher is required to receive this medal.");
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
    qm.sendOk("Winter crowns those who keep moving even when the cold tries to slow every step. Your resolve has given this season a worthy king.\r\n\r\nPlease accept #b#t1140001##k. May your name be remembered with the brightness of winter snow.");
    qm.dispose();
}
