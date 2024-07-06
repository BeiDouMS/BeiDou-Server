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
/* Author: Xterminator
	NPC Name: 		Rain
	Map(s): 		Maple Road : Amherst (1010000)
	Description: 		Talks about Amherst
*/
var status = -1;

function start() {
    cm.sendNext("这是位于枫之岛东北部的名为#bAmherst#k的小镇。你知道枫之岛是给新手玩家的，对吧？我很高兴这个地方周围只有一些弱小的怪物。");
}

function action(mode, type, selection) {
    status++;
    if (mode != 1) {
        if (mode == 0 && status == 2) {
            status -= 2;
            start();
        } else if (mode == 0) {
            status -= 2;
        } else {
            cm.dispose();
        }
    } else {
        if (status == 1) {
            cm.sendNextPrev("如果你想变得更强大，那就去#b南港#k，那里有一个港口。乘坐巨大的船只前往名为#b维多利亚岛#k的地方。与这个小岛相比，它的规模是无与伦比的。");
        } else if (status == 2) {
            cm.sendPrev("At the Victoria Island, you can choose your job. Is it called #bPerion#k...? I heard there's a bare, desolate town where warriors live. A highland...what kind of a place would that be?");
        } else if (status == 3) {
            cm.dispose();
        }
    }
}