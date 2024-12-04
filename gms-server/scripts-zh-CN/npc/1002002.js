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
    cm.sendSimple("你听说过位于立石港附近，能够欣赏到壮观海景的海滩#b黄金海滩#k吗？我可以带你去那里，只需#b1500金币#k，或者如果你有#b黄金海滩VIP门票#k的话，那就可以免费进入。\r\n#L0##b 我支付1500金币。#l\r\n#L1# 我有黄金海滩VIP门票。#l\r\n#L2# 什么是黄金海滩VIP门票？#l");
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
            cm.sendYesNo("所以你有一张#b黄金海滩VIP门票#k吗？你可以随时用它前往黄金海滩。好的，但要注意可能会遇到一些怪物。好的，你现在想前往黄金海滩吗？");
        } else if (selection == 2) {
            cm.sendNext("你一定对#b黄金海滩的VIP门票#k很感兴趣。哈哈，这很可以理解。黄金海滩的VIP门票是一种物品，只要你拥有它，就可以免费前往黄金海滩。这是一种非常稀有的物品，甚至我们也不得不购买，但不幸的是，我在几周前在我珍贵的暑假期间丢失了我的。");
        }
    } else if (status == 2) {
        if (type != 1 && selection != 0) {
            cm.sendNextPrev("我回来的时候没有带着它，感觉很糟糕没有它。希望有人捡到了它并把它放在安全的地方。不管怎样，这就是我的故事，谁知道呢，也许你能捡到它并好好利用。如果你有任何问题，随时问我。");
            cm.dispose();
        } else {
            if (cm.getMeso() < 1500 && selection == 0) {
                cm.sendNext("我觉得你缺少冒险币。有很多方法可以赚钱，比如...卖掉你的盔甲...打败怪物...做任务...你知道我在说什么。");
            } else if (!cm.haveItem(4031134) && selection != 0) {
                cm.sendNext("嗯，你的 #bVIP通行证#k 到黄金海滩到底在哪里？你确定你有吗？请再检查一遍。");
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