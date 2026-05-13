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

/* Gordon
    El Nath: El Nath Market (211000100)

    Refining NPC:
    * Shoes, level 60-80 all classes
*/

var status = 0;
var selectedType = -1;
var selectedItem = -1;
var item;
var mats;
var matQty;
var cost;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1)
        status++;
    else
        cm.dispose();
    if (status == 0 && mode == 1) {
        var selStr = "嗨，我是高登 有什么我可以帮助你的？？#b"
        var options = new Array("做一双战士鞋子", "做一双弓箭手鞋子", "做一双法师鞋子", "做一双盗贼鞋子");
        for (var i = 0; i < options.length; i++) {
            selStr += "\r\n#L" + i + "# " + options[i] + "#l";
        }

        cm.sendSimple(selStr);
    } else if (status == 1 && mode == 1) {
        selectedType = selection;
        var selStr;
        var shoes;
        if (selectedType == 0) { //warrior shoes
            selStr = "战士鞋子？好的，那要哪一套呢？#b";
            var shoes = ["#t1072147:##k - 剑士 等级. 60#b", "#t1072148:##k - 剑士 等级. 60#b", "#t1072149:##k - 剑士 等级. 60#b",
                "#t1072154:##k - 战士 等级. 70#b", "#t1072155:##k - 战士 等级. 70#b", "#t1072156:##k - 战士 等级. 70#b",
                "#t1072210:##k - 战士 等级. 80#b", "#t1072211:##k - 战士 等级. 80#b", "#t1072212:##k - 战士 等级. 80#b"];
        } else if (selectedType == 1) { //bowman shoes
            selStr = "弓箭手鞋子？好的，那要哪一套呢？#b";
            var shoes = ["#t1072144:##k - 弓箭手 等级. 60#b", "#t1072145:##k - 弓箭手 等级. 60#b", "#t1072146:##k - 弓箭手 等级. 60#b",
                "#t1072164:##k - 弓箭手 等级. 70#b", "#t1072165:##k - 弓箭手 等级. 70#b", "#t1072166:##k - 弓箭手 等级. 70#b", "#t1072167:##k - 弓箭手 等级. 70#b",
                "#t1072182:##k - 弓箭手 等级. 80#b", "#t1072183:##k - 弓箭手 等级. 80#b", "#t1072184:##k - 弓箭手 等级. 80#b", "#t1072185:##k - 弓箭手 等级. 80#b"];
        } else if (selectedType == 2) { //mage shoes
            selStr = "法师鞋子？好的，那要哪一套呢？#b";
            var shoes = ["#t1072136:##k - 法师 等级. 60#b", "#t1072137:##k - 法师 等级. 60#b", "#t1072138:##k - 法师 等级. 60#b", "#t1072139:##k - 法师 等级. 60#b",
                "#t1072157:##k - 法师 等级. 70#b", "#t1072158:##k - 法师 等级. 70#b", "#t1072159:##k - 法师 等级. 70#b", "#t1072160:##k - 法师 等级. 70#b",
                "#t1072177:##k - 法师 等级. 80#b", "#t1072178:##k - 法师 等级. 80#b", "#t1072179:##k - 法师 等级. 80#b"];
        } else if (selectedType == 3) { //thief shoes
            selStr = "飞侠鞋子？好的，那要哪一套呢？#b";
            var shoes = ["#t1072150:##k - 盗贼 等级. 60#b", "#t1072151:##k - 盗贼 等级. 60#b", "#t1072152:##k - 盗贼 等级. 60#b",
                "#t1072161:##k - 盗贼 等级. 70#b", "#t1072162:##k - 盗贼 等级. 70#b", "#t1072163:##k - 盗贼 等级. 70#b",
                "#t1072172:##k - 盗贼 等级. 80#b", "#t1072173:##k - 盗贼 等级. 80#b", "#t1072174:##k - 盗贼 等级. 80#b"];
        }
        for (var i = 0; i < shoes.length; i++) {
            selStr += "\r\n#L" + i + "# " + shoes[i] + "#l";
        }
        cm.sendSimple(selStr);
    } else if (status == 2 && mode == 1) {
        selectedItem = selection;

        if (selectedType == 0) { //warrior shoes
            var itemSet = [1072147, 1072148, 1072149, 1072154, 1072155, 1072156, 1072210, 1072211, 1072212];
            var matSet = [[4021008, 4011007, 4021005, 4000030, 4003000], [4021008, 4011007, 4011005, 4000030, 4003000], [4021008, 4011007, 4021000, 4000030, 4003000],
                [4005000, 4005002, 4011002, 4000048, 4003000], [4005000, 4005002, 4011005, 4000048, 4003000], [4005000, 4005002, 4021008, 4000048, 4003000],
                [4005000, 4005002, 4021000, 4000030, 4003000], [4005000, 4005002, 4021002, 4000030, 4003000], [4005000, 4005002, 4021008, 4000030, 4003000]];
            var matQtySet = [[1, 1, 8, 80, 55], [1, 1, 8, 80, 55], [1, 1, 8, 80, 55], [1, 3, 5, 100, 55], [2, 2, 5, 100, 55], [3, 1, 1, 100, 55],
                [2, 3, 7, 90, 65], [3, 2, 7, 90, 65], [4, 1, 2, 90, 65]];
            var costSet = [60000, 60000, 60000, 70000, 70000, 70000, 80000, 80000, 80000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 1) { //bowman shoes
            var itemSet = [1072144, 1072145, 1072146, 1072164, 1072165, 1072166, 1072167, 1072182, 1072183, 1072184, 1072185];
            var matSet = [[4011006, 4021000, 4021007, 4000030, 4003000], [4011006, 4021005, 4021007, 4000030, 4003000], [4011006, 4021003, 4021007, 4000030, 4003000],
                [4005002, 4005000, 4021005, 4000055, 4003000], [4005002, 4005000, 4021004, 4000055, 4003000], [4005002, 4005000, 4021003, 4000055, 4003000], [4005002, 4005000, 4021008, 4000055, 4003000],
                [4005002, 4005000, 4021002, 4000030, 4003000], [4005002, 4005000, 4021000, 4000030, 4003000], [4005002, 4005000, 4021003, 4000030, 4003000], [4005002, 4021008, 4000030, 4003000]];
            var matQtySet = [[5, 8, 1, 75, 50], [5, 8, 1, 75, 50], [5, 8, 1, 75, 50], [1, 3, 5, 100, 55], [2, 2, 5, 100, 55], [2, 2, 5, 100, 55], [3, 1, 1, 100, 55],
                [2, 3, 7, 90, 60], [3, 2, 7, 90, 60], [4, 1, 7, 90, 60], [5, 2, 90, 60]];
            var costSet = [60000, 60000, 60000, 70000, 70000, 70000, 70000, 80000, 80000, 80000, 80000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 2) { //mage shoes
            var itemSet = [1072136, 1072137, 1072138, 1072139, 1072157, 1072158, 1072159, 1072160, 1072177, 1072178, 1072179];
            var matSet = [[4021009, 4011006, 4011005, 4000030, 4003000], [4021009, 4011006, 4021003, 4000030, 4003000], [4021009, 4011006, 4011003, 4000030, 4003000], [4021009, 4011006, 4021002, 4000030, 4003000],
                [4005001, 4005003, 4021002, 4000051, 4003000], [4005001, 4005003, 4021000, 4000051, 4003000], [4005001, 4005003, 4011003, 4000051, 4003000], [4005001, 4005003, 4011006, 4000051, 4003000],
                [4005001, 4005003, 4021003, 4000030, 4003000], [4005001, 4005003, 4021001, 4000030, 4003000], [4005001, 4005003, 4021008, 4000030, 4003000]];
            var matQtySet = [[1, 4, 5, 70, 50], [1, 4, 5, 70, 50], [1, 4, 5, 70, 50], [1, 4, 5, 70, 50],
                [1, 3, 5, 100, 55], [2, 2, 5, 100, 55], [2, 2, 5, 100, 55], [3, 1, 3, 100, 55],
                [2, 3, 7, 85, 60], [3, 2, 7, 85, 60], [4, 1, 2, 85, 60]];
            var costSet = [60000, 60000, 60000, 60000, 70000, 70000, 70000, 70000, 80000, 80000, 80000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 3) { //thief shoes
            var itemSet = [1072150, 1072151, 1072152, 1072161, 1072162, 1072163, 1072172, 1072173, 1072174];
            var matSet = [[4021007, 4011007, 4021000, 4000030, 4003000], [4021007, 4011007, 4011006, 4000030, 4003000], [4021007, 4011007, 4021008, 4000030, 4003000],
                [4005003, 4005000, 4021001, 4000051, 4003000], [4005003, 4005002, 4021005, 4000051, 4003000], [4005002, 4005003, 4021000, 4000051, 4003000],
                [4005000, 4005003, 4021003, 4000030, 4003000], [4005002, 4005003, 4021000, 4000030, 4003000], [4005003, 4005002, 4021008, 4000030, 4003000]];
            var matQtySet = [[1, 1, 8, 75, 50], [1, 1, 5, 75, 50], [1, 1, 1, 75, 50],
                [1, 3, 5, 100, 55], [1, 3, 5, 100, 55], [1, 3, 5, 100, 55],
                [3, 2, 7, 90, 60], [3, 2, 7, 90, 60], [3, 2, 7, 90, 60]];
            var costSet = [60000, 60000, 60000, 70000, 70000, 70000, 80000, 80000, 80000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        }

        var prompt = "你想要做一双 #r#t" + item + ":##k ？ 我为了要做出最棒的品质，我需要以下物品，我建议你确保装备栏空间足够。\r\n#b";

        if (mats instanceof Array) {
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i" + mats[i] + "# " + matQty[i] + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i" + mats + "# " + matQty + " #t" + mats + "#";
        }

        if (cost > 0) {
            prompt += "\r\n#i4031138# " + cost + " 金钱";
        }
        cm.sendYesNo(prompt);
    } else if (status == 3 && mode == 1) {
        var complete = true;

        if (!cm.canHold(item, 1)) {
            cm.sendOk("首先检查你的物品栏是否有空位。");
            cm.dispose();
            return;
        } else if (cm.getMeso() < cost) {
            cm.sendOk("恐怕你支付不起我的服务费。");
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
            cm.sendOk("我只生产高质量的商品，而这是离不开合适的材料的。");
        } else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    cm.gainItem(mats[i], -matQty [i]);
                }
            } else {
                cm.gainItem(mats, -matQty);
            }
            cm.gainMeso(-cost);
            cm.gainItem(item, 1);
            cm.sendOk("都完成了。保持温暖！");
        }
        cm.dispose();
    }
}
