var map = 677000008;
var quest = 28219;
var questItem = 4032493;
var status = -1;

function start(mode, type, selection) {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (cm.isQuestStarted(quest)) {
            if (cm.haveItem(questItem)) {
                cm.sendYesNo("你想要移动到 #b#m" + map + "##k 吗？");
            } else {
                cm.sendOk("入口被一种力量封锁，只有持有徽章的人才能解除。");
                cm.dispose();
            }
        } else {
            cm.sendOk("入口被一股奇怪的力量阻挡住了。");
            cm.dispose();
        }
    } else {
        if (cm.haveItem(4032485, 1)) {
            cm.gainItem(4032485, -1);
        }
        if (cm.haveItem(4001355, 1)) {
            cm.gainItem(4001355, -1);
        }

        cm.warp(map, 0);
        cm.dispose();
    }
}