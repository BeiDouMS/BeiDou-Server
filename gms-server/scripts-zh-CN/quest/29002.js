var medalId = 1142003;
var requiredFameGain = 1000;
var challengeDurationMs = 30 * 24 * 60 * 60 * 1000;

function finishIfAlreadyAwarded() {
    if (qm.isQuestCompleted(qm.getQuest())) {
        qm.sendOk("你已经获得过#b#t" + medalId + "##k，这项挑战已经记录在你的冒险履历里了。");
        qm.dispose();
        return true;
    }
    if (qm.haveItemWithId(medalId, true)) {
        qm.forceCompleteQuest();
        qm.earnTitle(qm.getMedalName());
        qm.sendOk("你已经获得过#b#t" + medalId + "##k，这项挑战已经记录在你的冒险履历里了。");
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
    qm.sendOk("请在 30 天内把人气提高 #b" + requiredFameGain + "#k 点，再来领取#b#t" + medalId + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }

    var questRecord = qm.getQuestRecord(qm.getQuest());
    if (questRecord == null || System.currentTimeMillis() > questRecord.getCompletionTime() + challengeDurationMs) {
        qm.sendOk("30 天挑战期限已经结束了。如果你还想挑战#b#t" + medalId + "##k，请先重新接取任务。");
        qm.dispose();
        return;
    }

    var startFame = getRecordedStartFame();
    if (startFame < 0) {
        qm.sendOk("无法确认你接取挑战时的人气记录，请先放弃任务后重新接取。");
        qm.dispose();
        return;
    }

    var currentFame = qm.getPlayer().getFame();
    var gainedFame = currentFame - startFame;
    if (gainedFame < requiredFameGain) {
        qm.sendOk("领取这枚勋章需要你在 30 天内提高 #b" + requiredFameGain + "#k 点人气。\r\n当前已提升：#r" + gainedFame + "#k");
        qm.dispose();
        return;
    }

    if (!qm.haveItem(medalId)) {
        if (!qm.canHold(medalId)) {
            qm.sendOk("请在装备栏空出 1 个位置。");
            qm.dispose();
            return;
        }
        qm.gainItem(medalId, 1);
    }

    qm.forceCompleteQuest();
    qm.earnTitle(qm.getMedalName());
    qm.sendOk("你在 30 天内成功提高了 #b" + gainedFame + "#k 点人气，已经证明自己是真正的超人气冒险家。\r\n\r\n请收下#b#t" + medalId + "##k。");
    qm.dispose();
}
