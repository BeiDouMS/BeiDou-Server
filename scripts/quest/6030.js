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
	Quest: Carson's Fundamentals of Alchemy
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
            qm.sendNext("我会教你炼金术的基础知识。");
        } else if (status == 1) {
            qm.sendNextPrev("尽管科学有助于从微观的角度看待组成物品的元素，但仅靠科学还不足以发明出一种物品。");
        } else if (status == 2) {
            qm.sendNextPrev("要怎么把材料“融合”成一个整体呢？传统的锻造方式会降低物品的一些潜力。");
        } else if (status == 3) {
            qm.sendNextPrev("炼金术可用于完成这项任务。它可以干净利落地用材料#b交换#k以合成新的物品，几乎没有任何浪费。掌握这项技术需要一段时间，但一旦掌握，一切都会顺利完成。");
        } else if (status == 4) {
            qm.sendNextPrev("请记住：#b质量是守恒的#k，即炼金术基本原理中物质总量是不变的，就是没有任何东西可以凭空产生。明白了吗？");
        } else if (status == 5) {
            qm.gainMeso(-10000);

            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}