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
/* Author: Xterminator
	NPC Name: 		Sera
	Map(s): 		Maple Road : Entrance - Mushroom Town Training Camp (0), Maple Road: Upper level of the Training Camp (1), Maple Road : Entrance - Mushroom Town Training Camp (3)
	Description: 		First NPC
*/

var status = -1;

function start() {
    if (cm.c.getPlayer().getMapId() == 0 || cm.c.getPlayer().getMapId() == 3) {
        cm.sendYesNo("欢迎来到冒险岛的世界。这个训练营的目的是帮助新手。你想要进入这个训练营吗？有些人在开始他们的冒险之旅时并没有参加训练计划。但我强烈建议你先参加训练计划。");
    } else {
        cm.sendNext("这是您的第一个训练课程开始的图像室。在这个房间里，您将提前了解您选择的职业。");
    }
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && status == 0) {
            cm.sendYesNo("你真的想立刻开始你的冒险吗？");
            return;
        } else if (mode == 0 && status == 1 && type == 0) {
            status -= 2;
            start();
            return;
        } else if (mode == 0 && status == 1 && type == 1) {
            cm.sendNext("请在你最终做出决定后再和我交谈。");
        }
        cm.dispose();
        return;
    }
    if (cm.c.getPlayer().getMapId() == 0 || cm.c.getPlayer().getMapId() == 3) {
        if (status == 0) {
            cm.sendNext("好的，那么，我会让你进入训练营地。请跟着你的教练。");
        } else if (status == 1 && type == 1) {
            cm.sendNext("看起来你想要开始你的冒险之旅而不接受训练计划。那么，我会让你前往训练场地。小心~");
        } else if (status == 1) {
            cm.warp(1, 0);
            cm.dispose();
        } else {
            cm.warp(40000, 0);
            cm.dispose();
        }
    } else if (status == 0) {
        cm.sendPrev("一旦你训练得足够努力，你就有资格获得一份工作。你可以成为赫内西斯的弓箭手，艾琳尼亚的魔法师，佩里恩的战士，以及科尔宁城的盗贼……");
    } else {
        cm.dispose();
    }
}