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
/* Dark Lord
	Thief Job Advancement
	Victoria Road : Thieves' Hideout (103000003)
	Custom Quest 100009, 100011
*/

status = -1;
actionx = {"1stJob": false, "2ndjob": false, "3thJobI": false, "3thJobC": false};
job = 410;

spawnPnpc = false;
spawnPnpcFee = 7000000;
jobType = 4;

function start() {
    const GameConstants = Java.type('org.gms.constants.game.GameConstants');
    if (parseInt(cm.getJobId() / 100) == jobType && cm.canSpawnPlayerNpc(GameConstants.getHallOfFameMapid(cm.getJob()))) {
        spawnPnpc = true;

        var sendStr = "You have walked a long way to reach the power, wisdom and courage you hold today, haven't you? What do you say about having right now #ra NPC on the Hall of Fame holding the current image of your character#k? Do you like it?";
        if (spawnPnpcFee > 0) {
            sendStr += " I can do it for you, for the fee of #b " + cm.numberWithCommas(spawnPnpcFee) + " mesos.#k";
        }

        cm.sendYesNo(sendStr);
    } else {
        if (cm.getJobId() == 0) {
            actionx["1stJob"] = true;
            cm.sendNext("想成为一个#r飞侠#k吗？有一些标准要达到，毕竟我们不是谁都可以接纳的……#b所以你的等级至少10级，至少" + cm.getFirstJobStatRequirement(jobType) + "#k。让我看看。");   // thanks Vcoc for noticing a need to state and check requirements on first job adv starting message
        } else if (cm.getLevel() >= 30 && cm.getJobId() == 400) {
            actionx["2ndJob"] = true;
            if (cm.haveItem(4031012)) {
                cm.sendNext("我看到你做得很好。我会允许你迈出漫长道路上的下一步。");
            } else if (cm.haveItem(4031011)) {
                cm.sendOk("去找#b#p1072003##k。");
                cm.dispose();
            } else {
                cm.sendNext("你取得的进步令人惊讶。");
            }
        } else if (actionx["3thJobI"] || (cm.getPlayer().gotPartyQuestItem("JB3") && cm.getLevel() >= 70 && cm.getJobId() % 10 == 0 && parseInt(cm.getJobId() / 100) == 4 && !cm.getPlayer().gotPartyQuestItem("JBP"))) {
            actionx["3thJobI"] = true;
            cm.sendNext("你来了。几天前，奥西里亚的#b#p2020011##k跟我谈到了你。我看到你对于成为盗贼职业的第三次转职很感兴趣。为了实现这个目标，我需要测试一下你的实力，看看你是否配得上这个晋升。在金银岛的一个深水沼泽中有一个洞口，会带你通往一个秘密通道。一旦进入，你将面对我的分身。你的任务是打败他，并带着#b#t4031059##k回来。");
        } else if (cm.getPlayer().gotPartyQuestItem("JBP") && !cm.haveItem(4031059)) {
            cm.sendNext("请把#b#t4031059##k带给我。");
            cm.dispose();
        } else if (cm.haveItem(4031059) && cm.getPlayer().gotPartyQuestItem("JBP")) {
            actionx["3thJobC"] = true;
            cm.sendNext("干得好。你打败了我的分身，并安全地带回了#b#t4031059##k。你现在已经从物理角度证明了自己配得上进行第三次职业转职。现在你应该把这条项链交给在奥西里亚的#b#p2020011##k，以进行测试的第二部分。祝你好运。你会需要的。");
        } else if (cm.isQuestStarted(6141)) {
            cm.warp(910300000, 3);
        } else {
            cm.sendOk("你选择得很明智。");
            cm.dispose();
        }
    }
}

function action(mode, type, selection) {
    status++;
    if (mode == -1 && selection == -1) {
        cm.dispose();
        return;
    } else if (mode == 0 && type != 1) {
        status -= 2;
    }

    if (status == -1) {
        start();
        return;
    } else {
        if (spawnPnpc) {
            if (mode > 0) {
                if (cm.getMeso() < spawnPnpcFee) {
                    cm.sendOk("抱歉，您没有足够的金币购买在名人堂上的位置。");
                    cm.dispose();
                    return;
                }

                const PlayerNPC = Java.type('org.gms.server.life.PlayerNPC');
                const GameConstants = Java.type('org.gms.constants.game.GameConstants');
                if (PlayerNPC.spawnPlayerNPC(GameConstants.getHallOfFameMapid(cm.getJob()), cm.getPlayer())) {
                    cm.sendOk("给你了！希望你会喜欢它。");
                    cm.gainMeso(-spawnPnpcFee);
                } else {
                    cm.sendOk("抱歉，名人堂目前已满...");
                }
            }

            cm.dispose();
            return;
        } else {
            if (mode != 1 || status == 7 && type != 1 || (actionx["1stJob"] && status == 4) || (cm.haveItem(4031008) && status == 2) || (actionx["3thJobI"] && status == 1)) {
                if (mode == 0 && status == 2 && type == 1) {
                    cm.sendOk("你知道没有其他选择……");
                }
                if (!(mode == 0 && type != 1)) {
                    cm.dispose();
                    return;
                }
            }
        }
    }

    if (actionx["1stJob"]) {
        if (status == 0) {
            if (cm.getLevel() >= 10 && cm.canGetFirstJob(jobType)) {
                cm.sendYesNo("哦...！你看起来就很鸡贼，确实像是我们团队的一员... 你需要再多一点邪恶的心思，你觉得怎么样？想成为飞侠吗？ ");
            } else {
                cm.sendOk("再多训练一会儿，直到达到基本要求，我就可以教你成为#r盗贼#k的方法。");
                cm.dispose();
            }
        } else if (status == 1) {
            if (cm.canHold(2070000) && cm.canHoldAll([1472061, 1332063])) {
                if (cm.getJobId() == 0) {
                    cm.changeJobById(400);
                    cm.gainItem(2070015, 500);
                    cm.gainItem(1472061, 1);
                    cm.gainItem(1332063, 1);
                    cm.resetStats();
                }
                cm.sendNext("好的，从现在开始，你就是我们的一员了！你将在冒险岛过着流浪者的生活，但只要有耐心，很快你就会过上富裕的生活。好了，虽然不多，但我会传授给你一些我的能力... 哈啊啊啊！！");
            } else {
                cm.sendNext("清理一下你的背包，然后回来找我说话。");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.sendNextPrev("你现在变得更强大了。而且你的每一个物品栏都增加了槽位。确切地说，是一整行。自己去看看吧。我刚给了你一点#bSP#k。当你在屏幕左下角打开#b技能#k菜单时，可以使用SP学习技能。不过要注意一点：你不能一次性全部提升。在学习了一些技能之后，你还可以获得一些特定的技能。");
        } else if (status == 3) {
            cm.sendNextPrev("现在提醒一下。一旦你做出选择，就不能改变主意，试图选择另一条道路。现在去吧，做一个骄傲的飞侠。");
        } else {
            cm.dispose();
        }
    } else if (actionx["2ndJob"]) {
        if (status == 0) {
            if (cm.haveItem(4031012)) {
                cm.sendSimple("好的，当你做出决定后，点击底部的[我已经决定好了]。#b\r\n#L0#请向我解释一下成为刺客的所有内容。\r\n#L1#请向我解释一下成为侠客的所有内容。\r\n#L3#我已经决定好了");
            } else {
                cm.sendNext("做得好。你看起来很强壮，但我需要看看你是否真的足够强大来通过测试，这不是一个困难的测试，所以你会做得很好。拿着我的信先……确保你不要丢了它！");
                if (!cm.isQuestStarted(100009)) {
                    cm.startQuest(100009);
                }
            }
        } else if (status == 1) {
            if (!cm.haveItem(4031012)) {
                if (cm.canHold(4031011)) {
                    if (!cm.haveItem(4031011)) {
                        cm.gainItem(4031011, 1);
                    }
                    cm.sendNextPrev("请把这封信送到靠近废弃都市的#b#p1072003##k那里。他正在代替我担任教练的工作。把信交给他，他会代替我来测试你。祝你好运。");
                } else {
                    cm.sendNext("请在你的背包中腾出一些空间。");
                    cm.dispose();
                }
            } else {
                if (selection < 3) {
                    if (selection == 0) {    //assassin
                        cm.sendNext("擅长使用#r爪#k的盗贼。\r\n\r\n#b刺客#k是远程攻击者。他们非常节约金币，并且有很高的伤害潜力，但是比飞侠花费更多。");
                    } else if (selection == 1) {    //bandit
                        cm.sendNext("擅长使用匕首的盗贼。\r\n\r\n#b侠客#k是快速的近战攻击者，在二转职业中非常强大。他们不像刺客那样效率高，也没有远程攻击的优势，但在原始力量方面弥补了这一点。");
                    }

                    status -= 2;
                } else {
                    cm.sendSimple("现在... 你做好决定了吗？请选择你想要在二转时选择的职业。#b\r\n#L0#刺客\r\n#L1#侠客");
                }
            }
        } else if (status == 2) {
            if (cm.haveItem(4031011)) {
                cm.dispose();
                return;
            }
            job += selection * 10;
            cm.sendYesNo("所以你想要选择成为" + (job == 410 ? "#b刺客#k" : "#b侠客#k") + "进行第二次转职吗？你知道一旦在这里做出选择，就无法在第二次转职时选择其他职业了，对吗？");
        } else if (status == 3) {
            if (cm.haveItem(4031012)) {
                cm.gainItem(4031012, -1);
            }
            cm.completeQuest(100011);

            if (job == 410) {
                cm.sendNext("好的，从现在开始你就是#b刺客#k了。刺客有灵活的双手和敏捷的双脚来征服敌人。请继续训练。我会让你比现在更强大！");
            } else {
                cm.sendNext("好的，从现在开始你就是#b强盗了。强盗们喜欢在阴影和黑暗中游走，等待合适的时机将匕首突然而迅速地刺入敌人的心脏……请继续训练。我会让你比现在更强大。");
            }

            if (cm.getJobId() != job) {
                cm.changeJobById(job);
            }
        } else if (status == 4) {
            cm.sendNextPrev("我刚刚给了你一本书，上面列出了你作为一个刺客/侠客可以获得的技能清单。此外，你的杂项物品栏也扩展了一行。你的最大生命值和魔法值也增加了。去检查一下，看看吧。");
        } else if (status == 5) {
            cm.sendNextPrev("我也给了你一点 #bSP#k。打开左下角的 #b技能菜单#k。你可以提升新获得的二级技能。不过要注意，你不能一次性提升它们。有些技能只有在学会其他技能后才能使用。记得要牢记这一点。");
        } else if (status == 6) {
            cm.sendNextPrev((job == 410 ? "Assassin" : "Bandit") + " need to be strong. But remember that you can't abuse that power and use it on a weakling. Please use your enormous power the right way, because... for you to use that the right way, that is much harden than just getting stronger. Please find me after you have advanced much further. I'll be waiting for you.");
        }
    } else if (actionx["3thJobI"]) {
        if (status == 0) {
            if (cm.getPlayer().gotPartyQuestItem("JB3")) {
                cm.getPlayer().removePartyQuestItem("JB3");
                cm.getPlayer().setPartyQuestItemObtained("JBP");
            }
            cm.sendNextPrev("因为他是我的克隆，所以你可以期待一场艰难的战斗。他使用了许多特殊的攻击技能，与你以往所见的完全不同，你的任务是成功地与他一对一地战斗。在秘密通道中有一个时间限制，所以你必须在规定时间内打败他。祝你好运，希望你带着#b#t4031059##k来。");
        }
    } else if (actionx["3thJobC"]) {
        cm.getPlayer().removePartyQuestItem("JBP");
        cm.gainItem(4031059, -1);
        cm.gainItem(4031057, 1);
        cm.dispose();
    }
}