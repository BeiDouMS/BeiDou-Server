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
	Food depot on Ariant Residential area
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
        return;
    }
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
        if (!cm.isQuestStarted(3929)) {
            cm.sendOk("这里似乎没什么特别的。");
            cm.dispose();
            return;
        }

        var progress = cm.getQuestProgress(3929);
        var ch = progress[slot];
        if (ch == '3') {
            cm.sendOk("这里已经放过食物了。");
        } else if (ch == '2') {
            if (!cm.haveItem(4031580, 1)) {
                cm.sendOk("你身上没有#b#t4031580##k。");
            } else {
                var nextProgress = progress.substr(0, slot) + '3' + progress.substr(slot + 1);
                cm.gainItem(4031580, -1);
                cm.setQuestProgress(3929, nextProgress);
                cm.sendOk("你悄悄把#b#t4031580##k放在了居民家里。");
            }
        } else {
            cm.sendOk("这里似乎没什么特别的。");
        }

        cm.dispose();
    }
}
