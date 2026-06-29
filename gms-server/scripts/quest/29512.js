var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function finishIfAlreadyAwarded(medalId) {
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

function start(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.MONSTER_EXPERT_MEDAL_ID)) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("Collect #b" + Medal.MONSTER_BOOK_REQUIRED_CARDS + "#k monster cards, then return to receive the #b#t" + Medal.MONSTER_EXPERT_MEDAL_ID + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.MONSTER_EXPERT_MEDAL_ID)) {
        return;
    }
    var cards = Medal.getMonsterBookCards(qm.getPlayer());
    if (cards < Medal.MONSTER_BOOK_REQUIRED_CARDS) {
        qm.sendOk("Collect more monster cards.\r\nCards: #r" + cards + "#k / " + Medal.MONSTER_BOOK_REQUIRED_CARDS);
        qm.dispose();
        return;
    }

    awardMedal(Medal.MONSTER_EXPERT_MEDAL_ID);
}

function awardMedal(medalId) {
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
    qm.sendOk("You studied monsters not only by fighting them, but by learning their habits, traces, and secrets. That knowledge is a weapon of its own.\r\n\r\nPlease accept the #b#t" + medalId + "##k. From now on, you may proudly be called a Monster Expert.");
    qm.dispose();
}
