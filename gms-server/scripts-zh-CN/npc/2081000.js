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
var temp;
var cost;

var status = 0;

function start() {
    cm.sendSimple("...我可以帮你吗？\r\n#L0##b购买魔法种子#k#l\r\n#L1##b为利夫雷做点什么#k#l");
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status < 3)) {
        cm.dispose();
        return;
    } else if (mode == 0) {
        cm.sendOk("请仔细考虑。一旦你做出了决定，请告诉我。");
        cm.dispose();
        return;
    }
    status++;
    if (status == 1) {
        if (selection == 0) {
            cm.sendSimple("你好像不是本地人。我能帮你吗？#L0##b我想要一些#t4031346#。#k#l");
        } else {
            cm.sendNext("正在开发中...");
            cm.dispose();
        }
    } else if (status == 2) {
        cm.sendGetNumber("#b#t4031346##k is a precious iteml I cannot give it to you just like that. How about doing me a little favor? Then I'll give it to you. I'll sell the #b#t4031346##k to you for #b30,000 mesos#k each. Are you willing to make the purchase? How many would you like, then?", 0, 0, 99);
    } else if (status == 3) {
        if (selection == 0) {
            cm.sendOk("我不能卖给你0。");
            cm.dispose();
        } else {
            temp = selection;
            cost = temp * 30000;
            cm.sendYesNo("购买 #b" + temp + " #t4031346#(s)#k 将花费你 #b" + cost + " 金币#k。你确定要购买吗？");
        }
    } else if (status == 4) {
        if (cm.getMeso() < cost || !cm.canHold(4031346)) {
            cm.sendOk("请检查并查看您是否有足够的金币来进行购买。另外，我建议您检查杂项物品栏，看看是否有足够的空间来进行购买。");
        } else {
            cm.sendOk("再见~");
            cm.gainItem(4031346, temp);
            cm.gainMeso(-cost);
        }
        cm.dispose();
    }
}