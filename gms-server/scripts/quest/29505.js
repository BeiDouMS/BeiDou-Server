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
    if (finishIfAlreadyAwarded(Medal.CARNIVAL_VICTORY_MEDAL_ID)) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("Win Monster Carnival 2 #b" + Medal.CARNIVAL_REQUIRED_WINS + "#k times, then return to receive the #b#t" + Medal.CARNIVAL_VICTORY_MEDAL_ID + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.CARNIVAL_VICTORY_MEDAL_ID)) {
        return;
    }
    var wins = Medal.getProgress(qm.getPlayer(), Medal.CARNIVAL_VICTORY_QUEST_ID, Medal.PROGRESS_WINS);
    if (wins < Medal.CARNIVAL_REQUIRED_WINS) {
        qm.sendOk("Keep winning in Monster Carnival 2.\r\nWins: #r" + wins + "#k / " + Medal.CARNIVAL_REQUIRED_WINS);
        qm.dispose();
        return;
    }

    awardMedal(Medal.CARNIVAL_VICTORY_MEDAL_ID);
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
    qm.sendOk("One victory can be luck, but one hundred victories are a declaration. Your record in Monster Carnival 2 has become impossible to ignore.\r\n\r\nPlease accept the #b#t" + medalId + "##k. You have earned the name of Absolute Victory Carnivalian.");
    qm.dispose();
}
