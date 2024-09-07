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
/*
* @Author: Moogra, XxOsirisxX
* @NPC:    2091005
* @Name:   So Gong
* @Map(s): Dojo Hall
*/

var disabled = false;
var belts = Array(1132000, 1132001, 1132002, 1132003, 1132004);
var belt_level = Array(25, 35, 45, 60, 75);

/* var belt_points = Array(200, 1800, 4000, 9200, 17000); */
var belt_points = Array(5, 45, 100, 230, 425); /* Watered down version */

var status = -1;
var selectedMenu = -1;

function start() {
    if (disabled) {
        cm.sendOk("我的师傅要求现在关闭道馆，所以我不能让你进去。");
        cm.dispose();
        return;
    }

    if (isRestingSpot(cm.getPlayer().getMap().getId())) {
        var text = "I'm surprised you made it this far! But it won't be easy from here on out. You still want the challenge?\r\n\r\n#b#L0#I want to continue#l\r\n#L1#I want to leave#l\r\n";

        const GameConstants = Java.type('org.gms.constants.game.GameConstants');
        if (!GameConstants.isDojoPartyArea(cm.getPlayer().getMapId())) {
            text += "#L2#I want to record my score up to this point#l";
        }
        cm.sendSimple("抱歉，我无法完成你的要求。");
    } else if (cm.getPlayer().getLevel() >= 25) {
        if (cm.getPlayer().getMap().getId() == 925020001) {
            cm.sendSimple("我的主人是武陵最强大的人，你想挑战他？好吧，但你以后会后悔的。\r\n#b#L0#我想独自挑战他。#l\r\n#L1#我想组队挑战他。#l\r\n#L2#我想获得一条腰带。#l\r\n#L3#我想重置我的训练点数。#l\r\n#L4#我想获得一枚勋章。#l\r\n#L5#什么是武陵道场？#l");
        } else {
            cm.sendYesNo("什么，你要放弃了吗？你只需要达到下一个级别！你真的想要放弃并离开吗？");
        }
    } else {
        cm.sendOk("嘿！你在嘲笑我的主人吗？你以为你是谁来挑战他？这太可笑了！你至少应该是 #b25#k 级别。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (cm.getPlayer().getMap().getId() == 925020001) {
        if (mode >= 0) {
            if (status == -1) {
                selectedMenu = selection;
            }
            status++; //there is no prev.
            if (selectedMenu == 0) { //I want to challenge him alone.
                if (!cm.getPlayer().hasEntered("dojang_Msg") && !cm.getPlayer().isFinishedDojoTutorial()) { //kind of hackish...
                    if (status == 0) {
                        cm.sendYesNo("嘿！你！这是你第一次来吗？嗯，我的主人不会随便见任何人。他很忙。看你的样子，我觉得他不会理你。哈！但是，今天是你的幸运日……我告诉你吧，如果你能打败我，我就让你见我的主人。你觉得怎么样？");
                    } else if (status == 1) {
                        if (mode == 0) {
                            cm.sendNext("哈哈！你这样的心，想要给谁留下好印象呢？\r\n还是回到你应该去的地方吧！");
                        } else {
                            if (cm.getClient().getChannelServer().getMapFactory().getMap(925020010).getCharacters().size() > 0) {
                                cm.sendOk("有人已经在道馆里了。");
                                cm.dispose();
                                return;
                            }
                            cm.warp(925020010, 0);
                            cm.getPlayer().finishDojoTutorial();
                        }
                        cm.dispose();
                    }
                } else if (cm.getPlayer().getDojoStage() > 0) {
                    if (status == 0) {
                        cm.sendYesNo("上次你独自挑战时，你达到了第 " + cm.getPlayer().getDojoStage() + " 层。我现在可以带你去那里。你想去吗？");
                    } else {
                        cm.warp(mode == 1 ? 925020000 + cm.getPlayer().getDojoStage() * 100 : 925020100, 0);
                        cm.dispose();
                    }
                } else {
                    for (var i = 1; i < 39; i++) { //only 32 stages, but 38 maps
                        if (cm.getClient().getChannelServer().getMapFactory().getMap(925020000 + 100 * i).getCharacters().size() > 0) {
                            cm.sendOk("有人已经在道馆里了。" + i);
                            cm.dispose();
                            return;
                        }
                    }
                    cm.getClient().getChannelServer().getMapFactory().getMap(925020100).resetReactors();
                    cm.getClient().getChannelServer().getMapFactory().getMap(925020100).killAllMonsters();
                    cm.warp(925020100, 0);
                    cm.dispose();
                }
            } else if (selectedMenu == 1) { //I want to challenge him with a party.
                var party = cm.getPlayer().getParty();
                if (party == null) {
                    cm.sendNext("你以为你要去哪里？你甚至不是队伍的领袖！去告诉你的队伍领袖来找我谈话。");
                    cm.dispose();
                    return;
                }
                var lowest = cm.getPlayer().getLevel();
                var highest = lowest;
                for (var x = 0; x < party.getMembers().size(); x++) {
                    var lvl = party.getMembers().get(x).getLevel();
                    if (lvl > highest) {
                        highest = lvl;
                    } else if (lvl < lowest) {
                        lowest = lvl;
                    }
                }
                var isBetween30 = highest - lowest < 30;
                if (party.getLeader().getId() != cm.getPlayer().getId()) {
                    cm.sendNext("你以为你要去哪里？你甚至不是队伍的领袖！去告诉你的队伍领袖来找我谈话。");
                    cm.dispose();
                } else if (party.getMembers().size() == 1) {
                    cm.sendNext("你要独自接受挑战吗？");
                } else if (!isBetween30) {
                    cm.sendNext("你的队伍成员等级范围太广，无法进入。请确保你的所有队伍成员等级相差不超过#r30级#k。");
                } else {
                    for (var i = 1; i < 39; i++) { //only 32 stages, but 38 maps
                        if (cm.getClient().getChannelServer().getMapFactory().getMap(925020000 + 100 * i).getCharacters().size() > 0) {
                            cm.sendOk("有人已经在道馆里了。");
                            cm.dispose();
                            return;
                        }
                    }
                    cm.getClient().getChannelServer().getMapFactory().getMap(925020100).resetReactors();
                    cm.getClient().getChannelServer().getMapFactory().getMap(925020100).killAllMonsters();
                    cm.warpParty(925020100);
                    cm.dispose();
                }
                cm.dispose();
            } else if (selectedMenu == 2) { //I want to receive a belt.
                if (mode < 1) {
                    cm.dispose();
                    return;
                }
                if (status == 0) {
                    var selStr = "You have #b" + cm.getPlayer().getDojoPoints() + "#k training points. Master prefers those with great talent. If you obtain more points than the average, you can receive a belt depending on your score.\r\n";
                    for (var i = 0; i < belts.length; i++) {
                        if (cm.haveItemWithId(belts[i], true)) {
                            selStr += "\r\n     #i" + belts[i] + "# #t" + belts[i] + "#(Obtain)";
                        } else {
                            selStr += "\r\n#L" + i + "##i" + belts[i] + "# #t" + belts[i] + "#l";
                        }
                    }
                    cm.sendSimple("selStr 可能是一个变量名或者缩写，无法提供准确的翻译。");
                } else if (status == 1) {
                    var belt = belts[selection];
                    var level = belt_level[selection];
                    var points = belt_points[selection];
                    if (cm.getPlayer().getDojoPoints() > points) {
                        if (cm.getPlayer().getLevel() > level) {
                            cm.gainItem(belt, 1);
                        } else {
                            cm.sendNext("为了获得 #i" + belt + "# #b#t" + belt + "##k，你至少需要达到等级 #b" + level + "#k，并且至少需要获得 #b" + points + " 训练点数#k。\r\n\r\n如果你想获得这条腰带，你需要 #r" + (points - cm.getPlayer().getDojoPoints()) + "#k 更多的训练点数。");
                        }
                    } else {
                        cm.sendNext("为了获得 #i" + belt + "# #b#t" + belt + "##k，你至少需要达到等级 #b" + level + "#k，并且至少需要获得 #b" + points + " 训练点数#k。\r\n\r\n如果你想获得这条腰带，你需要 #r" + (points - cm.getPlayer().getDojoPoints()) + "#k 更多的训练点数。");
                    }
                    cm.dispose();
                }
            } else if (selectedMenu == 3) { //I want to reset my training points.
                if (status == 0) {
                    cm.sendYesNo("你知道如果你重置你的训练点，它会返回到0，对吧？虽然，这并不总是一件坏事。如果你在重置后可以重新开始获得训练点，你就可以再次获得腰带。你现在想要重置你的训练点吗？");
                } else if (status == 1) {
                    if (mode == 0) {
                        cm.sendNext("你需要冷静一下吗？深呼吸后再回来。");
                    } else {
                        cm.getPlayer().setDojoPoints(0);
                        cm.sendNext("好了！你所有的训练点数已经被重置。把它看作一个新的开始，努力训练吧！");
                    }
                    cm.dispose();
                }
            } else if (selectedMenu == 4) { //I want to receive a medal.
                if (status == 0 && cm.getPlayer().getVanquisherStage() <= 0) {
                    cm.sendYesNo("你还没有尝试过勋章吗？如果你在武陵道场打败某一种怪物#b100次#k，你就可以获得一个称号叫做#b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k。看起来你甚至还没有获得#b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k... 你想尝试一下#b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k吗？");
                } else if (status == 1 || cm.getPlayer().getVanquisherStage() > 0) {
                    if (mode == 0) {
                        cm.sendNext("如果你不想的话，没关系。");
                        cm.dispose();
                    } else {
                        if (cm.getPlayer().getDojoStage() > 37) {
                            cm.sendNext("你已经完成了所有勋章挑战。");
                        } else if (cm.getPlayer().getVanquisherKills() < 100 && cm.getPlayer().getVanquisherStage() > 0) {
                            cm.sendNext("你仍然需要 #b" + (100 - cm.getPlayer().getVanquisherKills()) + "#k 才能获得 #b#t" + (1142032 + cm.getPlayer().getVanquisherStage()) + "##k。请再努力一点。提醒一下，只有在武陵道场由我们的大师召唤的怪物才算数。哦，还要确保你不是在打怪后就退出！#r如果你打败怪物后没有进入下一关，就不算胜利#k。");
                        } else if (cm.getPlayer().getVanquisherStage() <= 0) {
                            cm.getPlayer().setVanquisherStage(1);
                        } else {
                            cm.sendNext("你已经获得了#b#t" + (1142032 + cm.getPlayer().getVanquisherStage()) + "##k。");
                            cm.gainItem(1142033 + cm.getPlayer().getVanquisherStage(), 1);
                            cm.getPlayer().setVanquisherStage(cm.c.getPlayer().getVanquisherStage() + 1);
                            cm.getPlayer().setVanquisherKills(0);
                        }
                    }
                    cm.dispose();
                }
            } else if (selectedMenu == 5) { //What is a Mu Lung Dojo?
                cm.sendNext("我们的师傅是武陵最强大的人。他建造的地方叫做武陵道场，一座有38层楼高的建筑！你可以在每一层上训练自己。当然，对于你这个级别的人来说，要到达顶层会很困难。");
                cm.dispose();
            }
        } else {
            cm.dispose();
        }
    } else if (isRestingSpot(cm.getPlayer().getMap().getId())) {
        if (selectedMenu == -1) {
            selectedMenu = selection;
        }
        status++;
        if (selectedMenu == 0) {
            cm.warp(cm.getPlayer().getMap().getId() + 100, 0);
            cm.dispose();
        } else if (selectedMenu == 1) { //I want to leave
            if (status == 0) {
                cm.sendAcceptDecline("So, you're giving up? You're really going to leave?");
            } else {
                if (mode == 1) {
                    cm.warp(925020002, "st00");
                }
                cm.dispose();
            }
        } else if (selectedMenu == 2) { //I want to record my score up to this point
            if (status == 0) {
                cm.sendYesNo("如果你记录下你的分数，下次可以从上次离开的地方开始。这不是很方便吗？你想记录下当前的分数吗？");
            } else {
                if (mode == 0) {
                    cm.sendNext("你觉得你还能更上一层楼吗？祝你好运！");
                } else if (925020000 + cm.getPlayer().getDojoStage() * 100 == cm.getMapId()) {
                    cm.sendOk("你的分数已经被记录下来。下次你挑战道馆时，你可以回到这个地方。");
                } else {
                    cm.sendNext("我已记录下你的分数。下次你再上去的时候告诉我，你就可以从上次离开的地方开始。");
                    cm.getPlayer().setDojoStage((cm.getMapId() - 925020000) / 100);
                }
                cm.dispose();
            }
        }
    } else {
        if (mode == 0) {
            cm.sendNext("停止改变主意！很快，你会哭着求我回去的。");
        } else if (mode == 1) {
            cm.warp(925020002, 0);
            cm.getPlayer().message("Can you make up your mind please?");
        }
        cm.dispose();
    }
}

function isRestingSpot(id) {
    return (id / 100 - 9250200) % 6 == 0;
}