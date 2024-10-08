/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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
    if (mode == -1) { // END CHAT
        return qm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            return qm.sendNext("哦，英雄。你的等级提高得那么快，我都快认不出你来了。你有什么事吗？");
        case 1:
            return qm.sendNextPrev("在你勤奋修炼的时候，#p1201000#和我对你的过去和封印石进行了全方位的调查。不久之前，我们了解到一件非常有趣的事情。你知道专为孩子们生产玩具的村庄#m220000000#吗？");
        case 2:
            return qm.sendNextPrev("#m220000000#有两个管理时间的时间塔。两个塔分别对时间进行管理，使#m220000000#的时间最终得以停止下来。因为孩子们长大之后，就不再需要玩具了，所以那里的时间一直是停止不动的。");
        case 3:
            return qm.sendNextPrev("但是其中一座时间塔好像由于某种原因坏掉了。#b#m220000000#也因此出现了空洞，人们可以通过空洞回到过去#k……有趣的地方就在这里。");
        case 4:
            return qm.sendNextPrev("#p1201000#对去过过去的人带回的信息进行了综合整理，得出的结论是那个时代#b和英雄你所生活的时代很相似#k。服饰、物品以及环境……那么，如果前往那里的话是不是也有可能得到有关封印石的信息呢？是吧？");
        case 5:
            return qm.sendAcceptDecline("封印石不管怎样都没关系。更重要的是，在那里也许可以遇到认识英雄的人。");
        case 6:
            if (type == 12 && mode == 0) { // DECLINE
                return qm.dispose();
            }
            // ACCEPT
            if (!qm.isQuestCompleted(21749)) {
                qm.gainExp(500);
                qm.forceCompleteQuest();
            }
            qm.sendOk("损坏的时间塔是#b右边的时间塔#k……也就是赫丽奥斯塔。在#b粉红色兔子头模样的建筑#k里，有管理时间的装置。据说装置坏了，从那里可以通往过去。")
        default:
            return qm.dispose();
    }
}
