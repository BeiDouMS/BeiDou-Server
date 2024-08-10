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
            qm.sendNext("嘿，战神。你看起来很强壮，从你从冰川中解脱出来的那一刻起。现在我将送给你一只坐骑。。");
        } else if (status == 1) {
            qm.sendAcceptDecline("选择了你的兴趣，嗯？很好，首先你必须去巴夸，那里有一个人给狼崽做食物，给我带一份来，我相信你能驯服和照顾一份。你说什么，你能试试吗?");
        } else if (status == 2) {
            qm.forceStartQuest();
            qm.sendNext("好吧。你一定要遇见的是一头蓝鲸，她在海底世界的某个地方，一头蓝鲸的顶上。祝你好运！");
        } else if (status == 3) {
            qm.dispose();
        }
    }
}