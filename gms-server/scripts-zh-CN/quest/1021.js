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
 * Edited by XxOsirisxX

	NPC Name: 		Roger
	Map(s): 		Maple Road : Lower level of the Training Camp (2)
	Description: 		Quest - Roger's Apple
*/
var status = -1;

function start(mode, type, selection) {
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
            qm.sendNext("嗨，我是罗杰，可以教你一些有用的知识。");
        } else if (status == 1) {
            qm.sendNextPrev("你问我为什么在这吗？呵呵！我想要帮助那些来到这里的冒险家们。");
        } else if (status == 2) {
            qm.sendAcceptDecline("来。。。开个小玩笑怎么样？咦！");
        } else if (status == 3) {
            if (qm.getPlayer().getHp() >= 50) {
                qm.getPlayer().updateHp(25);
            }

            if (!qm.haveItem(2010007)) {
                qm.gainItem(2010007, 1);
            }

            qm.forceStartQuest();
            qm.sendNext("是不是吓了一跳？HP跌到0就坏了。来，给你#r#t2010007##k，把它吃掉就会恢复了。你打开道具窗看看");
        } else if (status == 4) {
            qm.sendPrev("你要把我给你的#t2010007#全部吃掉，停滞在一个地方什么都不做HP也会恢复的。。。你恢复了全部的HP在跟我聊聊吧。");
        } else if (status == 5) {
            qm.showInfo("UI/tutorial.img/28");
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
            if (qm.c.getPlayer().getHp() < 50) {
                qm.sendNext("嗨，你的HP还没有完全恢复，使用我给你的苹果来补充吧！快去试试！");
                qm.dispose();
            } else {
                qm.sendNext("消耗道具。。。怎么样？很简单吧？可以在右下角设定#b快捷键#k，你还不知道吧？哈哈~");
            }
        } else if (status == 1) {
            qm.sendNextPrev("不错！学得很好应该给你礼物。这些都是在旅途中必需的，谢谢我吧！危机的时候好好使用。");
        } else if (status == 2) {
            qm.sendPrev("我能教你的只有这些了。有点儿舍不得也没办法，到了要离别的时候。路上小心，一路顺风啊！！！\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2010000# 3 #t2010000#\r\n#v2010009# 3 #t2010009#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 10 exp");
        } else if (status == 3) {
            if (qm.isQuestCompleted(1021)) {
                qm.dropMessage(1, "未知错误");
            } else if (qm.canHold(2010000) && qm.canHold(2010009)) {
                qm.gainExp(10);
                qm.gainItem(2010000, 3);
                qm.gainItem(2010009, 3);
                qm.forceCompleteQuest();
            } else {
                qm.dropMessage(1, "你的背包已经满了。");
            }
            qm.dispose();
        }
    }
}
