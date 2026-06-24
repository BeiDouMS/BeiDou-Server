var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("击败暗黑龙王 #b" + Medal.HORNTAIL_REQUIRED_KILLS + "#k 次后，再来领取#b#t" + Medal.HORNTAIL_SLAYER_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    var kills = Medal.getProgress(qm.getPlayer(), Medal.HORNTAIL_SLAYER_QUEST_ID, Medal.PROGRESS_KILLS);
    if (kills < Medal.HORNTAIL_REQUIRED_KILLS) {
        qm.sendOk("请先击败暗黑龙王，再回来报告。\r\n进度：#r" + kills + "#k / " + Medal.HORNTAIL_REQUIRED_KILLS);
        qm.dispose();
        return;
    }
    awardMedal(Medal.HORNTAIL_SLAYER_MEDAL_ID);
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