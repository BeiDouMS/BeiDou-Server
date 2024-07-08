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
 * @NPC Tory
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

        if (cm.getMapId() == 100000200) {
            if (status == 0) {
                em = cm.getEventManager("HenesysPQ");
                if (em == null) {
                    cm.sendOk("冒险岛的废弃都市组队任务遇到了一个错误。");
                    cm.dispose();
                    return;
                } else if (cm.isUsingOldPqNpcStyle()) {
                    action(1, 0, 0);
                    return;
                }

                cm.sendSimple("#e#b<组队任务：樱草山>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n我是托里。这里是一个美丽的山丘，樱草盛开。山里有一只老虎，叫做格劳利，他似乎在寻找食物。你想前往樱草山，与你的队友一起帮助格劳利吗？#b\r\n#L0#我想参加组队任务。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "关闭" : "开启") + "组队搜索。\r\n#L2#我想了解更多细节。\r\n#L3#我想兑换一个实例帽。");
            } else if (status == 1) {
                if (selection == 0) {
                    if (cm.getParty() == null) {
                        cm.sendOk("嗨！我是托里。这个地方充满了神秘的满月光环，没有一个人可以独自进入这里。");
                        cm.dispose();
                    } else if (!cm.isLeader()) {
                        cm.sendOk("如果你想进入这里，你们队伍的领袖必须和我交谈。和你们队伍的领袖谈谈这件事。");
                        cm.dispose();
                    } else {
                        var eli = em.getEligibleParty(cm.getParty());
                        if (eli.size() > 0) {
                            if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                                cm.sendOk("有人已经在尝试进行组队任务了。请等待他们完成，或者换到另一个频道。");
                            }
                        } else {
                            cm.sendOk("你目前无法开始这个组队任务，因为你的队伍可能不符合人数要求，有些队员可能不符合参与条件，或者他们不在这张地图上。如果你找不到队员，可以尝试使用组队搜索功能。");
                        }

                        cm.dispose();
                    }
                } else if (selection == 1) {
                    var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                    cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "启用" : "禁用") + "#k。想要改变状态时随时找我谈谈。");
                    cm.dispose();
                } else if (selection == 2) {
                    cm.sendOk("#e#b<组队任务：樱草山>#k#n\r\n在地图的底部从花朵上收集樱草种子，并将它们放在舞台上方的平台上。樱草种子的颜色必须匹配才能生长，所以要不断尝试，直到找到正确的组合。当所有种子都被种植后，也就是任务的第二部分开始时，要在月兔准备年糕给饥饿的格罗利时进行侦查。一旦格罗利满意，你的任务就完成了。");
                    cm.dispose();
                } else {
                    cm.sendYesNo("所以你想要用#b20 #b#t4001158##k来交换那个特殊设计的帽子吗？");
                }
            } else {
                if (cm.hasItem(4001158, 20)) {
                    if (cm.canHold(1002798)) {
                        cm.gainItem(4001158, -20);
                        cm.gainItem(1002798, 20);
                        cm.sendNext("这就是。尽情享受！");
                    }
                } else {
                    cm.sendNext("你还没有足够的 #t4001158# 来购买它！");
                }

                cm.dispose();
            }
        } else if (cm.getMapId() == 910010100) {
            if (status == 0) {
                cm.sendYesNo("谢谢你帮助喂养Growlie。事实上，你的团队已经因为达到这一步而得到了奖励。现在问题已经解决，但现在又出现了另一个问题，如果你感兴趣的话，可以去#bTommy#k那里查看信息。那么，你现在要直接回到Henesys吗？");
            } else if (status == 1) {
                if (cm.getEventInstance().giveEventReward(cm.getPlayer())) {
                    cm.warp(100000200);
                } else {
                    cm.sendOk("看起来你的某个背包空间不足。请先检查一下，以便正确获得奖励。");
                }
                cm.dispose();
            }
        } else if (cm.getMapId() == 910010400) {
            if (status == 0) {
                cm.sendYesNo("那么，你现在要回到明斯港吗？");
            } else if (status == 1) {
                if (cm.getEventInstance() == null) {
                    cm.warp(100000200);
                } else if (cm.getEventInstance().giveEventReward(cm.getPlayer())) {
                    cm.warp(100000200);
                } else {
                    cm.sendOk("看起来你的某个背包空间不足。请先检查一下，以便正确获得奖励。");
                }
                cm.dispose();
            }
        }
    }
}