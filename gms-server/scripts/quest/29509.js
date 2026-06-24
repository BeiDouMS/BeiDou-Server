var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("Complete the three Dangerous Dungeon quests in Sleepywood, then return to receive the #b#t" + Medal.CHALLENGER_MEDAL_ID + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (!qm.isQuestCompleted(2111) || !qm.isQuestCompleted(2112) || !qm.isQuestCompleted(2113)) {
        qm.sendOk("Complete #b#y2111##k, #b#y2112##k, and #b#y2113##k before reporting back.");
        qm.dispose();
        return;
    }

    awardMedal(Medal.CHALLENGER_MEDAL_ID);
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
