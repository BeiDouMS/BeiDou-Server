/*
 * NPC : Francis (Doll master)
 * Map : 910510200
 */

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        cm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 0) {
        cm.sendNext("我是黑翼的傀儡师弗朗西斯。你竟敢打扰我的木偶……这真的让我很生气，但这一次我会放过你。但如果我再发现你这样做，我发誓以黑魔法师的名义让你付出代价。");
    } else if (status == 1) {
        cm.sendNextPrev("#b(黑翼？嗯？他们是谁？这一切与黑魔法师有什么关系？嗯，也许你应该把这些信息报告给特鲁。)#k");
    } else if (status == 2) {
        cm.completeQuest(21719);
        cm.warp(105040200, 10);//104000004 
        cm.dispose();
    }
}