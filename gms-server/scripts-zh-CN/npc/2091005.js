/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>
    Copyleft (L) 2016 - 2019 RonanLana (HeavenMS)

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
* @Author: Moogra, XxOsirisxX, Ronan
* @NPC:    2091005
* @Name:   So Gong
* @Map(s): Dojo Hall
*/

var disabled = false;
var belts = Array(1132000, 1132001, 1132002, 1132003, 1132004);
var belt_level = Array(25, 35, 45, 60, 75);
var belt_on_inventory;
var belt_points;

var status = -1;
var selectedMenu = -1;
var dojoWarp = 0;

function start() {
    if (disabled) {
        cm.sendOk("我的师傅要求现在关闭道馆，所以我不能让你进去。");
        cm.dispose();
        return;
    }

    const GameConfig = Java.type('org.gms.config.GameConfig');
    belt_points = GameConfig.getServerBoolean("use_fast_dojo_upgrade") ? Array(10, 90, 200, 460, 850) : Array(200, 1800, 4000, 9200, 17000);

    belt_on_inventory = [];
    for (var i = 0; i < belts.length; i++) {
        belt_on_inventory.push(cm.haveItemWithId(belts[i], true));
    }

    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.getPlayer().setDojoStage(dojoWarp);
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        }

        if (status == 0) {
            if (isRestingSpot(cm.getPlayer().getMap().getId())) {
                var text = "I'm surprised you made it this far! But it won't be easy from here on out. You still want the challenge?\r\n\r\n#b#L0#I want to continue#l\r\n#L1#I want to leave#l\r\n";

                const MapId = Java.type('org.gms.constants.id.MapId');
                if (!MapId.isPartyDojo(cm.getPlayer().getMapId())) {
                    text += "#L2#I want to record my score up to this point#l";
                }
                cm.sendSimple(text);
            } else if (cm.getPlayer().getLevel() >= 25) {
                if (cm.getPlayer().getMap().getId() == 925020001) {
                    cm.sendSimple("我的主人是武陵最强大的人，你想挑战他？好吧，但你以后会后悔的。\r\n\r\n#b#L0#我想独自挑战他。#l\r\n#L1#我想组队挑战他。#l\r\n\r\n#L2#我想获得一条腰带。#l\r\n#L3#我想重置我的训练点数。#l\r\n#L4#我想获得一枚勋章。#l\r\n#L5#什么是武陵道场？#l");
                } else {
                    cm.sendYesNo("什么，你要放弃了吗？你只需要达到下一个级别！你真的想要放弃并离开吗？");
                }
            } else {
                cm.sendOk("嘿！你在嘲笑我的主人吗？你以为你是谁来挑战他？这太可笑了！你至少应该是 #b25#k 级别。");
                cm.dispose();

            }
        } else {
            if (cm.getPlayer().getMap().getId() == 925020001) {
                if (mode >= 0) {
                    if (status == 1) {
                        selectedMenu = selection;
                    }
                    if (selectedMenu == 0) { //I want to challenge him alone.
                        if (!cm.getPlayer().hasEntered("dojang_Msg") && !cm.getPlayer().isFinishedDojoTutorial()) { //kind of hackish...
                            if (status == 1) {
                                cm.sendYesNo("嘿！你！这是你第一次来吗？嗯，我的主人不会随便见任何人。他很忙。看你的样子，我觉得他不会理你。哈！但是，今天是你的幸运日……我告诉你吧，如果你能打败我，我就让你见我的主人。你觉得怎么样？");
                            } else if (status == 2) {
                                if (mode == 0) {
                                    cm.sendNext("哈哈！你这样的心，想要给谁留下好印象呢？\r\n还是回到你应该去的地方吧！");
                                    cm.dispose();

                                } else {
                                    var avDojo = cm.getClient().getChannelServer().ingressDojo(true, 0);

                                    if (avDojo < 0) {
                                        if (avDojo == -1) {
                                            cm.sendOk("所有道馆都已经被使用了。请稍等一会再尝试。");
                                        } else {
                                            cm.sendOk("你的队伍可能已经在使用道馆，或者你的队伍在道馆的预定时间还没有结束。请等待他们完成后再进入。");
                                        }
                                    } else {
                                        cm.getClient().getChannelServer().getMapFactory().getMap(925020010 + avDojo).resetMapObjects();

                                        cm.resetDojoEnergy();
                                        cm.warp(925020010 + avDojo, 0);
                                    }

                                    cm.dispose();

                                }
                            }
                        } else if (cm.getPlayer().getDojoStage() > 0) {
                            dojoWarp = cm.getPlayer().getDojoStage();
                            cm.getPlayer().setDojoStage(0);

                            var stageWarp = ((dojoWarp / 6) | 0) * 5;
                            cm.sendYesNo("上次你独自挑战时，你一直走到了第#b" + stageWarp + "#k关。我现在可以带你去那里。你想去那里吗？（选择#rNo#k来删除这个记录。）");
                        } else {
                            var avDojo = cm.getClient().getChannelServer().ingressDojo(false, dojoWarp);

                            if (avDojo < 0) {
                                if (avDojo == -1) {
                                    cm.sendOk("所有道馆都已经被使用了。请等一会儿再试。");
                                } else {
                                    cm.sendOk("你的队伍可能已经在使用道馆，或者你的队伍在道馆的允许时间还没有结束。请等待他们完成后再进入。");
                                }

                                cm.getPlayer().setDojoStage(dojoWarp);
                            } else {
                                var warpDojoMap = 925020000 + (dojoWarp + 1) * 100 + avDojo;
                                cm.getClient().getChannelServer().resetDojoMap(warpDojoMap);

                                cm.resetDojoEnergy();
                                cm.warp(warpDojoMap, 0);
                            }

                            cm.dispose();

                        }
                    } else if (selectedMenu == 1) { //I want to challenge him with a party.
                        var party = cm.getPlayer().getParty();
                        if (party == null) {
                            cm.sendNext("你以为你要去哪里？你甚至不是队伍的领袖！去告诉你的队伍领袖来找我谈话。");
                            cm.dispose();
                            return;
                        }

                        if (party.getLeader().getId() != cm.getPlayer().getId()) {
                            cm.sendNext("你以为你要去哪里？你甚至不是队伍的领袖！去告诉你的队伍领袖来找我谈话。");
                            cm.dispose();

                        }

                            //else if (party.getMembers().size() == 1) {
                            //    cm.sendNext("你要独自接受挑战吗？");
                        //}

                        else if (!isBetween(party, 30)) {
                            cm.sendNext("你的队伍成员等级范围太广，无法进入。请确保你的所有队伍成员等级相差不超过#r30级#k。");
                            cm.dispose();

                        } else {
                            var avDojo = cm.getClient().getChannelServer().ingressDojo(true, cm.getParty(), 0);

                            if (avDojo < 0) {
                                if (avDojo == -1) {
                                    cm.sendOk("所有道馆都已经被使用了。请稍等一会再尝试。");
                                } else {
                                    cm.sendOk("你的队伍可能已经在使用道馆，或者你的队伍在道馆的预定时间还没有结束。请等待他们完成后再进入。");
                                }
                            } else {
                                cm.getClient().getChannelServer().resetDojoMap(925030100 + avDojo);

                                cm.resetPartyDojoEnergy();
                                cm.warpParty(925030100 + avDojo);
                            }

                            cm.dispose();

                        }

                    } else if (selectedMenu == 2) { //I want to receive a belt.
                        if (!cm.canHold(belts[0])) {
                            cm.sendNext("在尝试领取腰带之前，请确保你的装备栏有足够的空间！");
                            cm.dispose();
                            return;
                        }
                        if (mode < 1) {
                            cm.dispose();
                            return;
                        }
                        if (status == 1) {
                            var selStr = "You have #b" + cm.getPlayer().getDojoPoints() + "#k training points. Master prefers those with great talent. If you obtain more points than the average, you can receive a belt depending on your score.\r\n";
                            for (var i = 0; i < belts.length; i++) {
                                if (belt_on_inventory[i]) {
                                    selStr += "\r\n#L" + i + "##i" + belts[i] + "# #t" + belts[i] + "# (Already on inventory)";
                                } else {
                                    selStr += "\r\n#L" + i + "##i" + belts[i] + "# #t" + belts[i] + "#";
                                }
                            }
                            cm.sendSimple(selStr);
                        } else if (status == 2) {
                            var belt = belts[selection];
                            var level = belt_level[selection];
                            var points = belt_points[selection];

                            var oldbelt = (selection > 0) ? belts[selection - 1] : -1;
                            var haveOldbelt = (oldbelt == -1 || cm.haveItemWithId(oldbelt, false));

                            if (selection > 0 && !belt_on_inventory[selection - 1]) {
                                sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points);
                            } else if (cm.getPlayer().getDojoPoints() >= points) {
                                if (selection > 0 && !haveOldbelt) {
                                    sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points);
                                } else if (cm.getPlayer().getLevel() > level) {
                                    if (selection > 0) {
                                        cm.gainItem(oldbelt, -1);
                                    }
                                    cm.gainItem(belt, 1);
                                    cm.getPlayer().setDojoPoints(cm.getPlayer().getDojoPoints() - points);
                                    cm.sendNext("这里有一个 #i" + belt + "# #b#t" + belt + "##k。你已经证明了自己的勇气，可以在道馆排名中晋升。干得好！");
                                } else {
                                    sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points);
                                }
                            } else {
                                sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points);
                            }

                            cm.dispose();

                        }
                    } else if (selectedMenu == 3) { //I want to reset my training points.
                        if (status == 1) {
                            cm.sendYesNo("你知道如果你重置你的训练点数，它会返回到0，对吧？不过，这并不总是件坏事。如果你在重置后能够重新开始赚取训练点数，你就可以再次获得腰带。你现在想要重置你的训练点数吗？");
                        } else if (status == 2) {
                            if (mode == 0) {
                                cm.sendNext("你需要冷静一下吗？深呼吸后再回来。");
                            } else {
                                cm.getPlayer().setDojoPoints(0);
                                cm.sendNext("好了！你所有的训练点数都已经重置了。把它看作一个新的开始，努力训练吧！");
                            }
                            cm.dispose();

                        }
                    } else if (selectedMenu == 4) { //I want to receive a medal.
                        if (status == 1 && cm.getPlayer().getVanquisherStage() <= 0) {
                            cm.sendYesNo("你还没有尝试过勋章吗？如果你在勇士部落道场中打败某种类型的怪物#b100次#k，你就可以获得一个称号，叫做#b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k。看起来你甚至还没有获得#b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k... 你想尝试一下#b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k吗？");
                        } else if (status == 2 || cm.getPlayer().getVanquisherStage() > 0) {
                            if (mode == 0) {
                                cm.sendNext("如果你不想的话，没关系。");
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

                        } else {
                            cm.dispose();

                        }
                    } else if (selectedMenu == 5) { //What is a Mu Lung Dojo?
                        cm.sendNext("我们的师傅是武陵最强大的人。他建造的地方叫做武陵道场，一栋有38层楼高的建筑！你可以在每一层上训练自己。当然，对于你这个级别的人来说，要到达顶层会很困难。");
                        cm.dispose();

                    }
                } else {
                    cm.dispose();

                }
            } else if (isRestingSpot(cm.getPlayer().getMap().getId())) {
                if (selectedMenu == -1) {
                    selectedMenu = selection;
                }

                if (selectedMenu == 0) {
                    var hasParty = (cm.getParty() != null);

                    var firstEnter = false;
                    var avDojo = cm.getClient().getChannelServer().lookupPartyDojo(cm.getParty());
                    if (avDojo < 0) {
                        if (hasParty) {
                            if (!cm.isPartyLeader()) {
                                cm.sendOk("你不是队长！如果你想继续，让你们的队长来找我谈话。");
                                cm.dispose();
                                return;
                            }

                            if (!isBetween(cm.getParty(), 35)) {
                                cm.sendOk("你的队伍成员等级范围太广，无法进入。请确保你的所有队伍成员等级相差不超过#r35级#k。");
                                cm.dispose();
                                return;
                            }
                        }

                        avDojo = cm.getClient().getChannelServer().ingressDojo(hasParty, cm.getParty(), Math.floor((cm.getPlayer().getMap().getId()) / 100) % 100);
                        firstEnter = true;
                    }

                    if (avDojo < 0) {
                        if (avDojo == -1) {
                            cm.sendOk("所有道馆都已经被使用了。请等一会儿再试。");
                        } else {
                            cm.sendOk("你的队伍已经注册了道馆。等待注册时间结束后再次进入。");
                        }
                    } else {
                        var baseStg = hasParty ? 925030000 : 925020000;
                        var nextStg = Math.floor((cm.getPlayer().getMap().getId() + 100) / 100) % 100;

                        var dojoWarpMap = baseStg + (nextStg * 100) + avDojo;
                        if (firstEnter) {
                            cm.getClient().getChannelServer().resetDojoMap(dojoWarpMap);
                        }

                        //non-leader party members can progress whilst having the record saved if they don't command to enter the next stage
                        cm.getPlayer().setDojoStage(0);

                        if (!hasParty || !cm.isLeader()) {
                            cm.warp(dojoWarpMap, 0);
                        } else {
                            cm.warpParty(dojoWarpMap, 0);
                        }
                    }

                    cm.dispose();

                } else if (selectedMenu == 1) { //I want to leave
                    if (status == 1) {
                        cm.sendYesNo("所以，你要放弃了吗？你真的要离开吗？");
                    } else {
                        if (mode == 1) {
                            cm.warp(925020002, "st00");
                        }
                        cm.dispose();

                    }
                } else if (selectedMenu == 2) { //I want to record my score up to this point
                    if (status == 1) {
                        cm.sendYesNo("如果你记录下你的分数，下次可以从上次离开的地方开始。这不是很方便吗？你想记录下当前的分数吗？");
                    } else {
                        if (mode == 0) {
                            cm.sendNext("你觉得你还能再走得更高吗？祝你好运！");
                        } else if (cm.getPlayer().getDojoStage() == Math.floor(cm.getMapId() / 100) % 100) {
                            cm.sendOk("你的分数已经被记录下来。下次挑战道馆时，你可以回到这个点。");
                        } else {
                            cm.sendNext("我已记录下你的分数。如果你告诉我下次你再次挑战时，你将能够从上次离开的地方开始。请注意，如果你选择继续挑战道馆，你的#r记录将被抹去#k，所以请谨慎选择。");
                            cm.getPlayer().setDojoStage(Math.floor(cm.getMapId() / 100) % 100);
                        }
                        cm.dispose();

                    }
                }
            } else {
                if (mode == 0) {
                    cm.sendNext("别再改变主意了！很快，你会哭着求我回去的。");
                } else if (mode == 1) {
                    var dojoMapId = cm.getPlayer().getMap().getId();

                    cm.warp(925020002, 0);
                    cm.getPlayer().message("Can you make up your mind please?");

                    cm.getClient().getChannelServer().freeDojoSectionIfEmpty(dojoMapId);
                }
                cm.dispose();
            }
        }
    }
}

function sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points) {
    var beltReqStr = (oldbelt != -1) ? " you must have the #i" + oldbelt + "# belt in your inventory," : "";

    var pointsLeftStr = (points - cm.getPlayer().getDojoPoints() > 0) ? " you need #r" + (points - cm.getPlayer().getDojoPoints()) + "#k more training points" : "";
    var beltLeftStr = (!haveOldbelt) ? " you must have the needed belt unequipped and available in your EQP inventory" : "";
    var conjStr = (pointsLeftStr.length > 0 && beltLeftStr.length > 0) ? " and" : "";

    cm.sendNext("为了获得 #i" + belt + "# #b#t" + belt + "##k," + beltReqStr + " 你至少需要达到等级 #b" + level + "#k 并且至少需要获得 #b" + points + " 训练点数#k。\r\n\r\n如果你想获得这条腰带，" + beltLeftStr + conjStr + pointsLeftStr + "。");
}

function isRestingSpot(id) {
    return (Math.floor(id / 100) % 100) % 6 == 0 && id != 925020001;
}

function isBetween(party, range) {
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
    return (highest - lowest) <= range;
}