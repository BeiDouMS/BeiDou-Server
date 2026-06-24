var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("贡献 #b" + Medal.DONATION_REQUIRED_MESO + "#k 金币用于公共设施建设后，再来领取#b#t" + Medal.DONATION_KING_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (qm.getPlayer().getMeso() < Medal.DONATION_REQUIRED_MESO) {
        qm.sendOk("完成这次公益贡献需要 #b" + Medal.DONATION_REQUIRED_MESO + "#k 金币。");
        qm.dispose();
        return;
    }
    if (!qm.haveItem(Medal.DONATION_KING_MEDAL_ID) && !qm.canHold(Medal.DONATION_KING_MEDAL_ID)) {
        qm.sendOk("请在装备栏空出 1 个位置。");
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
    qm.sendOk("真正温暖人心的力量，不只来自武器，也来自愿意帮助他人的善意。你的贡献会让后来者走得更安心。\r\n\r\n请收下#b#t" + medalId + "##k。这份称号，献给你藏在强大背后的爱心。");
    qm.dispose();
}
