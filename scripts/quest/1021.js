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
            qm.sendNext("嘿，" + (qm.getPlayer().getGender() == 0 ? "先生" : "女士") + "~ 我是罗杰，我来教你如何恢复状态");
        } else if (status == 1) {
            qm.sendNextPrev("你问我为什么这么做？哈哈哈！\r\n教导新来的旅行者是我的职责所在！");
        } else if (status == 2) {
            qm.sendAcceptDecline("让我们开始吧！");
        } else if (status == 3) {
            if (qm.getPlayer().getHp() >= 50) {
                qm.getPlayer().updateHp(25);
            }

            if (!qm.haveItem(2010007)) {
                qm.gainItem(2010007, 1);
            }

            qm.forceStartQuest();
            qm.sendNext("我把你HP减到了25，如果你的HP降到0你就挂了！现在我给你#r罗杰的苹果#k。按#bI#k键打开背包，选择消耗栏双击罗杰的苹果使用掉，你就能恢复HP了。");
        } else if (status == 4) {
            qm.sendPrev("等你吃完后再来找我。");
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
                qm.sendNext("你的血量还很低，快吃掉我给你的苹果。");
                qm.dispose();
            } else {
                qm.sendNext("很简单对吧！你还可以把消耗栏里的药水放到#b快捷键#k上快速使用！");
            }
        } else if (status == 1) {
            qm.sendNextPrev("好了，你已经学到很多了，请稍等一下");
        } else if (status == 2) {
            qm.sendPrev("请收下这些物资，希望对你的旅途有所帮助！\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n#v2010000# 3 #t2010000#\r\n#v2010009# 3 #t2010009#\r\n\r\n#fUI/UIWindow.img/QuestIcon/8/0# 10 exp");
        } else if (status == 3) {
            if (qm.isQuestCompleted(1021)) {
                qm.dropMessage(1, "未知错误");
            } else if (qm.canHold(2010000) && qm.canHold(2010009)) {
                qm.gainExp(10);
                qm.gainItem(2010000, 3);
                qm.gainItem(2010009, 3);
                qm.forceCompleteQuest();
            } else {
                qm.dropMessage(1, "背包满了");
            }
            qm.dispose();
        }
    }
}