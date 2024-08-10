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

/*
    NPC ID: 1052013 
    NPC NAME: Computer
    @author Ronan
*/

var status;
var pqArea;

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

        if (cm.getMapId() != 193000000) {
            var eim = cm.getEventInstance();

            if (status == 0) {
                if (!eim.isEventCleared()) {
                    var couponsNeeded = eim.getIntProperty("couponsNeeded");

                    if (cm.isEventLeader()) {
                        if (cm.haveItem(4001007, couponsNeeded)) {
                            cm.sendNext("你的团队收集了所有需要的优惠券，干得好！");
                            cm.gainItem(4001007, couponsNeeded);
                            eim.clearPQ();

                            cm.dispose();

                        } else {
                            cm.sendYesNo("你的团队必须收集#r" + couponsNeeded + "#k张优惠券才能完成这个副本。当你手头有足够的数量时，和我交谈...或者你想#b现在退出#k？请注意，如果你现在退出，#r你的团队也将被迫退出#k。");
                        }
                    } else {
                        cm.sendYesNo("你的队伍必须收集#r" + couponsNeeded + "#k张优惠券才能完成这个副本。让你的队长带着正确数量的优惠券来找我谈谈……或者你想要#b现在退出#k吗？请注意，如果你现在退出，你的队伍#r可能会人手不足#k以继续进行这个副本。");
                    }
                } else {
                    if (!eim.giveEventReward(cm.getPlayer())) {
                        cm.sendOk("请在您的ETC库存中腾出一个空间来接收奖品。");
                        cm.dispose();
                    } else {
                        cm.warp(193000000);
                        cm.dispose();
                    }
                }
            } else if (status == 1) {
                cm.warp(193000000);
                cm.dispose();
            }
        } else {
            var levels = ["#m190000000#", "#m191000000#", "#m192000000#", "#m195000000#", "#m196000000#", "#m197000000#"];
            if (status == 0) {
                var sendStr = "高级道路是一个聚集了各种类型怪物，刷经验、刷装备的理想场所。 #p1052014# 选择您想要面对的区域：\r\n\r\n#b";
                for (var i = 0; i < 6; i++) {
                    sendStr += "#L" + i + "#" + levels[i] + "#l\r\n";
                }

                cm.sendSimple(sendStr);
            } else if (status == 1) {
                pqArea = selection + 1;

                em = cm.getEventManager("CafePQ_" + pqArea);
                if (em == null) {
                    cm.sendOk("CafePQ_ + pqArea + 遇到了一个错误。");
                    cm.dispose();
                    return;
                } else if (cm.isUsingOldPqNpcStyle()) {
                    status = 1;
                    action(1, 0, 0);
                    return;
                }

                cm.sendSimple("#e#b<组队任务：高级之路 - " + levels[selection] + ">\r\n#k#n" + em.getProperty("party") + "\r\n\r\n#p1052014# 的操作方式与普通的不同。它们不使用金币或扭蛋券，而是使用 #r橡皮擦#k，可以通过完成高级之路上的任务获得。要前往那里，你必须找到队友并参加一个组队任务。当组队并准备好后，让你的 #b队长#k 与我交谈。#b\r\n#L0#我想参加组队任务。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "禁用" : "启用") + "组队搜索。\r\n#L2#我想了解更多详情。");
            } else if (status == 2) {
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
                            cm.sendOk("你目前无法开始这个组队任务，因为你的队伍可能不符合人数要求，有些队员可能不符合尝试条件，或者他们不在这张地图上。如果你找不到队员，可以尝试使用组队搜索功能。");
                        }

                        cm.dispose();
                    }
                } else if (selection == 1) {
                    var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                    cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "启用" : "禁用") + "#k。想要改变状态时随时找我谈谈。");
                    cm.dispose();
                } else {
                    cm.sendOk("#e#b<组队任务：高级之路>#k#n\r\n在前方的地图上，你将面对许多普通等级的怪物。击败它们，收集所有需要的优惠券交给我。然后所有成员将获得一个橡皮擦，与所面对的等级相对应。在机器上插入#b多个相同的橡皮擦或多种不同的橡皮擦#k，以获得更大奖品的机会。");
                    cm.dispose();
                }
            }
        }
    }
}