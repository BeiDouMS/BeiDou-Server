var medalId = 1142003;
var requiredFameGain = 1000;
var challengeDurationMs = 30 * 24 * 60 * 60 * 1000;

function finishIfAlreadyAwarded() {
    if (qm.isQuestCompleted(qm.getQuest())) {
        qm.sendOk("You have already received the #b#t" + medalId + "##k. This challenge is already recorded in your adventure history.");
        qm.dispose();
        return true;
    }
    if (qm.haveItemWithId(medalId, true)) {
        qm.forceCompleteQuest();
        qm.earnTitle(qm.getMedalName());
        qm.sendOk("You have already received the #b#t" + medalId + "##k. This challenge is already recorded in your adventure history.");
        qm.dispose();
        return true;
    }
    return false;
}

function getRecordedStartFame() {
    var progress = qm.getQuestProgress(qm.getQuest());
    if (progress == null || progress.length === 0) {
        return -1;
    }

    var startFame = parseInt(progress, 10);
    return isNaN(startFame) ? -1 : startFame;
}

function start(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }

    qm.forceStartQuest();
    qm.setQuestProgress(qm.getQuest(), "" + qm.getPlayer().getFame());
    qm.sendOk("Raise your fame by #b" + requiredFameGain + "#k within 30 days, then return to receive the #b#t" + medalId + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }

    var questRecord = qm.getQuestRecord(qm.getQuest());
    if (questRecord == null || System.currentTimeMillis() > questRecord.getCompletionTime() + challengeDurationMs) {
        qm.sendOk("The 30-day challenge period has expired. Please restart the challenge if you want to try for the #b#t" + medalId + "##k again.");
        qm.dispose();
        return;
    }

    var startFame = getRecordedStartFame();
    if (startFame < 0) {
        qm.sendOk("Your starting fame for this challenge could not be verified. Please forfeit the challenge and start it again.");
        qm.dispose();
        return;
    }

    var currentFame = qm.getPlayer().getFame();
    var gainedFame = currentFame - startFame;
    if (gainedFame < requiredFameGain) {
        qm.sendOk("You need to gain #b" + requiredFameGain + "#k fame within 30 days to receive this medal.\r\nCurrent fame gained: #r" + gainedFame + "#k");
        qm.dispose();
        return;
    }

    if (!qm.haveItem(medalId)) {
        if (!qm.canHold(medalId)) {
            qm.sendOk("Please make room in your equip inventory.");
            qm.dispose();
            return;
        }
        qm.gainItem(medalId, 1);
    }

    qm.forceCompleteQuest();
    qm.earnTitle(qm.getMedalName());
    qm.sendOk("You raised your fame by #b" + gainedFame + "#k within 30 days and proved that your popularity is backed by real adventures.\r\n\r\nPlease accept the #b#t" + medalId + "##k.");
    qm.dispose();
}
