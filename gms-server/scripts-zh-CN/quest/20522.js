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
	Author : Ronan Lana
*/
var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
    } else if (status >= 2 && mode == 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
	qm.sendNext("士的骑术和普通人的骑术有点不同。这是通过一种在这个岛上可以找到的咪咪种族的生物发生的；他们被称为#b咪咪亚纳斯 #k。骑士们骑的不是怪物，而是咪咪亚纳。有一件事你永远不应该忘记.");
    } else if (status == 1) {
	qm.sendNextPrev("别把这看成是一种坐骑或交通工具。这些坐骑可以是你的朋友，你的同志，你的同事。。。以上都是。即使是一个足够亲密的朋友也可以托付你的生命！这就是为什么埃雷夫骑士会自己种坐骑.");
    } else if (status == 2) {
	qm.sendAcceptDecline("看，这是一个米米安娜蛋。你准备好养一只小咪咪了吗？让它作为你余生的旅行伴侣?");
    } else if (status == 3) {
	if(!qm.haveItem(4220137) && !qm.canHold(4220137)) {
            qm.sendOk("在你的背包栏上空出一个位置 ，这样我就可以给你米米亚娜蛋了.");
            qm.dispose();
            return;
        }

        qm.forceStartQuest();
        if (!qm.haveItem(4220137)) {
            qm.gainItem(4220137);
        }
        qm.sendOk("米米安娜蛋可以通过 #b分享你的日常经验#k. 米米安娜长大后，请来找我.");
    } else if (status == 4) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if (mode != 1) {
        qm.dispose();
        return;
    }

    status++;
    if (status == 0) {
	qm.sendNext("嘿，在那儿！米米安娜的蛋怎么样了");
    } else if (status == 1) {   //pretty sure there would need to have an egg EXP condition... Whatever.
        if(!qm.haveItem(4220137)) {
            qm.sendOk("我明白了，你丢了你的蛋。。。当你抚养一个小咪咪的时候你需要更加小心!");
            return;
        }

        qm.forceCompleteQuest();
        qm.gainItem(4220137, -1);
        qm.gainExp(37600);
        qm.sendOk("哦，你能唤醒米米安娜蛋吗？太神奇了。。。大多数骑士都无法在这么短的时间内唤醒它.");
    } else if (status == 2) {
        qm.dispose();
    }
}

