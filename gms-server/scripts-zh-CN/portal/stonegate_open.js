/* 
 * This file is part of the OdinMS Maple Story Server
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
    Stage 2: Key door - Guild Quest
    @Author Lerk
*/
function enter(pi) {
    if (pi.getPlayer().getMap().getReactorByName("stonegate").getState() == 1) {
        pi.playPortalSound();
        pi.warp(990000430, 0);
        return true;
    } else {
        pi.getPlayer().dropMessage(5, "入口仍处于封锁状态");
        return false;
    }
}