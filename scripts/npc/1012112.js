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
                    cm.sendOk("加载脚本迎月花山丘组队任务失败，请联系管理员");
                    cm.dispose();
                    return;
                } else if (cm.isUsingOldPqNpcStyle()) {
                    action(1, 0, 0);
                    return;
                }

                cm.sendSimple("#e#b<组队任务：迎月花山丘>\r\n" +
                    "#k#n" + em.getProperty("party") + "\r\n\r\n" +
                    "这里面有一座美丽的山丘，山上开满了迎月花，还住着一只老虎，这只老虎现在看起来饥肠辘辘的。你愿意和同伴一起去迎月花山丘帮助它吗？#b\r\n" +
                    "#L0#开启组队任务\r\n" +
                    "#L1#" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "关闭" : "开启") + " 组队搜索\r\n" +
                    "#L2#关于这个任务\r\n" +
                    "#L3#兑换头顶年糕");
            } else if (status == 1) {
                if (selection == 0) {
                    if (cm.getParty() == null) {
                        cm.sendOk("你要先创建或者加入一支队伍才可以进去。");
                        cm.dispose();
                    } else if (!cm.isLeader()) {
                        cm.sendOk("请让队长来和我对话。");
                        cm.dispose();
                    } else {
                        var eli = em.getEligibleParty(cm.getParty());
                        if (eli.size() > 0) {
                            if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                                cm.sendOk("有其他队伍已经在里面了，请耐心等待他们出来或者换个频道试试。");
                            }
                        } else {
                            cm.sendOk("看起来你队伍里有人不满足任务限制。");
                        }

                        cm.dispose();
                    }
                } else if (selection == 1) {
                    var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                    cm.sendOk("队伍搜索功能已 #b" + (psState ? "开启" : "关闭"));
                    cm.dispose();
                } else if (selection == 2) {
                    cm.sendOk("#e#b<组队任务：迎月花山丘>#k#n\r\n" +
                        "搜集6种颜色的种子，并把它们种在月亮周围的六个平台上。把种子放在正确的平台上就会开出迎月花来，当六个平台开满迎月花，月亮就会变成满月，同时月妙将会出来捣年糕，收集#r10#k个年糕给老虎吃吧。");
                    cm.dispose();
                } else {
                    cm.sendYesNo("你要用#r20副本积分#k交换#b#t1002798##k吗？");
                }
            } else {
                if (cm.getPlayer().getPQPoint() >= 20) {
                    if (cm.canHold(1002798)) {
                        cm.getPlayer().gainPQPoint(-20);
                        cm.gainItem(1002798, 1, true, true);
                        cm.sendNext("请拿好！");
                    } else {
                        cm.sendOk("#r你的背包满了。")
                    }
                } else {
                    cm.sendNext("你的副本积分不够20点啊（你现在有#r" + cm.getPlayer().getPQPoint() + "副本积分#k）");
                }

                cm.dispose();
            }
        } else if (cm.getMapId() == 910010100) {
            if (status == 0) {
                cm.sendYesNo("你们的事迹#b#p1012114##k已经跟我说了，感谢你的帮助。如果你还觉得够尽兴的话可以去找#b#p1012113##k。要我带你返回射手村吗？");
            } else if (status == 1) {
                if (cm.getEventInstance().giveEventReward(cm.getPlayer())) {
                    cm.warp(100000200);
                } else {
                    cm.sendOk("#r你的背包满了。");
                }
                cm.dispose();
            }
        } else if (cm.getMapId() == 910010400) {
            if (status == 0) {
                cm.sendYesNo("要回射手村了吗？");
            } else if (status == 1) {
                if (cm.getEventInstance() == null) {
                    cm.warp(100000200);
                } else if (cm.getEventInstance().giveEventReward(cm.getPlayer())) {
                    cm.warp(100000200);
                } else {
                    cm.sendOk("#r你的背包满了。");
                }
                cm.dispose();
            }
        }
    }
}