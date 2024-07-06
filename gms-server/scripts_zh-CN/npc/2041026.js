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
/* Ghosthunter Bob
 */

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

        if (status == 0) {
            if (cm.haveItem(4220046, 1)) {
                // quest completing here when "forfeiting Timer's Egg", as well as reporting missing quests on M. Shrine are thanks to drmdsr & Thora

                cm.gainItem(4220046, -1);
                cm.sendOk("你想把#r#t4220046##k交给我，对吧？好的，我会替你拿着。");
            } else {
                cm.sendOk("你好！我是#b#p2041026##k，负责观察和报告这个地区的任何超自然活动。");
            }

            cm.dispose();
        }
    }
}