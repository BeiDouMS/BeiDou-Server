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
            qm.sendNext("金银岛封印石我已经找到了。你看，呵呵呵。");
        } else if (status == 1) {
            if (!qm.canHold(4032323, 1)) {
                qm.sendNext("Please free a slot on your ETC inventory before receiving the item.");
                qm.dispose();
                return;
            }

            if (!qm.haveItem(4032323, 1)) {
                qm.gainItem(4032323, 1);
            }
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
            if (qm.haveItem(4032323, 1)) {
                qm.sendNext("黑色之翼的动向，我已经从真相叔叔那里听说了。听说前不久还被他们袭击了一次......你还好吧？咦？这个......这就是金银岛封印石吗？没想到真相叔叔果然比那些家伙们早一步找到金银岛封印石。不知道这颗宝石到底有什么用......只知道这个东西肯定和黑魔法师有关。");
            } else {
                qm.dispose();
            }
        } else if (status == 1) {
            qm.gainItem(4032323, -1);
            qm.gainExp(6037);
            qm.forceCompleteQuest();

            qm.dispose();
        }
    }
}