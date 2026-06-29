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
 *2013002.js - Minerva the Goddess
 *@author Ronan
 * Modified: Added hidden exchange for 10 diary pages -> completed diary (4161014) while keeping original rewards.
 */
var status = 0;
var exchangeConfirmed = false; // 标记是否确认兑换

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (cm.getPlayer().getMapId() == 920010100) { // Center tower
            if (status == 0) {
                cm.sendYesNo("我已经解除了阻止通往塔楼监狱储藏室的咒语。你可能会在那里找到一些好东西……或者，你可能想现在离开。你准备好离开了吗？");
            } else if (status == 1) {
                cm.getEventInstance().startEventTimer(60000); //bonus time 60s
                cm.getEventInstance().warpEventTeam(920011100); // 奖励关
                cm.dispose();
            }

        } else if (cm.getPlayer().getMapId() == 920011100) {
            if (status == 0) {
                cm.sendYesNo("所以，你准备好退出了吗？");
            } else if (status == 1) {
                cm.warp(920011300, 0);
                cm.dispose();
            }

        } else if (cm.getPlayer().getMapId() == 920011300) {
            // ========== 通关地图 ==========
            if (status == 0) {
                cm.sendNext("谢谢你不仅修复了雕像，还救出了我，让我脱离困境。愿女神的祝福与你同在，直到最后……作为感激之情，请接受这份纪念品，以表彰你的勇敢。");
            } else if (status == 1) {
                // 检查是否拥有全部10页日记
                var pages = [4001064, 4001065, 4001066, 4001067, 4001068, 4001069, 4001070, 4001071, 4001072, 4001073];
                var hasAll = true;
                for (var i = 0; i < pages.length; i++) {
                    if (!cm.haveItem(pages[i], 1)) {
                        hasAll = false;
                        break;
                    }
                }
                // 只能持有一个，持有多个会报错
                if (hasAll && !cm.haveItem(4161014)) {
                    // 询问是否用10页合成日记本（额外选项，不影响原奖励）
                    cm.sendYesNo("我注意到你收集了我散落的日记残页。你是否愿意将10张残页交给我，让我为你合成完整的《女神的日记本》？\r\n（合成后你依然可以获得原有的通关奖励）");
                    exchangeConfirmed = true;
                } else {
                    // 没有页数，直接领取原奖励
                    exchangeConfirmed = false;
                    cm.sendOk("接下来，请接受我为你准备的通关奖励。");
                }
            } else if (status == 2) {
                if (exchangeConfirmed) {
                    // 用户确认兑换，执行合成
                    var pages = [4001064, 4001065, 4001066, 4001067, 4001068, 4001069, 4001070, 4001071, 4001072, 4001073];
                    for (var i = 0; i < pages.length; i++) {
                        cm.gainItem(pages[i], -1);
                    }
                    cm.gainItem(4161014, 1); // 女神的日记本
                    cm.sendOk("感谢你帮我找回这些珍贵的记忆。这是合成后的《女神的日记本》，请收好。\r\n接下来，请接受我为你准备的通关奖励。");

                } else {
                    // 用户拒绝兑换，直接发放原奖励
                    giveOriginalReward();
                }
            } else if (status == 3)  {
	         if (exchangeConfirmed) {
	            // 合成后继续发放原奖励
                    giveOriginalReward();
		} else {
			cm.dispose();
		}
            }
        }
    }
}

function giveOriginalReward() {
    // 原有奖励：经验、女神的羽毛、传送出去（使用giveEventReward检查背包）
    if (cm.getEventInstance() != null && cm.getEventInstance().giveEventReward(cm.getPlayer()) && cm.canHold(4001158)) {
        cm.gainItem(4001158, 1); // 女神的羽毛
        cm.warp(200080101, 0);
        cm.dispose();
    } else {
        cm.sendOk("请先在您的背包中腾出空间（至少一个空位）。");
        cm.dispose();
    }
}