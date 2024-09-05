/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Eurek the Alchemist - Multiple Place
 -- By ---------------------------------------------------------------------------------------------
 Information
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Information
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;
var menu = "";
var set;
var makeitem;
var access = true;
var reqitem = [];
var cost = 4000;
var makeditem = [4006000, 4006001];
var reqset = [[[[4000046, 20], [4000027, 20], [4021001, 1]],
        [[4000025, 20], [4000049, 20], [4021006, 1]],
        [[4000129, 15], [4000130, 15], [4021002, 1]],
        [[4000074, 15], [4000057, 15], [4021005, 1]],
        [[4000054, 7], [4000053, 7], [4021003, 1]]],

    [[[4000046, 20], [4000027, 20], [4011001, 1]],
        [[4000014, 20], [4000049, 20], [4011003, 1]],
        [[4000132, 15], [4000128, 15], [4011005, 1]],
        [[4000074, 15], [4000069, 15], [4011002, 1]],
        [[4000080, 7], [4000079, 7], [4011004, 1]]]];

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && (status == 1 || status == 2))) {
        cm.dispose();
        return;
    }
    if (mode == 0) {
        cm.sendNext("材料不够，是吗？别担心。收集到必要的物品后，来找我就行了。无论是打猎还是从他人那里购买，都有很多方法可以获取这些物品，所以继续努力吧。");
        cm.dispose();
    }
    if (mode == 1) {
        status++;
    }
    if (status == 0) {
        cm.sendNext("好的，把青蛙的舌头和松鼠的牙齿混合在一起，哦对了！忘了放闪闪发光的白色粉末！！天哪，那本来可能会很糟糕……哇！！你站在那里多久了？我可能有点沉迷于我的工作……嘿嘿。");
    } else if (status == 1) {
        cm.sendSimple("正如你所看到的，我只是一个旅行的炼金术士。我可能还在训练中，但我仍然可以制作一些你可能需要的东西。你想看看吗？\r\n\r\n#L0##b制作魔法石#k#l\r\n#L1##b制作召唤石#k#l");
    } else if (status == 2) {
        set = selection;
        makeitem = makeditem[set];
        for (i = 0; i < reqset[set].length; i++) {
            menu += "\r\n#L" + i + "##bMake it using #t" + reqset[set][i][0][0] + "# and #t" + reqset[set][i][1][0] + "##k#l";
        }
        cm.sendSimple("哈哈... #b#t" + makeitem + "##k 是一种神秘的岩石，只有我才能制造。许多旅行者似乎需要它来获得比魔法值和生命值更强大的技能。有5种方法可以制作 #t" + makeitem + "#。你想用哪种方法制作？" + menu);
    } else if (status == 3) {
        set = reqset[set][selection];
        reqitem[0] = [set[0][0], set[0][1]];
        reqitem[1] = [set[1][0], set[1][1]];
        reqitem[2] = [set[2][0], set[2][1]];
        menu = "";
        for (i = 0; i < reqitem.length; i++) {
            menu += "\r\n#v" + reqitem[i][0] + "# #b" + reqitem[i][1] + " #t" + reqitem[i][0] + "#s#k";
        }
        menu += "\r\n#i4031138# #b" + cost + " mesos#k";
        cm.sendYesNo("为了制作#b5 #t" + makeitem + "##k，我需要以下物品。其中大部分可以通过打猎获得，所以对你来说并不是非常困难。你觉得怎么样？你想要一些吗？\r\n" + menu);
    } else if (status == 4) {
        for (i = 0; i < reqitem.length; i++) {
            if (!cm.haveItem(reqitem[i][0], reqitem[i][1])) {
                access = false;
            }
        }
        if (access == false || !cm.canHold(makeitem) || cm.getMeso() < cost) {
            cm.sendNext("请检查并查看您是否拥有所有所需的物品，或者您的杂项物品栏是否已满。");
        } else {
            cm.sendOk("拿着这5块#b#t" + makeitem + "##k。即使是我也得承认，这是一件杰作。好吧，如果你需要我的帮助，尽管回来找我谈谈！");
            cm.gainItem(reqitem[0][0], -reqitem[0][1]);
            cm.gainItem(reqitem[1][0], -reqitem[1][1]);
            cm.gainItem(reqitem[2][0], -reqitem[2][1]);
            cm.gainMeso(-cost);
            cm.gainItem(makeitem, 5);
        }
        cm.dispose();
    }
}