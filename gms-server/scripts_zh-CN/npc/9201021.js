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

function start() {
    if (cm.getMapId() != 680000401) {
        cm.sendSimple("你好，你想去哪里？\r\n#b" + ((cm.getMapId() != 680000400) ? "#L0#未开发的心脏狩猎场#l\r\n" : "") + ((cm.getMapId() == 680000400) ? "#L1#我有7把钥匙。带我去打破箱子#l\r\n" : "") + "#L2#请将我传送出去。#l#k");
    } else {
        cm.sendSimple("你好，你现在想回去吗？再次返回这里将花费你 #r另外7把钥匙#k。\r\n#b#L2#请将我传送回训练场。#l#k");
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.sendOk("再见。");
        cm.dispose();
        return;
    }
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (status == 1) {
        if (selection < 1) {
            if (!cm.haveItem(4000313, 1)) {
                cm.sendOk("看起来你丢失了你的 #b#t4000313##k。很抱歉，但是没有那个物品我不能让你前往狩猎场地。");
                cm.dispose();
                return;
            }

            cm.warp(680000400, 0);
        } else if (selection < 2) {
            if (cm.haveItem(4031217, 7)) {
                cm.gainItem(4031217, -7);
                cm.warp(680000401, 0);
            } else {
                cm.sendOk("看起来你没有7把钥匙。在未驯化之心狩猎地杀死蛋糕和蜡烛以获取钥匙。");
            }
        } else if (selection > 1) {
            if (cm.getMapId() != 680000401) {
                cm.warp(680000500, 0);
                cm.sendOk("再见。希望你喜欢这场婚礼！");
            } else {
                cm.warp(680000400, 0);
            }
        }

        cm.dispose();
    }
}