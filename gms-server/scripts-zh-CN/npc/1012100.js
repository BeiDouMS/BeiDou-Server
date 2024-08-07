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

/*      Athena Pierce
	Bowman Job Advancement
	Victoria Road : Bowman Instructional School (100000201)
*/

status = -1;
actionx = {"1stJob": false, "2ndjob": false, "3thJobI": false, "3thJobC": false};
job = 310;

spawnPnpc = false;
spawnPnpcFee = 7000000;
jobType = 3;

function start() {
    const GameConstants = Java.type('org.gms.constants.game.GameConstants');
    if (parseInt(cm.getJobId() / 100) == jobType && cm.canSpawnPlayerNpc(GameConstants.getHallOfFameMapid(cm.getJob()))) {
        spawnPnpc = true;

        var sendStr = "你已经走了很长的路，才能达到你今天所拥有的权力、智慧和勇气，不是吗？ 你觉得现在#r名人堂上哪一个NPC形象适合你#k? 你确定要这么做吗?";
        if (spawnPnpcFee > 0) {
            sendStr += " 我可以为你做，费用是 #b " + cm.numberWithCommas(spawnPnpcFee) + " 金币。#k";
        }

        cm.sendYesNo(sendStr);
    } else {
        if (cm.getJobId() == 0) {
            actionx["1stJob"] = true;
            cm.sendNext("所以你决定成为一个#rbowman#k？你知道吗，有一些标准要达到……#b你的等级至少应该是10级，至少有" + cm.getFirstJobStatRequirement(jobType) + "#k。让我看看。");   // thanks Vcoc for noticing a need to state and check requirements on first job adv starting message
        } else if (cm.getLevel() >= 30 && cm.getJobId() == 300) {
            actionx["2ndJob"] = true;
            if (cm.haveItem(4031012)) {
                cm.sendNext("哈哈...我知道你会轻松通过那个测试的。我承认，你是一个很棒的弓箭手。我会让你比现在强大得多。不过，在那之前...你需要选择给你的两条路中的一条。这对你来说会是一个艰难的决定，但是...如果有任何问题需要问，请尽管问吧。");
            } else if (cm.haveItem(4031011)) {
                cm.sendOk("去找#b#p1072002##k。");
                cm.dispose();
            } else {
                cm.sendYesNo("嗯...自从上次见到你以来，你长大了许多。我再也看不到以前那个软弱的家伙，而是看起来更像一个弓箭手了。那么，你觉得呢？难道你不想变得更加强大吗？通过一个简单的测试，我就可以帮你实现。你想试试吗？");
            }
        } else if (actionx["3thJobI"] || (cm.getPlayer().gotPartyQuestItem("JB3") && cm.getLevel() >= 70 && cm.getJobId() % 10 == 0 && parseInt(cm.getJobId() / 100) == 3 && !cm.getPlayer().gotPartyQuestItem("JBP"))) {
            actionx["3thJobI"] = true;
            cm.sendNext("你来了。几天前，奥西里亚的#b#p2020010##k跟我谈到了你。我看到你对成为弓箭手职业的第三次转职很感兴趣。为了实现这个目标，我需要测试你的实力，看看你是否配得上这个晋升。在金银岛的深林中有一个开口，会带你通往一个秘密通道。一旦进入，你将面对我的分身。你的任务是打败她，并带着#b#t4031059##k回来。");
        } else if (cm.getPlayer().gotPartyQuestItem("JBP") && !cm.haveItem(4031059)) {
            cm.sendNext("请把#b#t4031059##k带给我。");
            cm.dispose();
        } else if (cm.haveItem(4031059) && cm.getPlayer().gotPartyQuestItem("JBP")) {
            actionx["3thJobC"] = true;
            cm.sendNext("干得好。你打败了我的分身，并安全地带回了#b#t4031059##k。你现在已经从物理角度证明了自己配得上进行第三次职业转职。现在你应该把这条项链交给在奥西里亚的#b#p2020011##k，以进行测试的第二部分。祝你好运。你会需要的。");
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
                cm.sendNextPrev("这是一个重要且最终的选择。你将无法回头。");
            } else {
                cm.sendOk("再多训练一会儿，直到你达到基本要求，我就可以教你成为一名#r弓箭手#k的方法。");
                cm.dispose();
            }
        } else if (status == 1) {
            if (cm.canHold(1452051) && cm.canHold(2070000)) {
                if (cm.getJobId() == 0) {
                    cm.changeJobById(300);
                    cm.gainItem(1452051, 1);
                    cm.gainItem(2060000, 1000);
                    cm.resetStats();
                }
                cm.sendNext("好的，从现在开始，你就是我们的一员了！你将在...过着流浪者的生活，但要耐心等待，很快你就会过上好日子。好了，虽然不多，但我会传授给你一些我的能力... 哈啊啊啊！！");
            } else {
                cm.sendNext("清理一下你的背包，然后回来找我说话。");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.sendNextPrev("你现在变得更强大了。而且你的所有物品栏都增加了槽位。确切地说，是一整行。自己去看看吧。我刚给了你一点 #bSP#k。当你在屏幕左下角打开 #b技能#k 菜单时，可以使用SP学习技能。不过要注意一点：你不能一次性全部提升。在学习了一些技能之后，你还可以获得一些特定的技能。");
        } else if (status == 3) {
            cm.sendNextPrev("现在提醒一下。一旦你做出选择，就不能改变主意，试图选择另一条道路。现在去吧，做一个骄傲的弓箭手。");
        } else {
            cm.dispose();
        }
    } else if (actionx["2ndJob"]) {
        if (status == 0) {
            if (cm.haveItem(4031012)) {
                cm.sendSimple("好的，当你做出决定后，点击底部的[我会选择我的职业]。#b\r\n#L0#请解释一下成为猎人的意义。\r\n#L1#请解释一下成为弩弓手的意义。\r\n#L3#我会选择我的职业！");
            } else {
                cm.sendNext("做得好。你看起来很强壮，但我需要看看你是否真的足够强大来通过测试，这不是一个困难的测试，所以你会做得很好。拿着我的信先……确保你不要丢了它！");
                if (!cm.isQuestStarted(100000)) {
                    cm.startQuest(100000);
                }
            }
        } else if (status == 1) {
            if (!cm.haveItem(4031012)) {
                if (cm.canHold(4031010)) {
                    if (!cm.haveItem(4031010)) {
                        cm.gainItem(4031010, 1);
                    }
                    cm.sendNextPrev("请将这封信交给射手村附近的#b#p1072002##k。她正在代替我担任教练的工作。把信交给她，她会代替我测试你。祝你好运。");
                    cm.dispose();
                } else {
                    cm.sendNext("请在你的背包中腾出一些空间。");
                    cm.dispose();
                }
            } else {
                if (selection < 3) {
                    if (selection == 0) {    //hunter
                        cm.sendNext("精通弓箭的弓箭手。\r\n\r\n在早期级别中，#b猎人#k的每分钟伤害输出更高，攻击速度更快，但略弱于弩弓手。#b猎人#k可以使用#r箭炸弹#k，这是一个略微较弱的攻击，可以使最多6个敌人被眩晕。");
                    } else if (selection == 1) {    //crossbowman 弩弓手
                        cm.sendNext("精通弓弩的弩弓手。\r\n\r\n与猎人相比，弩弓手的攻击力随等级提高而增加。弩弓手可以使用更强大的攻击技能#r铁箭#k，该技能不会自动追踪敌人，但可以穿墙。");
                    }

                    status -= 2;
                } else {
                    cm.sendSimple("现在... 你决定好了吗？请选择你想要在二转时选择的职业。#b\r\n#L0#猎人\r\n#L1#弩手");
                }
            }
        } else if (status == 2) {
            job += selection * 10;
            cm.sendYesNo("所以你想要选择成为" + (job == 310 ? "#b猎人#k" : "#b弩弓手#k") + "进行第二次转职吗？你知道一旦在这里做出选择，就无法在第二次转职时选择其他职业了，对吧？");
        } else if (status == 3) {
            if (cm.haveItem(4031012)) {
                cm.gainItem(4031012, -1);
            }

            cm.sendNext("好了，从现在开始你就是#b猎人#k了。#b猎人#k是聪明的一群，拥有惊人的视力，能够轻松地将箭射穿怪物的心脏……请每天都训练自己。我会帮助你变得比你现在更强大。");
            if (cm.getJobId() != job) {
                cm.changeJobById(job);
            }
        } else if (status == 4) {
            cm.sendNextPrev("我刚刚给了你一本书，上面列出了你作为一个猎人或弩弓手可以获得的技能清单。此外，你的杂项物品栏也扩展了一行。你的最大生命值和魔法值也增加了。去检查一下，看看吧。");
        } else if (status == 5) {
            cm.sendNextPrev("我也给了你一点 #bSP#k。打开左下角的 #b技能菜单#k。你可以提升新获得的二级技能。不过要注意，你不能一次性提升它们。有些技能只有在学会其他技能后才能使用。记得要记住这一点。");
        } else if (status == 6) {
            cm.sendNextPrev((job == 310 ? "Hunter" : "Crossbowman") + " 你需要坚强。但请记住，你不能滥用这种权力，把它用在弱者身上。请正确地使用你的巨大力量，因为……为了让你正确地使用这种力量，这比仅仅变得更强要困难得多。在你更进一步之后，请找到我。我等着你。");
        }
    } else if (actionx["3thJobI"]) {
        if (status == 0) {
            if (cm.getPlayer().gotPartyQuestItem("JB3")) {
                cm.getPlayer().removePartyQuestItem("JB3");
                cm.getPlayer().setPartyQuestItemObtained("JBP");
            }
            cm.sendNextPrev("因为她是我的克隆，你可以预料到前方将会是一场艰难的战斗。他使用了许多特殊的攻击技能，与你以往所见的完全不同，你的任务是成功地与他一对一地战斗。在秘密通道中有一个时间限制，所以你必须在规定时间内打败他。祝你好运，希望你带着#b#t4031059##k。");
        }
    } else if (actionx["3thJobC"]) {
        cm.getPlayer().removePartyQuestItem("JBP");
        cm.gainItem(4031059, -1);
        cm.gainItem(4031057, 1);
        cm.dispose();
    }
}