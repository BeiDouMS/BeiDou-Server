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
/*  Author:         Xterminator
	NPC Name: 		Second Eos Rock
	Map(s): 		Ludibrium : Eos Tower 71st Floor (221022900)
	Description: 	Brings you to 100th Floor or 71st Floor
*/
var status = 0;
var map = 221024400;

function start() {
    if (cm.haveItem(4001020)) {
        cm.sendSimple("你可以使用#b伊欧斯之石卷轴#k来激活#b第二个伊欧斯之石#k。你想要传送到哪块石头？#b\r\n#L0#第一个伊欧斯之石（100楼）#l\r\n#L1#第三个伊欧斯之石（41楼）#l");
    } else {
        cm.sendOk("有一块岩石可以让你传送到#b第一个伊欧斯岩石或第三个伊欧斯岩石#k，但如果没有卷轴是无法激活的。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 1) {
            if (selection == 0) {
                cm.sendYesNo("您可以使用#b伊欧斯之石卷轴#k来激活#b第二个伊欧斯之石#k。您要传送到第100层的#b第一个伊欧斯之石#k吗？");
            } else {
                cm.sendYesNo("您可以使用#b伊欧斯之石卷轴#k来激活#b第二个伊欧斯之石#k。您要传送到第41层的#b第三个伊欧斯之石#k吗？");
                map = 221021700;
            }
        } else if (status == 2) {
            cm.gainItem(4001020, -1);
            cm.warp(map, 3);
            cm.dispose();
        }
    }
}