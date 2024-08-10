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
            qm.sendNext("我把这个小小的，珍贵的生命放在你的手中…用你的生命守护它。。。");
        } else if (status == 1) {
            qm.sendYesNo("照看另一个生命…这是给你的不可避免的使命…跟随你的力量指引我。");
        } else if (status == 2) {
            qm.sendOk("把手放进口袋里。我想你的朋友已经找到你了。\r\n紫色的风铃草在阳光下浸泡在参天大树之间。。。沿着通向未知的路走，这条路会把你引向钟楼。我在这里等你.");
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
            qm.sendSimple("你好，旅行者。。。你终于来看我了。你履行职责了吗？\r\n #b#L0#什么职责？你是谁？#l#k");
        } else if (selection == 0 && status == 1) {
            qm.sendNext("你在口袋里找到宠物了吗？保护好宠物是你的职责。当你独自一人的时候，生活很艰难。在这样的时刻，没有什么比拥有一个朋友更能时刻陪伴着你。你听说过#b宠物#k吗\r\n人们养宠物是为了减轻负担、悲伤和孤独，因为知道你身边有人或事，真的会带来心灵的平静。但一切都有后果，随之而来的是责任。。。");
        } else if (status == 2) {
            qm.sendNextPrev("养宠物需要承担巨大的责任。记住宠物也是生命的一种形式，你需要悉心喂养它，给它取一个好听的名字，与它分享你的想法，最终形成一种纽带。这就是主人对这些宠物的依恋。");
        } else if (status == 3) {
            qm.sendNextPrev("我想把这个灌输给你，所以我送你一个我珍爱的孩子。你带来的宠物是#b蜗牛#k,通过魔法而生的生物。既然你把宠物带到这里时很小心，宠物很快就会孵化出来。");
        } else if (status == 4) {
            qm.sendNextPrev("蜗牛是拥有许多技能的宠物。它会拾取物品，给你吃药水，还能做很多其他的事情。缺点是，由于蜗牛是从魔法中诞生的，它的寿命很短。一旦变成洋娃娃，就永远无法复活.");
        } else if (status == 5) {
            qm.sendYesNo("现在你明白了吗？每一个行动都会带来后果，宠物也不例外。蜗牛的很快就会孵化.");
        } else if (status == 6) {
            canComplete = qm.canHold(5000054, 1);
            if (!canComplete) {
                qm.sendNext("请在您尝试接收宠物之前，在您的特殊栏中至少留有一个位置。。。");
                return;
            }

            qm.sendNext("这只蜗牛只会存在#5个小时#k.用爱来沐浴。你的爱最终会得到回报");
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