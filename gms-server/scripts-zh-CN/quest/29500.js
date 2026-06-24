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
    if (finishIfAlreadyAwarded(Medal.MAPLE_IDOL_MEDAL_ID)) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("人气达到 #b" + Medal.MAPLE_IDOL_REQUIRED_FAME + "#k 后，再来领取#b#t" + Medal.MAPLE_IDOL_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.MAPLE_IDOL_MEDAL_ID)) {
        return;
    }
    var player = qm.getPlayer();
    if (player.getFame() < Medal.MAPLE_IDOL_REQUIRED_FAME) {
        qm.sendOk("领取这枚勋章需要人气达到 #b" + Medal.MAPLE_IDOL_REQUIRED_FAME + "#k。\r\n当前人气：#r" + player.getFame() + "#k");
        qm.dispose();
        return;
    }
    awardMedal(Medal.MAPLE_IDOL_MEDAL_ID);
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
    qm.sendOk("你的名字已经在冒险岛世界里传开了。人气不是偶然的掌声，而是大家记住你冒险足迹的证明。\r\n\r\n请收下#b#t" + medalId + "##k。从今天起，你就是名副其实的冒险岛偶像明星。");
    qm.dispose();
}
