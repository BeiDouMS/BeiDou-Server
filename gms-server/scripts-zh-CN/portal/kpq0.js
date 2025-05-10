/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
Kerning PQ: 1st stage to 2nd stage portal
*/

function enter(pi) {
    var eim = pi.getPlayer().getEventInstance();
    var target = eim.getMapInstance(103000801);

    if (eim.getProperty("1stageclear") != null) {
        pi.playPortalSound();
        pi.getPlayer().changeMap(target, target.getPortal("st00"));
        return true;
    } else {
        pi.getPlayer().dropMessage(5, "传送门尚未开启。");
        return false;
    }
}