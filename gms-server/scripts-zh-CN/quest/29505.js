var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("在怪物嘉年华2中获胜 #b" + Medal.CARNIVAL_REQUIRED_WINS + "#k 次后，再来领取#b#t" + Medal.CARNIVAL_VICTORY_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    var wins = Medal.getProgress(qm.getPlayer(), Medal.CARNIVAL_VICTORY_QUEST_ID, Medal.PROGRESS_WINS);
    if (wins < Medal.CARNIVAL_REQUIRED_WINS) {
        qm.sendOk("请继续在怪物嘉年华2中取得胜利。\r\n胜场：#r" + wins + "#k / " + Medal.CARNIVAL_REQUIRED_WINS);
        qm.dispose();
        return;
    }
    awardMedal(Medal.CARNIVAL_VICTORY_MEDAL_ID);
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