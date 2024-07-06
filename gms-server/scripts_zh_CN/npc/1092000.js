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
/*
 * @Name         Tangyoon
 * @Author       xXOsirisXx (BubblesDev)
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || !cm.isQuestStarted(2180)) {
        cm.dispose();

    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            cm.sendNext("好的，我现在会把你送到我的牛棚去。小心那些喝光所有牛奶的小牛犊。你可不想白费力气。");
        } else if (status == 1) {
            cm.sendNextPrev("这不容易一眼就分辨出小牛和母牛。这些小牛可能只有一个月或两个月大，但它们已经长到和它们的母亲一样大了。它们甚至看起来很像……有时候连我自己都会感到困惑！祝你好运！");
        } else if (status == 2) {
            if (cm.canHold(4031847)) {
                cm.gainItem(4031847, 1);
                cm.warp(912000100, 0);
            } else {
                cm.sendOk("我无法给你空瓶，因为你的背包已满。请在杂项窗口中腾出一些空间。");
            }
            cm.dispose();
        }
    }
}