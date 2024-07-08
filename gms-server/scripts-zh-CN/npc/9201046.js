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
@	Description: Bonus stages of the Amorian Challenge
*/

var debug = false;
var status = 0;
var curMap, stage;

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
        if (curMap == 670010750) {
            if (cm.haveItem(4031597, 35)) {
                if (cm.canHold(1102101) && eim.getIntProperty("marriedGroup") == 0) {
                    eim.setIntProperty("marriedGroup", 1);

                    var baseId = (cm.getPlayer().getGender() == 0) ? 1102101 : 1102104;
                    var rnd = Math.floor(Math.random() * 3);
                    cm.gainItem(baseId + rnd);

                    cm.sendNext("太棒了！你是第一个领取35个#t4031597#奖励的人。拿着这件披风作为你的功绩奖赏吧。");
                    cm.gainItem(4031597, -35);
                    cm.gainExp(4000 * cm.getPlayer().getExpRate());
                } else if (eim.getIntProperty("marriedGroup") == 0) {
                    cm.sendNext("在谈论领取奖品之前，请检查您是否有可用的槽位！");
                } else {
                    cm.sendNext("35 #t4031597#。做得很好，可惜有人先拿走了奖品。赶紧去抓住奖励阶段的最后时刻！");
                    cm.gainItem(4031597, -35);
                    cm.gainExp(4000 * cm.getPlayer().getExpRate());
                }
            } else {
                cm.sendNext("要在这里领取奖品，从生成的怪物身上收集35个#t4031597#给我。只有#r第一个玩家可以领取大奖#k，尽管其他人仍然可以从这个壮举中获得经验加成。或者，你可以选择#b跳过这个奖励阶段#k，通过#b传送门#k继续进行通常的游戏。");
            }
        } else {
            cm.sendNext("赶紧去抓住奖励阶段的最后时刻！");
        }

        cm.dispose();
    }
}