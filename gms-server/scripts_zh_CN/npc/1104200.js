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
            cm.sendNext("“#b#p1104002##k...黑魔女...困住了我在这里...现在没时间了，她已经在去#r攻击艾洛斯#k的路上了！”");
        } else if (status == 1) {
            cm.sendYesNo("同伴骑士，你必须立刻前往#r埃雷夫#k，#r女皇陛下处于危险之中#k！即使在这种情况下，我仍然可以使用魔法传送你到那里。当你准备好时，和我交谈。#b你准备好面对埃莉诺了吗？#k");
        } else if (status == 2) {
            if (cm.getWarpMap(913030000).countPlayers() == 0) {
                cm.warp(913030000, 0);
            } else {
                cm.sendOk("已经有人在挑战她了。请稍等一会儿。");
            }

            cm.dispose();
        }
    }
}