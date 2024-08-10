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
            qm.sendNext("啊......没想到还会碰上这种事情。怎么都没想到人偶师还会潜伏到这里来。平时大概是疏于修炼了，完全被对方给算计了。不过，这也暴露出了他们的弱点。");
        } else if (status == 1) {
	qm.sendNextPrev("弱点？", 2);
        } else if (status == 2) {
            qm.gainExp(8000);
            qm.teachSkill(21100000, 0, 20, -1); // polearm mastery

            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}