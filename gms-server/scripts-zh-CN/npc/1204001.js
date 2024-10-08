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
        cm.sendNext("我是黑色之翼的人偶师弗朗西斯。你把我安置的好几个人偶都给找出来了……坏了我的好事。虽然我很恼火，不过这次先放你一马。你要是再敢和我作对………我以黑魔法师大人的名义发誓，绝不放过你。", 9);
    } else if (status == 1) {
        cm.sendNextPrev("#b（……黑色之翼？作对？……到底是怎么回事？在怪兽的身上找到人偶与黑魔法师有什么关系？该去找特鲁商里商里。）#k", 3);
    } else if (status == 2) {
        cm.completeQuest(21719);
        cm.warp(105040200, 10);//104000004 
        cm.dispose();
    }
}