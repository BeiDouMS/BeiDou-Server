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
    "Here's the question. Collect the same number of coupons as the minimum level required to make the first job advancement as warrior.",
    "Here's the question. Collect the same number of coupons as the minimum amount of STR needed to make the first job advancement as a warrior.",
    "Here's the question. Collect the same number of coupons as the minimum amount of INT needed to make the first job advancement as a magician.",
    "Here's the question. Collect the same number of coupons as the minimum amount of DEX needed to make the first job advancement as a bowman.",
    "Here's the question. Collect the same number of coupons as the minimum amount of DEX needed to make the first job advancement as a thief.",
    "Here's the question. Collect the same number of coupons as the minimum level required to advance to 2nd job.",
    "Here's the question. Collect the same number of coupons as the minimum level required to make the first job advancement as a magician.");
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
var stage3Combos = Array(Array(0, 0, 1, 1, 1), Array(0, 1, 0, 1, 1), Array(0, 1, 1, 0, 1),
    Array(0, 1, 1, 1, 0), Array(1, 0, 0, 1, 1), Array(1, 0, 1, 0, 1),
    Array(1, 0, 1, 1, 0), Array(1, 1, 0, 0, 1), Array(1, 1, 0, 1, 0),
    Array(1, 1, 1, 0, 0));
var stage4Combos = Array(Array(0, 0, 0, 1, 1, 1), Array(0, 0, 1, 0, 1, 1), Array(0, 0, 1, 1, 0, 1),
    Array(0, 0, 1, 1, 1, 0), Array(0, 1, 0, 0, 1, 1), Array(0, 1, 0, 1, 0, 1),
    Array(0, 1, 0, 1, 1, 0), Array(0, 1, 1, 0, 0, 1), Array(0, 1, 1, 0, 1, 0),
    Array(0, 1, 1, 1, 0, 0), Array(1, 0, 0, 0, 1, 1), Array(1, 0, 0, 1, 0, 1),
    Array(1, 0, 0, 1, 1, 0), Array(1, 0, 1, 0, 0, 1), Array(1, 0, 1, 0, 1, 0),
    Array(1, 0, 1, 1, 0, 0), Array(1, 1, 0, 0, 0, 1), Array(1, 1, 0, 0, 1, 0),
    Array(1, 1, 0, 1, 0, 0), Array(1, 1, 1, 0, 0, 0));

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
                    cm.sendNext("请赶紧前往下一个阶段，传送门已经打开了！");
                    cm.dispose();
                } else {
                    cm.sendNext("太棒了！你通过了所有的关卡来到了这一点。这是为了你出色的表现而给予的小奖品。在接受之前，请确保你的使用和其他物品栏有空位可用。");
                }
            } else if (curMap == 103000800) {   // stage 1
                if (cm.isEventLeader()) {
                    var numpasses = eim.getPlayerCount() - 1;     // minus leader

                    if (cm.hasItem(4001008, numpasses)) {
                        cm.sendNext("你收集了" + numpasses + "张通行证！恭喜你通过了这个关卡！我会制作一个传送你到下一个关卡的传送门。到那里有时间限制，所以请赶快。祝你们好运！");
                        clearStage(stage, eim, curMap);
                        eim.gridClear();
                        cm.gainItem(4001008, -numpasses);
                    } else {
                        cm.sendNext("对不起，但你的通行证数量不够。你需要给我正确数量的通行证；应该是你队伍成员数量减去队长的数量，在这种情况下需要 " + numpasses + " 张通行证来通过这个关卡。告诉你的队伍成员解决问题，收集通行证，然后交给你。");
                    }
                } else {
                    var data = eim.gridCheck(cm.getPlayer());

                    if (data == 0) {
                        cm.sendNext("谢谢你带来了优惠券。请把通行证交给你的队伍领袖继续。");
                    } else if (data == -1) {
                        data = Math.floor(Math.random() * stage1Questions.length) + 1;   //data will be counted from 1
                        eim.gridInsert(cm.getPlayer(), data);

                        var question = stage1Questions[data - 1];
                        cm.sendNext(question);
                    } else {
                        var answer = stage1Answers[data - 1];

                        if (cm.itemQuantity(4001007) == answer) {
                            cm.sendNext("这是正确的答案！为此，你刚刚获得了一个#b通行证#k。请将它交给队伍的领袖。");
                            cm.gainItem(4001007, -answer);
                            cm.gainItem(4001008, 1);
                            eim.gridInsert(cm.getPlayer(), 0);
                        } else {
                            var question = stage1Questions[eim.gridCheck(cm.getPlayer()) - 1];
                            cm.sendNext("对不起，但那不是正确的答案！\r\n" + 问题);
                        }
                    }
                }

                cm.dispose();
            } else if (curMap == 103000801) {   // stage 2
                var stgProperty = "stg2Property";
                var stgCombos = stage2Combos;
                var stgAreas = stage2Rects;

                var nthtext = "2nd", nthobj = "ropes", nthverb = "hang", nthpos = "hang on the ropes too low";
                var nextStgId = 103000802;

                if (!eim.isEventLeader(cm.getPlayer())) {
                    cm.sendOk("跟随你的队长给出的指示来完成这个阶段。");
                } else if (eim.getProperty(stgProperty) == null) {
                    cm.sendNext("嗨。欢迎来到第" + nthtext + "阶段。在我旁边，你会看到一些" + nthobj + "。在这些" + nthobj + "中，有#b3个与传送你到下一阶段的传送门相连#k。你只需要让#b3名队伍成员找到正确的" + nthobj + "并对其进行" + nthverb + "#k\r\n但是，如果你" + nthpos + "，这不算作答案；请确保靠近" + nthobj + "的中间位置才算作正确答案。此外，你的队伍只允许有3名成员站在" + nthobj + "上。当他们在上面" + nthverb + "时，队伍的领袖必须#b双击我来检查答案是否正确#k。现在，找到正确的" + nthobj + "进行" + nthverb + "吧！");
                    var c = Math.floor(Math.random() * stgCombos.length);
                    eim.setProperty(stgProperty, c.toString());
                } else {
                    var accept = rectangleStages(eim, stgProperty, stgCombos, stgAreas);

                    if (accept) {
                        clearStage(stage, eim, curMap);
                        cm.sendNext("请赶紧前往下一个阶段，传送门已经打开了！");
                    } else {
                        eim.showWrongEffect();
                        cm.sendNext("看起来你还没有找到第3个" + nthobj + "。请考虑不同的" + nthobj + "组合。只允许在" + nthobj + "上进行3次" + nthverb + "，如果你" + nthpos + "它可能不算作答案，所以请记住这一点。继续努力！");
                    }
                }

                cm.dispose();
            } else if (curMap == 103000802) {
                var stgProperty = "stg3Property";
                var stgCombos = stage3Combos;
                var stgAreas = stage3Rects;

                var nthtext = "3rd", nthobj = "platforms", nthverb = "stand", nthpos = "stand too close to the edges";
                var nextStgId = 103000803;

                if (!eim.isEventLeader(cm.getPlayer())) {
                    cm.sendOk("跟随你的队长给出的指示来完成这个阶段。");
                } else if (eim.getProperty(stgProperty) == null) {
                    cm.sendNext("嗨。欢迎来到第" + nthtext + "阶段。在我旁边，你会看到一些" + nthobj + "。在这些" + nthobj + "中，#b3个与传送你到下一阶段的传送门相连#k。你只需要让#b3个队员找到正确的" + nthobj + "并对其进行" + nthverb + "#k\r\n但是，如果你" + nthpos + "，这不算作答案；请确保靠近" + nthobj + "的中间位置才算作正确答案。此外，你的队伍只允许有3名成员站在" + nthobj + "上。一旦他们在上面" + nthverb + "，队伍的队长必须#b双击我来检查答案是否正确#k。现在，找到正确的" + nthobj + "进行" + nthverb + "吧！");
                    var c = Math.floor(Math.random() * stgCombos.length);
                    eim.setProperty(stgProperty, c.toString());
                } else {
                    var accept = rectangleStages(eim, stgProperty, stgCombos, stgAreas);

                    if (accept) {
                        clearStage(stage, eim, curMap);
                        cm.sendNext("请赶紧前往下一个阶段，传送门已经打开了！");
                    } else {
                        eim.showWrongEffect();
                        cm.sendNext("看起来你还没有找到第3个" + nthobj + "。请考虑不同的" + nthobj + "组合。只允许在" + nthobj + "上" + nthverb + "3次，如果你" + nthpos + "它可能不算作答案，所以请记住这一点。继续努力！");
                    }
                }

                cm.dispose();
            } else if (curMap == 103000803) {
                var stgProperty = "stg4Property";
                var stgCombos = stage4Combos;
                var stgAreas = stage4Rects;

                var nthtext = "4th", nthobj = "barrels", nthverb = "stand", nthpos = "stand too close to the edges";
                var nextStgId = 103000804;

                if (!eim.isEventLeader(cm.getPlayer())) {
                    cm.sendOk("跟随你的队长给出的指示来完成这个阶段。");
                } else if (eim.getProperty(stgProperty) == null) {
                    cm.sendNext("嗨。欢迎来到第" + nthtext + "阶段。在我旁边，你会看到一些" + nthobj + "。在这些" + nthobj + "中，#b3个与传送你到下一阶段的传送门相连#k。你只需要让#b3个队员找到正确的" + nthobj + "并对其进行" + nthverb + "#k\r\n但是，如果你" + nthpos + "，这不算作答案；请站在" + nthobj + "的中间才算作正确答案。此外，你的队伍只允许有3名成员站在" + nthobj + "上。当他们在上面" + nthverb + "时，队伍的领袖必须#b双击我来检查答案是否正确#k。现在，找到正确的" + nthobj + "进行" + nthverb + "吧！");
                    var c = Math.floor(Math.random() * stgCombos.length);
                    eim.setProperty(stgProperty, c.toString());
                } else {
                    var accept = rectangleStages(eim, stgProperty, stgCombos, stgAreas);

                    if (accept) {
                        clearStage(stage, eim, curMap);
                        cm.sendNext("请赶紧前往下一个阶段，传送门已经打开了！");
                    } else {
                        eim.showWrongEffect();
                        cm.sendNext("看起来你还没有找到第3个" + nthobj + "。请考虑不同的" + nthobj + "组合。只允许在" + nthobj + "上" + nthverb + "3次，如果你" + nthpos + "它可能不算作答案，所以请记住这一点。继续努力！");
                    }
                }

                cm.dispose();
            } else if (curMap == 103000804) {
                if (eim.isEventLeader(cm.getPlayer())) {
                    if (cm.haveItem(4001008, 10)) {
                        cm.sendNext("这是通往最后的奖励阶段的传送门。这个阶段让你更容易地击败普通怪物。你将有一定的时间来尽可能多地狩猎，但你可以随时通过NPC中途离开这个阶段。再次恭喜你通过了所有的阶段。让你的队伍跟我对话，他们可以通过到达奖励阶段来领取奖品。保重……");
                        cm.gainItem(4001008, -10);

                        clearStage(stage, eim, curMap);
                        eim.clearPQ();
                    } else {
                        cm.sendNext("你好。欢迎来到第五个也是最后一个阶段。在地图上四处走动，你会找到一些Boss怪物。打败它们，收集#b通行证#k，然后把它们交给我。一旦你获得了通行证，你的队伍领袖会收集它们，然后在收集齐#b通行证#k后再把它们交给我。这些怪物可能对你来说很熟悉，但它们可能比你想象的要强大，所以请小心。祝你好运！");
                    }
                } else {
                    cm.sendNext("欢迎来到第五个也是最后一个阶段。在地图上四处走动，你将能够找到一些Boss怪物。打败它们，收集#b通行证#k，并将它们#b交给你的队长#k。完成后，回到我这里领取你的奖励。");
                }

                cm.dispose();
            }
        } else if (status == 1) {
            if (!eim.giveEventReward(cm.getPlayer())) {
                cm.sendNext("请先在你的背包里腾出空间！");
            } else {
                cm.warp(103000805, "st00");
            }

            cm.dispose();
        }
    }
}