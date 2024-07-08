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
 * @author: Ronan
 * @npc: Romeo
 * @func: MagatiaPQ area NPC
 */

var status;

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

        var eim = cm.getEventInstance();

        if (!eim.isEventCleared()) {
            if (status == 0) {
                if (eim.getIntProperty("npcShocked") == 0 && cm.haveItem(4001131, 1)) {
                    cm.gainItem(4001131, -1);
                    eim.setIntProperty("npcShocked", 1);

                    cm.sendNext("哦？你给我信？像这样的时候，应该是什么…… 哇！伙计们，有大事发生了。集合起来，从现在开始，事情会比以往更加艰难！");
                    eim.dropMessage(6, "Romeo seemed very much in shock after reading Juliet's Letter.");

                    cm.dispose();

                } else if (eim.getIntProperty("statusStg4") == 1) {
                    var door = cm.getMap().getReactorByName("rnj3_out3");

                    if (door.getState() == 0) {
                        cm.sendNext("让我为你开门。");
                        door.hitReactor(cm.getClient());
                    } else {
                        cm.sendNext("请快点，朱丽叶有麻烦了。");
                    }

                    cm.dispose();

                } else if (cm.haveItem(4001134, 1) && cm.haveItem(4001135, 1)) {
                    if (cm.isEventLeader()) {
                        cm.gainItem(4001134, -1);
                        cm.gainItem(4001135, -1);
                        cm.sendNext("太好了！你手头上有艾尔卡德诺和泽尼玛斯特的文件。现在我们可以继续了。");

                        eim.showClearEffect();
                        eim.giveEventPlayersStageReward(4);
                        eim.setIntProperty("statusStg4", 1);

                        cm.getMap().killAllMonsters();
                        cm.getMap().getReactorByName("rnj3_out3").hitReactor(cm.getClient());
                    } else {
                        cm.sendOk("请让你们的领导把文件传给我。");
                    }

                    cm.dispose();

                } else {
                    cm.sendYesNo("我们必须继续战斗，拯救朱丽叶，请保持你的速度。如果你感觉不太好，无法继续，你的同伴和我都会理解……那么，你打算撤退吗？");
                }
            } else {
                cm.warp(926100700, 0);
                cm.dispose();
            }
        } else {
            if (status == 0) {
                if (eim.getIntProperty("escortFail") == 0) {
                    cm.sendNext("最终，朱丽叶安全了！多亏了你的努力，我们成功将她从尤利特的魔掌中解救出来，尤利特现在将因为反抗马加提亚而受到审判。从现在开始，他将开始接受康复治疗，我们将密切关注他的努力，确保他将不再在未来制造麻烦。");
                } else {
                    cm.sendNext("朱丽叶现在安全了，尽管战斗对她造成了一定的伤害……多亏了你们的努力，我们才能将她从尤利特的魔爪中解救出来，尤利特现在将因其反抗马加提亚而受到审判。谢谢你。");
                    status = 2;
                }
            } else if (status == 1) {
                cm.sendNext("现在，请将这份礼物视为我们对你的感激之情的接受表示。");
            } else if (status == 2) {
                if (cm.canHold(4001159)) {
                    cm.gainItem(4001159, 1);

                    if (eim.getIntProperty("normalClear") == 1) {
                        cm.warp(926100600, 0);
                    } else {
                        cm.warp(926100500, 0);
                    }
                } else {
                    cm.sendOk("确保你的杂项物品栏有空间。");
                }

                cm.dispose();
            } else {
                cm.warp(926100600, 0);
                cm.dispose();
            }
        }
    }
}