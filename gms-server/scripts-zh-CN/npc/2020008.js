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
    if (cm.isQuestStarted(6192)) {
        if (cm.getParty() == null) {
            cm.sendOk("组队开始这个副本。");
            cm.dispose();
            return;
        }

        var em = cm.getEventManager("ElnathPQ");
        if (em == null) {
            cm.sendOk("埃尔奈斯组队副本遇到了一个错误。");
            cm.dispose();
            return;
        }

        var eli = em.getEligibleParty(cm.getParty());
        if (eli.size() > 0) {
            if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                cm.sendOk("另一个队伍已经在挑战这个副本了。请尝试其他频道，或者等待当前队伍完成。");
            }
        } else {
            cm.sendOk("您目前无法开始这个副本，因为您的队伍人数不在范围内，部分队伍成员不符合尝试条件，或者他们不在这张地图上。如果您在寻找队伍成员方面遇到困难，请尝试使用队伍搜索功能。");
        }

        cm.dispose();
        return;
    }

    var jobBase = parseInt(cm.getJobId() / 100);
    var jobStyle = 1;
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
    cm.sendSimple("我可以帮你吗？#b" + (cm.getJobId() % 10 == 0 ? "\r\n#L0#我想进行第三次职业转职。" : "") + "\r\n#L1#请允许我进行扎昆地牢任务。");
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
            cm.sendYesNo("好的！现在，通过我，你将变身为一个更加强大的战士。在这之前，请确保你的SP已经被充分使用了，你需要至少使用到70级之前获得的所有SP来进行第三次转职。哦，还有，由于你已经在第二次转职时选择了职业方向，所以在第三次转职时就不需要再次选择了。你现在要进行转职吗？");
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

            if (Math.floor(cm.getJobId() / 10) == 11) {
                cm.sendNext("你刚刚成为了#b十字军#k。一些新的攻击技能，比如#b怒吼#k和#b连击#k都非常强大，而#b破甲#k将会削弱怪物的防御能力。最好集中精力学习你在战士时期掌握的武器技能。");
            } else if (Math.floor(cm.getJobId() / 10) == 12) {
                cm.sendNext("你刚刚成为了#b白骑士#k。你将会接触到一本新的技能书，其中包含各种新的攻击技能以及基于元素的攻击。建议白骑士继续使用与骑士团成员相配的武器类型，无论是剑还是钝器。有一个名为#b冲锋#k的技能，可以给武器增加冰、火和闪电元素，使白骑士成为唯一能够进行基于元素的攻击的战士。用元素弱化怪物，然后用#b冲锋打击#k造成巨大伤害。这将使你在这里成为一股毁灭性的力量。");
            } else {
                cm.sendNext("你从现在开始是#b龙骑士#k。你将学习一系列新的长矛和长柄武器的攻击技能，而作为长矛战士选择的武器也应该继续作为龙骑士的武器。像#b碎甲#k（对一个怪物造成最大伤害）和#b龙之怒#k（对多个怪物造成伤害）这样的技能被推荐作为主要的攻击技能，而一个名为#b龙之咆哮#k的技能将以毁灭性的力量对屏幕上的一切造成伤害。不足之处在于这个技能会消耗掉超过一半的可用生命值。");
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
            cm.sendNextPrev("这是测试的第二部分。这个测试将决定你是否足够聪明，可以迈向伟大的下一步。在Ossyria的雪地上有一个被雪覆盖的黑暗区域，被称为圣地，甚至怪物也无法到达。在这个区域的中心，有一块被称为圣石的巨大石头。你需要献上一件特殊的物品作为祭品，然后圣石将在当场测试你的智慧。");
        } else if (status == 2) {
            cm.sendNextPrev("你需要诚实而坚定地回答每一个问题。如果你能正确回答所有问题，那么圣石将正式接受你，并交给你#b#t4031058##k。把项链拿回来，我会帮助你迈向下一步。祝你好运。");
        }
    } else if (cm.getPlayer().gotPartyQuestItem("JB3") && selection == 0) {
        cm.sendNext("去，和#b#p1022000#对话，然后给我带来#b#t4031057#。");
        cm.dispose();
    } else if (cm.getPlayer().gotPartyQuestItem("JBQ") && selection == 0) {
        cm.sendNext("去，和#b#p2030006#对话#k，然后给我带来#b#t4031058##k。");
        cm.dispose();
    } else {
        if (sel == undefined) {
            sel = selection;
        }
        if (sel == 0) {
            if (cm.getPlayer().getLevel() >= 70 && cm.getJobId() % 10 == 0) {
                if (status == 0) {
                    cm.sendYesNo("欢迎。我是#b#p2020008##k，所有战士的首领，负责激发每位需要我的指导的战士的潜力。你似乎是那种想要迈出进步的战士，准备迎接三转职业挑战的人。但我见过无数渴望像你一样迈出这一步的战士，最终却失败了。你呢？你准备接受考验，迈出三转职业的步伐吗？");
                } else if (status == 1) {
                    cm.getPlayer().setPartyQuestItemObtained("JB3");
                    cm.sendNext("好的。你将在战士的两个重要方面进行测试：力量和智慧。我现在会向你解释测试的物理部分。还记得在佩里安的#b#p1022000##k吗？去找他，他会告诉你测试的第一部分的细节。请完成任务，并从#p1022000#那里得到#b#t4031057##k。");
                } else if (status == 2) {
                    cm.sendNextPrev("测试的心理部分只能在你通过了测试的身体部分之后才能开始。#b#t4031057##k 将证明你确实通过了测试。我会提前告诉#b#p1022000##k你要前往那里，所以做好准备。这不会很容易，但我对你有最大的信心。祝你好运。");
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