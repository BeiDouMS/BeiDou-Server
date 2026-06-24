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
    if (finishIfAlreadyAwarded(Medal.PINK_BEAN_SLAYER_MEDAL_ID)) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("击败品克缤 #b" + Medal.PINK_BEAN_REQUIRED_KILLS + "#k 次后，再来领取#b#t" + Medal.PINK_BEAN_SLAYER_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.PINK_BEAN_SLAYER_MEDAL_ID)) {
        return;
    }
    var kills = Medal.getProgress(qm.getPlayer(), Medal.PINK_BEAN_SLAYER_QUEST_ID, Medal.PROGRESS_KILLS);
    if (kills < Medal.PINK_BEAN_REQUIRED_KILLS) {
        qm.sendOk("请先击败品克缤，再回来报告。\r\n进度：#r" + kills + "#k / " + Medal.PINK_BEAN_REQUIRED_KILLS);
        qm.dispose();
        return;
    }
    awardMedal(Medal.PINK_BEAN_SLAYER_MEDAL_ID);
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
    qm.sendOk("品克缤那不可思议的力量，也没能动摇你的决心。冒险岛世界会记住你战胜传说的这一刻。\r\n\r\n请收下#b#t" + medalId + "##k。它将证明你曾直面品克缤，并取得胜利。");
    qm.dispose();
}
