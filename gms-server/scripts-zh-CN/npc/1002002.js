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
	NPC Name: 		Pison
	Map(s): 		Victoria Road : Lith Harbor (104000000)
	Description: 		Florina Beach Tour Guide
 */
var status = 0;

function start() {
    cm.sendSimple("你听说过位于明珠港附近、能够欣赏壮观海景的#b黄金海滩#k吗？我可以带你去那里，只需#b1500金币#k；如果你有#b黄金海滩自由旅行券#k，就可以免费前往。\r\n#L0##b支付 1500 金币。#l\r\n#L1#使用黄金海滩自由旅行券。#l\r\n#L2#什么是黄金海滩自由旅行券？#l");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if ((mode == 0 && type == 1) || mode == -1 || (mode == 0 && status == 1)) {
            if (type == 1) {
                cm.sendNext("你一定有一些事情要处理。你一定因为旅行和打猎而感到疲倦。去休息一下，如果你改变主意了，再来找我谈谈吧。");
            }
            cm.dispose();
            return;
        } else {
            status -= 2;
        }
    }
    if (selection == 0) {
        status++;
    }
    if (status == 1) {
        if (selection == 1) {
            cm.sendYesNo("你带着#b黄金海滩自由旅行券#k吗？有了它，就可以随时前往黄金海滩。不过那里也有怪物出没，请不要掉以轻心。要现在前往黄金海滩吗？");
        } else if (selection == 2) {
            cm.sendNext("#b黄金海滩自由旅行券#k是一种特别的旅行券，只要带在身上，就能免费前往黄金海滩。它非常稀有，就连我们导游也要花钱才能得到。可惜我几周前休假时不小心把自己的那张弄丢了。");
        }
    } else if (status == 2) {
        if (type != 1 && selection != 0) {
            cm.sendNextPrev("没把那张券带回来，想起来还真让人难受。希望有人把它捡到后放在安全的地方。这就是我的故事。如果你找到它，也许能好好利用起来。还有问题的话，随时来问我。");
            cm.dispose();
        } else {
            if (cm.getMeso() < 1500 && selection == 0) {
                cm.sendNext("你的金币似乎不够。赚钱的方法很多，比如出售装备、打倒怪物或完成任务。等准备好费用后再来找我吧。");
            } else if (!cm.haveItem(4031134) && selection != 0) {
                cm.sendNext("嗯？你的#b黄金海滩自由旅行券#k在哪里呢？你确定带在身上吗？请再确认一下。");
            } else {
                if (selection == 0) {
                    cm.gainMeso(-1500);
                }
                cm.getPlayer().saveLocation("FLORINA");
                cm.warp(110000000, "st00");
            }
            cm.dispose();
        }
    }
}
