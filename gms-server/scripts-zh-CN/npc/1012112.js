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
                    cm.sendOk("月妙组队任务遇到了错误。");
                    cm.dispose();
                    return;
                } else if (cm.isUsingOldPqNpcStyle()) {
                    action(1, 0, 0);
                    return;
                }

                cm.sendSimple("#e#b<组队任务: 迎月花山丘>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n我是达尔利。这里有一座美丽的山丘，迎月花在那里盛开。山丘上住着一只老虎，名叫兴儿，它似乎在找吃的。你想前往迎月花山丘，与你的队友们联手帮助兴儿吗？#b\r\n#L0#我想参加组队任务。\r\n#L1#我想" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "禁用" : "启用") + "队伍搜索。\r\n#L2#我想了解更多详情。\r\n#L3#我想兑换一件年糕的帽子。");
            } else if (status == 1) {
                if (selection == 0) {
                    if (cm.getParty() == null) {
                        cm.sendOk("嗨！我是达尔利。这个地方笼罩着满月的神秘气息，任何人都不能独自进入这里。");
                        cm.dispose();
                    } else if (!cm.isLeader()) {
                        cm.sendOk("如果你想进入这里，你的队伍队长必须和我交谈。和你的队长说一下这件事。");
                        cm.dispose();
                    } else {
                        var eli = em.getEligibleParty(cm.getParty());
                        if (eli.size() > 0) {
                            if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                                cm.sendOk("已经有人在尝试这个组队任务了。请等待他们完成，或者寻找其他频道。");
                            }
                        } else {
                            cm.sendOk("你还不能开始这个组队任务，因为你的队伍人数可能不在规定范围内，或者你的一些队员不符合参与条件，又或者他们不在这个地图里。如果你在寻找队员方面有困难，可以试试队伍搜索。");
                        }

                        cm.dispose();
                    }
                } else if (selection == 1) {
                    var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                    cm.sendOk("你的队伍搜索状态现在是: #b" + (psState ? "启用" : "禁用") + "#k。如果你想再次更改状态，随时和我交谈。");
                    cm.dispose();
                } else if (selection == 2) {
                    cm.sendOk("#e#b<组队任务: 迎月花山丘>#k#n\r\n从地图底部的花朵上收集迎月花种子，然后把它们扔到舞台上方的平台旁边。迎月花种子的颜色必须匹配才能让种子生长，所以要不断尝试直到找到正确的组合。当所有种子都种下后，也就是任务的第二阶段开始了，在月妙为饥饿的兴儿准备年糕时进行侦查。一旦兴儿吃饱了，你的任务就完成了。");
                    cm.dispose();
                } else {
                    cm.sendYesNo("所以你想用 #b20 个 #b#t4001101##k 兑换这件专属设计的帽子吗？");
                }
            } else {
                if (cm.hasItem(4001101, 20)) {
                    if (cm.canHold(1002798)) {
                        cm.gainItem(4001101, -20);
                        cm.gainItem(1002798, 1);
                        cm.sendNext("给你。尽情享用！");
                    }
                } else {
                    cm.sendNext("你还没有足够的 #t4001101# 来兑换它！");
                }

                cm.dispose();
            }
        } else if (cm.getMapId() == 910010100) {
            if (status == 0) {
                cm.sendYesNo("感谢你帮助喂养兴儿。事实上，你们团队已经因走到这一步而获得了奖励。这个问题现在已经解决了，但现在又出现了另一个问题，如果你感兴趣，可以找那边的 #b达尔米#k 了解信息。那么，你现在要直接返回射手村吗？");
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
                cm.sendYesNo("那么，你现在要返回射手村吗？");
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