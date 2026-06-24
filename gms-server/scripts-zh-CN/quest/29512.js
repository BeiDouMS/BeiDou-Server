var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("收集 #b" + Medal.MONSTER_BOOK_REQUIRED_CARDS + "#k 张怪物卡后，再来领取#b#t" + Medal.MONSTER_EXPERT_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    var cards = Medal.getMonsterBookCards(qm.getPlayer());
    if (cards < Medal.MONSTER_BOOK_REQUIRED_CARDS) {
        qm.sendOk("请继续收集怪物卡。\r\n已收集：#r" + cards + "#k / " + Medal.MONSTER_BOOK_REQUIRED_CARDS);
        qm.dispose();
        return;
    }
    awardMedal(Medal.MONSTER_EXPERT_MEDAL_ID);
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
    qm.sendOk("你了解怪物，不只是靠战斗，更靠观察它们的习性、踪迹与秘密。知识同样是一把锋利的武器。\r\n\r\n请收下#b#t" + medalId + "##k。从现在起，你可以自豪地被称为怪物博士。");
    qm.dispose();
}
