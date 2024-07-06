/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/* Thief Job Instructor
	Thief 2nd Job Advancement
	Victoria Road : Construction Site North of Kerning City (102040000)
*/

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.isQuestCompleted(100010)) {
                cm.sendOk("你真是一个真正的英雄！");
                cm.dispose();
            } else if (cm.isQuestCompleted(100009)) {
                cm.sendNext("好的，我会让你进去！打败里面的怪物，收集30个黑暗弹珠，然后和我同事里面的一个同事交谈。他会给你#b英雄的证明#k，这是你通过测试的证明。祝你好运。");
                status = 3;
            } else if (cm.isQuestStarted(100009)) {
                cm.sendNext("哦，这不是来自#b黑暗领主#k的一封信吗？");
            } else {
                cm.sendOk("一旦你准备好了，我可以告诉你路线。");
                cm.dispose();
            }
        } else if (status == 1) {
            cm.sendNextPrev("所以你想证明你的技能？好吧...");
        } else if (status == 2) {
            cm.sendAcceptDecline("I will give you a chance if you're ready.");
        } else if (status == 3) {
            cm.sendOk("你需要收集 #b30 个 #t4031013#。祝你好运。");
            cm.completeQuest(100009);
            cm.startQuest(100010);
            cm.gainItem(4031011, -1);
        } else if (status == 4) {
            cm.warp(108000400, 0);
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}