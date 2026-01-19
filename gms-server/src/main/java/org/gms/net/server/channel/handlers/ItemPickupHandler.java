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
import org.gms.client.autoban.AutobanFactory;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.maps.MapObject;

import java.awt.*;

/**
 * @author Matze
 * @author Ronan
 */
public final class ItemPickupHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(ItemPickupHandler.class);

    @Override
    public void handlePacket(final InPacket p, final Client c) {
        p.readInt(); //Timestamp
        p.readByte();
        p.readPos(); //cpos
        int oid = p.readInt();
        Character chr = c.getPlayer();
        MapObject ob = chr.getMap().getMapObject(oid);
        if (ob == null) {
            return;
        }

        Point charPos = chr.getPosition();
        Point obPos = ob.getPosition();
        if (Math.abs(charPos.getX() - obPos.getX()) > 800 || Math.abs(charPos.getY() - obPos.getY()) > 600) {

//            AutobanFactory.DISTANCE_HACK.alert(chr, "玩家" + chr.getName() + "地图ID：" + chr.getMapId() + "距离物品: " + Math.abs(charPos.getX() - obPos.getX()) + " " + Math.abs(charPos.getY() - obPos.getY()));
            AutobanFactory.DISTANCE_HACK.addPoint(chr.getAutoBanManager(), "玩家" + chr.getName() + "地图ID：" + chr.getMapId() + "距离物品: " + Math.abs(charPos.getX() - obPos.getX()) + " " + Math.abs(charPos.getY() - obPos.getY()));
            log.warn("玩家{}地图ID：{}距离物品: {} {}", chr.getName(), chr.getMapId(), Math.abs(charPos.getX() - obPos.getX()), Math.abs(charPos.getY() - obPos.getY()));
            return;
        }

        chr.pickupItem(ob);
    }
}
