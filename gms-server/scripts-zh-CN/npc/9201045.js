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
@       Author : Ronan
@
@	NPC = Amos (PQ)
@	Map = AmoriaPQ maps
@	Function = AmoriaPQ Host
@
@	Description: Last stages of the Amorian Challenge
*/

var debug = false;
var status = 0;
var curMap, stage;

function isAllGatesOpen() {
    var map = cm.getPlayer().getMap();

    for (var i = 0; i < 7; i++) {
        var gate = map.getReactorByName("gate0" + i);
        if (gate.getState() != 4) {
            return false;
        }
    }

    return true;
}

function clearStage(stage, eim, curMap) {
    eim.setProperty(stage + "stageclear", "true");

    eim.showClearEffect(true);
    eim.linkToNextStage(stage, "apq", curMap);  //opens the portal to the next map
}

function start() {
    curMap = cm.getMapId();
    stage = Math.floor((curMap - 670010200) / 100) + 1;

    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (mode == 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        var eim = cm.getPlayer().getEventInstance();
        if (eim.getProperty(stage.toString() + "stageclear") != null) {
            if (stage < 5) {
                cm.sendNext("传送门已经打开，前往那里等待你的考验。");
            } else if (stage == 5) {
                eim.warpEventTeamToMapSpawnPoint(670010700, 0);
            } else {
                if (cm.isEventLeader()) {
                    if (eim.getIntProperty("marriedGroup") == 0) {
                        eim.restartEventTimer(1 * 60 * 1000);
                        eim.warpEventTeam(670010800);
                    } else {
                        eim.setIntProperty("marriedGroup", 0);

                        eim.restartEventTimer(2 * 60 * 1000);
                        eim.warpEventTeamToMapSpawnPoint(670010750, 1);
                    }
                } else {
                    cm.sendNext("等待队长的指令开始奖励阶段。");
                }
            }
        } else {
            if (stage != 6) {
                if (eim.isEventLeader(cm.getPlayer())) {
                    var state = eim.getIntProperty("statusStg" + stage);

                    if (state == -1) {           // preamble
                        if (stage == 4) {
                            cm.sendOk("嗨。欢迎来到阿莫利亚挑战的#b舞台#k。在这个阶段，从这里周围的怪物身上收集#b50个#t4031597##k。");
                        } else if (stage == 5) {
                            cm.sendOk("嗨。欢迎来到阿莫利亚挑战的#b舞台#k。要到达这里可是一场不小的奔跑，是吧？好吧，无论如何，这个阶段的任务就是生存！首先，确保有人活着聚集在这里，然后再挑战boss。");
                        }

                        var st = (debug) ? 2 : 0;
                        eim.setProperty("statusStg" + stage, st);
                    } else {       // check stage completion
                        if (stage == 4) {
                            if (cm.haveItem(4031597, 50)) {
                                cm.gainItem(4031597, -50);

                                var tl = eim.getTimeLeft();
                                if (tl >= 5 * 60 * 1000) {
                                    eim.setProperty("timeLeft", tl.toString());
                                    eim.restartEventTimer(4 * 60 * 1000);
                                }

                                cm.sendNext("干得好！现在让我为你打开大门。");
                                cm.mapMessage(5, "Amos: The time runs short now. Your objective is to open the gates and gather together on the other side of the next map. Good luck!");
                                clearStage(stage, eim, curMap);
                            } else {
                                cm.sendNext("嘿，你没听清楚吗？我要求 #r50 #t4031597##k 作为这次试炼的成功报酬。");
                            }

                        } else if (stage == 5) {
                            var pass = true;

                            if (eim.isEventTeamTogether()) {
                                var party = cm.getEventInstance().getPlayers();
                                var area = cm.getMap().getArea(2);

                                for (var i = 0; i < party.size(); i++) {
                                    var chr = party.get(i);

                                    if (chr.isAlive() && !area.contains(chr.getPosition())) {
                                        pass = false;
                                        break;
                                    }
                                }
                            } else {
                                pass = false;
                            }

                            if (pass) {
                                if (isAllGatesOpen()) {
                                    var tl = eim.getProperty("timeLeft");
                                    if (tl != null) {
                                        var tr = eim.getTimeLeft();

                                        var tl = parseFloat(tl);
                                        eim.restartEventTimer(tl - (4 * 60 * 1000 - tr));
                                    }

                                    cm.sendNext("好的，你的团队已经集合好了。当你们感觉准备好与 #rGeist Balrog#k 战斗时，和我交谈。");

                                    cm.mapMessage(5, "Amos: Now only the boss fight remains! Once inside, talk to me only if you want to join the boss fight, you will be transported to action immediately.");
                                    clearStage(stage, eim, curMap);
                                } else {
                                    cm.sendNext("你们是通过传送到达这里的，是吗？我能感觉到。真是遗憾，所有的门都必须打开才能完成这个阶段。如果你们还有时间的话，回头走一遍你们的路，把那些门都关掉。");
                                }
                            } else {
                                cm.sendNext("你的团队还没有聚集在附近。给他们一些时间到达这里。");
                            }
                        }
                    }
                } else {
                    cm.sendNext("请告诉你的#b队长#k来找我谈话。");
                }
            } else {
                var area = cm.getMap().getArea(0);
                if (area.contains(cm.getPlayer().getPosition())) {
                    if (cm.getPlayer().isAlive()) {
                        cm.warp(670010700, "st01");
                    } else {
                        cm.sendNext("喂，退后一点……你已经死了。");
                    }
                } else {
                    if (cm.isEventLeader()) {
                        if (cm.haveItem(4031594, 1)) {
                            cm.gainItem(4031594, -1);
                            cm.sendNext("恭喜！你的队伍打败了鬼魂巴尔洛格，因此#b完成了阿莫利亚挑战#k！再次与我交谈以开始奖励阶段。");

                            clearStage(stage, eim, curMap);
                            eim.clearPQ();
                        } else {
                            cm.sendNext("How is it? Are you going to retrieve me the #b#t4031594##k? That's your last trial, hold on!")
                        }
                    } else {
                        cm.sendNext("请告诉你的#b队长#k来找我谈话。");
                    }
                }
            }
        }

        cm.dispose();
    }
}