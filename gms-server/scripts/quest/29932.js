var MEDAL_ID = 1142142;

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
    qm.sendOk("Defeat 50,000 monsters in Nett's Pyramid, then speak with Duarte or Dalair to receive #b#t1142142##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    var player = qm.getPlayer();
    var count = qm.getQuestProgressInt(29932, 7760);
    if (count < 50000) {
        qm.sendOk("Please keep eliminating monsters in Nett's Pyramid.\r\nProgress: #r" + count + "#k / 50000");
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
    qm.sendOk("The pyramid's ancient silence was guarded by your strength. Countless monsters fell, but your resolve never did.\r\n\r\nPlease accept #b#t1142142##k. The title of Pharaoh's protector now belongs to you.");
    qm.dispose();
}
