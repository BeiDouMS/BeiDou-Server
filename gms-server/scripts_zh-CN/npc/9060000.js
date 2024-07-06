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
var status = -1;
var completed;

function start() {
    completed = cm.haveItem(4031508, 5) && cm.haveItem(4031507, 5);

    if (completed) {
        cm.sendNext("哇~ 你成功收集了5个#b#t4031508##k和#b#t4031507##k。好的，那么我会送你去动物园。到了之后请再和我交谈。");
    } else {
        cm.sendYesNo("你还没有完成要求。你确定要离开吗？");
    }
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        cm.dispose();
        return;
    }

    if (status == 0) {
        cm.sendOk("好的，我会把你送回去。");
    } else {
        if (completed) {
            cm.getEventInstance().clearPQ();
        } else {
            cm.warp(923010100, 0);
        }

        cm.dispose();
    }
}