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
/* Dances with Balrog
	Warrior Job Advancement
	Victoria Road : Warriors' Sanctuary (102000003)

	Custom Quest 100003, 100005
*/

let status = -1;
let actionx = {"1stJob": false, "2ndjob": false, "3thJobI": false, "3thJobC": false};
let job = 110;

let spawnPnpc = false;
let spawnPnpcFee = 7000000;
let jobType = 1;

function start() {
    const GameConstants = Java.type('org.gms.constants.game.GameConstants');
    if (parseInt(cm.getJobId() / 100) === jobType && cm.canSpawnPlayerNpc(GameConstants.getHallOfFameMapid(cm.getJob()))) {
        spawnPnpc = true;

        let sendStr = "你看起来已经十分强大了，#r你想要在名人堂创建一个你自己的形象吗？";
        if (spawnPnpcFee > 0) {
            sendStr += "你只需要支付 #b " + cm.numberWithCommas(spawnPnpcFee) + " 金币。#k";
        }

        cm.sendYesNo(sendStr);
    } else {
        if (cm.getJobId() === 0) {
            actionx["1stJob"] = true;
            cm.sendNext("你想成为#r战士#k？#b你至少得10级，并且" + cm.getFirstJobStatRequirement(jobType) + "#k，让我看看你是否符合条件。");
        } else if (cm.getLevel() >= 30 && cm.getJobId() === 100) {
            actionx["2ndJob"] = true;
            if (cm.haveItem(4031012)) {
                cm.sendNext("你安全回来了！我果然没看错，你是一名合格的战士！现在你可以进行第二次转职了，有什么疑问就来问我吧。");
            } else if (cm.haveItem(4031008)) {
                cm.sendOk("去找#b#p1072000##k，他在#b#m102020300##k附近。");
                cm.dispose();
            } else {
                cm.sendNext("你取得了惊人的进步。");
            }
        } else if (actionx["3thJobI"] || (cm.getPlayer().gotPartyQuestItem("JB3") && cm.getLevel() >= 70 && (cm.getJobId() % 10 === 0 && parseInt(cm.getJobId() / 100) === 1 && !cm.getPlayer().gotPartyQuestItem("JBP")))) {
            actionx["3thJobI"] = true;
            cm.sendNext("我一直在等你！几天前，我从#b#p2020008##k听到了你的消息，你现在可以变得更强，但是你得通过我的考验。在蚂蚁洞广场有一个异界之门，里面有我的分身，你去击败它并带回#b#t4031059##k。");
        } else if (cm.getPlayer().gotPartyQuestItem("JBP") && !cm.haveItem(4031059)) {
            cm.sendNext("你还没有#b#t4031059##k。");
            cm.dispose();
        } else if (cm.haveItem(4031059) && cm.getPlayer().gotPartyQuestItem("JBP")) {
            actionx["3thJobC"] = true;
            cm.sendNext("你成功击败了我的分身并带回了#b#t4031059##k！看来你已经准备好第三次转职了，把#b#t4031057##k带给#b#p2020008##k，他会帮助你进行第三次转职的，祝你好运！");
        } else {
            cm.sendOk("你的选择很明智！");
            cm.dispose();
        }
    }
}

function action(mode, type, selection) {
    status++;
    if (mode === -1 && selection === -1) {
        cm.dispose();
        return;
    } else if (mode === 0 && type !== 1) {
        status -= 2;
    }

    if (status === -1) {
        start();
        return;
    } else {
        if (spawnPnpc) {
            if (mode > 0) {
                if (cm.getMeso() < spawnPnpcFee) {
                    cm.sendOk("你的金币不够。");
                    cm.dispose();
                    return;
                }

                const PlayerNPC = Java.type('org.gms.server.life.PlayerNPC');
                const GameConstants = Java.type('org.gms.constants.game.GameConstants');
                if (PlayerNPC.spawnPlayerNPC(GameConstants.getHallOfFameMapid(cm.getJob()), cm.getPlayer())) {
                    cm.sendOk("你的形象已成功入驻名人堂。");
                    cm.gainMeso(-spawnPnpcFee);
                } else {
                    cm.sendOk("抱歉，名人堂目前位置已满。");
                }
            }

            cm.dispose();
            return;
        } else {
            if (mode !== 1 || status === 7 && type !== 1 || (actionx["1stJob"] && status === 4) || (cm.haveItem(4031008) && status === 2) || (actionx["3thJob"] && status === 1)) {
                if (mode === 0 && status === 2 && type === 1) {
                    cm.sendOk("想好了再来找我吧。");
                }
                if (!(mode === 0 && type !== 1)) {
                    cm.dispose();
                    return;
                }
            }
        }
    }

    if (actionx["1stJob"]) {
        if (status === 0) {
            if (cm.getLevel() >= 10 && cm.canGetFirstJob(jobType)) {
                cm.sendNextPrev("看来你符合条件，请注意：#r一旦转职就不能再更换职业了#k，如果还没想好就点#b结束对话#k。");
            } else {
                cm.sendOk("你不满足成为#r战士#k的要求，继续努力吧。");
                cm.dispose();
            }
        } else if (status === 1) {
            if (cm.canHold(1302077)) {
                if (cm.getJobId() === 0) {
                    cm.changeJobById(100);
                    cm.gainItem(1302077, 1);
                    cm.resetStats();
                }
                cm.sendNext("从此刻起，你正式踏上了战士之路。这不是一件容易的事情，但只要你有足够的勇气和信心，你将克服所有的困难。");
            } else {
                cm.sendNext("背包满了");
                cm.dispose();
            }
        } else if (status === 2) {
            cm.sendNextPrev("作为转职奖励，我给你每个背包都增加了4个格子，另外我还给了你一点 SP你可以用它来提升技能。");
        } else if (status === 3) {
            cm.sendNextPrev("好了，你已经是一名合格的战士了！");
        } else {
            cm.dispose();
        }
    } else if (actionx["2ndJob"]) {
        if (status === 0) {
            if (cm.haveItem(4031012)) {
                cm.sendSimple("很好，那你接下来想#b\r\n#L0#介绍 剑客\r\n#L1#介绍 准骑士\r\n#L2#介绍 枪战士\r\n#L3#选择职业");
            } else {
                cm.sendNext("现在你可以准备第二次转职测试了。");
                if (!cm.isQuestStarted(100003)) {
                    cm.startQuest(100003);
                }
            }
        } else if (status === 1) {
            if (!cm.haveItem(4031012)) {
                if (cm.canHold(4031008)) {
                    if (!cm.haveItem(4031008)) {
                        cm.gainItem(4031008, 1);
                    }
                    cm.sendNextPrev("请把这封信带给#b#p1072000##k，他在#b#m102020300##k附近。");
                } else {
                    cm.sendNext("#r其他栏满了 -> #r其他栏已满");
                    cm.dispose();
                }
            } else {
                if (selection < 3) {
                    if (selection === 0) {// 介绍剑客
                        cm.sendNext("使用#r剑或斧#k的战士。\r\n\r\n剑客可以提高队伍的物理输出，完成第四次转职后拥有不俗的输出能力。");
                    } else if (selection === 1) {// 介绍准骑士
                        cm.sendNext("使用#r剑或钝器#k的战士。\r\n\r\n准骑士可以降低怪物属性，完成第三次转职后拥有属性攻击能力。");
                    } else {// 介绍枪战士
                        cm.sendNext("使用#r枪或矛#k的战士。\r\n\r\n枪战士可以提高队伍的生存能力，完成第三次转职后拥有不俗的输出能力。");
                    }

                    status -= 2;
                } else {
                    cm.sendSimple("请选择你的转职方向#b\r\n#L0#剑客\r\n#L1#准骑士\r\n#L2#枪战士");
                }
            }
        } else if (status === 2) {
            if (cm.haveItem(4031008)) {
                cm.dispose();
                return;
            }
            job += selection * 10;
            cm.sendYesNo("你确定要转职为" + (job === 110 ? "#b剑客#k" : job === 120 ? "#b准骑士#k" : "#b枪战士#k") + "？一旦选择，就不能再更改了。");
        } else if (status === 3) {
            if (cm.haveItem(4031012)) {
                cm.gainItem(4031012, -1);
            }
            cm.completeQuest(100005);

            if (job === 110) {
                cm.sendNext("恭喜你成为了一名剑客！");
            } else if (job === 120) {
                cm.sendNext("恭喜你成为了一名#b准骑士#k！");
            } else {
                cm.sendNext("恭喜你成为了一名#b枪战士#k！");
            }
            if (cm.getJobId() !== job) {
                cm.changeJobById(job);
            }
        } else if (status === 4) {
            cm.sendNextPrev("现在你可以开始学习#b" + (job === 110 ? "剑客" : job === 120 ? "准骑士" : "枪战士") + "#k的技能了，我扩展你的血量和背包空间。");
        } else if (status === 5) {
            cm.sendNextPrev("我还给了你一点二转的 #bSP#k，打开技能栏学习二转技能。");
        } else if (status === 6) {
            cm.sendNextPrev("好了继续你的旅途吧，下一次转职要等你70级了再来找我。");
        }
    } else if (actionx["3thJobI"]) {
        if (status === 0) {
            if (cm.getPlayer().gotPartyQuestItem("JB3")) {
                cm.getPlayer().removePartyQuestItem("JB3");
                cm.getPlayer().setPartyQuestItemObtained("JBP");
            }
            cm.sendNextPrev("我的分身相当强大，他会使用特殊技能，你应该跟他一对一战斗，击败他并带回#b#t4031059##k，祝你好运！");
        }
    } else if (actionx["3thJobC"]) {
        cm.getPlayer().removePartyQuestItem("JBP");
        cm.gainItem(4031059, -1);
        cm.gainItem(4031057, 1);
        cm.dispose();
    }
}

/* 3th Job Part
	PORTAL 20 MINUTES.
 */