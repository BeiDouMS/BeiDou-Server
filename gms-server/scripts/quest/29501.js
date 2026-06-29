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
    if (finishIfAlreadyAwarded(Medal.HORNTAIL_SLAYER_MEDAL_ID)) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("Defeat Horntail #b" + Medal.HORNTAIL_REQUIRED_KILLS + "#k time, then return to receive the #b#t" + Medal.HORNTAIL_SLAYER_MEDAL_ID + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.HORNTAIL_SLAYER_MEDAL_ID)) {
        return;
    }
    var kills = Medal.getProgress(qm.getPlayer(), Medal.HORNTAIL_SLAYER_QUEST_ID, Medal.PROGRESS_KILLS);
    if (kills < Medal.HORNTAIL_REQUIRED_KILLS) {
        qm.sendOk("Defeat Horntail and come back to report.\r\nProgress: #r" + kills + "#k / " + Medal.HORNTAIL_REQUIRED_KILLS);
        qm.dispose();
        return;
    }

    awardMedal(Medal.HORNTAIL_SLAYER_MEDAL_ID);
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
    qm.sendOk("The roar of Horntail has finally gone silent before you. Few adventurers can return from that battle and speak of victory.\r\n\r\nPlease accept the #b#t" + medalId + "##k. The title of Horntail Slayer belongs to you.");
    qm.dispose();
}
