/*
	QUEST: 公主的身份
	NPC: 维奥莉塔
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        qm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }


    if (status == 0) {
        qm.forceStartQuest();
        qm.sendNext("非常感谢你，#b#h ##k。你就是拯救我们帝国免于危险的英雄。我对你所做的一切感激不尽。我不知道该如何感谢你。请理解我为什么不能让你看到我的面孔。");
    } else if (status == 1) {
        qm.sendNextPrev("说出来真是难为情，但自从我还是个婴儿的时候，我的家人就把我的面容遮掩起来，不让世人看见。他们害怕有人会无法自拔地爱上我。我已经习惯了这种生活，甚至对女性也感到害羞。我知道，把背对着救世主是很失礼的，但在我能够鼓起勇气与你面对面相见之前，我还需要一些时间。");
    } else if (status == 2) {
        qm.sendNextPrev("我明白了...\r\n#b（哇，她究竟有多美？）", 2);
    } else if (status == 3) {
        qm.sendNextPrev("#b（这是什么意思？）", 2);
    } else if (status == 4) {
        qm.sendNextPrev("#b（在蘑菇世界里这也算是漂亮吗？！）", 2);
    } else if (status == 5) {
        qm.sendNextPrev("我好害羞，都脸红了。总之，谢谢你，#b#h ##k。");
    } else if (status == 6) {
        qm.forceStartQuest();
        qm.gainExp(1000);
        qm.forceCompleteQuest();
        qm.dispose();
    }
}

function end(mode, type, selection) {}
