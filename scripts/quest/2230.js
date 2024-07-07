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
            qm.sendNext("我把这珍贵的小生命交到你手中……请用你的生命去守护它……");
        } else if (status == 1) {
            qm.sendYesNo("跟随引导，到我身边来。");
        } else if (status == 2) {
            qm.sendOk("紫色的风铃草在阳光下浸泡在树影婆娑的树林间……沿着通向未知的路走，你就会到达风铃草。我会在这里等你。");
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
            qm.sendSimple("你好，旅行者……你终于来了。你坚守你的职责吗？\r\n #b#L0#什么职责？你是谁？#l#k");
        } else if (selection == 0 && status == 1) {
            qm.sendNext("你在口袋里发现小球了吗？那个小球就是你的职责。当你孤身一人时，没什么能比有一个一直陪在你身边的朋友更让人开心的了。你听说过#b宠物#k 吗？\n" +
                "人们养宠物是为了减轻负担、悲伤和孤独，因为知道在这件事上有人或有东西站在你这边，真的会让你安心。但凡事都有后果，随之而来的是责任……");
        } else if (status == 2) {
            qm.sendNextPrev("养宠物需要承担巨大的责任。记住，宠物也是一种生命，所以你需要喂养它、给它起名字、与它分享你的想法，并最终建立起一种纽带。这就是主人与这些宠物建立感情的方式。");
        } else if (status == 3) {
            qm.sendNextPrev("我想把这种思想灌输给你，所以我送了你一个我珍爱的宝宝。你带来的蛋是#b#z5000054##k，一种通过法力诞生的生物。因为你把蛋带到这里时非常小心，所以蛋很快就会孵化出来。");
        } else if (status == 4) {
            qm.sendNextPrev("#z5000054#诞生于魔法的力量，因此寿命很短。一旦变成娃娃，就再也无法复活。");
        } else if (status == 5) {
            qm.sendYesNo("现在你明白了吗？任何行为都会有后果。");
        } else if (status == 6) {
            canComplete = qm.canHold(5000054, 1);
            if (!canComplete) {
                qm.sendNext("#r你的现金栏满了");
                return;
            }

            qm.sendNext("这只宠物只能存活 #b5 小时#k。请善待它。");
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