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

function start() {
    if (cm.getPlayer().getMapId() === 10000) {
        cm.sendOk("您是一位新的冒险家，我会为你提供一些非常有用指示。如果您想与我们说话，你可以双击我们。您可以按#b左箭头和右箭头#k进行移动，通过按#bAlt#k来跳跃。来吧~试试吧！另外，有时候你需要爬梯子或使用绳子到达你想去的地方。你可以通过按#b上箭头#k来做到这一点。请记住这一点。");
        cm.dispose();
        return;
    }
    cm.sendYesNo("你的训练完成了吗？如果你愿意的话，我会让你离开这个训练营。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0) {
            cm.sendOk("你还没完成训练计划吗？如果你想离开这个地方，请毫不犹豫地告诉我。");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendNext("接下来，我会把你从这里送出去，祝您一路顺风。");
    } else {
        // cm.warp(40000, 0);
        cm.warp(0);
        cm.dispose();
    }
}