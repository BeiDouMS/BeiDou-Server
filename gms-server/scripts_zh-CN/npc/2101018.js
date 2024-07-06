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
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
	NPC NAME: Cesar (1)
	NPC ID: 2101018
	Author: Vcoc
	Function: AriantPQ
*/

status = -1;

function start() {
    if ((cm.getPlayer().getLevel() < 19 || cm.getPlayer().getLevel() > 30) && !cm.getPlayer().isGM()) {
        cm.sendNext("你的等级不在20级到30级之间。抱歉，你不能参加。");
        cm.dispose();
        return;
    }
    action(1, 0, 0);
}

function action(mode, type, selection) {
    status++;
    if (status == 4) {
        cm.getPlayer().saveLocation("MIRROR");
        cm.warp(980010000, 3);
        cm.dispose();
    }
    if (mode != 1) {
        if (mode == 0 && type == 0) {
            status -= 2;
        } else {
            cm.dispose();
            return;
        }
    }
    if (status == 0) {
        cm.sendNext("我已经在阿里安特为冒险岛的伟大战士们准备了一个盛大的节日。它被称为#b阿里安特角斗场挑战#k。");
    } else if (status == 1) {
        cm.sendNextPrev("阿里安特角斗场挑战是一项比赛，将怪物战斗技能与其他人进行比拼。在这项比赛中，你的目标不是猎杀怪物；相反，你需要从怪物身上消耗一定量的HP，然后用宝石吸收它。最终拥有最多宝石的战士将赢得比赛。");
    } else if (status == 2) {
        cm.sendSimple("如果你是来自#b佩里安#k，在巴尔洛克舞者指导下训练的强大勇士，那么你对参加阿里安竞技场挑战感兴趣吗？！\r\n#b#L0# 我很想参加这场盛大的比赛。#l");
    } else if (status == 3) {
        cm.sendNext("好的，现在我会把你送到战斗竞技场。我希望看到你取得胜利！");
    }
}