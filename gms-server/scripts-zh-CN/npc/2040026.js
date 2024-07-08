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
/*      Author: Xterminator, Moogra
	NPC Name: 		Third Eos Rock
	Map(s): 		Ludibrium : Eos Tower 41st Floor (221021700)
	Description: 		Brings you to 71st Floor or 1st Floor
*/
var status = 0;
var map;

function start() {
    if (cm.haveItem(4001020)) {
        cm.sendSimple("你可以使用#b伊欧斯之石卷轴#k来激活#b第三个伊欧斯之石#k。你想要传送到这些石头中的哪一个？#b\r\n#L0#第二个伊欧斯之石（71楼）#l\r\n#L1#第四个伊欧斯之石（1楼）#l");
    } else {
        cm.sendOk("有一块岩石可以让你传送到#b第二个伊欧斯岩石或第四个伊欧斯岩石#k，但如果没有卷轴是无法激活它的。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            if (selection == 0) {
                cm.sendYesNo("您可以使用#b伊欧斯之石卷轴#k来激活#b第三个伊欧斯之石#k。您要传送到第71层的#b第二个伊欧斯之石#k吗？");
                map = 221022900;
            } else {
                cm.sendYesNo("您可以使用#b伊欧斯之石卷轴#k来激活#b第三个伊欧斯之石#k。您要传送到一楼的#b第四个伊欧斯之石#k吗？");
                map = 221020000;
            }
        } else if (status == 2) {
            cm.gainItem(4001020, -1);
            cm.warp(map, map % 1000 == 900 ? 3 : 4);
            cm.dispose();
        }
    }
}