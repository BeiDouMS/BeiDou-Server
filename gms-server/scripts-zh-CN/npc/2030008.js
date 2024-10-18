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
/* Adobis
 * 
 * El Nath - The Door to Zakum (211042300)
 * 
 * Vs Zakum Recruiter NPC
 * 
 * Custom Quest 100200 = Whether you can start Zakum PQ
 * Custom Quest 100201 = Whether you have done the trials
*/

var status;
var em;
var selectedType;
var gotAllDocs;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (cm.haveItem(4001109, 1)) {
            cm.warp(921100000, "out00");
            cm.dispose();
            return;
        }

        if (!(cm.isQuestStarted(100200) || cm.isQuestCompleted(100200))) {   // thanks Vcoc for finding out a need of reapproval from the masters for Zakum expeditions
            if (cm.getPlayer().getLevel() >= 50) {  // thanks Z1peR for noticing not-so-clear unmet requirements message here.
                cm.sendOk("小心，古老的力量并未被遗忘……如果你希望有朝一日击败#r扎昆#k，首先要获得#b首领之家议会#k的批准，然后#b面对考验#k，只有这样你才有资格进行战斗。");
            } else {
                cm.sendOk("小心，古老的力量并未被遗忘……");
            }

            cm.dispose();
            return;
        }

        em = cm.getEventManager("ZakumPQ");
        if (em == null) {
            cm.sendOk("扎昆组队任务遇到了一个错误。");
            cm.dispose();
            return;
        }

        if (status == 0) {
            cm.sendSimple("#e#b<组队任务：扎昆战役>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n小心，古老的力量并未被遗忘... #b\r\n#L0#进入未知的死亡矿井（第1阶段）#l\r\n#L1#面对熔岩之息（第2阶段）#l\r\n#L2#锻造火眼（第3阶段）#l");
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
                if (cm.haveItem(4031061) && !cm.haveItem(4031062)) {
                    cm.sendYesNo("你已经成功通过了第一阶段。你还有很长的路才能到达扎昆的祭台。所以，你想好挑战下一个阶段了吗？");
                } else {
                    if (cm.haveItem(4031062)) {
                        cm.sendNext("你已经得到了#b熔岩之息#k，你不需要完成这个阶段。");
                    } else {
                        cm.sendNext("请先完成之前的试炼。");
                    }
                    cm.dispose();
                }
            } else {
                if (cm.haveItem(4031061) && cm.haveItem(4031062)) {
                    if (!cm.haveItem(4000082, 30)) {
                        cm.sendOk("你已经完成了试炼，但是还需要 #b#v4000082##t4000082# * 30#k 来锻造 #b5 个 #v4001017##t4001017##k。");
                    } else {
                        cm.completeQuest(100201);
                        cm.gainItem(4031061, -1);
                        cm.gainItem(4031062, -1);
                        cm.gainItem(4000082, -30);

                        cm.gainItem(4001017, 5);
                        cm.sendNext("你 #r已经完成了试炼#k，从现在开始我批准你挑战扎昆。");
                    }

                    cm.dispose();
                } else {
                    cm.sendOk("你缺少一些必要的物品\r\n#b#v4031061##t4031061# * 1#k\r\n#b#v4031062##t4031062# * 1#k\r\n来锻造#b#v4001017##t4001017##k。");
                    cm.dispose();
                }
            }
        } else if (status == 2) {
            cm.warp(280020000, 0);
            cm.dispose();
        }
    }
}