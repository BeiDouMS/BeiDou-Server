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
@	Author : Twdtwd
@       Author : Ronan
@
@	NPC = Violet Balloon
@	Map = Hidden-Street <Crack on the Wall>
@	NPC MapId = 922010900
@	Function = LPQ - Last Stage
@
@	Description: Used after the boss is killed to trigger the bonus stage.
*/

var status = 0;
var curMap, stage;

function start() {
    curMap = cm.getMapId();
    stage = Math.floor((curMap - 922010100) / 100) + 1;

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
            cm.sendNext("快点，去下一个阶段，传送门已经打开了！");
        } else {
            if (eim.isEventLeader(cm.getPlayer())) {
                var state = eim.getIntProperty("statusStg" + stage);

                if (state == -1) {           // preamble
                    cm.sendOk("嗨。欢迎来到#bBOSS阶段#k。在那个平台上杀死老鼠，揭示出阿利莎尔，并打败他！");
                    eim.setProperty("statusStg" + stage, 0);
                } else {                      // check stage completion
                    if (cm.haveItem(4001023, 1)) {
                        cm.gainItem(4001023, -1);
                        eim.setProperty("statusStg" + stage, 1);

                        var list = eim.getClearStageBonus(stage);     // will give bonus exp & mesos to everyone in the event
                        eim.giveEventPlayersExp(list.get(0));
                        eim.giveEventPlayersMeso(list.get(1));

                        eim.setProperty(stage + "stageclear", "true");
                        eim.showClearEffect(true);

                        eim.clearPQ();
                    } else {
                        cm.sendNext("请击败阿利莎并把他的#b#t4001023#带给我。#k");
                    }
                }
            } else {
                cm.sendNext("请告诉你的#b队长#k来找我谈话。");
            }
        }

        cm.dispose();
    }
}