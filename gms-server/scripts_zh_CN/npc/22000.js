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
/* Author: Xterminator
	NPC Name: 		Shanks
	Map(s): 		Maple Road : Southperry (60000)
	Description: 		Brings you to Victoria Island
*/
var status = 0;

function start() {
    cm.sendYesNo("要去金银岛吗？只需要支付 #e150 金币#n，我会把你送到 #b明珠港#k。#r但是#k一旦离开这里，你就不能再回来了，要出发吗？");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && type != 1) {
            status -= 2;
        } else if (type == 1 || (mode == -1 && type != 1)) {
            if (mode == 0) {
                cm.sendOk("好好享受这里的时光");
            }
            cm.dispose();
            return;
        }
    }
    if (status == 1) {
        if (cm.haveItem(4031801)) {
            cm.sendNext("这是路卡斯的推荐信？那你可以免费搭乘这次航班。");
        } else {
            cm.sendNext("收你 #e150 金币#n...");
        }
    } else if (status == 2) {
        if (cm.haveItem(4031801)) {
            cm.sendNextPrev("事不宜迟，我们出发吧！");
        } else if (cm.getLevel() > 6) {
            if (cm.getMeso() < 150) {
                cm.sendOk("你的金币不足以支付这次航行");
                cm.dispose();
            } else {
                cm.sendNext("事不宜迟，我们出发吧！");
            }
        } else {
            cm.sendOk("你的等级太低了，请7级后再来。");
            cm.dispose();
        }
    } else if (status == 3) {
        if (cm.haveItem(4031801)) {
            cm.gainItem(4031801, -1);
        } else {
            cm.gainMeso(-150);
        }
        cm.warp(104000000, 0);
        cm.dispose();
    }
}