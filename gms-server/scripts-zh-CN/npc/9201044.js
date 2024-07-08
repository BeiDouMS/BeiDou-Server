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
@	Description: Used to find the combo to unlock the next door. Players stand on 5 different crates to guess the combo.
*/

var debug = false;
var autopass = false;

function spawnMobs(maxSpawn) {
    var spawnPosX;
    var spawnPosY;

    var mapObj = cm.getMap();
    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    if (stage == 2) {
        spawnPosX = [619, 299, 47, -140, -471];
        spawnPosY = [-840, -840, -840, -840, -840];

        for (var i = 0; i < 5; i++) {
            for (var j = 0; j < 2; j++) {
                var mobObj1 = LifeFactory.getMonster(9400515);
                var mobObj2 = LifeFactory.getMonster(9400516);
                var mobObj3 = LifeFactory.getMonster(9400517);

                mapObj.spawnMonsterOnGroundBelow(mobObj1, new Point(spawnPosX[i], spawnPosY[i]));
                mapObj.spawnMonsterOnGroundBelow(mobObj2, new Point(spawnPosX[i], spawnPosY[i]));
                mapObj.spawnMonsterOnGroundBelow(mobObj3, new Point(spawnPosX[i], spawnPosY[i]));
            }
        }
    } else {
        spawnPosX = [2303, 1832, 1656, 1379, 1171];
        spawnPosY = [240, 150, 300, 150, 240];

        for (var i = 0; i < maxSpawn; i++) {
            var rndMob = 9400519 + Math.floor(Math.random() * 4);
            var rndPos = Math.floor(Math.random() * 5);

            var mobObj = LifeFactory.getMonster(rndMob);
            mapObj.spawnMonsterOnGroundBelow(mobObj, new Point(spawnPosX[rndPos], spawnPosY[rndPos]));
        }
    }
}

function generateCombo1() {
    var positions = Array(0, 0, 0, 0, 0, 0, 0, 0, 0);
    var rndPicked = Math.floor(Math.random() * Math.pow(3, 5));

    while (rndPicked > 0) {
        (positions[rndPicked % 3])++;

        rndPicked = Math.floor(rndPicked / 3);
    }

    var returnString = "";
    for (var i = 0; i < positions.length; i++) {
        returnString += positions[i];
        if (i != positions.length - 1) {
            returnString += ",";
        }
    }

    return returnString;
}

function generateCombo2() {
    var toPick = 5, rndPicked;
    var positions = Array(0, 0, 0, 0, 0, 0, 0, 0, 0);
    while (toPick > 0) {
        rndPicked = Math.floor(Math.random() * 9);

        if (positions[rndPicked] == 0) {
            positions[rndPicked] = 1;
            toPick--;
        }
    }

    var returnString = "";
    for (var i = 0; i < positions.length; i++) {
        returnString += positions[i];
        if (i != positions.length - 1) {
            returnString += ",";
        }
    }

    return returnString;
}

var status = 0;
var curMap, stage;

function clearStage(stage, eim, curMap) {
    eim.setProperty(stage + "stageclear", "true");
    if (stage > 1) {
        eim.showClearEffect(true);
        eim.linkToNextStage(stage, "apq", curMap);  //opens the portal to the next map
    } else {
        cm.getMap().getPortal("go01").setPortalState(false);

        var val = Math.floor(Math.random() * 3);
        eim.showClearEffect(670010200, "gate" + val, 2);

        cm.getMap().getPortal("go0" + val).setPortalState(true);
        eim.linkPortalToScript(stage, "go0" + val, "apq0" + val, curMap);
    }
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
            cm.sendNext("传送门已经打开，前进去迎接等待你的考验。");
        } else {
            if (eim.isEventLeader(cm.getPlayer())) {
                var state = eim.getIntProperty("statusStg" + stage);

                if (state == -1) {           // preamble
                    if (stage == 1) {
                        cm.sendOk("嗨。欢迎来到阿莫利亚挑战的#b舞台#k。在这个阶段，与#p9201047#交谈，他会向你传达任务的进一步细节。在打碎下面的魔镜后，将碎片交给#p9201047#，然后来这里获得进入下一个阶段的权限。");
                    } else if (stage == 2) {
                        cm.sendOk("嗨。欢迎来到阿莫利亚挑战的#b舞台#k。在这个阶段，让你的5名队员以某种方式爬上平台，尝试组合解锁通往下一级的传送门。当你感觉准备好了，和我交谈，我会告诉你情况。然而，请做好准备，如果传送门在几次尝试后没有解锁，怪物将会生成。");
                    } else if (stage == 3) {
                        cm.sendOk("嗨。欢迎来到阿莫利亚挑战的#b舞台#k。在这个阶段，让你的5名队员分别爬上平台，尝试组合以解锁通往下一级的传送门。当你准备好时，和我交谈，我会告诉你情况。提示：失败时，数一下场景中出现的史莱姆数量，这将告诉你有多少人的位置是正确的。");
                    }

                    var st = (autopass) ? 2 : 0;
                    eim.setProperty("statusStg" + stage, st);
                } else {       // check stage completion
                    if (state == 2) {
                        eim.setProperty("statusStg" + stage, 1);
                        clearStage(stage, eim, curMap);
                        cm.dispose();
                        return;
                    }

                    var map = cm.getPlayer().getMap();
                    if (stage == 1) {
                        if (eim.getIntProperty("statusStg" + stage) == 1) {
                            clearStage(stage, eim, curMap);
                        } else {
                            cm.sendOk("与#p9201047#交谈，了解更多关于这个阶段的信息。");
                        }
                    } else if (stage == 2 || stage == 3) {
                        if (map.countMonsters() == 0) {
                            objset = [0, 0, 0, 0, 0, 0, 0, 0, 0];
                            var playersOnCombo = 0;
                            var party = cm.getEventInstance().getPlayers();
                            for (var i = 0; i < party.size(); i++) {
                                for (var y = 0; y < map.getAreas().size(); y++) {
                                    if (map.getArea(y).contains(party.get(i).getPosition())) {
                                        playersOnCombo++;
                                        objset[y] += 1;
                                        break;
                                    }
                                }
                            }

                            if (playersOnCombo == 5/* || cm.getPlayer().gmLevel() > 1*/ || debug) {
                                var comboStr = eim.getProperty("stage" + stage + "combo");
                                if (comboStr == null || comboStr == "") {
                                    if (stage == 2) {
                                        comboStr = generateCombo1();
                                    } else {
                                        comboStr = generateCombo2();
                                    }

                                    eim.setProperty("stage" + stage + "combo", comboStr);
                                    if (debug) {
                                        print("generated " + comboStr + " for stg" + stage + "\n");
                                    }
                                }

                                var combo = comboStr.split(',');
                                var correctCombo = true;
                                var guessedRight = objset.length;
                                var playersRight = 0;

                                if (!debug) {
                                    for (i = 0; i < objset.length; i++) {
                                        if (parseInt(combo[i]) != objset[i]) {
                                            correctCombo = false;
                                            guessedRight--;
                                        } else {
                                            if (objset[i] > 0) {
                                                playersRight++;
                                            }
                                        }
                                    }
                                } else {
                                    for (i = 0; i < objset.length; i++) {
                                        var ci = cm.getPlayer().countItem(4000000 + i);

                                        if (ci != parseInt(combo[i])) {
                                            correctCombo = false;
                                            guessedRight--;
                                        } else {
                                            if (ci > 0) {
                                                playersRight++;
                                            }
                                        }
                                    }
                                }


                                if (correctCombo/* || cm.getPlayer().gmLevel() > 1*/) {
                                    eim.setProperty("statusStg" + stage, 1);
                                    clearStage(stage, eim, curMap);
                                    cm.dispose();
                                } else {
                                    var miss = eim.getIntProperty("missCount") + 1;
                                    var maxMiss = (stage == 2) ? 7 : 1;

                                    if (miss < maxMiss) {   //already implies stage 2
                                        eim.setIntProperty("missCount", miss);

                                        if (guessedRight == 6) { //6 unused slots on this stage
                                            cm.sendNext("所有的绳子重量都不同。考虑你接下来的行动，然后再试一次。");
                                            cm.mapMessage(5, "Amos: Hmm... All ropes weigh differently.");
                                        } else {
                                            cm.sendNext("一根绳子重量相同。考虑你接下来的行动，然后再试一次。");
                                            cm.mapMessage(5, "Amos: Hmm... One rope weigh the same.");
                                        }
                                    } else {
                                        spawnMobs(playersRight);
                                        eim.setIntProperty("missCount", 0);
                                        if (stage == 2) {
                                            eim.setProperty("stage2combo", "");

                                            cm.sendNext("你已经未能发现正确的组合，现在将被重置。重新开始吧！");
                                            cm.mapMessage(5, "Amos: You have failed to discover the right combination, now it shall be reset. Start over again!");
                                        }
                                    }

                                    eim.showWrongEffect();
                                    cm.dispose();
                                }
                            } else {
                                if (stage == 2) {
                                    cm.sendNext("看起来你们还没有找到这个试炼的方法。考虑一下在平台上安排5名成员。记住，只允许有5人站在平台上，如果你移动了，可能就不算作答案了，所以请记住这一点。继续努力！");
                                } else {
                                    cm.sendNext("看起来你们还没有找到这个试炼的方法。考虑一下在不同平台上安排队伍成员的方式。记住，只允许有5个人站在平台上，如果你移动了，可能就不算作答案了，所以请记住这一点。继续努力！");
                                }

                                cm.dispose();
                            }
                        } else {
                            cm.sendNext("在尝试组合之前先击败所有的怪物。");
                        }
                    }
                }
            } else {
                cm.sendNext("请告诉你的#b队伍领袖#k来找我谈话。");
            }
        }

        cm.dispose();
    }
}