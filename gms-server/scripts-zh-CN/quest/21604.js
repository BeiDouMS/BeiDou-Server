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
        return qm.dispose();
    }

    if (type == 0 && mode == 0) { // PREV
        status--;
    } else {
        status++;
    }

    switch (status) {
        case 0:
            return qm.sendNext("啊呀，你带着的不是狼吗？我已经好久都没见过带狼的人了。不过，带着狼却不#b骑乘#k，难道你还不会骑乘之术吗？");
        case 1:
            return qm.sendNextPrev("所谓骑乘，就是一种骑在狼背上快速行进，并能和狼之间实现良好沟通的技术。我曾经骑过#o5130104#和#o5140000#，当时我可帅呢！");
        case 2:
            return qm.sendAcceptDecline("你想学骑乘吗？如果想的话，#p2020007#就可以帮助你。");
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                return qm.dispose();
            }
            // ACCEPT
            if (!qm.isQuestStarted(21604) && !qm.isQuestCompleted(21604)) {
                qm.forceStartQuest();
            }
            return qm.sendNext("要想骑乘，没有任何准备，直接骑在狼背上是很困难的。要先弄个#b#t1912011##k，这样才能让狼不觉得难受。我会做狼鞍，你去找材料就好。");
        case 4:
            qm.sendPrev("制作#t1912011#的材料是#b#t4000048##k。大概#b50张#k就够了。等你把材料都找齐了，我就把骑乘的技巧和#t1912011#一起传授给你。赶紧去找材料吧。我也很期待啊。");
            return qm.dispose();
        default:
            return qm.dispose();
    }
}
