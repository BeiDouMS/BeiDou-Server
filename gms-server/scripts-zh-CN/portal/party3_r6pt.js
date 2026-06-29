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

    移动
*/
/**
 *@author Ronan
 *party3_r4pt

 * @update dwang
 * @desc:use teleport replace warp
 * @ 20260629
 */

function enter(pi) {
    var eim = pi.getEventInstance();
    if (eim.getProperty("stage6_comb") == null) {
        var comb = "0";

        for (var i = 0; i < 16; i++) {
            var r = Math.floor((Math.random() * 4)) + 1;
            comb += r.toString();
        }

        eim.setProperty("stage6_comb", comb);
    }

    var comb = eim.getProperty("stage6_comb");

    var name = pi.getPortal().getName().substring(2, 5); //获取名字 eg:rp013
    var portalId = parseInt(name, 10);


    var pRow = Math.floor(portalId / 10);
    var pCol = portalId % 10;

    if (pCol == parseInt(comb.substring(pRow, pRow + 1), 10)) {    //climb
        pi.playPortalSound();
        pi.goTeleport(false, pRow + 5);
        return false;

    } else {    //fail
        pRow--;
        pi.playPortalSound();
        var level =  (pRow / 4) > 1 ? parseInt(pRow / 4) : 0;
        // 最下面一层去22
        if (level == 0) {
           pi.goTeleport(false, 22);
        } else {
            // 9 13 17
            pi.goTeleport(false,  9 + (level - 1)*4);
        }
        return false;

    }

    return true;
}