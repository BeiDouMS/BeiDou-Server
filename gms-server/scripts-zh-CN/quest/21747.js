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
            qm.sendAcceptDecline("没想到在数百年的岁月之后，英雄的后裔又重新出现了......也不知道对冒险岛世界师傅还是祸......怎样都无所谓了。好吧......我告诉你有关武陵封印石的事情。");
        } else if (status == 1) {
            qm.sendNext("武陵的封印石所在的地方叫做封印的寺院。那里的入口被隐藏在武陵寺院内。你去仔细观察武陵寺院入口处熊猫提着的灯盏。如果能从中找出#b刻有入口字样的灯盏#k，就可以进入封印的寺院了。暗号是#b道可道非常道。#k");
        } else {
            qm.forceStartQuest();
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
            qm.sendNext("成功打败了影子武士吗？表情怎么这么凝重......难道说你失败了......");
        } else if (status == 1) {
            qm.sendNext("原来是这样，武陵的封印石最终还是被抢走了......很遗憾，不过也没办法。我现在也不明白英雄们为什么要把封印石交给武陵。");
        } else if (status == 2) {
            qm.gainExp(16000);
            qm.forceCompleteQuest();

            qm.dispose();
        }
    }
}