/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
 * @author: Ronan
 * @npc: Ellin
 * @map: 300030100 - Deep Fairy Forest
 * @func: Ellin PQ
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
            em = cm.getEventManager("EllinPQ");
            if (em == null) {
                cm.sendOk("艾琳组队任务遇到了一个错误。");
                cm.dispose();
                return;
            } else if (cm.isUsingOldPqNpcStyle()) {
                action(1, 0, 0);
                return;
            }

            cm.sendSimple("#e#b<组队任务：毒雾森林>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n你想要组建或加入一个队伍来解决#b毒雾森林#k的谜题吗？让你的#b队伍领袖#k和我交谈或者自己组建一个队伍。#b\r\n#L0#我想参加组队任务。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "禁用" : "启用") + "组队搜索。\r\n#L2#我想了解更多细节。\r\n#L3#我想要领取奖励。");
        } else if (status == 1) {
            if (selection == 0) {
                if (cm.getParty() == null) {
                    cm.sendOk("只有当你加入一个队伍时，才能参加派对任务。");
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
                        cm.sendOk("你目前无法开始这个组队任务，因为你的队伍可能不符合人数要求，有些队员可能不符合尝试条件，或者他们不在这张地图上。如果你找不到队员，可以尝试使用组队搜索功能。");
                    }

                    cm.dispose();
                }
            } else if (selection == 1) {
                var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "启用" : "禁用") + "#k。想要改变状态时随时找我谈谈。");
                cm.dispose();
            } else if (selection == 2) {
                cm.sendOk("#e#b<组队任务：毒雾森林>#k#n\r\n在这个组队任务中，你的任务是逐步穿越森林，对抗路上的所有坏家伙，解决你遇到的许多谜题，并团结一致，充分发挥团队合作的优势，以克服时间限制和强大的生物。击败最终BOSS后，你的团队有机会获得一个大理石，#b当它被喷泉在出口地图上掉落时#k，将会确保团队获得额外的奖励。祝你好运。");
                cm.dispose();
            } else {
                cm.sendSimple("那么，你想要获得什么奖品？\r\n#b#L0#给我阿尔泰耳环。\r\n#L1#给我闪亮的阿尔泰耳环。\r\n#L2#给我闪耀的阿尔泰耳环。");
            }
        } else if (status == 2) {
            if (selection == 0) {
                if (!cm.haveItem(1032060) && cm.haveItem(4001198, 10)) {
                    cm.gainItem(1032060, 1);
                    cm.gainItem(4001198, -10);
                    cm.dispose();
                } else {
                    cm.sendOk("你要么已经有了阿尔泰耳环，要么没有10个阿尔泰碎片。");
                    cm.dispose();
                }
            } else if (selection == 1) {
                if (cm.haveItem(1032060) && !cm.haveItem(1032061) && cm.haveItem(4001198, 10)) {
                    cm.gainItem(1032060, -1);
                    cm.gainItem(1032061, 1);
                    cm.gainItem(4001198, -10);
                    cm.dispose();
                } else {
                    cm.sendOk("你要么还没有阿尔泰耳环，要么没有10个阿尔泰碎片。");
                    cm.dispose();
                }
            } else if (selection == 2) {
                if (cm.haveItem(1032061) && !cm.haveItem(1032072) && cm.haveItem(4001198, 10)) {
                    cm.gainItem(1032061, -1);
                    cm.gainItem(1032072, 1);    // thanks yuxaij for noticing unexpected itemid here
                    cm.gainItem(4001198, -10);
                    cm.dispose();
                } else {
                    cm.sendOk("你要么还没有闪耀的阿尔泰尔耳环，要么没有10个阿尔泰尔碎片。");
                    cm.dispose();
                }
            }
        }
    }
}