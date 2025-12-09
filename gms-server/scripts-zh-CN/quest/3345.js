/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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

        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            if (qm.getQuestProgress(3345, 0) == 4) {
                qm.sendNext("看来你已经成功了。这样一来，玛加提亚城迫在眉睫的危机总算得以避免，做得好，勇敢的冒险着！");
                qm.forceCompleteQuest();

                qm.gainExp(20000);
            } else {
                qm.sendNext("难道你还没有封印#r玛迦提亚城地下的魔法阵#k吗？此事关系重大，还请你加快行动。");
            }

            qm.dispose();
        }

    }
}