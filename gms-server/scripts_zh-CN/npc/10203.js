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
    cm.sendNext("盗贼是幸运、灵巧和力量的完美结合，擅长对无助的敌人进行突袭攻击。高水平的闪避能力和速度使得盗贼能够从各个角度攻击敌人。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0) {
            cm.sendNext("If you wish to experience what it's like to be a Thief, come see me again.");
        }
        cm.dispose();
        return;
    }
    if (status == 0) {
        cm.sendYesNo("Would you like to experience what it's like to be a Thief?");
    } else if (status == 1) {
        cm.lockUI();
        cm.warp(1020400, 0);
        cm.dispose();
    }
}