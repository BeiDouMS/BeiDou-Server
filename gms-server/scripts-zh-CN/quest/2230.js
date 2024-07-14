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
	Author : 		Generic
	NPC Name: 		Mar the Fairy
	Map(s): 		Everywhere
	Description: 		Quest - A Mysterious Small Egg
	Quest ID: 		2230
*/

var status = -1;
var canComplete;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendNext("我将这小小而珍贵的生命托付于你……用你的生命来守护它……");
        } else if (status == 1) {
            qm.sendYesNo("照顾另一个生命……这是赋予你的使命……跟随引领你来到我身边的那个力量。");
        } else if (status == 2) {
            qm.sendOk("将手伸进口袋。我想你的朋友已经找到了你。\r\n在参天大树间沐浴阳光的紫钟花……沿着通往未知的小径，追随它，我将在这里等你。");
            qm.forceStartQuest();
            qm.gainItem(4032086, 1); // Mysterious Egg * 1
        } else if (status == 3) {
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendSimple("你好，冒险家... 你终于来见我了。你完成了你的任务吗？ \r\n #b#L0#什么任务？你是谁？#l#k");
        } else if (selection == 0 && status == 1) {
            qm.sendNext("你是否在你的口袋里找到了一颗小蛋？那颗蛋就是你的责任，你的义务。当你独自一人时，生活非常艰难。在这种时候，没有什么比拥有一个始终陪伴你的朋友更好的了。你听说过#b宠物#k吗？\r\n人们养宠物来减轻负担、悲伤和孤独，因为知道有某个人，或者在这种情况下是某个东西在你身边，会真正带来内心的平静。但一切都有后果，而随之而来的是责任……");
        } else if (status == 2) {
            qm.sendNextPrev("养宠物需要承担巨大的责任。记住宠物也是一种生命形式，所以你需要喂养它，给它取名，和它分享你的想法，最终形成一种联系。这就是宠物主人如何与这些宠物建立感情的。");
        } else if (status == 3) {
            qm.sendNextPrev("我想把这一点灌输给你，这就是为什么我送你一个我珍爱的宝贝。你带来的这颗蛋是#b符文蜗牛#k，一种通过魔力诞生的生物。由于你在带这颗蛋到这里的过程中悉心照料，它很快就会孵化。");
        } else if (status == 4) {
            qm.sendNextPrev("符文蜗牛是一种多才多艺的宠物。它会捡拾物品，用药水喂养你，还会做其他让你惊叹的事情。但缺点是，由于符文蜗牛是通过魔力诞生的，它的寿命非常短暂。一旦它变成玩偶，就再也无法复活了。");
        } else if (status == 5) {
            qm.sendYesNo("现在你明白了吗？每一个行动都有其后果，宠物也不例外。蜗牛的蛋很快就会孵化。");
        } else if (status == 6) {
            canComplete = qm.canHold(5000054, 1);
            if (!canComplete) {
                qm.sendNext("Please free a slot in your CASH inventory before you try to receive the pet...");
                return;
            }

            qm.sendNext("这只蜗牛只能存活#b5小时#k。尽情地爱它吧，最终你的爱会得到回报的。");
        } else if (status == 7) {
            if (canComplete) {
                qm.gainItem(4032086, -1); // Mysterious Egg * -1
                qm.forceCompleteQuest();
                qm.gainItem(5000054, 1, false, true, 5 * 60 * 60 * 1000);  // rune snail (5hrs), missing expiration time detected thanks to cljnilsson
            }

            qm.dispose();
        }
    }
}