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
 * @npc: Mark of the Squad
 * @map: Cave of Life - Cave Entrance (240050000)
 * @func: Horntail PQ
 */

var status = 0;
var price = 100000;
var em = null;
var hasPass;

function isRecruitingMap(mapid) {
    return mapid == 240050000;
}

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

        if (isRecruitingMap(cm.getMapId())) {
            if (status == 0) {
                em = cm.getEventManager("HorntailPQ");
                if (em == null) {
                    cm.sendOk("霍恩尾巴洞窟遭遇了一个错误。");
                    cm.dispose();
                    return;
                } else if (cm.isUsingOldPqNpcStyle()) {
                    action(1, 0, 0);
                    return;
                }

                cm.sendSimple("#e#b<组队任务：暴君蛋龙试炼场>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n这是通往暴君蛋龙巢穴的路径。如果你想面对他，你和你的队伍将在前方的试炼场上接受考验。#b\r\n#L0#让我们通过试炼场。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "禁用" : "启用") + "组队搜索。\r\n#L2#我想听更多细节。");
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
                            cm.sendOk("要么我不能接受你队伍中的某些成员进入洞穴，要么你的队伍不够强大。解决这个问题然后再来找我谈话！");
                        }

                        cm.dispose();
                    }
                } else if (selection == 1) {
                    var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                    cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "enabled" : "disabled") + "#k。想要改变状态时随时找我。");
                    cm.dispose();
                } else {
                    cm.sendOk("#e#b<组队任务：暴君之地>#k#n\r\n作为暴君巢穴的守门人，我只会允许值得的人进入。即使是对于那些人来说，内部的路径也像迷宫一样，充满了分支和考验。然而，那些擅长与团队BOSS战斗的人有更好的机会站在我们的领袖面前，尽管我们这种人也有机会。");
                    cm.dispose();
                }
            }
        } else {
            if (!cm.isEventLeader()) {
                cm.sendOk("只有你的队伍领袖才能与日程表进行交互。");
            } else if (cm.getMapId() == 240050100) {
                if (cm.haveItem(4001087) && cm.haveItem(4001088) && cm.haveItem(4001089) && cm.haveItem(4001090) && cm.haveItem(4001091)) {
                    cm.gainItem(4001087, -1);
                    cm.gainItem(4001088, -1);
                    cm.gainItem(4001089, -1);
                    cm.gainItem(4001090, -1);
                    cm.gainItem(4001091, -1);

                    cm.getEventInstance().warpEventTeam(240050200);
                } else {
                    cm.sendOk("你没有所有需要的钥匙来继续前进。");
                }
            } else if (cm.getMapId() == 240050300) {
                if (cm.haveItem(4001092, 1) && cm.haveItem(4001093, 6)) {
                    cm.gainItem(4001092, -1);
                    cm.gainItem(4001093, -6);
                    cm.getEventInstance().clearPQ();
                } else {
                    cm.sendOk("检查一下你是否带着6把红钥匙和1把蓝钥匙。");
                }
            } else if (cm.getMapId() == 240050310) {
                if (cm.haveItem(4001092, 1) && cm.haveItem(4001093, 6)) {
                    cm.gainItem(4001092, -1);
                    cm.gainItem(4001093, -6);
                    cm.getEventInstance().clearPQ();
                } else {
                    cm.sendOk("检查一下你是否带着所有6把红钥匙和1把蓝钥匙。");
                }
            }

            cm.dispose();
        }
    }
}