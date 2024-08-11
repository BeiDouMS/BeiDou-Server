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
/* Author: PurpleMadness
 * In search for the lost memory - warrior
*/

var status = -1;

function start(mode, type, selection) {
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
            qm.startQuest();
            qm.setQuestProgress(3507, 7081, 1); // thanks resinate for pointing out uncompletable quest due to non-updated progress
            qm.completeQuest();
            qm.sendOk("啊，原来是你。没想到很久之后还能看到你。我很高兴看到曾经是青涩的新手的你成为了出色的战士。看到很久不见但还记得我的你，我的心里充满了温暖。你是在寻找遗忘的记忆吗？想起来那已经是很久很久以前的事了，事隔多年，真是让人怀念啊。这样吧。你再去找#b#p2140001##k吧。相信他会帮助你。那么再见……");
        } else if (status == 1) {
            qm.dispose();
        }
    }
}
