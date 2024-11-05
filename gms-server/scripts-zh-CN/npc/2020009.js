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

status = -1;
var job;
var sel;
actionx = {"Mental": false, "Physical": false};

function start() {
    var jobBase = parseInt(cm.getJobId() / 100);
    var jobStyle = 2;
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
            cm.sendNext("做得好，完成了测试的智力部分。你明智地回答了所有问题。我必须说，你展现出的智慧水平让我印象深刻。在我们进行下一步之前，请先把项链交给我。");
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

            if (Math.floor(cm.getJobId() / 10) == 21) {
                cm.sendNext("你从现在开始就是#b火毒法师#k了。新的技能书包含了新的和改进的火毒系法术，还有像#b元素增幅#k（改进元素系法术）和#b法术增幅#k（提高你的攻击法术整体速度）这样的技能，能够让你快速有效地攻击怪物。防御性法术如#b部分抗性#k（使你对某些元素攻击更强）和#b封印#k（封印怪物）将有助于弥补法师的一个弱点：缺乏生命值。");
            } else if (Math.floor(cm.getJobId() / 10) == 22) {
                cm.sendNext("你现在是冰雷法师。新的技能书包含了全新和改进的冰雷系法术，像是元素增幅（增强元素法术）和法术加速（提高攻击法术的速度）等技能将使你能够快速有效地攻击怪物。防御法术如部分抗性（增强对特定元素攻击的抵抗力）和封印（封印怪物）将有助于弥补法师的一个弱点：生命值不足。");
            } else {
                cm.sendNext("你现在是#b牧师#k了。新的技能书包含了新的和改进的神圣法术，比如#b闪耀之光#k和#b召唤龙#k，以及#b神秘之门#k（创建通往最近城镇的门）和#b神圣祝福#k（提高经验值获取）等技能对于组队游戏至关重要。像#b末日#k（将怪物变成蜗牛）这样与众不同的法术使牧师成为所有职业中最独特的职业。");
            }
        } else if (status == 3) {
            cm.sendNextPrev("我也给了你一些SP和AP，这将帮助你开始。你现在确实已经成为一个强大的战士。但请记住，现实世界将等待着你，那里会有更艰难的障碍需要克服。当你觉得无法自我训练来达到更高的境界时，那时候，只有那时候，来找我。我会在这里等着。");
        }
    } else if (actionx["Physical"]) {
        if (status == 0) {
            cm.sendNext("完成了测试的体能部分，做得很棒。我知道你能做到。现在你已经通过了测试的前半部分，接下来是后半部分。请先把项链给我。");
        } else if (status == 1) {
            if (cm.haveItem(4031057)) {
                cm.gainItem(4031057, -1);
                cm.getPlayer().setPartyQuestItemObtained("JBQ");
            }
            cm.sendNextPrev("这是测试的第二部分。这个测试将决定你是否足够聪明，可以迈向伟大的下一步。在Ossyria的雪原上有一个被雪覆盖的黑暗区域，被称为圣地，即使怪物也无法到达。在这个区域的中心，有一块被称为圣石的巨大石头。你需要献上一件特殊的物品作为祭品，然后圣石将在当场测试你的智慧。");
        } else if (status == 2) {
            cm.sendNextPrev("你需要诚实而坚定地回答每一个问题。如果你能正确回答所有问题，那么圣石将正式接受你，并交给你#b#t4031058##k。把项链带回来，我会帮助你迈向下一步。祝你好运。");
        }
    } else if (cm.getPlayer().gotPartyQuestItem("JB3") && selection == 0) {
        cm.sendNext("去，和#b#p1032001#对话，给我带来#b#t4031057#。");
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
                    cm.sendYesNo("欢迎。我是#b#p2020009##k，所有魔法师的首领，愿意与愿意倾听的人分享我的街头知识和艰难生活。你似乎已经准备好迈出这一步，准备好迎接第三职业转职的挑战。太多的魔法师来来去去，无法达到第三职业转职的标准。你呢？你准备好接受考验并进行第三职业转职了吗？");
                } else if (status == 1) {
                    cm.getPlayer().setPartyQuestItemObtained("JB3");
                    cm.sendNext("好的。你将在魔法师的两个重要方面进行测试：力量和智慧。我现在会向你解释测试的物理部分。还记得在艾林尼亚的#b#p1032001##k吗？去找他，他会告诉你测试的第一部分的详细信息。请完成任务，并从#p1032001#那里得到#b#t4031057##k。");
                } else if (status == 2) {
                    cm.sendNextPrev("测试的心理部分只能在你通过了测试的身体部分之后才能开始。#b#t4031057##k 将证明你确实通过了测试。我会提前告诉#b#p1032001##k你要前往那里，所以做好准备。这不会容易，但我对你有着最大的信心。祝你好运。");
                }
            }
        } else {
            if (cm.getPlayer().getLevel() >= 50) {
                cm.sendOk("首领居住委员会授予你#b特许#k，让你成为#r反击扎昆的团队的一部分#k。祝你前程似锦。");
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