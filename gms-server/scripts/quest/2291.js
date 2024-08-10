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
            if (!qm.haveItem(4032521, 10)) {
                qm.sendNext("嘿，你还没有得到#b10张#t4032521##k吗？");
                qm.dispose();
                return;
            }

            qm.sendNext("你身上有#b#i4032521##k，很好，让我给你带路。");
        } else if (status == 1) {
            var em = qm.getEventManager("RockSpiritVIP");
            if (!em.startInstance(qm.getPlayer())) {
                qm.sendOk("嗯...看起来前面的房间现在有点拥挤。请在这儿等一会，好吗？");
                qm.dispose();
                return;
            }

            qm.gainItem(4032521, -10);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}
