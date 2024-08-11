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
	NPC Name: 		Hawkeye
	Map(s): 		Empress' Road : Ereve (130000000)
	Description: 		Quest - Knighthood Exam: Thunder Breaker
*/
var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            qm.sendNext("我猜你还没准备好?");
            qm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            qm.sendYesNo("所以，你准备好二转了?");
        } else if (status == 1) {
            if (qm.getPlayer().getJob().getId() == 1500 && qm.getPlayer().getRemainingSp() > ((qm.getPlayer().getLevel() - 30) * 3)) {
                qm.sendNext("你还有技能点没有使用完，所以你还不能成为正式的骑士！在一转技能上使用更多的SP.");
                qm.dispose();
            } else {
                if (qm.getPlayer().getJob().getId() != 1510) {
					if (!qm.canHold(1142067)) {
						qm.sendNext("请确认装备栏是否足够.");
						qm.dispose();
						return;
					}
                    qm.gainItem(4032100, -30);
                    qm.gainItem(1142067, 1);
                    const Job = Java.type('org.gms.client.Job');
                    qm.getPlayer().changeJob(Job.THUNDERBREAKER2);
                    qm.completeQuest();
                }
                qm.sendNext("训练已经结束。你现在皇家骑士团的骑士官员.");
            }
        } else if (status == 2) {
            qm.sendNextPrev("我也给了你一些 #b技能点#k 和霹雳的辅助技能，只有正式的骑士才能使用。这些技能是基于闪电的，所以要明智地使用它们!");
        } else if (status == 3) {
            qm.sendPrev("好吧，就我个人而言，我希望你在成为天鹅骑士后也不要失去热情。即使你在一大堆负面的东西中，也要寻找积极的一面.");
        } else if (status == 4) {
            qm.dispose();
        }
    }
}