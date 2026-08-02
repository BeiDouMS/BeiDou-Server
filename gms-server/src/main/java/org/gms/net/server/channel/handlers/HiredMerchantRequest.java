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
import org.gms.client.inventory.ItemFactory;
import org.gms.constants.game.GameConstants;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.server.maps.HiredMerchant;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.maps.PlayerShop;
import org.gms.server.maps.Portal;
import org.gms.util.PacketCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author XoticStory
 */
public final class HiredMerchantRequest extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(HiredMerchantRequest.class);

    @Override
    public final void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();

        try {
            for (MapObject mmo : chr.getMap().getMapObjectsInRange(chr.getPosition(), 23000, Arrays.asList(MapObjectType.HIRED_MERCHANT, MapObjectType.PLAYER))) {
                if (mmo instanceof Character mc) {

                    PlayerShop shop = mc.getPlayerShop();
                    if (shop != null && shop.isOwner(mc)) {
                        chr.sendPacket(PacketCreator.getMiniRoomError(13));
                        return;
                    }
                } else {
                    chr.sendPacket(PacketCreator.getMiniRoomError(13));
                    return;
                }
            }

            Point cpos = chr.getPosition();
            Portal portal = chr.getMap().findClosestTeleportPortal(cpos);
            if (portal != null && portal.getPosition().distance(cpos) < 120.0) {
                chr.sendPacket(PacketCreator.getMiniRoomError(10));
                return;
            }
        } catch (Exception e) {
            log.warn("Failed to validate HiredMerchant placement for character {}", chr.getName(), e);
        }

        if (!GameConstants.isFreeMarketRoom(chr.getMapId())) {
            chr.dropMessage(1, "You cannot open your hired merchant here.");
            return;
        }

        HiredMerchant existing = c.getWorldServer().getHiredMerchant(chr.getId());
        if (existing != null) {
            if (existing.isPublished()) {
                chr.dropMessage(1, "You already have a store open on channel " + existing.getChannel() + ".");
                return;
            }

            log.warn("Cleaning up unpublished HiredMerchant for character {}, original channel {}, still bound {}",
                    chr.getName(), existing.getChannel(), chr.getHiredMerchant() == existing);
            try {
                chr.setHiredMerchant(existing);
                existing.forceClose();
            } catch (RuntimeException ex) {
                log.error("Failed to clean up unpublished HiredMerchant for character {}, original channel {}",
                        chr.getName(), existing.getChannel(), ex);
                chr.dropMessage(1, "Your previous unfinished store could not be cleaned up. Please contact an administrator.");
                chr.sendPacket(PacketCreator.enableActions());
                return;
            }

            try {
                if (!ItemFactory.MERCHANT.loadItems(chr.getId(), false).isEmpty() || chr.getMerchantMeso() != 0) {
                    chr.dropMessage(5, "Your unfinished store was closed. Please retrieve its items from Fredrick.");
                    chr.sendPacket(PacketCreator.retrieveFirstMessage());
                } else {
                    chr.dropMessage(1, "Your unfinished store was closed and its items were returned. Please set it up again.");
                    chr.sendPacket(PacketCreator.enableActions());
                }
            } catch (SQLException ex) {
                log.error("Failed to verify cleanup result for character {}", chr.getName(), ex);
                chr.dropMessage(1, "Your unfinished store was closed, but its item state could not be verified.");
                chr.sendPacket(PacketCreator.enableActions());
            }
            return;
        }

        if (chr.hasMerchant()) {
            chr.dropMessage(1, "Your hired merchant state has not been cleaned up. Please contact an administrator.");
            return;
        }

        try {
            if (ItemFactory.MERCHANT.loadItems(chr.getId(), false).isEmpty() && chr.getMerchantMeso() == 0) {
                c.sendPacket(PacketCreator.hiredMerchantBox());
            } else {
                chr.sendPacket(PacketCreator.retrieveFirstMessage());
            }
        } catch (SQLException ex) {
            log.error("Failed to load HiredMerchant items for character {}", chr.getName(), ex);
        }
    }
}
