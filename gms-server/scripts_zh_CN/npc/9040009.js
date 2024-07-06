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
var status;
var stage;

function clearStage(stage, eim) {
    eim.setProperty("stage" + stage + "clear", "true");
    eim.showClearEffect(true);

    eim.giveEventPlayersStageReward(stage);
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
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

        var eim = cm.getPlayer().getEventInstance();
        if (eim == null) {
            cm.warp(990001100);
        } else {
            if (eim.getProperty("stage1clear") == "true") {
                cm.sendOk("干得好。你可以继续进行下一阶段了。");
                cm.dispose();
                return;
            }

            if (cm.isEventLeader()) {
                if (status == 0) {
                    if (eim.getProperty("stage1status") == null || eim.getProperty("stage1status") === "waiting") {
                        if (eim.getProperty("stage1phase") == null) {
                            stage = 1;
                            eim.setProperty("stage1phase", stage);
                        } else {
                            stage = parseInt(eim.getProperty("stage1phase"));
                        }

                        if (stage == 1) {
                            cm.sendOk("在这个挑战中，我将展示周围雕像上的一个图案。当我说出指令时，请重复这个图案以继续前进。");
                        } else {
                            cm.sendOk("我现在将为你呈现一个更难的谜题。祝你好运。");
                        }
                    } else if (eim.getProperty("stage1status") === "active") {
                        stage = parseInt(eim.getProperty("stage1phase"));

                        if (eim.getProperty("stage1combo") === eim.getProperty("stage1guess")) {
                            if (stage == 3) {
                                cm.getPlayer().getMap().getReactorByName("statuegate").forceHitReactor(1);
                                clearStage(1, eim);
                                cm.getGuild().gainGP(15);

                                cm.sendOk("干得好。你可以继续进行下一阶段。");
                            } else {
                                cm.sendOk("很好。不过你还有更多任务要完成。当你准备好的时候再和我交谈。");
                                eim.setProperty("stage1phase", stage + 1);
                                cm.mapMessage(5, "You have completed part " + stage + " of the Gatekeeper Test.");
                            }

                        } else {
                            eim.showWrongEffect();
                            cm.sendOk("你已经失败了这次测试。");
                            cm.mapMessage(5, "You have failed the Gatekeeper Test.");
                            eim.setProperty("stage1phase", "1");
                        }
                        eim.setProperty("stage1status", "waiting");
                        cm.dispose();
                    } else {
                        cm.sendOk("雕像正在按照图案工作。请稍等。");
                        cm.dispose();
                    }
                } else if (status == 1) {
                    var reactors = getReactors();
                    var combo = makeCombo(reactors);
                    cm.mapMessage(5, "Please wait while the combination is revealed.");
                    var delay = 5000;
                    for (var i = 0; i < combo.length; i++) {
                        cm.getPlayer().getMap().getReactorByOid(combo[i]).delayedHitReactor(cm.getClient(), delay + 3500 * i);
                    }
                    eim.setProperty("stage1status", "display");
                    eim.setProperty("stage1combo", "");
                    cm.dispose();
                }
            } else {
                cm.sendOk("我需要这个副本的领导和我交谈，其他人不要。");
                cm.dispose();
            }
        }
    }
}

//method for getting the statue reactors on the map by oid
function getReactors() {
    var reactors = [];

    var iter = cm.getPlayer().getMap().getReactors().iterator();
    while (iter.hasNext()) {
        var mo = iter.next();
        if (mo.getName() !== "statuegate") {
            reactors.push(mo.getObjectId());
        }
    }

    return reactors;
}

function makeCombo(reactors) {
    var combo = [];
    while (combo.length < (stage + 3)) {
        var chosenReactor = reactors[Math.floor(Math.random() * reactors.length)];
        var repeat = false;
        if (combo.length > 0) {
            for (var i = 0; i < combo.length; i++) {
                if (combo[i] == chosenReactor) {
                    repeat = true;
                    break;
                }
            }
        }
        if (!repeat) {
            combo.push(chosenReactor);
        }
    }
    return combo;
}