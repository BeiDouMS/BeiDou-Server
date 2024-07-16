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
/* Mr. Smith
	Victoria Road: Perion (102000000)
	
	Refining NPC: 
	* 战士 Gloves - 10-60 + upgrades
	* Processed Wood/Screws
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
        var selStr = "嗯……你好 #b#h ##k ，我是 #p1022003# 先生的学徒。他年纪大了，所以他主要负责一些繁重的工作，而我处理一些较轻的任务。我能为你做些什么呢？#b"
        var options = ["制作手套", "升级手套", "制作材料"];
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }

        cm.sendSimple(selStr);
    } else if (status == 1 && mode == 1) {
        selectedType = selection;
        if (selectedType == 0) { //glove refine
            var selStr = "好的，那么你想让我制作哪种手套呢？#b";
            var items = ["#i1082003##t1082003##k - 战士 Lv. 10#b", "#i1082000##t1082000##k - 战士 Lv. 15#b", "#i1082004##t1082004##k - 战士 Lv. 20#b", "#i1082001##t1082001##k - 战士 Lv. 25#b",
                         "#i1082007##t1082007##k - 战士 Lv. 30#b", "#i1082008##t1082008##k - 战士 Lv. 35#b", "#i1082023##t1082023##k - 战士 Lv. 40#b", "#i1082009##t1082009##k - 战士 Lv. 50#b", "#i1082059##t1082059##k - 战士 Lv. 60#b"];
            for (var i = 0; i < items.length; i++) {
                selStr += "\r\n#L" + i + "# " + items[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = true;
        } else if (selectedType == 1) { //glove upgrade
            var selStr = "升级手套？那应该不太难。你具体想要升级哪种手套呢？#b";
            var crystals = ["#i1082005##t1082005##k - 战士 Lv. 30#b", "#i1082006##t1082006##k - 战士 Lv. 30#b", "#i1082035##t1082035##k - 战士 Lv. 35#b", "#i1082036##t1082036##k - 战士 Lv. 35#b",
                            "#i1082024##t1082024##k - 战士 Lv. 40#b", "#i1082025##t1082025##k - 战士 Lv. 40#b", "#i1082010##t1082010##k - 战士 Lv. 50#b", "#i1082011##t1082011##k - 战士 Lv. 50#b",
                            "#i1082060##t1082060##k - 战士 Lv. 60#b", "#i1082061##t1082061##k - 战士 Lv. 60#b"];
            for (var i = 0; i < crystals.length; i++) {
                selStr += "\r\n#L" + i + "# " + crystals[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = true;
        } else if (selectedType == 2) { //material refine
            var selStr = "材料？我知道一些可以为你制作的材料……#b";
            var materials = ["使用树枝制作加工木材","使用木块制作加工木材","制作螺丝（15个）"];
            for (var i = 0; i < materials.length; i++) {
                selStr += "\r\n#L" + i + "# " + materials[i] + "#l";
            }
            cm.sendSimple(selStr);
            equip = false;
        }
        if (equip) {
            status++;
        }
    } else if (status == 2 && mode == 1) {
        selectedItem = selection;
        if (selectedType == 2) { //material refine
            var itemSet = [4003001, 4003001, 4003000];
            var matSet = [4000003, 4000018, [4011000, 4011001]];
            var matQtySet = [10, 5, [1, 1]];
            var costSet = [0, 0, 0];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }

        var prompt = "那么，你想让我制作一些#t" + item + "#吗？你希望我制作多少？";

        cm.sendGetNumber(prompt, 1, 1, 100)
    } else if (status == 3 && mode == 1) {
        if (equip) {
            selectedItem = selection;
            qty = 1;
        } else {
            qty = (selection > 0) ? selection : (selection < 0 ? -selection : 1);
        }

        if (selectedType == 0) { //glove refine
            var itemSet = [1082003, 1082000, 1082004, 1082001, 1082007, 1082008, 1082023, 1082009, 1082059];
            var matSet = [[4000021, 4011001], 4011001, [4000021, 4011000], 4011001, [4011000, 4011001, 4003000], [4000021, 4011001, 4003000], [4000021, 4011001, 4003000],
                [4011001, 4021007, 4000030, 4003000], [4011007, 4011000, 4011006, 4000030, 4003000]];
            var matQtySet = [[15, 1], 2, [40, 2], 2, [3, 2, 15], [30, 4, 15], [50, 5, 40], [3, 2, 30, 45], [1, 8, 2, 50, 50]];
            var costSet = [1000, 2000, 5000, 10000, 20000, 30000, 40000, 50000, 70000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 1) { //glove upgrade
            var itemSet = [1082005, 1082006, 1082035, 1082036, 1082024, 1082025, 1082010, 1082011, 1082060, 1082061];
            var matSet = [[1082007, 4011001], [1082007, 4011005], [1082008, 4021006], [1082008, 4021008], [1082023, 4011003], [1082023, 4021008],
                [1082009, 4011002], [1082009, 4011006], [1082059, 4011002, 4021005], [1082059, 4021007, 4021008]];
            var matQtySet = [[1, 1], [1, 2], [1, 3], [1, 1], [1, 4], [1, 2], [1, 5], [1, 4], [1, 3, 5], [1, 2, 2]];
            var costSet = [20000, 25000, 30000, 40000, 45000, 50000, 55000, 60000, 70000, 80000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }

        var prompt = "你想让我制作多少 ";
        if (qty == 1) {
            prompt += "一个 #i" + item + "##t" + item + "#?";
        } else {
            prompt += qty + " #t" + item + "#?";
        }

        prompt += "在这种情况下，我需要从你这儿得到一些特定的物品来进行制作。不过，请确保你的背包有足够的空间！#b";

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
        var recvItem = item, recvQty;

        if (item == 4003000)//screws
        {
            recvQty = 15 * qty;
        } else {
            recvQty = qty;
        }

        if (!cm.canHold(recvItem, recvQty)) {
            cm.sendOk("首先检查你的物品栏是否有空位。");
            cm.dispose();
            return;
        } else if (cm.getMeso() < cost * qty) {
            cm.sendOk("我虽然还是一个学徒，但我还是需要谋生的啊。");
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
            cm.sendOk("我还是一个学徒，我不知道有什么东西可以代替这些材料... 你还是找特定的材料来吧。");
        } else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    cm.gainItem(mats[i], -matQty[i] * qty);
                }
            } else {
                cm.gainItem(mats, -matQty * qty);
            }

            if (cost > 0) {
                cm.gainMeso(-cost * qty);
            }

            cm.gainItem(recvItem, recvQty);
            cm.sendOk("很棒对吧？如果你有任何东西让我练习，再来找我。");
        }
        cm.dispose();
    }
}