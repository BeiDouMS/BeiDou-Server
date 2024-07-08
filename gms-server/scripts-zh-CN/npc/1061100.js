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
 Hotel Receptionist - Sleepywood Hotel(105040400)
 -- By ---------------------------------------------------------------------------------------------
 Unknown
 -- Version Info -----------------------------------------------------------------------------------
 1.3 - More Cleanup by Moogra - 12/17/09
 1.2 - Cleanup and Statement fix by Moogra
 1.1 - Statement fix [Information]
 1.0 - First Version by Unknown
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;
var regcost = 499;
var vipcost = 999;
var iwantreg = 0;

function start() {
    cm.sendNext("欢迎光临。我们是冬青树镇酒店。我们的酒店一直努力为您提供最好的服务。如果您因打猎而感到疲惫不堪，不妨在我们的酒店放松一下吧？");
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 1)) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 2) {
"We offer other kinds of services, too, so please think carefully and then make your decision."
            cm.dispose();
            return;
        }
        status++;
        if (status == 1) {
            cm.sendSimple("我们提供两种房间供您选择。请选择您喜欢的一种。\r\n#b#L0#普通桑拿房（每次使用" + regcost + " 枚迷宫币）#l\r\n#L1#VIP桑拿房（每次使用" + vipcost + " 枚迷宫币）#l");
            iwantreg = 1;
        } else if (status == 2) {
            if (selection == 0) {
                cm.sendYesNo("你选择了普通桑拿浴。你的生命值和魔法值将会快速恢复，你甚至可以在那里购买一些物品。你确定要进去吗？");
            } else if (selection == 1) {
                cm.sendYesNo("你选择了VIP桑拿。你的HP和MP恢复速度甚至比普通桑拿还要快，而且你甚至可以在那里找到一个特别的物品。你确定要进去吗？");
                iwantreg = 0;
            }
        } else if (status == 3) {
            if (iwantreg == 1) {
                if (cm.getMeso() >= regcost) {
                    cm.warp(105040401);
                    cm.gainMeso(-regcost);
                } else {
                    cm.sendNext("对不起，看起来你的金币不够。至少需要" + regcost + "金币才能在我们的旅馆住宿。");
                }
            } else {
                if (cm.getMeso() >= vipcost) {
                    cm.warp(105040402);
                    cm.gainMeso(-vipcost);
                } else {
                    cm.sendNext("对不起，看起来你的金币不够。至少需要" + vipcost + "金币才能在我们的酒店住宿。");
                }
            }
            cm.dispose();
        }
    }
}