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

/* Magician Job Instructor
	Magician 2nd Job Advancement
	Victoria Road : The Forest North of Ellinia (101020000)
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
            if (cm.isQuestCompleted(100007)) {
                cm.sendOk("你真是一个真正的英雄！");
                cm.dispose();
            } else if (cm.isQuestCompleted(100006)) {
                cm.sendNext("好的，我会让你进去！打败里面的怪物，收集30个黑暗弹珠，然后和我里面的一位同事交谈。他会给你#b英雄的证明#k，证明你已经通过了测试。祝你好运。");
                status = 4;
            } else if (cm.isQuestStarted(100006)) {
                cm.sendNext("嗯...这绝对是#b古老的格伦戴尔#k的来信...所以你来到这里是为了接受测试，成为一个魔法师进行第二次职业转职。好吧，我来给你解释一下测试。不要太担心，它并不是那么复杂。");
            } else {
                cm.sendOk("一旦你准备好了，我可以告诉你路线。");
                cm.dispose();
            }
        } else if (status == 1) {
            cm.sendNextPrev("我会送你去一个隐藏的地图。你会看到一些平时不会见到的怪物。它们看起来和普通的怪物一样，但态度完全不同。它们既不会提升你的经验等级，也不会给你提供物品。");
        } else if (status == 2) {
            cm.sendNextPrev("你在打倒那些怪物的时候，将能够获得一种名为#b#t4031013##k的大理石。这是一种由它们邪恶的心灵制成的特殊大理石。收集30个，然后去找我的一个同事谈谈。这就是你通过考验的方法。");
        } else if (status == 3) {
            cm.sendYesNo("一旦你进入，就不能离开，直到完成你的任务。如果你死了，你的经验等级会减少。所以你最好做好准备……那么，你现在想去吗？");
        } else if (status == 4) {
            cm.sendNext("好的，我会让你进去！打败里面的怪物，收集30个黑暗弹珠，然后和我的同事交谈。他会给你#b英雄的证明#k，这是你通过测试的证明。祝你好运。");
            cm.completeQuest(100006);
            cm.startQuest(100007);
            cm.gainItem(4031009, -1);
        } else if (status == 5) {
            cm.warp(108000200, 0);
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}