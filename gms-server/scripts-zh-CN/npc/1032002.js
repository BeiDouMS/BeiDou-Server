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
/* Francois
	Victoria Island: Ellinia (101000000)
	
	Refining NPC: (magicians)
	* Gloves
	* Glove Upgrades
	* Hats
	* Wand
	* Staff
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;

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
        var selStr = "欢迎来到我的生态安全精炼工厂！今天你想要什么？#b"
        var options = ["制作手套", "升级手套", "升级帽子", "制作魔杖", "制作法杖"];
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }

        cm.sendSimple(selStr);
    } else if (status == 1 && mode == 1) {
        selectedType = selection;
        if (selectedType == 0) { //glove refine
            var selStr = "那么，你希望我做什么样的手套？#b";
            var items = ["#i1082019##t1082019##k - 魔法师 Lv. 15#b", "#i1082020##t1082020##k - 魔法师 Lv. 20#b", "#i1082026##t1082026##k - 魔法师 Lv. 25#b", "#i1082051##t1082051##k - 魔法师 Lv. 30#b", "#i1082054##t1082054##k - 魔法师 Lv. 35#b", "#i1082062##t1082062##k - 魔法师 Lv. 40#b",
                "#i1082081##t1082081##k - 魔法师 Lv. 50#b", "#i1082086##t1082086##k - 魔法师 Lv. 60#b"];
            for (var i = 0; i < items.length; i++) {
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 1) { //glove upgrade
            var selStr = "那么，你打算升级到什么样的手套呢？#b";
            var items = ["#i1082021##t1082021##k - 魔法师 Lv. 20#b", "#i1082022##t1082022##k - 魔法师 Lv. 20#b", "#i1082027##t1082027##k - 魔法师 Lv. 25#b", "#i1082028##t1082028##k - 魔法师 Lv. 25#b", "#i1082052##t1082052##k - 魔法师 Lv. 30#b", "#i1082053##t1082053##k - 魔法师 Lv. 30#b",
                "#i1082055##t1082055##k - 魔法师 Lv. 35#b", "#i1082056##t1082056##k - 魔法师 Lv. 35#b", "#i1082063##t1082063##k - 魔法师 Lv. 40#b", "#i1082064##t1082064##k - 魔法师 Lv. 40#b", "#i1082082##t1082082##k - 魔法师 Lv. 50#b", "#i1082080##t1082080##k - 魔法师 Lv. 50#b",
                "#i1082087##t1082087##k - 魔法师 Lv. 60#b", "#i1082088##t1082088##k - 魔法师 Lv. 60#b"];
            for (var i = 0; i < items.length; i++) {
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 2) { //hat upgrade
            var selStr = "一顶帽子？你在想哪顶？#b";
            var items = ["#i1002065##t1002065##k - 魔法师 Lv. 30#b", "#i1002013##t1002013##k - 魔法师 Lv. 30#b"];
            for (var i = 0; i < items.length; i++) {
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 3) { //wand refine
            var selStr = "魔杖，是吗？更喜欢口袋里的小武器？你想要哪种类型的武器？#b";
            var items = ["#i1372005##t1372005##k - 全职 Lv. 8#b", "#i1372006##t1372006##k - 全职 Lv. 13#b", "#i1372002##t1372002##k - 全职 Lv. 18#b", "#i1372004##t1372004##k - 魔法师 Lv. 23#b", "#i1372003##t1372003##k - 魔法师 Lv. 28#b",
                "#i1372001##t1372001##k - 魔法师 Lv. 33#b", "#i1372000##t1372000##k - 魔法师 Lv. 38#b", "#i1372007##t1372007##k - 魔法师 Lv. 48#b"];
            for (var i = 0; i < items.length; i++) {
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 4) { //staff refine
            var selStr = "啊，一把法杖，一个人权力的伟大象征！你想做哪一把呢？#b";
            var items = ["#i1382000##t1382000##k - 魔法师 Lv. 10#b", "#i1382003##t1382003##k - 魔法师 Lv. 15#b", "#i1382005##t1382005##k - 魔法师 Lv. 15#b", "#i1382004##t1382004##k - 魔法师 Lv. 20#b", "#i1382002##t1382002##k - 魔法师 Lv. 25#b",
                "#i1382001##t1382001##k - 魔法师 Lv. 45#b"];
            for (var i = 0; i < items.length; i++) {
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
        }
    } else if (status == 2 && mode == 1) {
        selectedItem = selection;

        if (selectedType == 0) { //glove refine
            var itemSet = [1082019, 1082020, 1082026, 1082051, 1082054, 1082062, 1082081, 1082086];
            var matSet = [4000021, [4000021, 4011001], [4000021, 4011006], [4000021, 4021006, 4021000], [4000021, 4011006, 4011001, 4021000],
                [4000021, 4021000, 4021006, 4003000], [4021000, 4011006, 4000030, 4003000], [4011007, 4011001, 4021007, 4000030, 4003000]];
            var matQtySet = [15, [30, 1], [50, 2], [60, 1, 2], [70, 1, 3, 2], [80, 3, 3, 30], [3, 2, 35, 40], [1, 8, 1, 50, 50]];
            var costSet = [7000, 15000, 20000, 25000, 30000, 40000, 50000, 70000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 1) { //glove upgrade
            var itemSet = [1082021, 1082022, 1082027, 1082028, 1082052, 1082053, 1082055, 1082056, 1082063, 1082064, 1082082, 1082080, 1082087, 1082088];
            var matSet = [[1082020, 4011001], [1082020, 4021001], [1082026, 4021000], [1082026, 4021008], [1082051, 4021005],
                [1082051, 4021008], [1082054, 4021005], [1082054, 4021008], [1082062, 4021002], [1082062, 4021008],
                [1082081, 4021002], [1082081, 4021008], [1082086, 4011004, 4011006], [1082086, 4021008, 4011006]];
            var matQtySet = [[1, 1], [1, 2], [1, 3], [1, 1], [1, 3], [1, 1], [1, 3], [1, 1], [1, 4],
                [1, 2], [1, 5], [1, 3], [1, 3, 5], [1, 2, 3]];
            var costSet = [20000, 25000, 30000, 40000, 35000, 40000, 40000, 45000, 45000, 50000, 55000, 60000, 70000, 80000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 2) { //hat upgrade
            var itemSet = [1002065, 1002013];
            var matSet = [[1002064, 4011001], [1002064, 4011006]];
            var matQtySet = [[1, 3], [1, 3]];
            var costSet = [40000, 50000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 3) { //wand refine
            var itemSet = [1372005, 1372006, 1372002, 1372004, 1372003, 1372001, 1372000, 1372007];
            var matSet = [4003001, [4003001, 4000001], [4011001, 4000009, 4003000], [4011002, 4003002, 4003000], [4011002, 4021002, 4003000],
                [4021006, 4011002, 4011001, 4003000], [4021006, 4021005, 4021007, 4003003, 4003000], [4011006, 4021003, 4021007, 4021002, 4003002, 4003000]];
            var matQtySet = [5, [10, 50], [1, 30, 5], [2, 1, 10], [3, 1, 10], [5, 3, 1, 15], [5, 5, 1, 1, 20], [4, 3, 2, 1, 1, 30]];
            var costSet = [1000, 3000, 5000, 12000, 30000, 60000, 120000, 200000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 4) { //staff refine
            var itemSet = [1382000, 1382003, 1382005, 1382004, 1382002, 1382001];
            var matSet = [4003001, [4021005, 4011001, 4003000], [4021003, 4011001, 4003000], [4003001, 4011001, 4003000],
                [4021006, 4021001, 4011001, 4003000], [4011001, 4021006, 4021001, 4021005, 4003000, 4000010, 4003003]];
            var matQtySet = [5, [1, 1, 5], [1, 1, 5], [50, 1, 10], [2, 1, 1, 15], [8, 5, 5, 5, 30, 50, 1]];
            var costSet = [2000, 2000, 2000, 5000, 12000, 180000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }

        var prompt = "你想让我做一个 #t" + item + "#? 如果是那样的话，我需要你提供一些特定的物品才能完成任务。不过，请确保你的背包中有足够的空间！#b";

        if (mats instanceof Array) {
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i" + mats[i] + "# " + matQty[i] + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i" + mats + "# " + matQty + " #t" + mats + "#";
        }

        if (cost > 0) {
            prompt += "\r\n#i4031138# " + cost + " 金币";
        }

        cm.sendYesNo(prompt);
    } else if (status == 3 && mode == 1) {
        var complete = true;

        if (!cm.canHold(item, 1)) {
            cm.sendOk("首先检查你的物品栏是否有空位。");
            cm.dispose();
            return;
        } else if (cm.getMeso() < cost) {
            cm.sendOk("对不起，但我们都需要钱来生活，等你能付我学费的时候再来，好吗？")
            cm.dispose();
            return;
        } else {
            if (mats instanceof Array) {
                for (var i = 0; complete && i < mats.length; i++) {
                    if (!cm.haveItem(mats[i], matQty[i])) {
                        complete = false;
                    }
                }
            } else if (!cm.haveItem(mats, matQty)) {
                complete = false;
            }
        }

        if (!complete) {
            cm.sendOk("嗯...我身上没有额外的材料。抱歉。");
        } else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    cm.gainItem(mats[i], -matQty[i]);
                }
            } else {
                cm.gainItem(mats, -matQty);
            }

            if (cost > 0) {
                cm.gainMeso(-cost);
            }

            cm.gainItem(item, 1);
            cm.sendOk("成功了！哦，我从来没有感到如此活力四射！请再回来！");
        }
        cm.dispose();
    }
}