/*
    This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* Sarah
    Ludibrium : Tara and Sarah's House (220000303)

    Refining NPC:
    * Gloves - All classes, 30-50, stimulator (4130000) available on upgrades
    * Price is 90% of locations on same items
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var stimulator = false;
var stimID = 4130000;
var levelList = [];

function start() {
    cm.getPlayer().setCS(true);

    var selStr = "你好，欢迎来到吉乐肯手套店。今天有什么可以帮您的吗？#b";
    var options = ["什么是辅助剂？", "制作战士手套", "制作弓箭手手套", "制作魔法师手套", "制作飞侠手套",
        "制作使用辅助剂的战士手套", "制作使用辅助剂的弓手手套", "制作使用辅助剂的法师手套", "制作使用辅助剂的飞侠手套"];
    for (var i = 0; i < options.length; i++) {
        selStr += "\r\n#L" + i + "# " + options[i] + "#l";
    }
    cm.sendSimple(selStr);
}

function action(mode, type, selection) {
    if (mode > 0) {
        status++;
    } else {
        cm.dispose();
        return;
    }

    if (status == 1) {
        selectedType = selection;
        stimulator = selectedType > 4;

        if (selectedType == 0) {
            cm.sendNext("辅助剂是一种特殊的药水，可以在制作物品时增加随机属性。使用辅助剂有可能获得高属性物品，也有可能物品属性不佳，甚至10%的几率物品制作失败。请谨慎选择。");
            cm.dispose();
            return;
        }

        var selStr = "";
        var list = [];
        switch (selectedType) {
            case 1:
                selStr = "请选择要制作的战士手套，选择一种：#b";
                list = [1082007, 1082008, 1082023, 1082009];
                levelList = [30, 35, 40, 50];
                break;
            case 2:
                selStr = "请选择要制作的弓箭手手套，选择一种：#b";
                list = [1082048, 1082068, 1082071, 1082084];
                levelList = [30, 35, 40, 50];
                break;
            case 3:
                selStr = "请选择要制作的法师手套，选择一种：#b";
                list = [1082051, 1082054, 1082062, 1082081];
                levelList = [30, 35, 40, 50];
                break;
            case 4:
                selStr = "请选择要制作的飞侠手套，选择一种：#b";
                list = [1082042, 1082046, 1082075, 1082065];
                levelList = [30, 35, 40, 50];
                break;
            case 5:
                selStr = "使用辅助剂的战士手套：#b";
                list = [1082005, 1082006, 1082035, 1082036, 1082024, 1082025, 1082010, 1082011];
                levelList = [30, 30, 35, 35, 40, 40, 50, 50];
                break;
            case 6:
                selStr = "使用辅助剂的弓手手套：#b";
                list = [1082049, 1082050, 1082069, 1082070, 1082072, 1082073, 1082085, 1082083];
                levelList = [30, 30, 35, 35, 40, 40, 50, 50];
                break;
            case 7:
                selStr = "使用辅助剂的魔法手套：#b";
                list = [1082052, 1082053, 1082055, 1082056, 1082063, 1082064, 1082082, 1082080];
                levelList = [30, 30, 35, 35, 40, 40, 50, 50];
                break;
            case 8:
                selStr = "使用辅助剂的飞侠手套：#b";
                list = [1082043, 1082044, 1082047, 1082045, 1082076, 1082074, 1082067, 1082066];
                levelList = [30, 30, 35, 35, 40, 40, 50, 50];
                break;
        }

        for (var i = 0; i < list.length; i++) {
            selStr += "\r\n#L" + i + ":# #t" + list[i] + ":# LV-" + levelList[i] + "#l";
        }
        cm.sendSimple(selStr);
    } else if (status == 2) {
        selectedItem = selection;

        // 根据选项设置材料
        var itemSet, matSet, matQtySet, costSet;
        if (selectedType == 1) { // 战士手套
            itemSet = [1082007, 1082008, 1082023, 1082009];
            matSet = [[4011000, 4011001, 4003000], [4000021, 4011001, 4003000], [4000021, 4011001, 4003000], [4011001, 4021007, 4000030, 4003000]];
            matQtySet = [[3, 2, 15], [30, 4, 15], [50, 5, 40], [3, 2, 30, 45]];
            costSet = [18000, 27000, 36000, 45000];
        } else if (selectedType == 2) { // 弓手手套
            itemSet = [1082048, 1082068, 1082071, 1082084];
            matSet = [[4000021, 4011006, 4021001], [4011000, 4011001, 4000021, 4003000], [4011001, 4021000, 4021002, 4000021, 4003000], [4011004, 4011006, 4021002, 4000030, 4003000]];
            matQtySet = [[50, 2, 1], [1, 3, 60, 15], [3, 1, 3, 80, 25], [3, 1, 2, 40, 35]];
            costSet = [18000, 27000, 36000, 45000];
        } else if (selectedType == 3) { // 魔法师手套
            itemSet = [1082051, 1082054, 1082062, 1082081];
            matSet = [[4000021, 4021006, 4021000], [4000021, 4011006, 4011001, 4021000], [4000021, 4021000, 4021006, 4003000], [4021000, 4011006, 4000030, 4003000]];
            matQtySet = [[60, 1, 2], [70, 1, 3, 2], [80, 3, 3, 30], [3, 2, 35, 40]];
            costSet = [22500, 27000, 36000, 45000];
        } else if (selectedType == 4) { // 飞侠手套
            itemSet = [1082042, 1082046, 1082075, 1082065];
            matSet = [[4011001, 4000021, 4003000], [4011001, 4011000, 4000021, 4003000], [4021000, 4000101, 4000021, 4003000], [4021005, 4021008, 4000030, 4003000]];
            matQtySet = [[2, 50, 10], [3, 1, 60, 15], [3, 100, 80, 30], [3, 1, 40, 30]];
            costSet = [22500, 27000, 36000, 45000];
        } else if (selectedType == 5) { // 战士手套+辅助剂
            itemSet = [1082005, 1082006, 1082035, 1082036, 1082024, 1082025, 1082010, 1082011];
            matSet = [[1082007, 4011001], [1082007, 4011005], [1082008, 4021006], [1082008, 4021008], [1082023, 4011003], [1082023, 4021008], [1082009, 4011002], [1082009, 4011006]];
            matQtySet = [[1, 1], [1, 2], [1, 3], [1, 1], [1, 4], [1, 2], [1, 5], [1, 4]];
            costSet = [18000, 22500, 27000, 36000, 40500, 45000, 49500, 54000];
        } else if (selectedType == 6) { // 弓手手套+辅助剂
            itemSet = [1082049, 1082050, 1082069, 1082070, 1082072, 1082073, 1082085, 1082083];
            matSet = [[1082048, 4021003], [1082048, 4021008], [1082068, 4011002], [1082068, 4011006], [1082071, 4011006], [1082071, 4021008], [1082084, 4011000, 4021000], [1082084, 4011006, 4021008]];
            matQtySet = [[1, 3], [1, 1], [1, 4], [1, 2], [1, 4], [1, 2], [1, 1, 5], [1, 2, 2]];
            costSet = [13500, 18000, 19800, 22500, 27000, 36000, 49500, 54000];
        } else if (selectedType == 7) { // 法师手套+辅助剂
            itemSet = [1082052, 1082053, 1082055, 1082056, 1082063, 1082064, 1082082, 1082080];
            matSet = [[1082051, 4021005], [1082051, 4021008], [1082054, 4021005], [1082054, 4021008], [1082062, 4021002], [1082062, 4021008], [1082081, 4021002], [1082081, 4021008]];
            matQtySet = [[1, 3], [1, 1], [1, 3], [1, 1], [1, 4], [1, 2], [1, 5], [1, 3]];
            costSet = [31500, 36000, 36000, 40500, 40500, 45000, 49500, 54000];
        } else if (selectedType == 8) { // 飞侠手套+辅助剂
            itemSet = [1082043, 1082044, 1082047, 1082045, 1082076, 1082074, 1082067, 1082066];
            matSet = [[1082042, 4011004], [1082042, 4011006], [1082046, 4011005], [1082046, 4011006], [1082075, 4011006], [1082075, 4021008], [1082065, 4021000], [1082065, 4011006, 4021008]];
            matQtySet = [[1, 2], [1, 1], [1, 3], [1, 2], [1, 4], [1, 2], [1, 5], [1, 2, 1]];
            costSet = [13500, 18000, 19800, 22500, 36000, 45000, 49500, 54000];
        }

        item = itemSet[selectedItem];
        mats = matSet[selectedItem];
        matQty = matQtySet[selectedItem];
        cost = costSet[selectedItem];

        var prompt = "你想制作 #i" + item + ":##t" + item + "# LV-" + levelList[selectedItem] + " 吗？请准备以下材料：\r\n#b";

        if (stimulator) prompt += "\r\n#i" + stimID + "# 1 #t" + stimID + "#";
        if (mats instanceof Array) {
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i" + mats[i] + "# " + matQty[i] + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i" + mats + "# " + matQty + " #t" + mats + "#";
        }
        if (cost > 0) prompt += "\r\n#i4031138# " + cost + " 金币";

        cm.sendYesNo(prompt);
    } else if (status == 3) {
        var complete = true;

        if (!cm.canHold(item, 1)) {
            cm.sendOk("请确保你的物品栏有空位。");
            cm.dispose();
            return;
        }

        if (cm.getMeso() < cost) {
            cm.sendOk("抱歉，你的金币不足。");
            cm.dispose();
            return;
        }

        if (mats instanceof Array) {
            for (var i = 0; complete && i < mats.length; i++) {
                if (!cm.haveItem(mats[i], matQty[i])) complete = false;
            }
        } else if (!cm.haveItem(mats, matQty)) complete = false;

        if (stimulator && !cm.haveItem(stimID)) complete = false;

        if (!complete) {
            cm.sendOk("抱歉，你缺少制作此物品所需的材料。");
        } else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) cm.gainItem(mats[i], -matQty[i]);
            } else cm.gainItem(mats, -matQty);

            cm.gainMeso(-cost);

            if (stimulator) {
                cm.gainItem(stimID, -1);
                var deleted = Math.floor(Math.random() * 10);
                if (deleted != 0) {
                    cm.gainItem(item, 1);
                    cm.sendOk("手套已经准备好了，请小心，它还很烫！");
                } else {
                    cm.sendOk("哎呀！辅助剂使用过多，物品制作失败……抱歉，不予退款。");
                }
            } else {
                cm.gainItem(item, 1);
                cm.sendOk("手套已经准备好了，请小心，它还很烫！");
            }
        }

        cm.dispose();
    }
}
