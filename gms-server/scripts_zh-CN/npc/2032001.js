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
/* Spiruna
Orbis : Old Man's House (200050001)

Refining NPC:
 * Dark Crystal - Half Price compared to Vogen, but must complete quest
 */

var status = 0;

function start() {
    if (cm.isQuestCompleted(3034)) {
        cm.sendYesNo("你对我帮助很大……如果你有任何黑暗水晶矿石，我可以为你精炼，每个只需#b500000金币#k。");
    } else {
        cm.sendOk("走开，我在冥想。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
        cm.dispose();
        return;
    }
    status++;
    if (status == 1) {
        cm.sendGetNumber("Okay, so how many do you want me to make?", 1, 1, 100);
    } else if (status == 2) {
        var complete = true;

        if (cm.getMeso() < 500000 * selection) {
            cm.sendOk("对不起，但我不会免费做这件事。");
            cm.dispose();
            return;
        } else if (!cm.haveItem(4004004, 10 * selection)) {
            complete = false;
        } else if (!cm.canHold(4005004, selection)) {
            cm.sendOk("你的库存没有空位吗？先解决这个问题！");
            cm.dispose();
            return;
        }
        if (!complete) {
            cm.sendOk("我需要那些矿石来提炼水晶。没有例外。");
        } else {
            cm.gainItem(4004004, -10 * selection);
            cm.gainMeso(-500000 * selection);
            cm.gainItem(4005004, selection);
            cm.sendOk("明智地使用它。");
        }
        cm.dispose();
    }
}