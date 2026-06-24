var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("完成林中之城的三个危险迷宫任务后，再来领取#b#t" + Medal.CHALLENGER_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (!qm.isQuestCompleted(2111) || !qm.isQuestCompleted(2112) || !qm.isQuestCompleted(2113)) {
        qm.sendOk("请先完成#b#y2111##k、#b#y2112##k、#b#y2113##k 后再来报告。");
        qm.dispose();
        return;
    }
    awardMedal(Medal.CHALLENGER_MEDAL_ID);
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
    qm.sendOk("危险的道路、幽暗的迷宫、数不清的怪物，都没能让你停下脚步。你一次次迎向挑战，直到把道路亲手打开。\r\n\r\n请收下#b#t" + medalId + "##k。坚强的挑战者这个称号，现在属于你。");
    qm.dispose();
}
