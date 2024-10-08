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
            return qm.sendNext("封印石……那是很久很久以前，由武陵看管的东西……难道说觊歈它的人又出现了……", 1 << 3);
        case 1:
            return qm.sendNextPrev("请告诉我有关封印石的事情。", 1 << 1);
        case 2:
            return qm.sendAcceptDecline("那可不行。#o9300351#这个家伙确实很危险，但我怎么知道你会比他不危险呢？我要考验一下你……你接受#b考验#k吗？");
        case 3:
            if (type == 12 && mode == 0) { // DECLINE
                return qm.dispose();
            }
            var mapobj = qm.getWarpMap(925040001);
            if (mapobj.countPlayers() == 0) {
                mapobj.resetPQ(1);

                qm.warp(925040001, 0);
                qm.forceStartQuest();
            } else {
                qm.sendOk("有人已经在挑战，请等待他挑战结束。");
            }
        default:
            return qm.dispose();
    }
}
