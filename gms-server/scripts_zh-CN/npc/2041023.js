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
//First version thanks to Moogra

/**
 * @author: Ronan
 * @npc: Flo
 * @map: Ludibrium - Path of Time (220050300)
 * @func: Elemental Thanatos room
 */

var status = 0;
var em = null;

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
            if (!(cm.isQuestCompleted(6316) && (cm.isQuestStarted(6225) || cm.isQuestStarted(6315)))) {
                cm.sendOk("你似乎没有理由去遇见基于元素的萨那托斯。");
                cm.dispose();
                return;
            }

            em = cm.getEventManager("ElementalBattle");
            if (em == null) {
                cm.sendOk("元素战斗遇到了一个错误。");
                cm.dispose();
                return;
            } else if (cm.isUsingOldPqNpcStyle()) {
                action(1, 0, 0);
                return;
            }

            cm.sendSimple("#e#b<组队任务：元素塔纳托斯>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n你正在寻找元素塔纳托斯，对吗？如果你和另一个法师组队，而且他的元素属性与你相反，你们就能够克服它们。作为队长，当你准备好出发时，和我交谈。#b\r\n#L0#我想参加组队任务。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "禁用" : "启用") + "组队搜索。\r\n#L2#我想了解更多细节。");
        } else if (status == 1) {
            if (selection == 0) {
                if (cm.getParty() == null) {
                    cm.sendOk("只有当你加入一个队伍时，你才能参加派对任务。");
                    cm.dispose();
                } else if (!cm.isLeader()) {
                    cm.sendOk("你的队长必须与我交谈才能开始这个组队任务。");
                    cm.dispose();
                } else {
                    var eli = em.getEligibleParty(cm.getParty());
                    if (eli.size() > 0) {
                        if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                            cm.sendOk("另一个队伍已经进入了该频道的#r组队任务#k。请尝试其他频道，或者等待当前队伍完成。");
                        }
                    } else {
                        cm.sendOk("你目前无法开始这个组队任务，因为你的队伍可能不符合人数要求，有些队员可能不符合参与条件，或者他们不在这张地图上。如果你找不到队员，可以尝试使用组队搜索功能。");
                    }

                    cm.dispose();
                }
            } else if (selection == 1) {
                var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "enabled" : "disabled") + "#k。想要改变状态时随时找我。");
                cm.dispose();
            } else {
                cm.sendOk("#e#b<组队任务：元素死神>#k#n\r\n在进入舞台之前，与另一位具有#rdifferent elemental affinity#k的法师组队。这个团队合作对于克服内部的元素非常关键。");
                cm.dispose();
            }
        }
    }
}