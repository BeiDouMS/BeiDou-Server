var status = 0;
var section = 0;

//questid 29931, infoquest 7662

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
                cm.sendOk("请为1个杂项槽腾出空间。");
            } else {
                cm.gainItem(itemid, 1);
                cm.warp(910320000, 0);
            }
            cm.dispose();
        } else if (cm.getMapId() >= 910320100 && cm.getMapId() <= 910320304) {
            cm.sendYesNo("你想要离开这个地方吗？");
            status = 99;
        } else {
            cm.sendSimple("我的名字是林先生。\r\n#b#e#L1#进入尘土飞扬的平台。#l#n\r\n#L2#前往999号列车。#l\r\n#L3#获得<荣誉员工>勋章。#l#k");
        }
    } else if (status == 2) {
        section = selection;
        if (selection == 1) {
            if (cm.getPlayer().getLevel() < 25 || cm.getPlayer().getLevel() > 30 || !cm.isLeader()) {
                cm.sendOk("你必须处于25-30级的等级范围，并且是队伍的队长。");
            } else {
                if (!cm.start_PyramidSubway(-1)) {
                    cm.sendOk("尘土飞扬平台目前已满。");
                }
            }
            //todo
        } else if (selection == 2) {
            if (cm.haveItem(4001321)) {
                if (cm.bonus_PyramidSubway(-1)) {
                    cm.gainItem(4001321, -1);
                } else {
                    cm.sendOk("目前列车999已经满员了");
                }
            } else {
                cm.sendOk("您没有登机证。");
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
                cm.sendOk("请在站点击败至少10,000只怪物，然后再来找我。击杀数：mons");
            } else if (cm.canHold(1142141) && !cm.haveItem(1142141)) {
                cm.gainItem(1142141, 1);
                cm.startQuest(29931);
                cm.completeQuest(29931);
            } else {
                cm.sendOk("请腾出空间。");
            }
        }
        cm.dispose();
    } else if (status == 100) {
        cm.warp(910320000, 0);
        cm.dispose();
    }
}