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

/* Warrior Job Instructor
	Warrior 2nd Job Advancement
	Victoria Road : West Rocky Mountain IV (102020300)
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
            if (cm.isQuestCompleted(100004)) {
                cm.sendOk("你是真正的英雄！");
                cm.dispose();
            } else if (cm.isQuestCompleted(100003)) {
                cm.sendNext("我带你去训练场，打败里面的怪物，收集30个黑珠，然后和里面的教官交谈。他会给你#b英雄的证明#k，证明你通过了测试。祝你好运。");
                status = 4;
            } else if (cm.isQuestStarted(100003)) {
                cm.sendNext("嗯……这是#b#p1022000##k的信……好的，那么我们开始测验吧");
            } else {
                cm.sendOk("等你准备好了，我就带你进去。");
                cm.dispose();
            }
        } else if (status == 1) {
            cm.sendNextPrev("我会带你去一张隐藏地图。你会看到平时也能看到的怪物。它们看起来和普通怪物一样，却完全不同。它们既不会提升你的经验等级，也不会为你提供物品。");
        } else if (status == 2) {
            cm.sendNextPrev("击倒这些怪物时，你将能够获得#b#t4031013##k。这是一颗由它们邪恶的思想制成的特殊珠子。收集30颗，然后与里面的教官交谈。这就是你通过测试的方法。");
        } else if (status == 3) {
            cm.sendYesNo("如果在里面死了也会丢失经验的，请准备充足后再进去。");
        } else if (status == 4) {
            cm.sendNext("我现在就带你进去，击败怪物收集30颗#b#t4031013##k找里面的教官换取证书就可以回去找#b#p1022000##k了。");
            cm.completeQuest(100003);
            cm.startQuest(100004);
            cm.gainItem(4031008, -1);
        } else if (status == 5) {
            cm.warp(108000300, 0);
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}
