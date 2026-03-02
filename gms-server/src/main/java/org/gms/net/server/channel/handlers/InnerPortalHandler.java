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
package org.gms.net.server.channel.handlers;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.server.maps.Portal;

/**
 * @author BubblesDev
 */
public final class InnerPortalHandler extends AbstractPacketHandler {
    // 玩家需要处在内传送门附近，才认为这是一次有效“树洞/内传送”触发
    private static final double INNER_PORTAL_TRIGGER_DISTANCE_SQ = 90000.0; // 约 300 像素

    @Override
    public final void handlePacket(InPacket p, Client c) {
        Character player = c.getPlayer();
        if (player == null || player.getMap() == null) {
            return;
        }

        // 防滥用：只有靠近真实内传送门时才打“瞬移”标记，避免任意发包获得距离豁免
        Portal nearestTeleportPortal = player.getMap().findClosestTeleportPortal(player.getPosition());
        if (nearestTeleportPortal == null) {
            return;
        }

        if (nearestTeleportPortal.getPosition().distanceSq(player.getPosition()) <= INNER_PORTAL_TRIGGER_DISTANCE_SQ) {
            player.markTeleportLikeMove();
        }
    }
}
