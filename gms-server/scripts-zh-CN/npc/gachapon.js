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
/* NPC Base
	Map Name (Map ID)
	Extra NPC info.
 */

var status;
var ticketId = 5220000;
var mapName = ["射手村", "魔法密林", "勇士部落", "废弃都市", "林中之城", "蘑菇神社", "昭和澡堂（男）", "昭和澡堂（女）", "玩具城", "新叶城", "冰峰雪域", "诺特勒斯号"];
var curMapName = "";

function start() {
    status = -1;
    curMapName = mapName[(cm.getNpc() != 9100117 && cm.getNpc() != 9100109) ? (cm.getNpc() - 9100100) : cm.getNpc() == 9100109 ? 9 : 11];

    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0 && mode == 1) {
            if (cm.haveItem(ticketId)) {
                cm.sendYesNo("你可以使用" + curMapName + "快乐百宝箱。你想要使用你的快乐百宝券吗？");
            } else {
                cm.sendSimple("欢迎来到" + curMapName + "快乐百宝箱。我可以为您做些什么呢？\r\n\r\n#L0#什么是快乐百宝箱？#l\r\n#L1#在哪里可以购买快乐百宝券？#l");
            }
        } else if (status == 1 && cm.haveItem(ticketId)) {
            if (cm.canHold(1302000) && cm.canHold(2000000) && cm.canHold(3010001) && cm.canHold(4000000)) { // One free slot in every inventory.
                cm.gainItem(ticketId, -1);
                cm.doGachapon();
            } else {
                cm.sendOk("请确保你的#r装备、消耗、设置#k和#r其他#k物品栏中至少有一个空位。");
            }
            cm.dispose();
        } else if (status == 1) {
            if (selection == 0) {
                cm.sendNext("玩转快乐百宝箱，赢得稀有卷轴、装备、椅子、熟练书和其他酷炫物品！你只需要一张 #b快乐百宝券#k 就有机会成为随机物品的幸运获得者。");
            } else {
                cm.sendNext("快乐百宝券可以在#r现金商店#k购买，可以使用NX或枫叶点购买。点击屏幕右下角的红色商店图标进入#r现金商店#k，就能购买快乐百宝券。");
            }
        } else if (status == 2) {
            cm.sendNextPrev("你会在" + curMapName + "的快乐百宝箱中找到各种物品，但最有可能找到与" + curMapName + "相关的物品和卷轴。");
        } else {
            cm.dispose();
        }
    }
}
