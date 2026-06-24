var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("人气达到 #b" + Medal.MAPLE_IDOL_REQUIRED_FAME + "#k 后，再来领取#b#t" + Medal.MAPLE_IDOL_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
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
    qm.dispose();
}