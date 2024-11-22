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
/* Kyrin
	Pirate Job Advancement
	
	Custom Quest 100009, 100011
*/

status = -1;
actionx = {"1stJob": false, "2ndjob": false, "2ndjobT": false, "3thJobI": false, "3thJobC": false};
job = 510;

spawnPnpc = false;
spawnPnpcFee = 7000000;
jobType = 5;

var advQuest = 0;

function start() {
    const GameConstants = Java.type('org.gms.constants.game.GameConstants');
    if (cm.isQuestStarted(6330)) {
        if (cm.getEventInstance() != null) {    // missing script for skill test found thanks to Jade™
            advQuest = 5;                       // string visibility thanks to iPunchEm & Glvelturall
            cm.sendNext("一点也不错。我们到外面讨论一下吧！");
        } else if (cm.getQuestProgressInt(6330, 6331) == 0) {
            advQuest = 1;
            cm.sendNext("你准备好了吗？现在试着忍受我的攻击2分钟。我不会手下留情的。祝你好运，因为你会需要的。");
        } else {
            advQuest = 3;
            cm.teachSkill(5121003, 0, 10, -1);
            cm.forceCompleteQuest(6330);

            cm.sendNext("恭喜你。你成功通过了我的测试。我会教你一个叫做“超级转变”的新技能。\r\n\r\n  #s5121003#    #b#q5121003##k");
        }
    } else if (cm.isQuestStarted(6370)) {
        if (cm.getEventInstance() != null) {
            advQuest = 6;
            cm.sendNext("一点也不错。我们到外面讨论一下吧！");
        } else if (cm.getQuestProgressInt(6370, 6371) == 0) {
            advQuest = 2;
            cm.sendNext("你准备好了吗？现在试着忍受我的攻击两分钟。我不会手下留情的。祝你好运，因为你会需要的。");
        } else {
            advQuest = 4;
            cm.teachSkill(5221006, 0, 10, -1);
            cm.forceCompleteQuest(6370);

            cm.sendNext("恭喜你。你成功通过了我的测试。我会教你一个叫做“战舰”（Battleship）的新技能。\r\n\r\n  #s5221006#    #b#q5221006##k");
        }
    } else if (parseInt(cm.getJobId() / 100) == jobType && cm.canSpawnPlayerNpc(GameConstants.getHallOfFameMapid(cm.getJob()))) {
        spawnPnpc = true;

        var sendStr = "You have walked a long way to reach the power, wisdom and courage you hold today, haven't you? What do you say about having right now #ra NPC on the Hall of Fame holding the current image of your character#k? Do you like it?";
        if (spawnPnpcFee > 0) {
            sendStr += " I can do it for you, for the fee of #b " + cm.numberWithCommas(spawnPnpcFee) + " mesos.#k";
        }

        cm.sendYesNo(sendStr);
    } else {
        if (cm.getJobId() == 0) {
            actionx["1stJob"] = true;
            cm.sendNext("想成为一个#r海盗#k吗？有一些标准需要满足，因为我们不能接受每个人……#b你的等级至少应该是10级，具有" + cm.getFirstJobStatRequirement(jobType) + "最低要求#k。让我们看看。");   // thanks Vcoc for noticing a need to state and check requirements on first job adv starting message
        } else if (cm.getLevel() >= 30 && cm.getJobId() == 500) {
            actionx["2ndJob"] = true;
            if (cm.isQuestCompleted(2191) || cm.isQuestCompleted(2192)) {
                cm.sendNext("我看到你做得很好。我会允许你迈出漫长道路上的下一步。");
            } else {
                cm.sendNext("你取得的进步令人惊讶。");
            }
        } else if (actionx["3thJobI"] || (cm.getPlayer().gotPartyQuestItem("JB3") && cm.getLevel() >= 70 && cm.getJobId() % 10 == 0 && parseInt(cm.getJobId() / 100) == 5 && !cm.getPlayer().gotPartyQuestItem("JBP"))) {
            actionx["3thJobI"] = true;
            cm.sendNext("你来了。几天前，奥西里亚的#b#p2020013##k跟我谈到了你。我看到你对于成为海盗职业的第三次转职很感兴趣。为了实现这个目标，我需要测试一下你的实力，看看你是否配得上这个晋升。金银岛上有一个洞穴中的开口，会通往一个秘密通道。一旦进入，你将面对我的分身。你的任务是打败他，并带着#b#t4031059##k回来。");
        } else if (cm.getPlayer().gotPartyQuestItem("JBP") && !cm.haveItem(4031059)) {
            cm.sendNext("请把#b#t4031059##k带给我。");
            cm.dispose();
        } else if (cm.haveItem(4031059) && cm.getPlayer().gotPartyQuestItem("JBP")) {
            actionx["3thJobC"] = true;
            cm.sendNext("干得好。你打败了我的分身，并安全地带回了#b#t4031059##k。你现在已经从物理角度证明了自己配得上进行第三次转职。现在你应该把这条项链交给在奥西里亚的#b#p2020013##k，以进行测试的第二部分。祝你好运。你会需要的。");
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
        if (advQuest > 0) {
            if (advQuest < 3) {
                var em = cm.getEventManager(advQuest == 1 ? "4jship" : "4jsuper");
                if (!em.startInstance(cm.getPlayer())) {
                    cm.sendOk("有人已经在挑战测试了。请稍后再试。");
                }
            } else if (advQuest < 5) {
                if (advQuest == 3) {
                    cm.sendOk("它类似于“变身”，但比那个更强大。继续训练，希望能在游戏中见到你。");
                } else {
                    cm.sendOk("与你作为海盗使用的大多数其他技能不同，这个技能绝对是独特的。你实际上可以骑着“战舰”并用它来攻击敌人。你在船上的时候，你的防御等级会提高，这将极大地帮助你在战斗中。愿你成为最优秀的枪手……");
                }
            } else {
                if (advQuest < 6) {
                    cm.setQuestProgress(6330, 6331, 2);
                } else {
                    cm.setQuestProgress(6370, 6371, 2);
                }

                cm.warp(120000101);
            }

            cm.dispose();
        } else if (spawnPnpc) {
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
                cm.sendYesNo("哦...！你看起来就像是我们团队的一员... 你只需要一点俚语，然后... 是的... 那么，你觉得怎么样？想成为海盗吗？");
            } else {
                cm.sendOk("再多训练一会儿，直到你达到基本要求，我就可以教你成为#r海盗#k的方法。");
                cm.dispose();
            }
        } else if (status == 1) {
            if (cm.canHold(2070000) && cm.canHoldAll([1482000, 1492000])) {
                if (cm.getJobId() == 0) {
                    cm.changeJobById(500);
                    cm.gainItem(1492000, 1);
                    cm.gainItem(1482000, 1);
                    cm.gainItem(2330000, 1000);
                    cm.resetStats();
                }
                cm.sendNext("好的，从现在开始，你就是我们的一员了！你将在...过着流浪者的生活，但要耐心等待，很快你就会过上富裕的生活。好了，虽然不多，但我会传授给你一些我的能力... 哈啊啊啊！！");
            } else {
                cm.sendNext("清理一下你的背包，然后回来找我说话。");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.sendNextPrev("你现在变得更强大了。而且你的所有物品栏都增加了槽位。确切地说，是一整行。自己去看看吧。我刚给了你一点 #bSP#k。当你在屏幕左下角打开 #b技能#k 菜单时，可以使用SP学习技能。不过要注意一点：你不能一次性全部提升。在学习了一些技能之后，你还可以获得一些特定的技能。");
        } else if (status == 3) {
            cm.sendNextPrev("现在提醒一下。一旦你做出选择，就不能改变主意，试图选择另一条道路。现在去吧，做一个骄傲的海盗。");
        } else {
            cm.dispose();
        }
    } else if (actionx["2ndJob"]) {
        if (status == 0) {
            if (cm.isQuestCompleted(2191) || cm.isQuestCompleted(2192)) {
                cm.sendSimple("好的，当你做出决定后，点击底部的[我已经做好了选择！]。#b\r\n#L0#请解释一下拳手职业的特点。\r\n#L1#请解释一下枪手职业的特点。\r\n#L3#我已经做好了选择！");
            } else {
                cm.sendNext("做得好。你看起来很强壮，但我需要看看你是否真的足够强大来通过测试，这不是一个困难的测试，所以你会做得很好。");
            }
        } else if (status == 1) {
            if (!cm.isQuestCompleted(2191) && !cm.isQuestCompleted(2192)) {
                // Pirate works differently from the other jobs. It warps you directly in.
                actionx["2ndJobT"] = true;
                cm.sendYesNo("你现在想要参加测试吗？");
            } else {
                if (selection < 3) {
                    if (selection == 0) {    //brawler
                        cm.sendNext("掌握#r指节#k的海盗。\r\n\r\n#b格斗家#k是近战、近距离拳击手，造成大量伤害并拥有高生命值。携带#r螺旋冲击#k，可以一次对多个目标造成巨大伤害。#r橡木桶#k使人能够在艰难战斗中进行侦察或伪装，为面对危险时提供可能的逃生路线。");
                    } else if (selection == 1) {    //gunslinger
                        cm.sendNext("掌握#r枪械#k的海盗。\r\n\r\n#b枪手#k更快速且远程攻击者。通过#r翅膀#k技能，枪手可以在空中盘旋，比普通跳跃更长、更持久。#r空白射击#k可以使附近多个目标陷入眩晕状态。");
                    }

                    status -= 2;
                } else {
                    cm.sendNextPrev("你还有很长的路要走，但成为一名海盗会帮助你实现目标。牢记这一点，你会做得很好。");
                }
            }
        } else if (status == 2) {
            if (actionx["2ndJobT"]) {
                var map = 0;
                if (cm.isQuestStarted(2191)) {
                    map = 108000502;
                } else {
                    map = 108000501;
                }
                if (cm.getPlayerCount(map) > 0) {
                    cm.sendOk("所有的训练地图目前正在使用中。请稍后再试。");
                    cm.dispose();
                } else {
                    cm.warp(map, 0);
                    cm.dispose();

                }
            } else {
                if (cm.isQuestCompleted(2191) && cm.isQuestCompleted(2192)) {
                    job = (Math.random() < 0.5) ? 510 : 520;
                } else if (cm.isQuestCompleted(2191)) {
                    job = 510;
                } else if (cm.isQuestCompleted(2192)) {
                    job = 520;
                }

                cm.sendYesNo("所以你想要选择成为" + (job == 510 ? "#b格斗家#k" : "#b神射手#k") + "进行第二次转职吗？你知道在这里做出选择后，第二次转职就无法选择其他职业了，对吧？");
            }
        } else if (status == 3) {
            if (cm.haveItem(4031012)) {
                cm.gainItem(4031012, -1);
            }

            if (job == 510) {
                cm.sendNext("从现在开始，你是一个#b格斗家#k。格斗家用他们的光膀子统治世界……这意味着他们需要比其他人更多地锻炼身体。如果你在训练中遇到任何困难，我会很乐意帮助你。");
            } else {
                cm.sendNext("从现在开始，你是一名#b枪手#k。枪手以其类似狙击手的远程攻击和使用枪支作为主要武器而闻名。你应该继续训练，真正掌握你的技能。如果你在训练中遇到困难，我会在这里帮助你。");
            }

            if (cm.getJobId() != job) {
                cm.changeJobById(job);
            }
        } else if (status == 4) {
            cm.sendNextPrev("我刚刚给了你一本书，上面列出了你作为一名\"格斗家\"或\"枪手\"可以获得的技能清单。此外，你的杂项物品栏也扩展了一行。你的最大生命值和魔法值也增加了。去检查一下，看看吧。");
        } else if (status == 5) {
            cm.sendNextPrev("我也给了你一点 #bSP#k。打开左下角的 #b技能菜单#k。你可以提升新获得的二级技能。不过要注意，你不能一次性提升它们。有些技能只有在学会其他技能后才能使用。记得要牢记这一点。");
        } else if (status == 6) {
            cm.sendNextPrev((job == 510 ? "Brawlers" : "Gunslingers") + " need to be strong. But remember that you can't abuse that power and use it on a weakling. Please use your enormous power the right way, because... for you to use that the right way, that is much harden than just getting stronger. Please find me after you have advanced much further. I'll be waiting for you.");
        }
    } else if (actionx["3thJobI"]) {
        if (status == 0) {
            if (cm.getPlayer().gotPartyQuestItem("JB3")) {
                cm.getPlayer().removePartyQuestItem("JB3");
                cm.getPlayer().setPartyQuestItemObtained("JBP");
            }
            cm.sendNextPrev("因为他是我的克隆，所以你可以期待一场艰难的战斗。他使用了许多特殊的攻击技能，与你以往所见的完全不同，你的任务是成功地与他一对一地战斗。在秘密通道中有一个时间限制，所以你必须在规定时间内打败他。祝你好运，希望你带着#b#t4031059##k。");
        }
    } else if (actionx["3thJobC"]) {
        cm.getPlayer().removePartyQuestItem("JBP");
        cm.gainItem(4031059, -1);
        cm.gainItem(4031057, 1);
        cm.dispose();
    }
}