/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

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
	Author : Generic
	NPC Name: 		Kiku
	Map(s): 		Empress' Road : Training Forest I
	Description: 		Quest - The Path of a Knight
	Quest ID : 		20000
*/

var status = -1;
var choice1;

function start(mode, type, selection) {
    if (mode < 1) {
        qm.dispose();
    } else if (mode > 0) {
        status++;
    }
    
    if (status == 0)
        qm.sendSimple("你准备好执行任务了吗？如果你不能通过这个测试，那么你就不能称自己为真正的骑士。你确定你能做到吗？如果你害怕这样做，告诉我。我不会告诉奈哈特的. \r\n #L0#我稍后再试试这个.#l \r\n #L1#我不怕。我们这样做吧.#l");
    else if (status == 1) {
        if (selection == 0) {
            qm.sendNext("如果你称自己为骑士，那就不要犹豫。向大家展示你有多大的勇气.");
            qm.dispose();
        } else if (selection == 1) {
            choice1 = selection;
            qm.sendSimple("我很高兴你没有逃跑，但是。。。你确定你想成为一名训练中的骑士吗？我要问的是，你是否愿意加入一个冒险骑士团，并因此被捆绑在皇后在任何时候？她可能是皇后，但她还是个孩子。你确定你能为她而战吗？我不会让内哈特知道的，所以告诉我你的真实感受。\如果皇后想要枫树世界的和平，那我就不管了. \r\n #L2#如果皇后想要枫树世界的和平，那我就不管了.#l \r\n #L3#只要我能成为一名骑士，我会忍受一切 #l");
            qm.forceStartQuest();
            qm.forceCompleteQuest();
        }
    } else if (status == 2) {
        qm.dispose();
    }
}