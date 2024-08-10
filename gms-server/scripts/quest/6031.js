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
	Quest: Hughes the Fuse's Basic of Theory of Science
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
            qm.sendNext("我将教你有关科学理论的基础知识。");
        } else if (status == 1) {
            qm.sendNextPrev("科学阶段是炼金术无法满足要求的地方。所有物品都有分子构成。物品的#r排列方式和每个内在物质单位#k定义了物品将具有的许多属性。");
        } else if (status == 2) {
            qm.sendNextPrev("这也适用于#r制造者#k的情况。一个人必须能够研究正在用来形成物品的每个组件的痕迹，才能判断实验是否会最终成功或失败。");
        } else if (status == 3) {
            qm.sendNextPrev("记住这一点：科学的主要视角，使其流畅运转的那一台引擎，无论是什么情况，都是#b理解产生结果的过程#k，而不是随意地尝试。");
        } else if (status == 4) {
            qm.sendNextPrev("这清楚了吗？很好，那么课程结束。下课。");
        } else if (status == 5) {
            qm.gainMeso(-10000);

            qm.forceCompleteQuest();
            qm.dispose();
        }

    }
}
