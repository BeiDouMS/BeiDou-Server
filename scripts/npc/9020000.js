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
 * @npc: Lakelis
 * @map: 103000000 - Kerning City
 * @func: Kerning PQ
 */

var status = 0;
var state;
var em = null;

function start() {
    status = -1;
    state = (cm.getMapId() >= 103000800 && cm.getMapId() <= 103000805) ? 1 : 0;
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
            if (state == 1) {
                cm.sendYesNo("要放弃了吗？");
            } else {
                em = cm.getEventManager("KerningPQ");
                if (em == null) {
                    cm.sendOk("废都组队任务脚本加载失败，请联系管理员。");
                    cm.dispose();
                } else if (cm.isUsingOldPqNpcStyle()) {
                    action(1, 0, 0);
                    return;
                }

                cm.sendSimple("#e#b<组队任务：废都下水道>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n" +
                    "想要和伙伴们一起接受考验吗？让你们的#b队长#k来找我开启挑战！#b\r\n" +
                    "#L0#开启组队任务\r\n" +
                    "#L3#单人挑战\r\n" +
                    "#L1##r" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "关闭" : "开启") + "#b 队伍搜索\r\n" +
                    "#L2#关于这个任务");
            }
        } else if (status == 1) {
            if (state == 1) {
                cm.warp(103000000);
                cm.dispose();
            } else {
                if (selection == 0) {
                    if (cm.getParty() == null) {
                        cm.sendOk("请先创建或者加入一支队伍");
                        cm.dispose();
                    } else if (!cm.isLeader()) {
                        cm.sendOk("请让你们的#b队长#k来");
                        cm.dispose();
                    } else {
                        var eli = em.getEligibleParty(cm.getParty());
                        if (eli.size() > 0) {
                            if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                                cm.sendOk("当前频道已经有另外一支队伍在挑战 #r组队任务#k 了，请耐心等待里面的队伍完成挑战，或者去其他频道看看。");
                            }
                        } else {
                            cm.sendOk("你的队伍不满足挑战要求。");
                        }

                        cm.dispose();
                    }
                } else if (selection == 1) {
                    var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                    cm.sendOk("队伍搜索已经: #b" + (psState ? "开启" : "关闭") + "#k");
                    cm.dispose();
                } else if (selection == 3) {
                    if (cm.getParty() == null) {
                        cm.sendOk("单人挑战也要先创建队伍");
                        cm.dispose();
                    } else if (cm.getParty().getPartyMembers().size() > 1) {
                        cm.sendOk("你队伍里不只有你一个人？！");
                        cm.dispose();
                    } else {
                        var eli = em.getEligibleParty(cm.getParty(), true); // 单人模式
                        if (eli.size() > 0) {
                            if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                                cm.sendOk("当前频道已经有另外一支队伍在挑战 #r组队任务#k 了，请耐心等待里面的队伍完成挑战，或者去其他频道看看。");
                            }
                        } else {
                            cm.sendOk("你不满足挑战要求。");
                        }

                        cm.dispose();
                    }
                } else {
                    cm.sendOk("#e#b<组队任务：废都下水道>#k#n\r\n" +
                        "emm。。。管理员懒得写了，如果你愿意写的话，请联系管理员。");
                    cm.dispose();
                }
            }
        }
    }
}