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
/*Amos the Strong - Entrance
**9201043
**@author Jvlaple
*/

var status = 0;
var MySelection = -1;

function start() {
    cm.sendSimple("我的名字是强壮的阿莫斯。你想做什么？\r\n#b#L0#参加阿莫利亚挑战！#l\r\n#L1#用10把钥匙交换门票！#l\r\n#k");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.sendOk("好的，当你准备好的时候再回来。");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 1 && selection == 0) {
            if (cm.haveItem(4031592, 1)) {
                cm.sendYesNo("所以你想进入 #b入口#k？");
                MySelection = selection;
            } else {
                cm.sendOk("您必须有入场券才能进入。");
                cm.dispose();
            }
        } else if (status == 1 && selection == 1) {
            if (cm.haveItem(4031592)) {
                cm.sendOk("你已经有一张入场券了！");
                cm.dispose();
            } else if (cm.haveItem(4031593, 10)) {
                cm.sendYesNo("所以你想要一张门票？");
                MySelection = selection;
            } else {
                cm.sendOk("请先给我拿到10把钥匙！");
                cm.dispose();
            }
        } else if (status == 2 && MySelection == 0) {
            cm.warp(670010100, 0);
            cm.gainItem(4031592, -1)
            cm.dispose();
        } else if (status == 2 && MySelection == 1) {
            cm.gainItem(4031593, -10);
            cm.gainItem(4031592, 1);
            cm.dispose();
        }
    }
}