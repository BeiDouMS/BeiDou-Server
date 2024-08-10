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
	qm.sendNext("你失去了你的咪咪安娜？！天哪，你一定要为他们上心啊，因为他们是皇后给我们的礼物！你必须再次接受教育：骑士的骑术和普通人的骑术有点不同。这是通过一种在这个岛上可以找到的咪咪种族的生物发生的；他们被称为#b咪咪安娜#k。骑士骑咪咪安娜而不是骑怪物。这件事你永远不应该忘记.");
    } else if (status == 1) {
	qm.sendNextPrev("别把这看成是一种坐骑或交通工具。这些坐骑可以是你的朋友，你的同志，你的同事。。。以上都是。即使是一个足够亲密的朋友也可以托付你的生命！这就是为什么埃雷夫骑士会自己种坐骑.");
    } else if (status == 2) {
	qm.sendAcceptDecline("现在，这是一个米米安娜蛋。你准备好养一只小咪咪了吗？让它作为你余生的旅行伴侣?");
    } else if (status == 3) {
	if(!qm.haveItem(4220137) && !qm.canHold(4220137)) { 
            qm.sendOk("在你的背包里空出一个位置，这样我就可以给你米米亚娜蛋了.");
            qm.dispose();
            return;
        }

        qm.forceStartQuest();

        if (!qm.haveItem(4220137)) {
            qm.gainItem(4220137);
        }
        qm.sendOk("米米安娜的蛋可以通过#b分享你的日常经验来养大#k。 等米米安娜完全长大后, 请务必来找我. 还有一事, 我和他谈过 #p2060005# 事先为您取回 #b#t4032117##k. 当然了价格不变: #r10,000,000 金币#k.");
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
	qm.sendNext("嘿，在那儿！米米安娜的蛋怎么样了?");
    } else if (status == 1) {   //pretty sure there would need to have an egg EXP condition... Whatever.
        if(!qm.haveItem(4220137)) {
            qm.sendOk("我明白了，你丢了你的蛋。。。当你抚养一个小咪咪的时候你需要更加小心!");
            qm.dispose();
            return;
        }
        if(!qm.canHold(1902005)) {
            qm.sendOk("请在你的装备栏上为你的咪咪空出一个空间!");
            qm.dispose();
            return;
        }

        qm.forceCompleteQuest();
        qm.gainItem(1902005, 1);
        qm.gainItem(4220137, -1);
        qm.gainMeso(-10000000);
        qm.sendOk("好了，你现在可以再次骑上米米安娜了。这次一定要好好照顾.");
    } else if (status == 2) {
        qm.dispose();
    }
}

