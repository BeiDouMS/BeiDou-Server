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
import org.gms.net.packet.InPacket;
import org.gms.net.packet.Packet;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;
import org.gms.exception.EmptyMovementException;

public final class MovePlayerHandler extends AbstractMovementPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        Character mover = c.getPlayer();
        int fieldKey = p.readUnsignedByte();
        c.applyFieldMovement(mover, fieldKey, () -> move(p, mover));
    }

    private void move(InPacket p, Character mover) {
        p.skip(8);
        try {   // thanks Sa for noticing empty movement sequences crashing players
            int movementDataStart = p.getPosition();
            updatePosition(p, mover, 0);
            long movementDataLength = p.getPosition() - movementDataStart; //how many bytes were read by updatePosition
            p.seek(movementDataStart);

            MapleMap map = mover.getMap();
            map.movePlayer(mover, mover.getPosition());
            Packet movement = PacketCreator.movePlayer(mover.getId(), p, movementDataLength);
            if (mover.isHidden()) {
                map.broadcastGMMessage(mover, movement, false);
            } else {
                map.broadcastMessage(mover, movement, false);
            }
        } catch (EmptyMovementException e) {
        }
    }
}
