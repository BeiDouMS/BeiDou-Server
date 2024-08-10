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
            if (!qm.haveItem(4001094, 1)) {
                qm.sendNext("你没有 #b#t4001094##k...");
                qm.dispose();
                return;
            }

            if (qm.haveItem(2041200, 1)) {
                qm.sendOk("（自从到达这个地方后，我包里的 #b#t2041200##k 变得更加明亮了... 再次注意到，那边的小龙似乎对它怒视着。）");
                qm.dispose();
                return;
            }

            qm.sendNext("你带来了一个 #b#t4001094##k，感谢你为我们的巢穴带回了一个同类！请接受这个...\r\n\r\n....... (bleuuhnuhgh) (blahrgngnhhng) ...\r\n\r\n呃，#b#t2041200##k 作为我们同类的感激之情。还有一个请求，请把那个东西带走...");
        } else if (status == 1) {
            if (!qm.canHold(2041200, 1)) {
                qm.sendOk("请在消耗栏中腾出空位来领取奖励。");
                qm.dispose();
                return;
            }

            qm.forceCompleteQuest();
            qm.gainItem(4001094, -1);
            qm.gainItem(2041200, 1);    // 任务奖励问题找到并修复感谢 MedicOP & Thora
            qm.gainExp(42000);
            qm.dispose();
        }
    }
}

