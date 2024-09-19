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

/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Shalon - Ticketing Usher
 -- By ---------------------------------------------------------------------------------------------
 Whoever written this script
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Whoever written this script
 2.0 - Second Version by Jayd
 ---------------------------------------------------------------------------------------------------
 **/

status = -1;
oldSelection = -1;

function start() {
    cm.sendSimple("你好，我是来自新加坡机场的莎伦。我可以帮助你迅速到达废弃都市。你想去废弃都市吗？\r\n#b#L0#我想买一张去废弃都市的飞机票\r\n#b#L1#让我进入出发点。");
}

function action(mode, type, selection) {
    status++;
    if (mode <= 0) {
        oldSelection = -1;
        cm.dispose();
    }

    if (status == 0) {
        if (selection == 0) {
            cm.sendYesNo("门票的价格是5,000金币。你要购买门票吗？");
        } else if (selection == 1) {
            cm.sendYesNo("您现在想要进去吗？一旦您进去，您的票就会作废！感谢您选择北斗航空公司。");
        }
        oldSelection = selection;
    } else if (status == 1) {
        if (oldSelection == 0) {
            if (cm.getPlayer().getMeso() > 4999 && !cm.getPlayer().haveItem(4031732)) {
                if (cm.getPlayer().canHold(4031732, 1)) {
                    cm.gainMeso(-5000);
                    cm.gainItem(4031732);
                    cm.sendOk("感谢您选择北斗航空！祝您旅途愉快！");
                    cm.dispose();
                } else {
                    cm.sendOk("你的ETC库存中没有空闲的插槽来放置这张票，请提前创建一个空位。");
                    cm.dispose();
                }
            } else {
                cm.sendOk("你没有足够的金币，或者你已经购买了一张门票。");
                cm.dispose();
            }
        } else if (oldSelection == 1) {
            if (cm.itemQuantity(4031732) > 0) {
                var em = cm.getEventManager("AirPlane");
                if (em.getProperty("entry") == "true") {
                    cm.warp(540010001);
                    cm.gainItem(4031732, -1);
                } else {
                    cm.sendOk("抱歉，飞机已经起飞，请稍等几分钟。");
                }
            } else {
                cm.sendOk("你需要一个#b#t4031732##k才能上飞机！");
            }
        }
        cm.dispose();
    }
}