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
		if(type == 15 && mode == 0) {
			qm.sendNext("哦，不需要拒绝我的提议。这没什么大不了的。只是一种药水。好吧,如果你改变主意了就告诉我.");
			qm.dispose();
			return;
		}
		//status -= 2;
	}

    if (status == 0) {
	qm.sendNext("咦？ 这个岛上的什么人？ 喔， 您认识 #p1201000#吗？ #p1201000#到这里有什么事情...啊，这位是不是#p1201000#大人认识的人呢？神么？你说这位是英雄吗？");
    } else if (status == 1) {
	qm.sendNextPrev("     #i4001170#");//gms like
    } else if (status == 2) {
	qm.sendNextPrev("这位正是 #p1201000#家族数百年等待的英雄！喔喔！难怪看起来不是什么平凡的人物...");
    } else if (status == 3) { 
	qm.sendAcceptDecline("但是，因为黑魔法师的诅咒而在巨冰里沉睡着，所以，好像英雄的体力都消耗掉了的样子。#b我给你一个恢复体力用的药水，赶紧喝喝看#k");//nexon probably forgot to remove the '.' before '#k', lol	
    } else if (status == 4) {
       	if (qm.getPlayer().getHp() >= 50) {
            	qm.getPlayer().updateHp(25);
        } 
	if (!qm.isQuestStarted(21010) && !qm.isQuestCompleted(21010)) {
        	qm.gainItem(2000022, 1);
			qm.forceStartQuest();
	}
	qm.sendNext("先喝了它。然后我们再谈.", 9);
    } else if (status == 5) {
	qm.sendNextPrev("#b(我怎么喝药水？我不记得了..)", 3);
    } else if (status == 6) {	
	qm.guideHint(14);
        qm.dispose();
    }
}

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (type == 1 && mode == 0) {
            qm.dispose();
        } else {
            qm.dispose();
            return;
        }
    }
    if (status == 0) {
        if (qm.c.getPlayer().getHp() < 50) {
            qm.sendNext("你还没喝那药水呢.");
            qm.dispose();
        } else {
            qm.sendNext("我们一直在冰洞里挖啊挖希望能找到英雄但我从没想过我真的看到了那一天预言是真的! 你说得对, #p1201000#! 现在，一个传奇英雄回来了，我们没有理由害怕黑法师！");
        }
    } else if (status == 1) {
        qm.sendOk("我让你等太久了对不起，我有点忘乎所以了。我相信其他企鹅也是这么想的。我知道你很忙，但是你能#b停下来和其他企鹅谈谈#k 在去镇上的路上？他们会很荣幸的.\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#i2000022# 5 #t2000022#\r\n#i2000023# 5 #t2000023#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 16 exp");
    } else if (status == 2) {
        if(qm.isQuestStarted(21010) && !qm.isQuestCompleted(21010)) {
            qm.gainExp(16);
            qm.gainItem(2000022, 3);
            qm.gainItem(2000023, 3);
            qm.forceCompleteQuest();
        }
        
        qm.sendNext("哦，你的水平！你甚至可能已经得到了一些技能点数。在枫树世界里，你每次都能获得3个技能点。按下 #bK 键 #k去查看技能窗口", 9);
    } else if (status == 3) {
	qm.sendNextPrev("#b(所有人都对我很好，但我什么都不记得了。我真的是英雄吗？我应该检查一下我的技能。但是我怎么检查呢?)", 3);
    } else if (status == 4) {
        qm.guideHint(15);
        qm.dispose();
    }
}

