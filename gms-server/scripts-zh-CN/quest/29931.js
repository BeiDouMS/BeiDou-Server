var MEDAL_ID = 1142141;

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
    qm.sendOk("在废弃地铁月台消灭 10,000 只怪物后，前往林车长或德烈处领取#b#t1142141##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    var player = qm.getPlayer();
    var count = qm.getQuestProgressInt(29931, 7662);
    if (count < 10000) {
        qm.sendOk("请继续在废弃地铁月台消灭怪物。\r\n当前进度：#r" + count + "#k / 10000");
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
    qm.sendOk("废弃月台因为你的坚持而重新看见了安全的希望。你击退的不只是怪物，也是笼罩在地铁线路上的阴影。\r\n\r\n请收下#b#t1142141##k。废都的轨道，会记住你的付出。");
    qm.dispose();
}
