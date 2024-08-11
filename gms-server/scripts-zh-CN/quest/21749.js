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
            qm.sendNext("到目前为止，在#r射手村#k和#r艾琳森林#k这两个地区发现了两个封印石。。。事情似乎开始失控了。");
        } else if (status == 1) {
            qm.sendNext("战神，你现在要做的就是再次通过#b通往艾琳森林的时光之门#k。这次你一定要找回#r艾琳森林的封印石#k。通过我的情报了解到，#b#p2131002##k有关于那个封印石的线索，#r找到她#k。请一定要做到，我们的世界比以前更需要你的帮助！");
        } else if (status == 2) {
            qm.forceCompleteQuest();
            qm.gainExp(500);
            qm.dispose();
        }
    }
}
