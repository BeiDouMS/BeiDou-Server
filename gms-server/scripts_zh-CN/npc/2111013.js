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
// NPC - Picture frame
// Location: Magatia - Home of the Missing Alchemist
// Used to handle quest 3322 - Phyllia's Pendant

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (!cm.isQuestStarted(3322) || cm.haveItem(4031697, 1)) {
            cm.dispose();
            return;
        }

        if (status == 0) {
            cm.sendOk("挂在画框后面的钩子被解开，揭示了画框内部的一个秘密空间。在那里，发现了一枚银色吊坠。小心地取下吊坠后，画框被合上，放回桌子上。");
        }
        else if (status == 1) {
            if (cm.canHold(4031697, 1)) {
                cm.gainItem(4031697);
            }
            else {
                cm.sendNext("您的背包已满，请确保您有一个杂项槽可用。");
            }
        }
        else {
            cm.dispose();
        }
    }
}