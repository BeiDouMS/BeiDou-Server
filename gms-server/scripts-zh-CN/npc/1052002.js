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
/* JM from tha Streetz
        Victoria Road: Kerning City (103000000)
        
        Refining NPC: 
        * Gloves
        * Glove Upgrade
        * Claw
        * Claw Upgrade
        * Processed Wood/Screws

        * Note: JM by default is used as a Megaphone shop. To move this shop to Frederick in the FM,
        * following MySQL command:
        * UPDATE `shops` SET `npcid`='9030000' WHERE (`shopid`='0')
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
        var selStr = "嘘……如果你有合适的货物，我可以把它变成一些好东西……#b"
        var options = ["制作手套"，"升级手套"，"制作拳套"，"升级拳套"，"制作材料"];
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }
        cm.sendSimple(selStr);
    } else if (status == 1 && mode == 1) {
        selectedType = selection;
        if (selectedType == 0) { //glove refine
            var selStr = "那么，你希望我制作哪种手套呢？?#b";
            var gloves = ["工地手套#k - 全职 Lv. 10#b", "Brown Duo#k - Thief Lv. 15#b", "Blue Duo#k - Thief Lv. 15#b", "Black Duo#k - Thief Lv. 15#b", "Bronze Mischief#k - Thief Lv. 20#b", "Bronze Wolfskin#k - Thief Lv. 25#b", "Steel Sylvia#k - Thief Lv. 30#b",
                "Steel Arbion#k - Thief Lv. 35#b", "Red Cleave#k - Thief Lv. 40#b", "Blue Moon Glove#k - Thief Lv. 50#b", "Bronze Pow#k - Thief Lv. 60#b"];
            for (var i = 0; i < gloves.length; i++) {
                selStr += "\r\n#L" + i + "# " + gloves[i] + "#l";
            }
            equip = true;
            cm.sendSimple(selStr);
        } else if (selectedType == 1) { //glove upgrade
            var selStr = "升级手套？没问题，但请注意，升级不会转移到新物品上...#b";
            var gloves = ["Mithril Mischief#k - Thief Lv. 20#b", "Dark Mischief#k - Thief Lv. 20#b", "Mithril Wolfskin#k - Thief Lv. 25#b",
                "Dark Wolfskin#k - Thief Lv. 25#b", "Silver Sylvia#k - Thief Lv. 30#b", "Gold Sylvia#k - Thief Lv. 30#b", "Orihalcon Arbion#k - Thief Lv. 35#b", "Gold Arbion#k - Thief Lv. 35#b", "Gold Cleave#k - Thief Lv. 40#b",
                "Dark Cleave#k - Thief Lv. 40#b", "Red Moon Glove#k - Thief Lv. 50#b", "Brown Moon Glove#k - Thief Lv. 50#b", "Silver Pow#k - Thief Lv. 60#b", "Gold Pow#k - Thief Lv. 60#b"];
            for (var i = 0; i < gloves.length; i++) {
                selStr += "\r\n#L" + i + "# " + gloves[i] + "#l";
            }
            equip = true;
            cm.sendSimple(selStr);
        } else if (selectedType == 2) { //claw refine
            var selStr = "那么，你想让我制作哪种拳套呢？#b";
            var claws = ["Steel Titans#k - Thief Lv. 15#b", "Bronze Igor#k - Thief Lv. 20#b", "Meba#k - Thief Lv. 25#b", "Steel Guards#k - Thief Lv. 30#b", "Bronze Guardian#k - Thief Lv. 35#b", "Steel Avarice#k - Thief Lv. 40#b", "Steel Slain#k - Thief Lv. 50#b"];
            for (var i = 0; i < claws.length; i++) {
                selStr += "\r\n#L" + i + "# " + claws[i] + "#l";
            }
            equip = true;
            cm.sendSimple(selStr);
        } else if (selectedType == 3) { //claw upgrade
            var selStr = "升级拳套？当然可以，但请注意，升级不会转移到新物品上……#b";
            var claws = ["Mithril Titans#k - Thief Lv. 15#b", "Gold Titans#k - Thief Lv. 15#b", "Steel Igor#k - Thief Lv. 20#b", "Adamantium Igor#k - Thief Lv. 20#b", "Mithril Guards#k - Thief Lv. 30#b", "Adamantium Guards#k - Thief Lv. 30#b",
                "Silver Guardian#k - Thief Lv. 35#b", "Dark Guardian#k - Thief Lv. 35#b", "Blood Avarice#k - Thief Lv. 40#b", "Adamantium Avarice#k - Thief Lv. 40#b", "Dark Avarice#k - Thief Lv. 40#b", "Blood Slain#k - Thief Lv. 50#b", "Sapphire Slain#k - Thief Lv. 50#b"];
            for (var i = 0; i < claws.length; i++) {
                selStr += "\r\n#L" + i + "# " + claws[i] + "#l";
            }
            equip = true;
            cm.sendSimple(selStr);
        } else if (selectedType == 4) { //material refine
            var selStr = "材料吗？我有几种材料可以为你制作……#b";
            var materials = ["使用树枝制作加工木材，使用木块制作加工木材，制作螺丝（15个一包）。"];
            for (var i = 0; i < materials.length; i++) {
                selStr += "\r\n#L" + i + "# " + materials[i] + "#l";
            }
            equip = false;
            cm.sendSimple(selStr);
        }
        if (equip) {
            status++;
        }
    } else if (status == 2 && mode == 1) {
        selectedItem = selection;
        if (selectedType == 4) { //material refine
            var itemSet = [4003001, 4003001, 4003000];
            var matSet = [4000003, 4000018, [4011000, 4011001]];
            var matQtySet = [10, 5, [1, 1]];
            var costSet = [0, 0, 0];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }

        var prompt = "那么，你想让我制作一些#t" + item + "#s吗？你希望我制作多少？";

        cm.sendGetNumber(prompt, 1, 1, 100)
    } else if (status == 3 && mode == 1) {
        if (equip) {
            selectedItem = selection;
            qty = 1;
        } else {
            qty = (selection > 0) ? selection : (selection < 0 ? -selection : 1);
        }

        if (selectedType == 0) { //glove refine
            var itemSet = [1082002, 1082029, 1082030, 1082031, 1082032, 1082037, 1082042, 1082046, 1082075, 1082065, 1082092];
            var matSet = [4000021, [4000021, 4000018], [4000021, 4000015], [4000021, 4000020], [4011000, 4000021], [4011000, 4011001, 4000021], [4011001, 4000021, 4003000], [4011001, 4011000, 4000021, 4003000], [4021000, 4000014, 4000021, 4003000], [4021005, 4021008, 4000030, 4003000], [4011007, 4011000, 4021007, 4000030, 4003000]];
            var matQtySet = [15, [30, 20], [30, 20], [30, 20], [2, 40], [2, 1, 10], [2, 50, 10], [3, 1, 60, 15], [3, 200, 80, 30], [3, 1, 40, 30], [1, 8, 1, 50, 50]];
            var costSet = [1000, 7000, 7000, 7000, 10000, 15000, 25000, 30000, 40000, 50000, 70000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 1) { //glove upgrade
            var itemSet = [1082033, 1082034, 1082038, 1082039, 1082043, 1082044, 1082047, 1082045, 1082076, 1082074, 1082067, 1082066, 1082093, 1082094];
            var matSet = [[1082032, 4011002], [1082032, 4021004], [1082037, 4011002], [1082037, 4021004], [1082042, 4011004], [1082042, 4011006], [1082046, 4011005], [1082046, 4011006], [1082075, 4011006], [1082075, 4021008], [1082065, 4021000], [1082065, 4011006, 4021008], [1082092, 4011001, 4000014], [1082092, 4011006, 4000027]];
            var matQtySet = [[1, 1], [1, 1], [1, 2], [1, 2], [1, 2], [1, 1], [1, 3], [1, 2], [1, 4], [1, 2], [1, 5], [1, 2, 1], [1, 7, 200], [1, 7, 150]];
            var costSet = [5000, 7000, 10000, 12000, 15000, 20000, 22000, 25000, 40000, 50000, 55000, 60000, 70000, 80000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 2) { //claw refine
            var itemSet = [1472001, 1472004, 1472007, 1472008, 1472011, 1472014, 1472018];
            var matSet = [[4011001, 4000021, 4003000], [4011000, 4011001, 4000021, 4003000], [1472000, 4011001, 4000021, 4003001], [4011000, 4011001, 4000021, 4003000], [4011000, 4011001, 4000021, 4003000], [4011000, 4011001, 4000021, 4003000], [4011000, 4011001, 4000030, 4003000]];
            var matQtySet = [[1, 20, 5], [2, 1, 30, 10], [1, 3, 20, 30], [3, 2, 50, 20], [4, 2, 80, 25], [3, 2, 100, 30], [4, 2, 40, 35]];
            var costSet = [2000, 3000, 5000, 15000, 30000, 40000, 50000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 3) { //claw upgrade
            var itemSet = [1472002, 1472003, 1472005, 1472006, 1472009, 1472010, 1472012, 1472013, 1472015, 1472016, 1472017, 1472019, 1472020];
            var matSet = [[1472001, 4011002], [1472001, 4011006], [1472004, 4011001], [1472004, 4011003], [1472008, 4011002], [1472008, 4011003], [1472011, 4011004], [1472011, 4021008], [1472014, 4021000], [1472014, 4011003], [1472014, 4021008], [1472018, 4021000], [1472018, 4021005]];
            var matQtySet = [[1, 1], [1, 1], [1, 2], [1, 2], [1, 3], [1, 3], [1, 4], [1, 1], [1, 5], [1, 5], [1, 2], [1, 6], [1, 6]];
            var costSet = [1000, 2000, 3000, 5000, 10000, 15000, 20000, 25000, 30000, 30000, 35000, 40000, 40000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }

        var prompt = "你想让我制作多少 ";
        if (qty == 1) {
            prompt += "a #t" + item + "#?";
        } else {
            prompt += qty + " #t" + item + "#?";
        }

        prompt += " 在这种情况下，我需要从你这儿得到一些特定的物品来进行制作。不过，请确保你的背包有足够的空间！#b";

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
            cm.sendOk("请先检查你的背包，找一个空闲的格子。");
            cm.dispose();
            return;
        } else if (cm.getMeso() < cost * qty) {
            cm.sendOk("恐怕你负担不起我的服务费用。");
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
            cm.sendOk("你在打什么主意？想白嫖吗？不给我材料，我什么也做不了。");
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
            cm.sendOk("都搞定了。如果你还需要什么，可以随时过来找我，反正我哪也不去。");
        }
        cm.dispose();
    }
}