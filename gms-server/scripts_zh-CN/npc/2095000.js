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
/* Delli
	Looking for Delli 3 (925010200)
	Hypnotize skill quest NPC.
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.getMapId() != 925010400) {
                em = cm.getEventManager("DelliBattle");
                if (em == null) {
                    cm.sendOk("德利战斗遇到了一个错误。");
                    cm.dispose();
                    return;
                } else if (cm.isUsingOldPqNpcStyle()) {
                    action(1, 0, 0);
                    return;
                }

                cm.sendSimple("#e#b<组队任务：拯救 Delli>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n啊，#r#p1095000##k 让你来的？她担心我吗？... 很抱歉听到这个消息，但我现在真的不能回去，一些怪物受到黑魔法师的影响，我需要解救它们！... 你似乎也不会接受这个事实，对吗？你愿意和队友一起帮助我吗？如果愿意，请让你的 #b队长#k 和我交谈。#b\r\n#L0#我想参加组队任务。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "关闭" : "开启") + " 组队搜索。\r\n#L2#我想了解更多细节。");
            } else {
                cm.sendYesNo("任务成功了，感谢你的护送！我可以带你去#b#m120000104##k，你准备好了吗？");
            }
        } else if (status == 1) {
            if (cm.getMapId() != 925010400) {
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
                    cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "enabled" : "disabled") + "#k。想要改变状态时随时找我。");
                    cm.dispose();
                } else {
                    cm.sendOk("#e#b<组队任务：拯救戴利>#k#n\r\n 一场伏击正在进行中！我必须在战场上站立大约6分钟才能完成解放，请在此期间保护我，以便完成我的任务。");
                    cm.dispose();
                }
            } else {
                cm.warp(120000104);
                cm.dispose();
            }
        }
    }
}