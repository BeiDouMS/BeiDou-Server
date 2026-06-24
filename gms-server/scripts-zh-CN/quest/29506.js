var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function finishIfAlreadyAwarded(medalId) {
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

function start(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.CARNIVAL_GENIUS_MEDAL_ID)) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("参加至少 #b" + Medal.CARNIVAL_GENIUS_MIN_MATCHES + "#k 场怪物嘉年华2，并保持 #b" + Medal.CARNIVAL_GENIUS_WIN_RATE + "%#k 以上胜率。");
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
        qm.sendOk("请继续提升怪物嘉年华2战绩。\r\n场次：#r" + matches + "#k / " + Medal.CARNIVAL_GENIUS_MIN_MATCHES + "\r\n胜率：#r" + rate + "%#k / " + Medal.CARNIVAL_GENIUS_WIN_RATE + "%");
        qm.dispose();
        return;
    }
    awardMedal(Medal.CARNIVAL_GENIUS_MEDAL_ID);
}

function awardMedal(medalId) {
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
    qm.sendOk("你并不是只会取胜，而是用判断、节奏和胆识持续赢下去。真正的嘉年华天才，就该有这样的战绩。\r\n\r\n请收下#b#t" + medalId + "##k。你的胜率，比任何夸耀都更有说服力。");
    qm.dispose();
}
