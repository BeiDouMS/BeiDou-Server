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
@	Author : Raz
@       Author : Ronan
@
@	NPC = Lime Balloon
@	Map = Hidden-Street <Stage 4>
@	NPC MapId = 922010400
@	Function = LPQ - 4th Stage
@
*/

var status = 0;
var curMap, stage;

function start() {
    curMap = cm.getMapId();
    stage = Math.floor((curMap - 922010100) / 100) + 1;

    status = -1;
    action(1, 0, 0);
}

function clearStage(stage, eim, curMap) {
    eim.setProperty(stage + "stageclear", "true");
    eim.showClearEffect(true);

    eim.linkToNextStage(stage, "lpq", curMap);  //opens the portal to the next map
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
                    cm.sendOk("嗨。欢迎来到#bstage#k。在这个阶段，有几种生物隐藏在这座塔的内部阴影中。其中一些无法通过物理手段击败，因此需要使用魔法攻击来完成任务，而其他一些则相反。这次给我带来6个#t4001022#。");
                    eim.setProperty("statusStg" + stage, 0);
                } else {                      // check stage completion
                    if (cm.haveItem(4001022, 6)) {
                        cm.sendOk("干得好！你已经收集了所有6个#b#t4001022#。#k");
                        cm.gainItem(4001022, -6);

                        eim.setProperty("statusStg" + stage, 1);
                        clearStage(stage, eim, curMap);
                    } else {
                        cm.sendNext("抱歉，你没有全部6个 #b#t4001022#。#k");
                    }
                }
            } else {
                cm.sendNext("请告诉你的#b队伍领袖#k来找我谈话。");
            }
        }

        cm.dispose();
    }
}