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
            qm.sendNext("喂，你在哪呢？大事不好了！你快来趟特鲁的情报商店！\r\n（喂......？ 特鲁可是一直喊我英雄的......）");
        } else {
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
            qm.sendNext("英雄，真的非常感谢你！没想到人偶师竟然能绕过明珠港的警备潜伏到这里来。他大概是因为之前的事情想要报复我们。幸好你及时赶到，做得很好！");
        } else if (status == 1) {
            qm.sendNextPrev("作为这次行动的奖励，我会把#r精准矛#k技能传授给你。学会之后，你使用矛时的熟练度和命中率都会提高。", 2);
        } else if (status == 2) {
            qm.gainExp(8000);
            qm.teachSkill(21100000, 0, 20, -1); // polearm mastery

            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}