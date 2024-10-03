function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendSimple("等等！等你达到10级的时候，你会自己搞清楚这些东西的，但如果你绝对想提前准备，你可以查看以下信息。\r\n\r\n 告诉我，你想知道什么？\r\n#b#L0#关于你#l\r\n#L1#小地图#l\r\n#L2#任务窗口#l\r\n#L3#背包#l\r\n#L4#普通攻击狩猎#l\r\n#L5#如何拾取物品#l\r\n#L6#如何装备物品#l\r\n#L7#技能窗口#l\r\n#L8#如何使用快捷栏#l\r\n#L9#如何打开箱子#l\r\n#L10#如何坐在椅子上#l\r\n#L11#世界地图#l\r\n#L12#任务通知#l\r\n#L13#增强属性#l\r\n#L14#谁是皇家骑士？#l");
        } else if (status == 1) {
            if (selection == 0) {
                cm.sendNext("我侍奉着守护女皇赛琳苏的神兽神树。我的主人神树命令我引导所有来到枫之世界的人加入皇家骑士团。在你成为骑士或达到11级之前，我会一直在你身边协助和跟随你。如果你有任何问题，请告诉我。");
            } else if (selection == 1) {
                cm.guideHint(1);
                cm.dispose();
            } else if (selection == 2) {
                cm.guideHint(2);
                cm.dispose();
            } else if (selection == 3) {
                cm.guideHint(3);
                cm.dispose();
            } else if (selection == 4) {
                cm.guideHint(4);
                cm.dispose();
            } else if (selection == 5) {
                cm.guideHint(5);
                cm.dispose();
            } else if (selection == 6) {
                cm.guideHint(6);
                cm.dispose();
            } else if (selection == 7) {
                cm.guideHint(7);
                cm.dispose();
            } else if (selection == 8) {
                cm.guideHint(8);
                cm.dispose();
            } else if (selection == 9) {
                cm.guideHint(9);
                cm.dispose();
            } else if (selection == 10) {
                cm.guideHint(10);
                cm.dispose();
            } else if (selection == 11) {
                cm.guideHint(11);
                cm.dispose();
            } else if (selection == 12) {
                cm.guideHint(12);
                cm.dispose();
            } else if (selection == 13) {
                cm.guideHint(13);
                cm.dispose();
            } else if (selection == 14) {
                cm.sendOk("黑魔法师正试图复活并征服我们和平的枫之世界。作为对这一威胁的回应，帝国女皇赛琳娜组建了一个骑士团，现在被称为皇家骑士团。当你达到10级时，你可以成为一名骑士。");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.sendNextPrev("现在没有必要检查这些信息。这些都是你在游戏中会逐渐学会的基础知识。当你达到10级后，你可以随时问我遇到的问题，所以放松一点吧。");
            cm.dispose();
        }
    }
}