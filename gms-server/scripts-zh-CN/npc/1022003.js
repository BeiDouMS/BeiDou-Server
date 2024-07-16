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
/* Mr. Thunder
        Victoria Road: Perion (102000000)
        
        Refining NPC: 
        * Minerals
        * Jewels
        * Shields
        * Helmets
*/
var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;
var qty;
var equip;

function start() {
    cm.getPlayer().setCS(true);
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.dispose();
    }
    if (status == 0 && mode == 1) {
        var selStr = "嗯？你是谁？哦，你听说过我的锻造技术？如果是这样的话，我会很乐意帮你加工一些矿石……不过需要收费。#b"
        var options = ["提炼矿石", "提炼宝石", "升级头盔", "升级盾牌"];
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }

        cm.sendSimple(selStr);
    } else if (status == 1 && mode == 1) {
        selectedType = selection;
        if (selectedType == 0) { //mineral refine
            var selStr = "那么，你想要提炼哪种矿石？#b";
            var minerals = ["#i4011000##t4011000#", "#i4011001##t4011001#", "#i4011002##t4011002#", "#i4011003##t4011003#", "#i4011004##t4011004#", "#i4011005##t4011005#", "#i4011006##t4011006#"];
            for (var i = 0; i < minerals.length; i++) {
                selStr += "\r\n#L" + i + "# " + minerals[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        } else if (selectedType == 1) { //jewel refine
            var selStr = "那么，你想要提炼哪种宝石？#b";
            var jewels = ["#i4021000##t4021000#", "#i4021001##t4021001#", "#i4021002##t4021002#", "#i4021003##t4021003#", "#i4021004##t4021004#", "#i4021005##t4021005#", "#i4021006##t4021006#", "#i4021007##t4021007#", "#i4021008##t4021008#"];
            for (var i = 0; i < jewels.length; i++) {
                selStr += "\r\n#L" + i + "# " + jewels[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        } else if (selectedType == 2) { //helmet refine
            var selStr = "啊，你想要升级头盔？那么告诉我，哪一个？#b";
            var helmets = ["#i1002042##t1002042##k - 全职 Lv. 15#b", "#i1002041##t1002041##k - 全职 Lv. 15#b", "#i1002002##t1002002##k - 战士 Lv. 10#b", "#i1002044##t1002044##k - 战士 Lv. 10#b", "#i1002003##t1002003##k - 战士 Lv. 12#b",
                           "#i1002040##t1002040##k - 战士 Lv. 12#b", "#i1002007##t1002007##k - 战士 Lv. 15#b", "#i1002052##t1002052##k - 战士 Lv. 15#b", "#i1002011##t1002011##k - 战士 Lv. 20#b", "#i1002058##t1002058##k - 战士 Lv. 20#b",
                           "#i1002009##t1002009##k - 战士 Lv. 20#b", "#i1002056##t1002056##k - 战士 Lv. 20#b", "#i1002087##t1002087##k - 战士 Lv. 22#b", "#i1002088##t1002088##k - 战士 Lv. 22#b", "#i1002050##t1002050##k - 战士 Lv. 25#b",
                           "#i1002049##t1002049##k - 战士 Lv. 25#b", "#i1002047##t1002047##k - 战士 Lv. 35#b", "#i1002048##t1002048##k - 战士 Lv. 35#b", "#i1002099##t1002099##k - 战士 Lv. 40#b", "#i1002098##t1002098##k - 战士 Lv. 40#b",
                           "#i1002085##t1002085##k - 战士 Lv. 50#b", "#i1002028##t1002028##k - 战士 Lv. 50#b", "#i1002022##t1002022##k - 战士 Lv. 55#b", "#i1002101##t1002101##k - 战士 Lv. 55#b"];
            for (var i = 0; i < helmets.length; i++) {
                selStr += "\r\n#L" + i + "# " + helmets[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = true;
        } else if (selectedType == 3) { //shield refine
            var selStr = "啊，你想要升级盾牌？那么告诉我，哪一个？#b";
            var shields = ["#i1092014##t1092014##k - 战士 Lv. 40#b", "#i1092013##t1092013##k - 战士 Lv. 40#b", "#i1092010##t1092010##k - 战士 Lv. 60#b", "#i1092011##t1092011##k - 战士 Lv. 60#b"];
            for (var i = 0; i < shields.length; i++) {
                selStr += "\r\n#L" + i + "# " + shields[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = true;
        }
        if (equip) {
            status++;
        }
    } else if (status == 2 && mode == 1) {
        selectedItem = selection;
        if (selectedType == 0) { //mineral refine
            var itemSet = [4011000, 4011001, 4011002, 4011003, 4011004, 4011005, 4011006];
            var matSet = [4010000, 4010001, 4010002, 4010003, 4010004, 4010005, 4010006];
            var matQtySet = [10, 10, 10, 10, 10, 10, 10];
            var costSet = [300, 300, 300, 500, 500, 500, 800];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 1) { //jewel refine
            var itemSet = [4021000, 4021001, 4021002, 4021003, 4021004, 4021005, 4021006, 4021007, 4021008];
            var matSet = [4020000, 4020001, 4020002, 4020003, 4020004, 4020005, 4020006, 4020007, 4020008];
            var matQtySet = [10, 10, 10, 10, 10, 10, 10, 10, 10];
            var costSet = [500, 500, 500, 500, 500, 500, 500, 1000, 3000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }

        var prompt = "你想要我制作一些 #i" + item + "##t" + item + "#？你想要我制作多少个？";

        cm.sendGetNumber(prompt, 1, 1, 100)
    } else if (status == 3 && mode == 1) {
        if (equip) {
            selectedItem = selection;
            qty = 1;
        } else {
            qty = (selection > 0) ? selection : (selection < 0 ? -selection : 1);
        }

        if (selectedType == 2) { //helmet refine
            var itemSet = [1002042, 1002041, 1002002, 1002044, 1002003, 1002040, 1002007, 1002052, 1002011, 1002058, 1002009, 1002056, 1002087, 1002088, 1002050, 1002049, 1002047, 1002048, 1002099, 1002098, 1002085, 1002028, 1002022, 1002101];
            var matSet = [[1002001, 4011002], [1002001, 4021006], [1002043, 4011001], [1002043, 4011002], [1002039, 4011001], [1002039, 4011002], [1002051, 4011001], [1002051, 4011002], [1002059, 4011001], [1002059, 4011002],
                [1002055, 4011001], [1002055, 4011002], [1002027, 4011002], [1002027, 4011006], [1002005, 4011005], [1002005, 4011006], [1002004, 4021000], [1002004, 4021005], [1002021, 4011002], [1002021, 4011006], [1002086, 4011002],
                [1002086, 4011004], [1002100, 4011007, 4011001], [1002100, 4011007, 4011002]];
            var matQtySet = [[1, 1], [1, 1], [1, 1], [1, 1], [1, 1], [1, 1], [1, 2], [1, 2], [1, 3], [1, 3], [1, 3], [1, 3], [1, 4], [1, 4], [1, 5], [1, 5], [1, 3], [1, 3],
                [1, 5], [1, 6], [1, 5], [1, 4], [1, 1, 7], [1, 1, 7]];
            var costSet = [500, 300, 500, 800, 500, 800, 1000, 1500, 1500, 2000, 1500, 2000, 2000, 4000, 4000, 5000, 8000, 10000, 12000, 15000, 20000, 25000, 30000, 30000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 3) { //shield refine
            var itemSet = [1092014, 1092013, 1092010, 1092011];
            var matSet = [[1092012, 4011003], [1092012, 4011002], [1092009, 4011007, 4011004], [1092009, 4011007, 4011003]];
            var matQtySet = [[1, 10], [1, 10], [1, 1, 15], [1, 1, 15]];
            var costSet = [100000, 100000, 120000, 120000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }
        var prompt = "你想让我制作";
        if (qty == 1) {
            prompt += "一个 #i" + item + "##t" + item + "#?";
        } else {
            prompt += qty + " #t" + item + "#?";
        }
        prompt += "我需要从你那里获取一些特定的材料才能制作。确保你的库存有足够的空间！#b";
        if (mats instanceof Array) {
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i" + mats[i] + "# " + matQty[i] * qty + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i" + mats + "# " + matQty * qty + " #t" + mats + "#";
        }
        if (cost > 0) {
            prompt += "\r\n#i4031138# " + cost * qty + " meso";
        }
        cm.sendYesNo(prompt);
    } else if (status == 4 && mode == 1) {
        var complete = true;

        if (!cm.canHold(item, qty)) {
            cm.sendOk("首先检查你的物品栏是否有空位。");
            cm.dispose();
            return;
        } else if (cm.getMeso() < cost * qty) {
            cm.sendOk("恐怕你支付不起我的服务费。");
            cm.dispose();
            return;
        } else {
            if (mats instanceof Array) {
                for (var i = 0; complete && i < mats.length; i++) {
                    if (!cm.haveItem(mats[i], matQty[i] * qty)) {
                        complete = false;
                    }
                }
            } else if (!cm.haveItem(mats, matQty * qty)) {
                complete = false;
            }
        }
        if (!complete) {
            cm.sendOk("我觉得你还缺少一些材料。准备好再来找我，好吗？");
        } else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    cm.gainItem(mats[i], -matQty[i] * qty);
                }
            } else {
                cm.gainItem(mats, -matQty * qty);
            }
            cm.gainMeso(-cost * qty);
            cm.gainItem(item, qty);
            cm.sendOk("好了，完成了。你觉得怎么样，是不是一件艺术品？嗯，如果你需要其他东西，请再来找我。");
        }
        cm.dispose();
    }
}