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
            qm.sendNext("我会教你们科学理论的基础知识。");
        } else if (status == 1) {
            qm.sendNextPrev("炼金术无法满足要求的科学阶段。所有物品都有分子结构。它们的#b排列方式#k决定了物品将具有的许多属性。");
        } else if (status == 2) {
            qm.sendNextPrev("在#r锻造#k的场景中也是如此。必须研究用于形成物品的每个材料，才能判断这些材料是否有效。");
        } else if (status == 3) {
            qm.sendNextPrev("请记住：科学的主要观点是，无论是哪种情况，#b理解#k过程很重要，不要简单地放弃尝试。");
        } else if (status == 4) {
            qm.sendNextPrev("已经说清楚了吧？好，那这节课就结束了。下课。");
        } else if (status == 5) {
            qm.gainMeso(-10000);

            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}