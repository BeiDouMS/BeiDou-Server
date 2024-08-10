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
            qm.sendNext("所以你来参加我的课程了，是吧？好的，我会快点。");
        } else if (status == 1) {
            qm.sendNextPrev("我将教你#b制造者#k方法的实际应用。你只需要想好要制作的物品，收集所有配方中的原料，并以#r科学炼金术的方式#k混合它们。简单，不是吗？");
        } else if (status == 2) {
            qm.sendNextPrev("以制作#b重量耳环#k为例。有一个相当特定的#r延展性理论#k来生成它，就像其他'独特'物品一样，名字围绕着我们正在处理的东西的#r主要物理力量#k：在这种情况下，是#b重量耳环的延展性引力理论#k（因为它是'重量耳环'，明白了吗？）");
        } else if (status == 3) {
            qm.sendNextPrev("好的，现在你需要交给我一笔费用，就是10,000金币，作为这些信息的费用。收取的费用将用于获取你学习#b制造者#k这门艺术所需的材料。");
        } else if (status == 4) {
            qm.gainMeso(-10000);

            qm.forceCompleteQuest();
            qm.dispose();
        }

        }
    }

