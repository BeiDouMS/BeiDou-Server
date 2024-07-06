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
/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 VIP Cab - Victoria Road : Ellinia (101000000)
 -- By ---------------------------------------------------------------------------------------------
 Xterminator
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Xterminator
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;
var cost = 10000;

function start() {
    cm.sendNext("你好！这辆出租车只接待VIP客户。与普通出租车只能带你去不同的城镇不同，我们提供更好的服务，值得VIP级别的待遇。价格有点高，但是……只需10,000金币，我们就会安全地带你去#b蚁穴#k。");
}

function action(mode, type, selection) {
    status++;
    if (mode == -1) {
        cm.dispose();
        return;
    } else if (mode == 0) {
        cm.sendOk("这个城镇也有很多值得一看的地方。如果你想去蚁洞公园，找到我们。");
        cm.dispose();
        return;
    }
    if (status == 1) {
        cm.sendYesNo(cm.getJobId() == 0 ? "We have a special 90% discount for beginners. The Ant Tunnel is located deep inside in the dungeon that's placed at the center of the Victoria Island, where the 24 Hr Mobile Store is. Would you like to go there for #b1,000 mesos#k?" : "The regular fee applies for all non-beginners. The Ant Tunnel is located deep inside in the dungeon that's placed at the center of the Victoria Island, where 24 Hr Mobile Store is. Would you like to go there for #b10,000 mesos#k?");
        cost /= ((cm.getJobId() == 0) ? 10 : 1);
    } else if (status == 2) {
        if (cm.getMeso() < cost) {
            cm.sendNext("It looks like you don't have enough mesos. Sorry but you won't be able to use this without it.")
        } else {
            cm.gainMeso(-cost);
            cm.warp(105070001);
        }
        cm.dispose();
    }
}