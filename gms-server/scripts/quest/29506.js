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
    if (finishIfAlreadyAwarded(Medal.CARNIVAL_GENIUS_MEDAL_ID)) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("Play at least #b" + Medal.CARNIVAL_GENIUS_MIN_MATCHES + "#k Monster Carnival 2 matches and keep a win rate of #b" + Medal.CARNIVAL_GENIUS_WIN_RATE + "%#k or higher.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.CARNIVAL_GENIUS_MEDAL_ID)) {
        return;
    }
    var wins = Medal.getProgress(qm.getPlayer(), Medal.CARNIVAL_GENIUS_QUEST_ID, Medal.PROGRESS_WINS);
    var losses = Medal.getProgress(qm.getPlayer(), Medal.CARNIVAL_GENIUS_QUEST_ID, Medal.PROGRESS_LOSSES);
    var matches = wins + losses;
    if (!Medal.hasCarnivalGeniusMedalProgress(qm.getPlayer())) {
        var rate = matches == 0 ? 0 : Math.floor(wins * 100 / matches);
        qm.sendOk("Keep improving your Monster Carnival 2 record.\r\nMatches: #r" + matches + "#k / " + Medal.CARNIVAL_GENIUS_MIN_MATCHES + "\r\nWin rate: #r" + rate + "%#k / " + Medal.CARNIVAL_GENIUS_WIN_RATE + "%");
        qm.dispose();
        return;
    }

    awardMedal(Medal.CARNIVAL_GENIUS_MEDAL_ID);
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
    qm.sendOk("You did not simply win; you kept winning with discipline, judgment, and nerve. That is the mark of a true Carnival genius.\r\n\r\nPlease accept the #b#t" + medalId + "##k. Your record speaks louder than any boast.");
    qm.dispose();
}
