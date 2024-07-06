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
	Author: Traitor, XxOsirisxX, Moogra
*/

/**
 * Dojo Entrance NPC
 */
var status = -2;
var readNotice = 0;

function start() {
    cm.sendSimple("#e< 注意 >#n\r\n如果有人有勇气挑战武陵道场，请来武陵道场。 - 武功 -\r\n\r\n\r\n#b#L0#挑战武陵道场。#l\r\n#L1#更详细地阅读通知。#l");
}

function action(mode, type, selection) {
    status++;
    if (mode == 0 && type == 0) {
        status -= 2;
    }
    if (mode >= 0) {
        if (selection == 1 || readNotice == 1) {
            if (status == -1) {
                readNotice = 1;
                cm.sendNext("#e< 注意：接受挑战！ >#n\r\n我的名字是慕容，慕龙道场的主人。自古以来，我一直在慕龙修炼，直到我的技能达到了巅峰。从今天开始，我将接受所有对慕龙道场的申请者。慕龙道场的权利将只赋予最强大的人。\r\n如果有人希望向我学习，随时来挑战吧！如果有人希望挑战我，也欢迎。我会让你充分意识到自己的弱点。");
            } else if (status == 0) {
                cm.sendPrev("PS:You can challenge me on your own. But if you don't have that kind of courage, go ahead and call all your friends.");
            } else {
                cm.dispose();
            }
        } else {
            if (status == -1 && mode == 1) {
                cm.sendYesNo("（当我把手放在公告板上时，一股神秘的能量开始包围着我。）\r\n\r\n你想去勇士部落道场吗？");
            } else if (status == 0) {
                if (mode == 0) {
                    cm.sendNext("当我把手从公告板上拿开时，覆盖在我身上的神秘能量也消失了。");
                } else {
                    cm.getPlayer().saveLocation("MIRROR");
                    cm.warp(925020000, 4);
                }
                cm.dispose();
            }
        }
    } else {
        cm.dispose();
    }
}