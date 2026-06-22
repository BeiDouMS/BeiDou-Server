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
/* 
	Jewel depot on Ariant Residential area
 */

var status;
var slot = 0;

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

        if (status == 0) {
            if (!cm.isQuestStarted(3926)) {
                cm.sendOk("There does not seem to be anything special here.");
                cm.dispose();
                return;
            }

            var progress = cm.getQuestProgress(3926);
            var ch = progress[slot];
            if (ch == '3') {
                cm.sendOk("You have already placed a #b#t4031579##k here.");
            } else if (ch == '2') {
                if (!cm.haveItem(4031579, 1)) {
                    cm.sendOk("You do not have a #b#t4031579##k. Take some jewels from the large chest at the Red Scorpion hideout first.");
                } else {
                    var nextProgress = progress.substr(0, slot) + '3' + progress.substr(slot + 1);

                    cm.gainItem(4031579, -1);
                    cm.setQuestProgress(3926, nextProgress);
                    cm.sendOk("You quietly placed a #b#t4031579##k in the residence.");
                }
            } else {
                cm.sendOk("This is not where you need to place the jewels.");
            }

            cm.dispose();
        }
    }
}
