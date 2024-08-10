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

var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && type == 12) {
            qm.sendNext("当你明智地接受了你的决定后再回来。");
        }
        qm.dispose();
        return;
    }
    if (status == 0) {
        qm.sendAcceptDecline("训练进展如何？哇，你达到了这么高的水平！太棒了。我知道你在维多利亚岛会做得很好。。。哦，看看我。我在浪费你的时间。我知道你很忙，但你得回岛上一会儿。");
    } else if (status == 1) {
        qm.sendOk("你的#b#p1201001###b#m140000000##k突然变得奇怪起来。据记载，北极熊在召唤主人的时候会这样做。#它在召唤你。请回到岛上，检查一下。");
    } else if (status == 2) {
        qm.startQuest();
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && type == 1) {
            qm.sendNext("嘿！至少你试过了!");
        }
        qm.dispose();
        return;
    }
    if (status == 0) {
        qm.sendNext("嗡 嗡 嗡 嗡 嗡...."); //Giant Polearm
    }//Giant Polearm
    else if (status == 1) {
        qm.sendNextPrev("#b(#p1201001#产生了一个起伏的回波。但是站在那边的那个男孩是谁?)", 2);
    } else if (status == 2) {
        qm.sendNextPrev("#b(你以前从没见过他。他看起来不像人.)", 2);
    } else if (status == 3) {
        qm.sendNextPrev("哟，战神！你没听见吗？我说，你没听见吗！呃，真是郁闷!");
    } else if (status == 4) {
        qm.sendNextPrev("#b(嗯？那是谁的声音？听起来像个愤怒的男孩。。。)", 2);
    } else if (status == 5) {
        qm.sendNextPrev("啊，我唯一的主人被困在冰里几百年，完全抛弃了我，现在完全无视我.");
    } else if (status == 6) {
        qm.sendNextPrev("你是谁？", 2);
    } else if (status == 7) {
        qm.sendNextPrev("战神,你现在听到我说话了吗？是我！你不认识我吗？我是你的武器，#b#p1201002#战矛#k!");
    } else if (status == 8) {
        qm.sendNextPrev("#b(...#p1201002#?#p1201001#他居然能说话?)", 2);
    } else if (status == 9) {
        qm.sendNextPrev("你脸上那可疑的表情是怎么回事？我知道你失忆了，但你也忘了我吗？你怎么能？!");
    } else if (status == 10) {
        qm.sendNextPrev("很抱歉，但我什么都记不起来了。", 2);
    } else if (status == 11) {
        qm.sendYesNo("对不起，说什么都没有用！你知道几百年来我是多么孤独和无聊吗？我不在乎它需要什么！记住我！现在记住我!");
    } else if (status == 12) {
        qm.completeQuest();
        qm.sendNext("#b(自称是#p1201002#这个#p1201001#在沮丧中大叫, 你不认为这次谈话会有任何进展。你最好去和我谈谈#p1201000#.)", 2);
        //qm.sendNoExit("#b(The voice that claims to be #p1201002# the #p1201001# is yelling in frustration. You don't think this conversation is going anywhere. You better go talk to #p1201000# first.)", true);
    } else if (status == 13) {
        //qm.showVideo("Maha");
        qm.dispose();
    }
}