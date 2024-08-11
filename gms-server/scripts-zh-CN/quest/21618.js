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
/* Aran lv 200 mount quest
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
            qm.sendNext("哦，这只你的狼朋友。。。你看，我感觉到他皮毛背后隐藏着某种力量。等等，主人，如果我醒来，那是隐藏的力量?", 9);
        } else if (status == 1) {
            qm.sendNextPrev("等等，你能做到吗?", 3);
        } else if (status == 2) {
            qm.sendAcceptDecline("很惊讶吧？冰河中冰冻的时间是否也妨碍了你的感官？当然，为什么！准备好了告诉我!", 9);
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
            if(!qm.haveItemWithId(1902017, false)) {
                qm.sendNext("在开始进化之前，你必须先解开狼的尾巴.");
                qm.dispose();
                return;
            }
            
            qm.sendNext("让开，瞧瞧，玛哈的威力!!");
        } else if (status == 1) {
            qm.forceCompleteQuest();

            qm.gainItem(1902017, -1);
            qm.gainItem(1902018, 1);

            qm.dispose();
        }
    }
}