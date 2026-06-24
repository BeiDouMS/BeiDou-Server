var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("Collect #b" + Medal.MONSTER_BOOK_REQUIRED_CARDS + "#k monster cards, then return to receive the #b#t" + Medal.MONSTER_EXPERT_MEDAL_ID + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
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
    qm.dispose();
}
