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
/* Author: Xterminator, Moogra
	NPC Name: 		Trainer Bartos
	Map(s): 		Victoria Road : Pet-Walking Road (100000202)
	Description: 		Pet Trainer
*/
var status = 0;

function start() {
    cm.sendSimple("你有什么事情找我吗？\r\n#L0##b请告诉我关于这个地方的情况。#l\r\n#L1#我是因为仙子玛尔的话才来这里的…#k#l");
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (mode == 0) {
        cm.sendNext("嗯...现在太忙了？如果你想做的话，回来找我吧。");
        cm.dispose();
    } else {
        status++;
        if (status == 1) {
            if (selection == 0) {
                if (cm.haveItem(4031035)) {
                    cm.sendNext("拿到那封信，和你的宠物一起跳过障碍物，把那封信带给我的兄弟弗罗德训练师。把信交给他，你的宠物会有好事发生。");
                    cm.dispose();
                } else {
                    cm.sendYesNo("这是你可以带宠物散步的路。你可以和它一起四处走动，或者训练它通过这里的障碍。如果你和宠物还不够亲近，可能会出现问题，它就不会像你想的那样听从你的命令。那么，你觉得呢？想要训练你的宠物吗？");
                }
            } else {
                cm.sendOk("嘿，你确定你见过#b仙灵玛尔#k吗？如果你以前从未见过她，不要对我撒谎，因为很明显。那甚至不是一个好谎言！");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.gainItem(4031035, 1);
            cm.sendNext("好的，这是信。如果你直接去那里，他不会知道我派你去的，所以带着你的宠物通过障碍，到达顶部，然后和弗罗德训练师交谈，把信交给他。如果你在通过障碍时注意你的宠物，这并不难。祝你好运！");
            cm.dispose();
        }
    }
}