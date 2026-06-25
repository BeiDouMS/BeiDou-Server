var MEDAL_ID = 1142132;

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
    qm.sendOk("成为 120 级以上战神后，前往德烈处领取#b#t1142132##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded()) {
        return;
    }
    var player = qm.getPlayer();
    if (player.getLevel() < 120 || ((player.getJob().getId() / 100) | 0) != 21) {
        qm.sendOk("领取这枚勋章需要成为 #b120 级以上战神#k。");
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
    qm.sendOk("希望已经在愤怒与混乱散去的地方重新生长。那个为了守护他人而战的英雄，终于又一次挺直了身影。\r\n\r\n请收下#b#t1142132##k。愿它承载你重新点燃的希望。");
    qm.dispose();
}
