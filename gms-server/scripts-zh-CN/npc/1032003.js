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

/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Shane - Ellinia (101000000)
 -- By ---------------------------------------------------------------------------------------------
 Unknown
 -- Version Info -----------------------------------------------------------------------------------
 1.1 - Statement fix [Information]
 1.0 - First Version by Unknown
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;
var check = 0;

function start() {
    if (cm.getLevel() < 25) {
        cm.sendOk("你必须达到更高的等级才能进入耐心森林。");
        cm.dispose();
        check = 1;
    } else {
        cm.sendYesNo("嗨，我是谢恩。我可以让你进入耐心森林，只需要支付一小笔费用。你想用 #b5000#k 金币进入吗？");
    }
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendOk("好的，下次见。");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 1) {
            if (check != 1) {
                if (cm.getPlayer().getMeso() < 5000) {
                    cm.sendOk("抱歉，你好像没有足够的金币!")
                    cm.dispose();
                } else {
                    if (cm.isQuestStarted(2050)) {
                        cm.warp(101000100, 0);
                    } else if (cm.isQuestStarted(2051)) {
                        cm.warp(101000102, 0);
                    } else if (cm.getLevel() >= 25 && cm.getLevel() < 50) {
                        cm.warp(101000100, 0);
                    } else if (cm.getLevel() >= 50) {
                        cm.warp(101000102, 0);
                    }
                    cm.gainMeso(-5000);
                    cm.dispose();
                }
            }
        }
    }
}