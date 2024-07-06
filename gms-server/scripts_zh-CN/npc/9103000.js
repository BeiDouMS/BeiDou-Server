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
*	Author : Raz
*	Author : Ronan
*
*	NPC = 9103000 - Pierre
*	Map =  Ludibrium - Ludibrium Maze 16
*	NPC MapId = 809050015
*	Function = Gives LMPQ EXP reward
*
*/

var status = 0;
var qty = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.isEventLeader()) {
                if (!cm.getEventInstance().isEventTeamTogether()) {
                    cm.sendOk("一个或多个队员尚未到达，请等待他们先到达这里。");
                    cm.dispose();
                } else if (cm.hasItem(4001106, 30)) {
                    qty = cm.getItemQuantity(4001106);
                    cm.sendYesNo("太棒了！你从这次冒险中获得了" + qty + "个#t4001106#，现在你的队伍将从这次行动中获得公平的经验值。你准备好离开了吗？");
                } else {
                    cm.sendOk("你的队伍还不能完成这个组队任务，因为你手头上还没有达到最低要求的30个#t4001106#。");
                    cm.dispose();
                }
            } else {
                cm.sendOk("让你的队长和我交谈，结束这个任务。");
                cm.dispose();
            }
        } else if (status == 1) {
            cm.removeAll(4001106);
            cm.getEventInstance().giveEventPlayersExp(50 * qty);
            cm.getEventInstance().clearPQ();
            cm.dispose();
        }
    }
}