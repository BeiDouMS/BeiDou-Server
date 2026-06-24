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
    if (finishIfAlreadyAwarded(Medal.CARNIVAL_VICTORY_MEDAL_ID)) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("在怪物嘉年华2中获胜 #b" + Medal.CARNIVAL_REQUIRED_WINS + "#k 次后，再来领取#b#t" + Medal.CARNIVAL_VICTORY_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.CARNIVAL_VICTORY_MEDAL_ID)) {
        return;
    }
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
    qm.sendOk("一场胜利也许是运气，一百场胜利就是宣言。你在怪物嘉年华2中的战绩，已经没人能够忽视。\r\n\r\n请收下#b#t" + medalId + "##k。百战百胜者的名号，配得上你的实力。");
    qm.dispose();
}
