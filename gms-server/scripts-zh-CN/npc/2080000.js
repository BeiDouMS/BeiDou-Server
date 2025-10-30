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
var status = 0;
var selectedType = -1;
var selectedItem = -1;
var stimulator = false;
var item;
var mats;
var matQty;
var cost;
var stimID;

var cd_item = 4001078;
var cd_mats = [4011001, 4011002, 4001079];
var cd_matQty = [1, 1, 1];
var cd_cost = 25000;

function start() {
    cm.getPlayer().setCS(true);
    var selStr = "龙的力量不容小觑。如果你愿意，我可以将龙之力注入你的某件武器中。但前提是，这件武器的潜力足以承载龙之力……#b";
    var options = ["什么是刺激剂？", "制作战士武器", "制作弓箭手武器", "制作魔法师武器", "制作盗贼武器", "制作海盗武器",
        "使用刺激剂制作战士武器", "使用刺激剂制作弓箭手武器", "使用刺激剂制作魔法师武器", "使用刺激剂制作盗贼武器", "使用刺激剂制作海盗武器"];

    if (cm.isQuestStarted(7301) || cm.isQuestStarted(7303)) {
        options.push("Make #t4001078#");
    }

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
        if (selectedType > 5 && selectedType < 11) {
            stimulator = true;
            selectedType -= 5;
        } else {
            stimulator = false;
        }
        if (selectedType == 0) { //What's a stim?
            cm.sendNext("刺激剂是一种特殊药剂，我可以在制作特定物品时加入它。它能让物品生成类似怪物掉落的随机属性。但也有可能毫无变化，甚至属性低于平均值。而且使用刺激剂时有10%的概率无法获得任何物品，所以请谨慎选择。");
            cm.dispose();
        } else if (selectedType == 1) { //warrior weapon
            var selStr = "好的，那你想让哪件战士武器承载龙之力？#b";
            var weapon = ["狂龙闪电剑#k - 110级 单手剑#b", "狂龙怒斩#k - 110级 单手斧#b", "狂龙地锤#k - 110级 单手钝器#b", "飞龙巨剑#k - 110级 双手剑#b", "炼狱魔龙斧#k - 110级 双手斧#b", "金龙轰天锤#k - 110级 双手钝器#b",
                "盘龙七冲枪#k - 110级 长枪#b", "血龙神斧#k - 110级 长杖#b"]; // BW：Blunt Weapon（钝器）
            for (var i = 0; i < weapon.length; i++) {
                selStr += "\r\n#L" + i + "# " + weapon[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 2) { //bowman weapon
            var selStr = "好的，那你想让哪件弓箭手武器承载龙之力？#b";
            var weapon = ["金龙振翅弓#k - 110级 弓#b", "黄金飞龙弩#k - 110级 十字弓#b"];
            for (var i = 0; i < weapon.length; i++) {
                selStr += "\r\n#L" + i + "# " + weapon[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 3) { //magician weapon
            var selStr = "好的，那你想让哪件魔法师武器承载龙之力？#b";
            var weapon = ["佘太君龙杖#k - 108级 魔杖#b", "黑精灵王杖#k - 110级 法杖#b"];
            for (var i = 0; i < weapon.length; i++) {
                selStr += "\r\n#L" + i + "# " + weapon[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 4) { //thief weapon
            var selStr = "好的，那你想让哪件盗贼武器承载龙之力？#b";
            var weapon = ["蝉翼龙牙破#k - 110级 力量型匕首#b", "半月龙鳞裂#k - 110级 运气型匕首#b", "寒木升龙拳#k - 110级 飞镖#b"]; // Claw：盗贼使用的飞镖武器
			for (var i = 0; i < weapon.length; i++) {
                selStr += "\r\n#L" + i + "# " + weapon[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 5) { //pirate weapon
            var selStr = "好的，那你想让哪件海盗武器承载龙之力？#b";
            var weapon = ["撕裂者#k - 110级 指虎#b", "枭龙#k - 110级 手枪#b"]; // Knuckle：海盗（拳手）使用的指虎武器
            for (var i = 0; i < weapon.length; i++) {
                selStr += "\r\n#L" + i + "# " + weapon[i] + "#l";
            }
            cm.sendSimple(selStr);
        } else if (selectedType == 11) { //cornian's dagger
            var selStr = "哦，你是想混进这些蜥蜴怪里去救莫伊拉（Moira）吗？我会尽全力支持你。给我一些材料，我就能帮你做一把和#t4001078#几乎一样的匕首。";
            cm.sendNext(selStr);
        }
    } else if (status == 2) {
        selectedItem = selection;

        if (selectedType == 1) { //warrior weapon
            var itemSet = [1302059, 1312031, 1322052, 1402036, 1412026, 1422028, 1432038, 1442045];
            var matSet = [[1302056, 4000244, 4000245, 4005000], [1312030, 4000244, 4000245, 4005000], [1322045, 4000244, 4000245, 4005000], [1402035, 4000244, 4000245, 4005000],
                [1412021, 4000244, 4000245, 4005000], [1422027, 4000244, 4000245, 4005000], [1432030, 4000244, 4000245, 4005000], [1442044, 4000244, 4000245, 4005000]];
            var matQtySet = [[1, 20, 25, 8], [1, 20, 25, 8], [1, 20, 25, 8], [1, 20, 25, 8], [1, 20, 25, 8], [1, 20, 25, 8], [1, 20, 25, 8], [1, 20, 25, 8]];
            var costSet = [120000, 120000, 120000, 120000, 120000, 120000, 120000, 120000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 2) { //bowman weapon
            var itemSet = [1452044, 1462039];
            var matSet = [[1452019, 4000244, 4000245, 4005000, 4005002], [1462015, 4000244, 4000245, 4005000, 4005002]];
            var matQtySet = [[1, 20, 25, 3, 5], [1, 20, 25, 5, 3]];
            var costSet = [120000, 120000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 3) { //magician weapon
            var itemSet = [1372032, 1382036];
            var matSet = [[1372010, 4000244, 4000245, 4005001, 4005003], [1382035, 4000244, 4000245, 4005001, 4005003]];
            var matQtySet = [[1, 20, 25, 6, 2], [1, 20, 25, 6, 2]];
            var costSet = [120000, 120000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 4) { //thief weapon
            var itemSet = [1332049, 1332050, 1472051];
            var matSet = [[1332051, 4000244, 4000245, 4005000, 4005002], [1332052, 4000244, 4000245, 4005002, 4005003], [1472053, 4000244, 4000245, 4005002, 4005003]];
            var matQtySet = [[1, 20, 25, 5, 3], [1, 20, 25, 3, 5], [1, 20, 25, 2, 6]];
            var costSet = [120000, 120000, 120000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 5) { //pirate weapon
            var itemSet = [1482013, 1492013];
            var matSet = [[1482012, 4000244, 4000245, 4005000, 4005002], [1492012, 4000244, 4000245, 4005000, 4005002]];
            var matQtySet = [[1, 20, 25, 5, 3], [1, 20, 25, 3, 5]];
            var costSet = [120000, 120000];
            item = itemSet[selectedItem];
            mats = matSet[selectedItem];
            matQty = matQtySet[selectedItem];
            cost = costSet[selectedItem];
        } else if (selectedType == 11) { //cornian's dagger
            item = cd_item;
            mats = cd_mats;
            matQty = cd_matQty;
            cost = cd_cost;
        }

        var prompt = "你想让我制作一把 #t"+ item +"# 吗？这样的话，我需要你提供一些特定的材料才能制作。不过要确保你的背包里有足够的空间哦！#b";
        if (stimulator) {
            stimID = getStimID(item);
            prompt += "\r\n#i" + stimID + "# 1 #t" + stimID + "#";
        }
        if (mats instanceof Array) {
            for (var i = 0; i < mats.length; i++) {
                prompt += "\r\n#i" + mats[i] + "# " + matQty[i] + " #t" + mats[i] + "#";
            }
        } else {
            prompt += "\r\n#i" + mats + "# " + matQty + " #t" + mats + "#";
        }
        if (cost > 0) {
            prompt += "\r\n#i4031138# " + cost + " meso";
        }
        cm.sendYesNo(prompt);
    } else if (status == 3) {
        var complete = true;

        if (!cm.canHold(item, 1)) {
            cm.sendOk("首先检查你的物品栏是否有空位。");
            cm.dispose();
            return;
        } else if (cm.getMeso() < cost) {
            cm.sendOk("你没有满足我需要的物资。");
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
        if (stimulator) { //check for stimulator
            if (!cm.haveItem(stimID)) {
                complete = false;
            }
        }
        if (!complete) {
            cm.sendOk("恐怕没有正确的物品，龙之精华就不能成为一个非常可靠的武器。下次请带来正确的物品。");
        } else {
            if (mats instanceof Array) {
                for (var i = 0; i < mats.length; i++) {
                    cm.gainItem(mats[i], -matQty[i]);
                }
            } else {
                cm.gainItem(mats, -matQty);
            }
            cm.gainMeso(-cost);
            if (stimulator) { //check for stimulator
                cm.gainItem(stimID, -1);
                var deleted = Math.floor(Math.random() * 10);
                if (deleted != 0) {
                    cm.gainItem(item, 1, true, true);
                    cm.sendOk("过程已经完成。好好对待你的武器，免得招惹龙的愤怒。");
                } else {
                    cm.sendOk("不幸的是，龙的精华与你的武器产生了冲突。对你的损失我深感抱歉。");
                }
            } else {//just give basic item
                cm.gainItem(item, 1);
                cm.sendOk("过程已经完成。好好对待你的武器，免得招惹龙的愤怒。");
            }
        }
        cm.dispose();
    }
}

function getStimID(equipID) {
    var cat = Math.floor(equipID / 10000);
    switch (cat) {
        case 130: //1h sword
            return 4130002;
        case 131: //1h axe
            return 4130003;
        case 132: //1h bw
            return 4130004;
        case 140: //2h sword
            return 4130005;
        case 141: //2h axe
            return 4130006;
        case 142: //2h bw
            return 4130007;
        case 143: //spear
            return 4130008;
        case 144: //polearm
            return 4130009;
        case 137: //wand
            return 4130010;
        case 138: //staff
            return 4130011;
        case 145: //bow
            return 4130012;
        case 146: //xbow
            return 4130013;
        case 148: //knuckle
            return 4130016;
        case 149: //pistol
            return 4130017;
        case 133: //dagger
            return 4130014;
        case 147: //claw
            return 4130015;
    }
    return 4130002;
}
