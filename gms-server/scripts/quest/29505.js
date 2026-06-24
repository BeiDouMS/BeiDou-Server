var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("Win Monster Carnival 2 #b" + Medal.CARNIVAL_REQUIRED_WINS + "#k times, then return to receive the #b#t" + Medal.CARNIVAL_VICTORY_MEDAL_ID + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
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
    qm.dispose();
}
