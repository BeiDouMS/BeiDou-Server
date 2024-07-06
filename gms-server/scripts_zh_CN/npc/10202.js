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
    cm.sendNext("战士拥有巨大的力量和持久力来支持它，他们在近战战斗中表现最出色。普通攻击本身就很强大，再加上复杂的技能，这个职业非常适合进行爆发性攻击。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0) {
            cm.sendNext("如果你想体验成为一名战士的感觉，再来找我吧。");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendYesNo("你想体验一下成为一名战士是什么感觉吗？");
    } else if (status == 1) {
        cm.lockUI();
        cm.warp(1020100, 0);
        cm.dispose();
    }
}