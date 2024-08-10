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
            qm.sendNext("这段时间升级很快嘛，英雄大人？我终于又发现了和黑色之翼有关的有趣事情了。这一次咱们早点......#b武陵#k这个村子你知道吗？看来你得去一趟那里。");
        } else if (status == 1) {
            qm.sendAcceptDecline("武陵的#b陈道人#k好像已经和黑色之翼相接触。虽然不知道事情是怎么变成这样的，但信息应该准确无误。");
        } else if (status == 2) {
            qm.sendNext("你如果准备好的话，#b就请马上去武陵。#k你去查出黑色之翼为什么会和陈道人接触，以及它们之间到底有着怎样的交易。");
        } else if (status == 3) {
            qm.forceStartQuest();
            qm.dispose();
        }
    }
}
