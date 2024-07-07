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
/* 
	Quest: Meren's Class on the Actual Practice
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
            qm.sendNext("你是来上我的课的？好，我们开始吧。");
        } else if (status == 1) {
            qm.sendNextPrev("我将教你如何应用#b锻造#k方法。");
        } else if (status == 2) {
            qm.sendNextPrev("你需要做的就是在脑海中想好要制作的物品，收集所需要的材料，然后以#科学炼金术#k的方式将它们混合在一起。很简单吧。这里面还运用到#r重力#k的作用哦。");
        } else if (status == 3) {
            qm.sendNextPrev("好了，现在你得给我#b10000金币#k，用于搜集材料，供你学习#b炼金术#k。");
        } else if (status == 4) {
            qm.gainMeso(-10000);

            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}