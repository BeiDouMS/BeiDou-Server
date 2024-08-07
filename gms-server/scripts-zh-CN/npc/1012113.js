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
 * @author BubblesDev
 * @author Ronan
 * @npc Tommy (HPQ)
 */
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
    } else {
        status++;
        if (cm.getPlayer().getMap().getId() == 910010100) { //Clear map
            if (status == 0) {
                cm.sendNext("你好！我是汤米。我们站的附近有一个猪镇。那里的猪很吵闹，无法控制，甚至偷了许多旅行者的武器。它们被赶出了自己的城镇，目前躲藏在猪镇里。");
            } else if (status == 1) {
                if (cm.isEventLeader()) {
                    cm.sendNext("你觉得和你的队员一起去那里，给那些吵闹的猪一个教训怎么样？");
                } else {
                    cm.sendOk("感兴趣吗？告诉你的队长和我联系，前往那里！");
                    cm.dispose();

                }
            } else if (status == 2) {
                cm.getEventInstance().startEventTimer(5 * 60000);
                cm.getEventInstance().warpEventTeam(910010200);

                cm.dispose();

            }
        } else if (cm.getPlayer().getMap().getId() == 910010200) { //Bonus map
            if (status == 0) {
                cm.sendYesNo("你想现在退出奖励吗？");
            } else {
                cm.warp(910010400);
                cm.dispose();

            }
        } else if (cm.getPlayer().getMap().getId() == 910010300) { //Exit map
            if (status == 0) {
                cm.sendOk("你现在将被传送出去，谢谢你的帮助！");
            } else {
                cm.warp(100000200);
                cm.dispose();

            }
        }
    }
}