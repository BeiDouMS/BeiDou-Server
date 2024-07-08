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
 Debbie
 -- By ---------------------------------------------------------------------------------------------
 Angel (get31720)
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Angel
 2.0 - Second Version by happydud3 & XotiCraze
 ---------------------------------------------------------------------------------------------------
 **/

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1 || mode == 0) {
        cm.sendOk("再见。");
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }

    var eim = cm.getEventInstance();
    if (eim == null) {
        cm.warp(680000000, 0);
        cm.dispose();
        return;
    }

    var isMarrying = (cm.getPlayer().getId() == eim.getIntProperty("groomId") || cm.getPlayer().getId() == eim.getIntProperty("brideId"));

    switch (status) {
        case 0:
            var hasEngagement = false;
            for (var x = 4031357; x <= 4031364; x++) {
                if (cm.haveItem(x, 1)) {
                    hasEngagement = true;
                    break;
                }
            }

            if (cm.haveItem(4000313) && isMarrying) {
                if (eim.getIntProperty("weddingStage") == 3) {
                    cm.sendOk("恭喜你们的婚礼。请与#b#p9201007##k交谈，开始婚礼后的派对。");
                    cm.dispose();
                } else if (hasEngagement) {
                    if (!cm.createMarriageWishlist()) {
                        cm.sendOk("您已经发送了您的愿望清单...");
                    }
                    cm.dispose();
                } else {
                    cm.sendOk("你没有所需的物品来继续参加这场婚礼。很遗憾，这就结束了…");
                }
            } else {
                if (eim.getIntProperty("weddingStage") == 3) {
                    if (!isMarrying) {
                        cm.sendYesNo("这对夫妇刚刚结婚，很快他们就会开始婚礼后的派对。你应该在这里等他们。你真的准备好退出这场婚礼，回到阿莫利亚了吗？");
                    } else {
                        cm.sendOk("恭喜你们的婚礼。请与#b#p9201007##k交谈，开始婚礼后的派对。");
                        cm.dispose();
                    }
                } else {
                    cm.sendYesNo("你确定要#r退出这场婚礼#k并返回#b阿莫利亚#k吗？");
                }
            }
            break;

        case 1:
            cm.warp(680000000, 0);
            cm.dispose();
            break;
    }
}