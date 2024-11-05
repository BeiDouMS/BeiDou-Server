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

//** Archer 3rd Job Instructor Rene

status = -1;
var job;
var sel;
actionx = {"Mental": false, "Physical": false};

function start() {
    var jobBase = parseInt(cm.getJobId() / 100);
    var jobStyle = 3;
    if (!(cm.getPlayer().getLevel() >= 70 && jobBase == jobStyle && cm.getJobId() % 10 == 0)) {
        if (cm.getPlayer().getLevel() >= 50 && jobBase % 10 == jobStyle) {
            status++;
            action(1, 0, 1);
            return;
        }

        cm.sendNext("你好。");
        cm.dispose();
        return;
    }
    if (cm.haveItem(4031058)) {
        actionx["Mental"] = true;
    } else if (cm.haveItem(4031057)) {
        actionx["Physical"] = true;
    }
    cm.sendSimple("你需要我做什么吗？#b" + (cm.getJobId() % 10 == 0 ? "\r\n#L0#我想进行第三次职业转职。" : "") + "\r\n#L1#请允许我进行扎昆地牢任务。");
}

function action(mode, type, selection) {
    status++;
    if (mode == 0 && type == 0) {
        status -= 2;
    } else if (mode != 1 || (status > 2 && !actionx["Mental"]) || status > 3) {
        if (mode == 0 && type == 1) {
            cm.sendNext("下定决心。");
        }
        cm.dispose();
        return;
    }
    if (actionx["Mental"]) {
        if (status == 0) {
            cm.sendNext("完成测试的智力部分做得很好。你明智地回答了所有的问题。我必须说，你展现出的智慧水平让我印象深刻。在我们进行下一步之前，请先把项链交给我。");
        } else if (status == 1) {
            cm.sendYesNo("好的！现在，通过我，你将变成一个更加强大的冒险家。在这之前，请确保你的SP已经被充分使用了，你需要至少使用到70级之前获得的所有SP来进行第三次职业转职。哦，还有，由于你已经在第二次职业转职时选择了你的职业方向，所以在第三次职业转职时就不需要再次选择了。你现在要进行转职吗？");
        } else if (status == 2) {
            if (cm.getPlayer().getRemainingSp() > 0) {
                if (cm.getPlayer().getRemainingSp() > (cm.getLevel() - 70) * 3) {
                    cm.sendNext("请在继续之前使用你所有的SP。");
                    cm.dispose();
                    return;
                }
            }
            if (cm.getJobId() % 10 == 0) {
                cm.gainItem(4031058, -1);
                cm.changeJobById(cm.getJobId() + 1);
                cm.getPlayer().removePartyQuestItem("JBQ");
            }

            if (Math.floor(cm.getJobId() / 10) == 31) {
                cm.sendNext("你已正式成为#b游侠#k。你将真正拥抱的其中一个技能是名为#b致命一击#k的技能，它允许游侠在近距离射出箭矢。#b地狱火#k允许游侠暂时对怪物进行火属性攻击，而#b傀儡#k（召唤一个稻草人吸引怪物的注意力）和#b银鹰#k（召唤一只银鹰攻击怪物）等技能巩固了弓箭手作为远程攻击专家的地位。");
            } else {
                cm.sendNext("你已正式成为 #b狙击手#k。你将真正拥抱的其中一个技能是名为 #b致命一击#k 的技能，它允许狙击手在近距离射箭。 #b暴风雪#k 允许狙击手暂时对怪物进行冰属性攻击，而像 #b稻草人#k（召唤一个吸引怪物注意力的稻草人）和 #b金雕#k（召唤一个攻击怪物的金雕）这样的技能巩固了弓箭手作为远程攻击专家的地位。");
            }
        } else if (status == 3) {
            cm.sendNextPrev("我也给了你一些SP和AP，这将帮助你开始。你现在确实已经成为一个强大的战士。但请记住，现实世界将等待着你，那里会有更艰难的障碍需要克服。当你觉得无法自我训练来达到更高的境界时，那时候，只有那时候，来找我。我会在这里等着你。");
        }
    } else if (actionx["Physical"]) {
        if (status == 0) {
            cm.sendNext("完成了测试的体能部分，做得很棒。我知道你能做到。现在你已经通过了测试的前半部分，接下来是后半部分。请先把项链给我。");
        } else if (status == 1) {
            if (cm.haveItem(4031057)) {
                cm.gainItem(4031057, -1);
                cm.getPlayer().setPartyQuestItemObtained("JBQ");
            }
            cm.sendNextPrev("这是测试的第二部分。这个测试将决定你是否足够聪明，可以迈向伟大的下一步。在Ossyria的雪地上有一个被雪覆盖的黑暗区域，被称为圣地，甚至怪物也无法到达。在这个区域的中心，有一块被称为圣石的巨大石头。你需要献上一件特殊的物品作为祭品，然后圣石将在当场测试你的智慧。");
        } else if (status == 2) {
            cm.sendNextPrev("你需要诚实而坚定地回答每一个问题。如果你能正确回答所有问题，那么圣石将正式接受你，并交给你#b#t4031058##k。把项链拿回来，我会帮助你迈向下一步。祝你好运。");
        }
    } else if (cm.getPlayer().gotPartyQuestItem("JB3") && selection == 0) {
        cm.sendNext("去，和#b#p1012100#对话，给我带来#b#t4031057#。");
        cm.dispose();
    } else if (cm.getPlayer().gotPartyQuestItem("JBQ") && selection == 0) {
        cm.sendNext("去，和#b#p2030006# #k交谈，然后给我带来#b#t4031058##k。");
        cm.dispose();
    } else {
        if (sel == undefined) {
            sel = selection;
        }
        if (sel == 0) {
            if (cm.getPlayer().getLevel() >= 70 && cm.getJobId() % 10 == 0) {
                if (status == 0) {
                    cm.sendYesNo("欢迎。我是#b#p2020010##k，所有弓箭手的首领，愿意分享我的弓箭知识和艰难的生活给那些愿意倾听的人。你似乎已经准备好迈出这一步，准备好迎接第三职业转职的挑战。太多的弓箭手来来去去，无法达到第三职业转职的标准。你呢？你准备好接受考验并进行第三职业转职了吗？");
                } else if (status == 1) {
                    cm.getPlayer().setPartyQuestItemObtained("JB3");
                    cm.sendNext("好的。你将在弓箭手的两个重要方面进行测试：力量和智慧。我现在会向你解释测试的物理部分。还记得来自冒险岛的#b#p1012100##k吗？去找他，他会告诉你测试的第一部分的详细信息。请完成任务，并从#p1012100#那里得到#b#t4031057##k。");
                } else if (status == 2) {
                    cm.sendNextPrev("测试的心理部分只能在你通过了测试的身体部分之后才能开始。#b#t4031057##k 将证明你确实通过了测试。我会提前告诉#b#p1012100##k你正在前往那里，所以做好准备。这不会容易，但我对你有着最大的信心。祝你好运。");
                }
            }
        } else {
            if (cm.getPlayer().getLevel() >= 50) {
                cm.sendOk("首领居住委员会授予你#b特许#k，成为#r反击扎昆团队#k的一部分。祝你前程似锦。");
                if (!(cm.isQuestStarted(100200) || cm.isQuestCompleted(100200))) {
                    cm.startQuest(100200);
                }
                const GameConfig = Java.type('org.gms.config.GameConfig');
                if (GameConfig.getServerBoolean("use_enable_solo_expeditions") && !cm.isQuestCompleted(100201)) {
                    cm.completeQuest(100201);
                }
            } else {
                cm.sendOk("你太弱了，无法成为#rcounteroffensive团队对抗扎昆#k的一部分。至少达到#blevel 50#k，然后再和我说话。");
            }
            cm.dispose();
        }
    }
}