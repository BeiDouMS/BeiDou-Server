var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("参加至少 #b" + Medal.CARNIVAL_GENIUS_MIN_MATCHES + "#k 场怪物嘉年华2，并保持 #b" + Medal.CARNIVAL_GENIUS_WIN_RATE + "%#k 以上胜率。");
    qm.dispose();
}

function end(mode, type, selection) {
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
    qm.dispose();
}