/*
QUEST: Jame's Whereabouts (1)
NPC: James
 */

var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }

    if (status == 0) {
        qm.sendNext("我好……好害怕……请你一定要救救我……");
    } else if (status == 1) {
        qm.sendNextPrev("别害怕，是#b#p1300005##k让我来找你的。", 2);
    } else if (status == 2) {
        qm.forceCompleteQuest();
        qm.gainExp(6000);
        qm.sendOk("嗯？是哥哥让你来找我的？啊……终于得救了。真是谢谢你。");
    } else if (status == 3) {
        qm.dispose();
    }
}
