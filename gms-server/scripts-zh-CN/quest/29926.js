var MEDAL_ID = 1142131;

function finishIfAlreadyAwarded() {
    if (qm.isQuestCompleted(qm.getQuest())) {
        qm.sendOk("你已经获得过#b#t" + MEDAL_ID + "##k，这项挑战已经记录在你的冒险履历里了。");
        qm.dispose();
        return true;
    }
    if (qm.haveItemWithId(MEDAL_ID, true)) {
        qm.forceCompleteQuest();
        qm.earnTitle(qm.getMedalName());
        qm.sendOk("你已经获得过#b#t" + MEDAL_ID + "##k，这项挑战已经记录在你的冒险履历里了。");
        qm.dispose();
        return true;
    }
    return false;
}

function start(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("成为 70 级以上战神后，前往德烈处领取#b#t1142131##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    var player = qm.getPlayer();
    if (player.getLevel() < 70 || ((player.getJob().getId() / 100) | 0) != 21) {
        qm.sendOk("领取这枚勋章需要成为 #b70 级以上战神#k。");
        qm.dispose();
        return;
    }
    awardMedal();
}

function awardMedal() {
    if (!qm.haveItem(MEDAL_ID)) {
        if (!qm.canHold(MEDAL_ID)) {
            qm.sendOk("请在装备栏空出 1 个位置。");
            qm.dispose();
            return;
        }
        qm.gainItem(MEDAL_ID, 1);
    }
    qm.forceCompleteQuest();
    qm.earnTitle(qm.getMedalName());
    qm.sendOk("痛苦与迷惘曾不断考验你，却没能让你停下脚步。真正的英雄并非没有伤痕，而是在伤痕之中依然选择前进。\r\n\r\n请收下#b#t1142131##k。它会证明你的意志从未被击垮。");
    qm.dispose();
}
