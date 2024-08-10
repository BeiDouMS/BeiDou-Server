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
            qm.sendNext("战神，你平安回来了！在武陵的任务完成的如何了？#r影子武士#k偷袭了武陵并再次偷走了封印石？那真不幸。至少你没有受伤，我很高兴。");
        } else if (status == 1) {
            qm.sendNext("我研究了一些新的技能，试图帮你找回记忆。好消息的是，我想起了其中一个：#r战神突进#k！有了它，你将能够击退前面的敌人。对你的能力来说是一个很好的提升，对吧？");
        } else if (status == 2) {
            qm.gainExp(20000);
            qm.teachSkill(21100002, 0, 30, -1); // final charge

            qm.forceCompleteQuest();

            qm.dispose();
        }
    }
}