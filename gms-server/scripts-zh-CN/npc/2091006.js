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
var status = -1;
var readNotice = false;

function start() {
    status = -1;
    readNotice = false;
    cm.sendSimple("#e<公告>#n\r\n有意挑战武陵道场的勇敢年轻人们请到武陵道场来。-#p2091007#-\r\n\r\n\r\n#b#L0#尝试挑战武陵道场#l\r\n#L1#再仔细阅读公告#l");
}

function action(mode, type, selection) {
    if (mode == -1) { // END CHAT
        return cm.dispose();
    }

    if (mode == 0 && type == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            if (type == 4 && mode == 0) { // END CHAT
                return cm.dispose();
            }
            if (selection == 0) {
                return cm.sendYesNo("#b（刚一碰触公告，就有一股神秘的气息开始包裹住了我。）#k\r\n\r\n要就此前往武陵道场吗？");
            } else {
                readNotice = true;
                return cm.sendNext("#e< 注意：接受挑战！ >#n\r\n我的名字是慕容，慕龙道场的主人。自古以来，我一直在慕龙修炼，直到我的技能达到了巅峰。从今天开始，我将接受所有对慕龙道场的申请者。慕龙道场的权利将只赋予最强大的人。\r\n如果有人希望向我学习，随时来挑战吧！如果有人希望挑战我，也欢迎。我会让你充分意识到自己的弱点。");
            }
        case 1:
            if (readNotice) {
                return cm.sendPrev("PS:You can challenge me on your own. But if you don't have that kind of courage, go ahead and call all your friends.");
            } else {
                if (type == 1 && mode == 0) {
                    cm.sendNext("当我把手从公告板上拿开时，覆盖在我身上的神秘能量也消失了。");
                    return cm.dispose();
                }
                cm.getPlayer().saveLocation("MIRROR");
                cm.warp(925020000, 4);
                return cm.dispose();
            }
        default:
            return cm.dispose();
    }
}