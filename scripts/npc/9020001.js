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

/**
 * @author: Stereo, Moogra, Ronan
 * @npc: Cloto
 * @map: 1st Accompaniment - KPQ
 * @func: Kerning PQ
 */

var stage1Questions = Array(
    "收集与答案相同数量的证书来交换通行证：#b转职为战士需要多少等级",
    "收集与答案相同数量的证书来交换通行证：#b转职为战士需要多少力量",
    "收集与答案相同数量的证书来交换通行证：#b转职为魔法师需要多少智力",
    "收集与答案相同数量的证书来交换通行证：#b转职为弓箭手需要多少敏捷",
    "收集与答案相同数量的证书来交换通行证：#b转职为飞侠需要多少敏捷",
    "收集与答案相同数量的证书来交换通行证：#b第二次转职需要多少等级",
    "收集与答案相同数量的证书来交换通行证：#b转职为魔法师需要多少等级");
var stage1Answers = Array(10, 35, 20, 25, 25, 30, 8);

const Rectangle = Java.type('java.awt.Rectangle');
var stage2Rects = Array(new Rectangle(-755, -132, 4, 218), new Rectangle(-721, -340, 4, 166), new Rectangle(-586, -326, 4, 150), new Rectangle(-483, -181, 4, 222));
var stage3Rects = Array(new Rectangle(608, -180, 140, 50), new Rectangle(791, -117, 140, 45),
    new Rectangle(958, -180, 140, 50), new Rectangle(876, -238, 140, 45),
    new Rectangle(702, -238, 140, 45));
var stage4Rects = Array(new Rectangle(910, -236, 35, 5), new Rectangle(877, -184, 35, 5),
    new Rectangle(946, -184, 35, 5), new Rectangle(845, -132, 35, 5),
    new Rectangle(910, -132, 35, 5), new Rectangle(981, -132, 35, 5));

var stage2Combos = Array(Array(0, 1, 1, 1), Array(1, 0, 1, 1), Array(1, 1, 0, 1), Array(1, 1, 1, 0));
var stage2SingleCombos = [[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]];
var stage3Combos = Array(Array(0, 0, 1, 1, 1), Array(0, 1, 0, 1, 1), Array(0, 1, 1, 0, 1),
    Array(0, 1, 1, 1, 0), Array(1, 0, 0, 1, 1), Array(1, 0, 1, 0, 1),
    Array(1, 0, 1, 1, 0), Array(1, 1, 0, 0, 1), Array(1, 1, 0, 1, 0),
    Array(1, 1, 1, 0, 0));
var stage3SingleCombos = [[1, 0, 0, 0, 0], [0, 1, 0, 0, 0], [0, 0, 1, 0, 0], [0, 0, 0, 1, 0], [0, 0, 0, 0, 1]];
var stage4Combos = Array(Array(0, 0, 0, 1, 1, 1), Array(0, 0, 1, 0, 1, 1), Array(0, 0, 1, 1, 0, 1),
    Array(0, 0, 1, 1, 1, 0), Array(0, 1, 0, 0, 1, 1), Array(0, 1, 0, 1, 0, 1),
    Array(0, 1, 0, 1, 1, 0), Array(0, 1, 1, 0, 0, 1), Array(0, 1, 1, 0, 1, 0),
    Array(0, 1, 1, 1, 0, 0), Array(1, 0, 0, 0, 1, 1), Array(1, 0, 0, 1, 0, 1),
    Array(1, 0, 0, 1, 1, 0), Array(1, 0, 1, 0, 0, 1), Array(1, 0, 1, 0, 1, 0),
    Array(1, 0, 1, 1, 0, 0), Array(1, 1, 0, 0, 0, 1), Array(1, 1, 0, 0, 1, 0),
    Array(1, 1, 0, 1, 0, 0), Array(1, 1, 1, 0, 0, 0));
var stage4SingleCombos = [[1, 0, 0, 0, 0, 0], [0, 1, 0, 0, 0, 0], [0, 0, 1, 0, 0, 0], [0, 0, 0, 1, 0, 0], [0, 0, 0, 0, 1, 0], [0, 0, 0, 0, 0, 1]];

function clearStage(stage, eim, curMap) {
    eim.setProperty(stage + "stageclear", "true");
    eim.showClearEffect(true);

    eim.linkToNextStage(stage, "kpq", curMap);  //opens the portal to the next map
}

function rectangleStages(eim, property, areaCombos, areaRects) {
    var c = eim.getProperty(property);
    if (c == null) {
        c = Math.floor(Math.random() * areaCombos.length);
        eim.setProperty(property, c.toString());
    } else {
        c = parseInt(c);
    }

    // get player placement
    var players = eim.getPlayers();
    var playerPlacement = [0, 0, 0, 0, 0, 0];

    for (var i = 0; i < eim.getPlayerCount(); i++) {
        for (var j = 0; j < areaRects.length; j++) {
            if (areaRects[j].contains(players.get(i).getPosition())) {
                playerPlacement[j] += 1;
                break;
            }
        }
    }

    var curCombo = areaCombos[c];
    var accept = true;
    for (var j = 0; j < curCombo.length; j++) {
        if (curCombo[j] != playerPlacement[j]) {
            accept = false;
            break;
        }
    }

    return accept;
}

var status = -1;
var eim;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    eim = cm.getEventInstance();

    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            var curMap = cm.getMapId();
            var stage = curMap - 103000800 + 1;
            if (eim.getProperty(stage.toString() + "stageclear") != null) {
                if (stage < 5) {
                    cm.sendNext("传送门已开启，赶快去下一关吧！");
                    cm.dispose();
                } else {
                    cm.sendNext("你们已经通过了全部的考验，我将给你们颁发奖励，请确保背包有足够的空间。");
                }
            } else if (curMap == 103000800) {   // stage 1
                let playerCount = eim.getPlayerCount();
                if (cm.isEventLeader() && playerCount > 1) {
                    var numpasses = playerCount - 1;     // minus leader

                    if (cm.hasItem(4001008, numpasses)) {
                        cm.sendNext("你们收集到了 " + numpasses + " 张通行证！恭喜你们完成挑战，我将为你们打开传送门。");
                        clearStage(stage, eim, curMap);
                        eim.gridClear();
                        cm.gainItem(4001008, -numpasses);
                    } else {
                        cm.sendNext("你需要给我 " + numpasses + " 张通行证才能完成挑战，赶快让你的队员来交换通行证。");
                    }
                } else {
                    var data = eim.gridCheck(cm.getPlayer());

                    if (data == 0) {
                        cm.sendNext("把通行证交给队长。");
                    } else if (data == -1) {
                        data = Math.floor(Math.random() * stage1Questions.length) + 1;   //data will be counted from 1
                        eim.gridInsert(cm.getPlayer(), data);

                        var question = stage1Questions[data - 1];
                        cm.sendNext(question);
                    } else {
                        var answer = stage1Answers[data - 1];

                        if (cm.itemQuantity(4001007) == answer) {
                            cm.gainItem(4001007, -answer);
                            cm.gainItem(4001008, 1);
                            eim.gridInsert(cm.getPlayer(), 0);
                            if (playerCount === 1 && cm.isEventLeader()) {
                                clearStage(stage, eim, curMap);
                                eim.gridClear();
                                cm.gainItem(4001008, -1);
                                cm.sendNext("恭喜你完成挑战，我将为你打开传送门。");
                            } else {
                                cm.sendNext("回答正确！这是你的 #b通行证#k。把它交给队长！");
                            }
                        } else {
                            var question = stage1Questions[eim.gridCheck(cm.getPlayer()) - 1];
                            cm.sendNext("这不是正确的答案！\r\n" + question);
                        }
                    }
                }

                cm.dispose();
            } else if (curMap == 103000801) {   // stage 2
                var stgProperty = "stg2Property";
                var stgCombos = stage2Combos;
                var stgAreas = stage2Rects;
                
                if (eim.getPlayerCount() === 1) stgCombos = stage2SingleCombos;

                if (!eim.isEventLeader(cm.getPlayer())) {
                    cm.sendOk("请让队长来对话");
                } else if (eim.getProperty(stgProperty) == null) {
                    if (eim.getPlayerCount() === 1) cm.sendNext("欢迎来到#b第二关#k。\r\n在这一关有四条绳子，需要你爬上其中一条绳子然后和我对话，只有爬上正确的绳子才可以通关。");
                    else cm.sendNext("欢迎来到#b第二关#k\r\n在这一关有四条绳子，需要任意三名成员爬上三条不同的绳子，由队长来和我对话，只有正确的组合可以通关。");
                    
                    var c = Math.floor(Math.random() * stgCombos.length);
                    eim.setProperty(stgProperty, c.toString());
                } else {
                    if (rectangleStages(eim, stgProperty, stgCombos, stgAreas)) {
                        clearStage(stage, eim, curMap);
                        cm.sendNext("传送门已开启");
                    } else {
                        eim.showWrongEffect();
                        cm.sendNext("请赶快安排成员上绳子吧，注意不要多人在一条绳子上挂着。");
                    }
                }

                cm.dispose();
            } else if (curMap == 103000802) {
                var stgProperty = "stg3Property";
                var stgCombos = stage3Combos;
                var stgAreas = stage3Rects;
                if (eim.getPlayerCount() === 1) stgCombos = stage3SingleCombos;

                if (!eim.isEventLeader(cm.getPlayer())) {
                    cm.sendOk("请让队长来对话");
                } else if (eim.getProperty(stgProperty) == null) {
                    if (eim.getPlayerCount() === 1) cm.sendNext("欢迎来到#b第四关#k\r\n在这一关有五个平台，你需要站在其中一个平台中间，然后和我对话，只有站在正确的平台上才可以通关。");
                    else cm.sendNext("欢迎来到#b第四关#k\r\n在这一关有五个平台，你需要安排三名成员站在不同的三个平台中间，由队长来和我对话，只有正确的组合可以通关。");
                    var c = Math.floor(Math.random() * stgCombos.length);
                    eim.setProperty(stgProperty, c.toString());
                } else {
                    var accept = rectangleStages(eim, stgProperty, stgCombos, stgAreas);

                    if (accept) {
                        clearStage(stage, eim, curMap);
                        cm.sendNext("传送门已开启");
                    } else {
                        eim.showWrongEffect();
                        cm.sendNext("请赶快安排成员站在平台上吧，注意不要多人站在同一个平台上。");
                    }
                }

                cm.dispose();
            } else if (curMap == 103000803) {
                var stgProperty = "stg4Property";
                var stgCombos = stage4Combos;
                var stgAreas = stage4Rects;
                if (eim.getPlayerCount() === 1) stgCombos = stage4SingleCombos;

                if (!eim.isEventLeader(cm.getPlayer())) {
                    cm.sendOk("请让队长来对话");
                } else if (eim.getProperty(stgProperty) == null) {
                    if (eim.getPlayerCount() === 1) cm.sendNext("欢迎来到#b第四关#k\r\n在这一关，共有6个木桶，你需要站在其中一个木桶上，再来和我对话，只有站在正确的木桶上才可以通关。");
                    else cm.sendNext("欢迎来到#b第四关#k\r\n在这一关，共有6个木桶，你需要安排三名成员站在不同的三个木桶上，由队长和我对话，只有正确的组合可以通关。");
                    var c = Math.floor(Math.random() * stgCombos.length);
                    eim.setProperty(stgProperty, c.toString());
                } else {
                    var accept = rectangleStages(eim, stgProperty, stgCombos, stgAreas);

                    if (accept) {
                        clearStage(stage, eim, curMap);
                        cm.sendNext("传送门已开启");
                    } else {
                        eim.showWrongEffect();
                        cm.sendNext("请赶快安排成员站在木桶上吧，注意不要多人站在同一个木桶上。");
                    }
                }

                cm.dispose();
            } else if (curMap == 103000804) {
                if (eim.isEventLeader(cm.getPlayer())) {
                    if (cm.haveItem(4001008, 10)) {
                        cm.sendNext("恭喜你们通关了这次副本，请让队员和我对话领取奖励，同时我将把你们传送到隐藏地图。");
                        cm.gainItem(4001008, -10);

                        clearStage(stage, eim, curMap);
                        eim.clearPQ();
                    } else {
                        cm.sendNext("欢迎来到#b最终关#k\r\n击败这里全部的怪物并收集通行证（10张）由队长交给我，我将给予你们通关奖励！");
                    }
                } else {
                    cm.sendNext("欢迎来到#b最终关#k\r\n击败这里全部的怪物并收集通行证（10张）由队长交给我，我将给予你们通关奖励！");
                }

                cm.dispose();
            }
        } else if (status == 1) {
            if (!eim.giveEventReward(cm.getPlayer())) {
                cm.sendNext("#r背包满了");
            } else {
                cm.warp(103000805, "st00");
            }

            cm.dispose();
        }
    }
}
