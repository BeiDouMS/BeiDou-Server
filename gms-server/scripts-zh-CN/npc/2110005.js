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
/*
-- Odin JavaScript --------------------------------------------------------------------------------
    Camel Cab - Magatia (GMS Like)
-- Version Info -----------------------------------------------------------------------------------
    1.3 - Actually fixed by Alan (SharpAceX)
    1.2 - Fixed and recoded by Moogra
    1.1 - Shortened by Moogra
    1.0 - First Version by Maple4U - who stepped up and coded first version of several Magatia NPCs
---------------------------------------------------------------------------------------------------
*/

var toMagatia = "要搭乘#b骆驼出租车#k前往炼金术之城#b玛加提亚#k吗？车费是#b1500金币#k。";
var toAriant = "要搭乘#b骆驼出租车#k前往灼热之路的城镇#b阿里安特#k吗？车费是#b1500金币#k。";

function start() {
    cm.sendYesNo(cm.getPlayer().getMapId() == 260020000 ? toMagatia : toAriant);
}

function action(mode, type, selection) {
    if (mode == 1) {
        if (cm.getMeso() < 1500) {
            cm.sendNext("对不起，你的金币不够。没有足够的车费，就不能搭乘骆驼出租车。请准备好金币后再来吧。");
            cm.dispose();
        } else {
            cm.warp(cm.getPlayer().getMapId() == 260020000 ? 261000000 : 260000000, 0);
            cm.gainMeso(-1500);
            cm.dispose();
        }
    } else if (mode == 0) {
        cm.sendNext("嗯……现在很忙吗？如果想搭车，随时回来找我吧。");
    }
    cm.dispose();
}
