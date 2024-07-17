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
 * @description 拍卖行中心脚本
 */
var status = -1;
function start() {
    action(1, 0, 0)
}

function action(mode, type, selection) {
    if (mode === 1) {
        status++;
    } else if (mode === -1) {
        status--;
    } else {
        cm.dispose();
        return;
    }

    if (status === 0) {
        let text = " \t\t\t\t#e欢迎来到#rBeiDou#k脚本中心#n\r\n\r\n";
        text += "当前点券：" + cm.getPlayer().getCashShop().getCash(1) + "\r\n";
        text += "当前抵用券：" + cm.getPlayer().getCashShop().getCash(2) + "\r\n";
        text += "当前信用券：" + cm.getPlayer().getCashShop().getCash(4) + "\r\n";
        text += " \r\n\r\n";
        text += "#L0#每日签到#l \t #L1#在线奖励#l \t #L2#传送自由#l\r\n";
        if (cm.getPlayer().isGM()) {
            text += "\r\n\r\n";
            text += "\t\t\t\t#r=====以下内容仅GM可见=====\r\n";
            text += "#L61#超级传送#l \t #L62#超级商店#l \t #L63#整容集合#l\r\n\r\n";
			text += "#L64#UI查询#l";
        }
        cm.sendSimple(text);
    } else if (status === 1) {
        doSelect(selection);
    } else {
        cm.dispose();
    }
}

function doSelect(selection) {
    switch (selection) {
        // 非GM功能
        case 0:
            // openNpc("DailySign");
            cm.sendOk("该功能暂不支持，敬请期待！");
            cm.dispose();
            break;
        case 1:
            // openNpc("OnlineReward");
            cm.sendOk("该功能暂不支持，敬请期待！");
            cm.dispose();
            break;
        case 2:
            cm.warp(910000000);
            break;
        // GM功能
        case 61:
            // openNpc("SuperTeleport");
            cm.sendOk("该功能暂不支持，敬请期待！");
            cm.dispose();
            break;
        case 62:
            // openNpc("SuperShop");
            cm.sendOk("该功能暂不支持，敬请期待！");
            cm.dispose();
            break;
        case 63:
            openNpc("Salon");
            break;
        case 64:
            openNpc("UI查询");
            break;			
        default:
            cm.sendOk("该功能暂不支持，敬请期待！");
            cm.dispose();
    }
}

function openNpc(scriptName) {
    cm.dispose();
    cm.openNpc(1022101, scriptName);
}