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
            qm.sendNext("啊额... 雪人 #b#t4032339##k 刚刚被偷了! 多么令人沮丧, 雪人为了得到它而努力工作, 就为了让那只乌鸦偷走它。。。", 9);
        } else if (status == 1) {
            qm.sendNext("嘿，我只是路过，不能不听到你刚才。我可以借给你我的力量，小偷去哪了?", 3);
        } else if (status == 2) {
            qm.sendNext("哦，你真好。。。小偷从西边的门前走过，把#i4032339##k 拿回来，雪人需要它送给心爱的人.", 9);
        } else if (status == 3) {
            qm.sendNext("好的，在那儿等着。我马上还给你!", 3);
        } else if (status == 4) {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}
