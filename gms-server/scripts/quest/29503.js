var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("Contribute #b" + Medal.DONATION_REQUIRED_MESO + "#k mesos to support public facilities, then return to receive the #b#t" + Medal.DONATION_KING_MEDAL_ID + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (qm.getPlayer().getMeso() < Medal.DONATION_REQUIRED_MESO) {
        qm.sendOk("You need #b" + Medal.DONATION_REQUIRED_MESO + "#k mesos to complete this contribution.");
        qm.dispose();
        return;
    }
    if (!qm.haveItem(Medal.DONATION_KING_MEDAL_ID) && !qm.canHold(Medal.DONATION_KING_MEDAL_ID)) {
        qm.sendOk("Please make room in your equip inventory.");
        qm.dispose();
        return;
    }

    qm.gainMeso(-Medal.DONATION_REQUIRED_MESO);
    awardMedal(Medal.DONATION_KING_MEDAL_ID);
}

function awardMedal(medalId) {
    if (!qm.haveItem(medalId)) {
        qm.gainItem(medalId, 1);
    }

    qm.forceCompleteQuest();
    qm.earnTitle(qm.getMedalName());
    qm.dispose();
}
