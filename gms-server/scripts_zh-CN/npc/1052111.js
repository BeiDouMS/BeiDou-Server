/*
	Trash Can 3
	Kerning Subway
*/

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();

    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        } else if (mode == 0) {
            status--;
        } else {
            status++;
        }

        if (status == 0) {
            if (cm.isQuestStarted(20710)) {
                if (!cm.hasItem(4032136)) {
                    if (cm.canHold(4032136)) {
                        cm.gainItem(4032136, 1);
                        cm.sendNext("你在垃圾桶里找到了一个#b#t4032136##k！ #i4032136#");
                    } else {
                        cm.sendOk("#i4032136# 您的杂项物品栏空间不足。");
                    }
                } else {
                    cm.sendOk("只是一个垃圾桶就坐在那里。");
                }
            } else {
                cm.sendOk("只是一个垃圾桶就坐在那里。");
            }
        } else if (status == 1) {
            cm.dispose();
        }
    }
}