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
	Author : kevintjuh93
*/

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 15 && mode == 0) {
            qm.sendNext("啊！战神大人拒绝了！");
            qm.dispose();
            return;
        } else {
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        qm.sendAcceptDecline("呃呃……吓死我了……快，快带到赫丽娜那边去！");
    } else if (status == 1) {
        qm.gainItem(4001271, 1);
        qm.forceStartQuest();
        qm.warp(914000300, 0);
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            qm.sendNext("孩子呢？孩子救出来了的话，就赶紧让我们看看。");
        }

        qm.dispose();
        return;
    }

    if (status == 0) {
        qm.sendYesNo("你平安回来了？孩子呢？孩子也带回来了吗？");
    } else if (status == 1) {
        qm.sendNext("太好了……真是太好了。", 9);
    } else if (status == 2) {
        qm.sendNextPrev("赶快上船！已经没时间了！", 3);
    } else if (status == 3) {
        qm.sendNextPrev("啊，没错。现在不是感伤的时候。黑魔法师的气息越来越近！似乎他们已经察觉方舟的位置，得赶紧启航，不然就来不及了！", 9);
    } else if (status == 4) {
        qm.sendNextPrev("立刻出发！", 3);
    } else if (status == 5) {
        qm.sendNextPrev("战神！请你也上船吧！我们理解你渴望战斗的心情……不过，现在已经晚了！战斗就交给你的那些同伴吧，和我们一起去金银岛吧！", 9);
    } else if (status == 6) {
        qm.sendNextPrev("不行！", 3);
    } else if (status == 7) {
        qm.sendNextPrev("赫丽娜，你先出发去金银岛。一定要活着，我们一定会再见的。我要和同伴们一起同黑魔法师战斗！", 3);
    } else if (status == 8) {
        qm.gainItem(4001271, -1);
        qm.removeEquipFromSlot(-11);
        qm.forceCompleteQuest();

        qm.warp(914090010, 0); // Initialize Aran Tutorial Scenes
        qm.dispose();
    }
}