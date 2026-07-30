var MEDAL_ID = 1142142;

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
    qm.sendOk("在奈特的金字塔中消灭 50,000 只怪物后，前往杜阿特或德烈处领取#b#t1142142##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    var player = qm.getPlayer();
    var count = qm.getQuestProgressInt(29932, 7760);
    if (count < 50000) {
        qm.sendOk("请继续在奈特的金字塔中消灭怪物。\r\n当前进度：#r" + count + "#k / 50000");
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
    qm.sendOk("古老金字塔的寂静，由你的力量重新守护。无数怪物倒下，而你的信念始终没有退后一步。\r\n\r\n请收下#b#t1142142##k。从现在起，法老守护者的称号属于你。");
    qm.dispose();
}
