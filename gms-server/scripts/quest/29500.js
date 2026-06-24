var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("Reach #b" + Medal.MAPLE_IDOL_REQUIRED_FAME + "#k fame, then return to receive the #b#t" + Medal.MAPLE_IDOL_MEDAL_ID + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    var player = qm.getPlayer();
    if (player.getFame() < Medal.MAPLE_IDOL_REQUIRED_FAME) {
        qm.sendOk("You need #b" + Medal.MAPLE_IDOL_REQUIRED_FAME + "#k fame to receive this medal.\r\nCurrent fame: #r" + player.getFame() + "#k");
        qm.dispose();
        return;
    }

    awardMedal(Medal.MAPLE_IDOL_MEDAL_ID);
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
    qm.sendOk("Your name is already being carried across Maple World. That kind of fame is not luck; it is proof that people remember your adventures.\r\n\r\nPlease accept the #b#t" + medalId + "##k. From today on, you stand among the true Maple Idol Stars.");
    qm.dispose();
}
