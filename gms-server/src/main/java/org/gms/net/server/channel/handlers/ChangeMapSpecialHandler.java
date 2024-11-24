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

import org.gms.client.Client;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.server.Trade;
import org.gms.server.Trade.TradeResult;
import org.gms.server.maps.Portal;
import org.gms.util.PacketCreator;

/**
 * 传送点脚本传送玩家到指定地图触发
 */
public final class ChangeMapSpecialHandler extends AbstractPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        p.readByte();
        String startwp = p.readString();
        p.readShort();
        Portal portal = c.getPlayer().getMap().getPortal(startwp);
        if (portal == null || c.getPlayer().portalDelay() > currentServerTime() || c.getPlayer().getBlockedPortals().contains(portal.getScriptName())) {
            c.sendPacket(PacketCreator.enableActions());
            return;
        }
        if (c.getPlayer().isChangingMaps() || c.getPlayer().isBanned()) {
            c.sendPacket(PacketCreator.enableActions());
            return;
        }
        if (c.getPlayer().getTrade() != null) {
            Trade.cancelTrade(c.getPlayer(), TradeResult.UNSUCCESSFUL_ANOTHER_MAP);
        }
        portal.enterPortal(c);
    }
}
