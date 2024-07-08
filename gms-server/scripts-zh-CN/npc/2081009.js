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
//Moose, Warps to exit

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode <= 0) {
        cm.dispose();
        return;
    }

    status++;
    if (status == 0) {
        if (cm.isQuestStarted(6180) && cm.getQuestProgressInt(6180, 9300096) < 200) {
            cm.sendYesNo("请注意：在你待在训练场内的时候，确保你已经装备了#t1092041#，这非常重要。你准备好去训练区了吗？");
        } else {
            cm.sendOk("只有指定人员才能进入训练场地。");
            cm.dispose();
        }
    } else if (status == 1) {
        if (cm.getPlayer().haveItemEquipped(1092041)) {
            cm.sendNext("直到任务结束前都要装备好你的盾牌，否则你就得重新开始了！");
        } else {
            cm.sendOk("请在进入训练场之前装备#r#t1092041##k。");
            cm.dispose();
        }
    } else {
        cm.warp(924000001, 0);
        cm.dispose();
    }
}