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
/*  Maker Skill
	A Surprise Outcome
	3rd skill level
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
            qm.sendNext("又来烦我了？这是什么？！");
        } else if (status == 1) {
            if (qm.haveItem(4031980, 1)) {
                qm.sendNext("你制作了一个#b#t4031980##k？！怎么可能？你是怎么做到的！！嗯。。不得不承认青出于蓝而胜于蓝，你已经超越我了。\r\n\r\n看来你已经准备好学习最高级的锻造技能了。");
            } else {
                qm.sendNext("...请让开，如果时时刻刻都分心，我就无法完成这项工作。");
                qm.dispose();

            }
        } else if (status == 2) {
            qm.forceCompleteQuest();

            qm.gainItem(4031980, -1);
            var skillid = Math.floor(qm.getPlayer().getJob().getId() / 1000) * 10000000 + 1007;
            qm.teachSkill(skillid, 3, 3, -1);
            qm.gainExp(300000);

            qm.dispose();
        }
    }
}