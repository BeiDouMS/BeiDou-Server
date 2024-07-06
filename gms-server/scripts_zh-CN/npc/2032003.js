/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* Lira
 * 
 * Adobis's Mission I : Breath of Lava <Level 2> (280020001)
 * Zakum Quest NPC 
 */

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;

        if (status == 0) {
            cm.sendNext("恭喜你走到了这一步！好吧，我想我最好给你#b烈焰之息#k。你确实赢得了它！");
        } else if (status == 1) {
            if (!cm.canHold(4031062)) {
                cm.sendOk("尝试释放一个空位来接收#b#t4031062##k。");
                cm.dispose();
                return;
            }

            cm.sendNext("好了，是时候你离开了。");
        } else if (status == 2) {
            cm.gainItem(4031062, 1);
            cm.gainExp(10000 * cm.getPlayer().getExpRate());
            cm.warp(211042300);

            cm.dispose();
        }
    }
}