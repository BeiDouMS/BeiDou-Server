/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendNext("你好，战神。我们收到情报，黑色翅膀成员之一的人偶师，似乎藏身在#b林中之城深处的某个地方#k。你的任务是进入那里，彻底击退他。");
        } else {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendNext("你成功了，战神！人偶师暂时不会再威胁金银岛的和平了。这样一来，我们也能更清楚地调查黑色翅膀在金银岛的行动。");
        } else if (status == 1) {
            qm.sendNext("他们盯上的是#b金银岛的封印石#k。封印石能够阻止黑魔法师一次性把各大陆纳入掌控。每片大陆都有一颗，而金银岛的封印石现在安全了。");
        } else if (status == 2) {
            qm.sendNext("为了表彰你在这一连串任务中的英勇表现，我要给你合适的奖励。收下#r连环吸血#k技能吧。它可以让你在攻击怪物时恢复一部分造成的伤害。");
        } else if (status == 3) {
            qm.forceCompleteQuest();

            qm.gainExp(12500);
            qm.teachSkill(21100005, 0, 20, -1); // combo drain

            qm.dispose();
        }
    }
}
