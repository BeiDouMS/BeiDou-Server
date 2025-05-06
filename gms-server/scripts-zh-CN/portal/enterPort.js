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

function enter(pi) {
    if (pi.isQuestStarted(21301) && pi.getQuestProgressInt(21301, 9001013) == 0) {
        if (pi.getPlayerCount(108010700) != 0) {
            pi.message("传送门被另一侧封锁了，莫非有人在讨伐小偷乌鸦？");
            return false;
        } else {
            var map = pi.getClient().getChannelServer().getMapFactory().getMap(108010700);
            spawnMob(2732, 3, 9001013, map);

            pi.playPortalSound();
            pi.warp(108010700, "west00");
        }
    } else {
        pi.playPortalSound();
        pi.warp(140020300, 1);
    }
    return true;
}

function spawnMob(x, y, id, map) {
    if (map.getMonsterById(id) != null) {
        return;
    }

    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    var mob = LifeFactory.getMonster(id);
    map.spawnMonsterOnGroundBelow(mob, new Point(x, y));
}