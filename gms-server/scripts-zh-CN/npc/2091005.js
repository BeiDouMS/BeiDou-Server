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
                var text = "真没想到你能走到这里！不过从这里开始可没那么容易了。你还想继续挑战吗？\r\n\r\n#b#L0#我要继续挑战#l\r\n#L1#我要离开#l\r\n";

                const MapId = Java.type('org.gms.constants.id.MapId');
                if (!MapId.isPartyDojo(cm.getPlayer().getMapId())) {
                    text += "#L2#我要记录目前为止的成绩#l";
                }
                cm.sendSimple(text);
            } else if (cm.getPlayer().getLevel() >= 25) {
                if (cm.getPlayer().getMap().getId() == 925020001) {
                    cm.sendSimple("我的主人是武陵最强大的人，你想挑战他？好吧，但你以后会后悔的。\r\n\r\n#b#L0#我想独自挑战他。#l\r\n#L1#我想组队挑战他。#l\r\n\r\n#L2#我想获得一条腰带。#l\r\n#L3#我想重置我的训练点数。#l\r\n#L4#我想获得一枚勋章。#l\r\n#L5#什么是武陵道场？#l");
                } else {
                    cm.sendYesNo("怎么，你要放弃了吗？你只差再通过下一关而已！你真的要离开吗？");
                }
            } else {
                cm.sendOk("嘿！你是在嘲笑我的主人吗？你以为你是谁，竟然敢来挑战他？真是可笑！你至少也得达到 #b25#k 级吧。");
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
                                cm.sendYesNo("喂！就是你！这是你第一次来吧？哼，我的主人可不会随便见任何人，他可是很忙的。看你这副模样，我觉得他多半不会理你。哈！不过今天算你走运……这样吧，只要你能打败我，我就让你去见我的主人。怎么样？");
                            } else if (status == 2) {
                                if (mode == 0) {
                                    cm.sendNext("哈哈！就凭你这点胆量，还想给谁留下好印象？\r\n还是回你该去的地方吧！");
                                    cm.dispose();

                                } else {
                                    var avDojo = cm.getClient().getChannelServer().ingressDojo(true, 0);

                                    if (avDojo < 0) {
                                        if (avDojo == -1) {
                                            cm.sendOk("所有道馆都已经有人在使用了。请稍等一会儿再来尝试。");
                                        } else {
                                            cm.sendOk("你的队伍可能已经在使用道馆，或者队伍在道馆的使用时间还没有结束。请等他们完成后再进入。");
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
                            cm.sendYesNo("你上次独自挑战时，已经打到了第 #b" + stageWarp + "#k 关。我现在可以直接送你过去。你想过去吗？（选择 #rNo#k 会删除这条记录。）");
                        } else {
                            var avDojo = cm.getClient().getChannelServer().ingressDojo(false, dojoWarp);

                            if (avDojo < 0) {
                                if (avDojo == -1) {
                                    cm.sendOk("所有道馆都已经有人在使用了。请稍等一会儿再来尝试。");
                                } else {
                                    cm.sendOk("你的队伍可能已经在使用道馆，或者队伍在道馆的使用时间还没有结束。请等他们完成后再进入。");
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
                            cm.sendNext("你想去哪里？你又不是队长！去叫你们队长来跟我说话。");
                            cm.dispose();
                            return;
                        }

                        if (party.getLeader().getId() != cm.getPlayer().getId()) {
                            cm.sendNext("你想去哪里？你又不是队长！去叫你们队长来跟我说话。");
                            cm.dispose();

                        }

                            //else if (party.getMembers().size() == 1) {
                            //    cm.sendNext("你打算一个人组队来挑战吗？");
                        //}

                        else if (!isBetween(party, 30)) {
                            cm.sendNext("你们队伍成员的等级差距太大，无法进入。请确保所有队员之间的等级差不超过 #r30 级#k。");
                            cm.dispose();

                        } else {
                            var avDojo = cm.getClient().getChannelServer().ingressDojo(true, cm.getParty(), 0);

                            if (avDojo < 0) {
                                if (avDojo == -1) {
                                    cm.sendOk("所有道馆都已经有人在使用了。请稍等一会儿再来尝试。");
                                } else {
                                    cm.sendOk("你的队伍可能已经在使用道馆，或者队伍在道馆的使用时间还没有结束。请等他们完成后再进入。");
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
                            cm.sendNext("在领取腰带之前，请先确保你的装备栏有足够的空间！");
                            cm.dispose();
                            return;
                        }
                        if (mode < 1) {
                            cm.dispose();
                            return;
                        }
                        if (status == 1) {
                            var selStr = "你目前拥有 #b" + cm.getPlayer().getDojoPoints() + "#k 点修炼点数。师傅偏爱有天赋的人。如果你的点数达到要求，就可以按成绩领取对应的腰带。\r\n";
                            for (var i = 0; i < belts.length; i++) {
                                if (belt_on_inventory[i]) {
                                    selStr += "\r\n#L" + i + "##i" + belts[i] + "# #t" + belts[i] + "#（背包中已拥有）";
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
                                    cm.sendNext("这是你的 #i" + belt + "# #b#t" + belt + "##k。你已经证明了自己的勇气，足以在道馆中更进一步。做得好！");
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
                            cm.sendYesNo("你知道重置训练点数后，它会归零吧？不过这也不一定是坏事。只要你重新累积训练点数，就还能再次领取腰带。你现在要重置训练点数吗？");
                        } else if (status == 2) {
                            if (mode == 0) {
                                cm.sendNext("你需要先冷静一下吗？深呼吸之后再回来吧。");
                            } else {
                                cm.getPlayer().setDojoPoints(0);
                                cm.sendNext("好了！你的训练点数已经全部重置。就把这当作全新的开始，再接再厉吧！");
                            }
                            cm.dispose();

                        }
                    } else if (selectedMenu == 4) { //I want to receive a medal.
                        if (status == 1 && cm.getPlayer().getVanquisherStage() <= 0) {
                            cm.sendYesNo("你还没有尝试过勋章挑战吗？如果你在武陵道场里打败某一种怪物 #b100 次#k，就可以获得一个名为 #b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k 的称号。看起来你连 #b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k 都还没拿到……你想挑战 #b#t" + (1142033 + cm.getPlayer().getVanquisherStage()) + "##k 吗？");
                        } else if (status == 2 || cm.getPlayer().getVanquisherStage() > 0) {
                            if (mode == 0) {
                                cm.sendNext("如果你不想尝试，那也没关系。");
                            } else {
                                if (cm.getPlayer().getDojoStage() > 37) {
                                    cm.sendNext("你已经完成了所有勋章挑战。");
                                } else if (cm.getPlayer().getVanquisherKills() < 100 && cm.getPlayer().getVanquisherStage() > 0) {
                                    cm.sendNext("你还需要再击败 #b" + (100 - cm.getPlayer().getVanquisherKills()) + "#k 只，才能获得 #b#t" + (1142032 + cm.getPlayer().getVanquisherStage()) + "##k。请再加把劲。提醒你一下，只有在武陵道场里由我们师傅召唤出的怪物才会计入次数。还有，别在打完怪之后就直接退出！#r如果你打败怪物后没有进入下一关，就不会算作胜利#k。");
                                } else if (cm.getPlayer().getVanquisherStage() <= 0) {
                                    cm.getPlayer().setVanquisherStage(1);
                                } else {
                                    cm.sendNext("你已经获得了 #b#t" + (1142032 + cm.getPlayer().getVanquisherStage()) + "##k。");
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
                        cm.sendNext("我们的师傅是武陵最强大的人。他建造的地方叫做武陵道场，是一栋高达 #r38 层#k 的建筑！你可以一层层往上挑战，锻炼自己。当然，以你现在的实力，要登上顶层可不容易。");
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
                                cm.sendOk("你不是队长！如果想继续挑战，就让队长来和我对话。");
                                cm.dispose();
                                return;
                            }

                            if (!isBetween(cm.getParty(), 35)) {
                                cm.sendOk("你们队伍成员的等级差距太大，无法进入。请确保所有队员之间的等级差不超过 #r35 级#k。");
                                cm.dispose();
                                return;
                            }
                        }

                        avDojo = cm.getClient().getChannelServer().ingressDojo(hasParty, cm.getParty(), Math.floor((cm.getPlayer().getMap().getId()) / 100) % 100);
                        firstEnter = true;
                    }

                    if (avDojo < 0) {
                        if (avDojo == -1) {
                            cm.sendOk("所有道馆都已经有人在使用了。请稍等一会儿再来尝试。");
                        } else {
                            cm.sendOk("你的队伍已经登记过道馆了。请等登记时间结束后再重新进入。");
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
                        cm.sendYesNo("所以，你要放弃了吗？你真的要离开？");
                    } else {
                        if (mode == 1) {
                            cm.warp(925020002, "st00");
                        }
                        cm.dispose();

                    }
                } else if (selectedMenu == 2) { //I want to record my score up to this point
                    if (status == 1) {
                        cm.sendYesNo("如果你记录下当前成绩，下次就可以从离开的地方继续开始。这样不是很方便吗？你要记录现在的成绩吗？");
                    } else {
                        if (mode == 0) {
                            cm.sendNext("你觉得自己还能冲得更高吗？祝你好运！");
                        } else if (cm.getPlayer().getDojoStage() == Math.floor(cm.getMapId() / 100) % 100) {
                            cm.sendOk("你的成绩已经记录好了。下次再来挑战道馆时，你就能从这里继续。");
                        } else {
                            cm.sendNext("我已经帮你记录了成绩。等你下次再来挑战时，就能从这次离开的地方继续。请注意，如果你选择 #b继续挑战武陵道场#k，当前的 #r记录会被清除#k，所以请谨慎选择。");
                            cm.getPlayer().setDojoStage(Math.floor(cm.getMapId() / 100) % 100);
                        }
                        cm.dispose();

                    }
                }
            } else {
                if (mode == 0) {
                    cm.sendNext("别再反复改主意了！再这样下去，你很快就会哭着求我带你回去的。");
                } else if (mode == 1) {
                    var dojoMapId = cm.getPlayer().getMap().getId();

                    cm.warp(925020002, 0);
                    cm.getPlayer().message("先想清楚再来吧。");

                    cm.getClient().getChannelServer().freeDojoSectionIfEmpty(dojoMapId);
                }
                cm.dispose();
            }
        }
    }
}

function sendBeltRequirements(belt, oldbelt, haveOldbelt, level, points) {
    var beltReqStr = (oldbelt != -1) ? " 你背包里还必须有 #i" + oldbelt + "# 这条腰带，" : "";

    var pointsLeftStr = (points - cm.getPlayer().getDojoPoints() > 0) ? " 你还需要再获得 #r" + (points - cm.getPlayer().getDojoPoints()) + "#k 点训练点数" : "";
    var beltLeftStr = (!haveOldbelt) ? " 你必须先把所需的前置腰带卸下，并放在装备栏里" : "";
    var conjStr = (pointsLeftStr.length > 0 && beltLeftStr.length > 0) ? "，并且" : "";

    cm.sendNext("想获得 #i" + belt + "# #b#t" + belt + "##k，" + beltReqStr + "你的等级必须至少达到 #b" + level + "#k，并且至少要累积 #b" + points + " 点训练点数#k。\r\n\r\n如果你想领取这条腰带，" + beltLeftStr + conjStr + pointsLeftStr + "。");
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
