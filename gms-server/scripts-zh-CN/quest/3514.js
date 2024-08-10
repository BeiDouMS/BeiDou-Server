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
 * The sorcerer who sells emotions
*/

var status = -1;

function start(mode, type, selection) {
    if (qm.getPlayer().getMeso() >= 1000000) {
        if (qm.canHold(2022337, 1)) {
            qm.gainItem(2022337, 1);
            qm.gainMeso(-1000000);

            //qm.sendOk("Nice doing business with you~~.");
            qm.startQuest(3514);
        } else {
            qm.sendOk("保你的消耗栏有空位.");
        }
    } else {
        qm.sendOk("喂，你没有足够的钱。我出售情感药剂需要 #r1,000,000金币#k。没钱就算了.");
    }

    qm.dispose();
}

function usedPotion(ch) {
    const BuffStat = Java.type('org.gms.client.BuffStat');
    return ch.getBuffSource(BuffStat.HPREC) == 2022337;
}

function end(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode == -1) {
        qm.dispose();
        return;
    } else {
        status++;
    }

    if (status == 0) {
        if (!usedPotion(qm.getPlayer())) {
            if (qm.haveItem(2022337)) {
                qm.sendOk("你害怕喝这个药剂吗？我可以向你保证它只有轻微的 #r副作用#k.");
            } else {
                if (qm.canHold(2022337)) {
                    qm.gainItem(2022337, 1);
                    qm.sendOk("丢了吗？幸运的是我成功找回了。拿去吧.");
                } else {
                    qm.sendOk("丢了吗？幸运的是我成功找回了。请腾出空间来获取它.");
                }
            }

            qm.dispose();

        } else {
            qm.sendOk("你的情绪不再冻结了。哦，天啊... 你身体状况不佳，#b快速排毒#k.");
        }
    } else if (status == 1) {
        qm.gainExp(891500);
        qm.completeQuest(3514);
        qm.dispose();
    }
}
