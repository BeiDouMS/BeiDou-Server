var status = 0;
var section = 0;

// questid 29931, infoquest 7662

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        if (cm.getMapId() == 910320001) {
            cm.warp(910320000, 0);
            cm.dispose();
        } else if (cm.getMapId() == 910330001) {
            var itemid = 4001321;
            if (!cm.canHold(itemid)) {
                cm.sendOk("请在其它栏空出 1 个位置。");
            } else {
                cm.gainItem(itemid, 1);
                cm.warp(910320000, 0);
            }
            cm.dispose();
        } else if (cm.getMapId() >= 910320100 && cm.getMapId() <= 910320304) {
            cm.sendYesNo("你想离开这里吗？");
            status = 99;
        } else {
            cm.sendSimple("我的名字是林车长。\r\n#b#e#L1#进入尘土飞扬的平台。#l#n\r\n#L2#前往 999 号列车。#l\r\n#L3#询问<荣誉乘务员>勋章。#l#k");
        }
    } else if (status == 2) {
        section = selection;
        if (selection == 1) {
            if (cm.getPlayer().getLevel() < 25 || cm.getPlayer().getLevel() > 30 || !cm.isLeader()) {
                cm.sendOk("你必须处于 25-30 级范围内，并且是队长。");
            } else {
                if (!cm.start_PyramidSubway(-1)) {
                    cm.sendOk("尘土飞扬的平台目前已满。");
                }
            }
        } else if (selection == 2) {
            if (cm.haveItem(4001321)) {
                if (cm.bonus_PyramidSubway(-1)) {
                    cm.gainItem(4001321, -1);
                } else {
                    cm.sendOk("999 号列车目前已满。");
                }
            } else {
                cm.sendOk("你没有登车证。");
            }
        } else if (selection == 3) {
            var record = cm.getQuestRecord(7662);
            var data = record.getCustomData();
            if (data == null) {
                record.setCustomData("0");
                data = record.getCustomData();
            }
            var mons = parseInt(data);
            if (mons < 10000) {
                cm.sendOk("请在月台消灭至少 10,000 只怪物后再来找我。\r\n击杀数：#r" + mons + "#k / 10000");
            } else {
                cm.sendOk("你已经足以被称为荣誉乘务员。请前往德烈处领取勋章。");
            }
        }
        cm.dispose();
    } else if (status == 100) {
        cm.warp(910320000, 0);
        cm.dispose();
    }
}
