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
            qm.sendNext("战神，我发现了一些令人不安的消息。。。你说你来自东部森林区，对吗？我们追踪并研究了用来支撑进入未来之门的魔法。结果发现那是一种#r时间之门#k。你用的衣服。。。以前从没有人见过。那一定意味着，你一定来自未来。");
        } else if (status == 1) {
            qm.sendNext("现在关于这个问题：在你的时间轴上似乎丢失了封印石。。。它是一个强大的神器，可以阻止黑魔法师的军队围攻我们的世界。。如果那个封印石消失了，再没有什么能阻止他了。因为这是一件非常重要的事情，所以要从未来找到我的自我。明白了，把我从未来带走吧！");
        } else if (status == 2) {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}
