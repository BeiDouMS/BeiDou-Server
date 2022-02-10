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
package net.server.channel.handlers;

import client.Character;
import client.Client;
import client.inventory.InventoryType;
import client.inventory.manipulator.InventoryManipulator;
import constants.id.ItemId;
import constants.id.MapId;
import net.AbstractPacketHandler;
import net.packet.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Trade;
import server.maps.MapleMap;
import server.maps.Portal;
import tools.PacketCreator;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ChangeMapHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChangeMapHandler.class);

    @Override
    public void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();

        if (chr.isChangingMaps() || chr.isBanned()) {
            if (chr.isChangingMaps()) {
                log.warn("Chr {} got stuck when changing maps. Last visited mapids: {}", chr.getName(), chr.getLastVisitedMapids());
            }

            c.sendPacket(PacketCreator.enableActions());
            return;
        }
        if (chr.getTrade() != null) {
            Trade.cancelTrade(chr, Trade.TradeResult.UNSUCCESSFUL_ANOTHER_MAP);
        }
        if (p.available() == 0) { //Cash Shop :)
            if (!chr.getCashShop().isOpened()) {
                c.disconnect(false, false);
                return;
            }
            String[] socket = c.getChannelServer().getIP().split(":");
            chr.getCashShop().open(false);

            chr.setSessionTransitionState();
            try {
                c.sendPacket(PacketCreator.getChannelChange(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1])));
            } catch (UnknownHostException ex) {
                ex.printStackTrace();
            }
        } else {
            if (chr.getCashShop().isOpened()) {
                c.disconnect(false, false);
                return;
            }
            try {
                p.readByte(); // 1 = from dying 0 = regular portals
                int targetid = p.readInt();
                String startwp = p.readString();
                Portal portal = chr.getMap().getPortal(startwp);
                p.readByte();
                boolean wheel = p.readByte() > 0;

                boolean chasing = p.readByte() == 1 && chr.isGM();
                if (chasing) {
                    chr.setChasing(true);
                    chr.setPosition(new Point(p.readInt(), p.readInt()));
                }

                if (targetid != -1) {
                    if (!chr.isAlive()) {
                        MapleMap map = chr.getMap();
                        if (wheel && chr.haveItemWithId(ItemId.WHEEL_OF_FORTUNE, false)) {
                            // thanks lucasziron (lziron) for showing revivePlayer() triggering by Wheel

                            InventoryManipulator.removeById(c, InventoryType.CASH, ItemId.WHEEL_OF_FORTUNE, 1, true, false);
                            chr.sendPacket(PacketCreator.showWheelsLeft(chr.getItemQuantity(ItemId.WHEEL_OF_FORTUNE, false)));

                            chr.updateHp(50);
                            chr.changeMap(map, map.findClosestPlayerSpawnpoint(chr.getPosition()));
                        } else {
                            boolean executeStandardPath = true;
                            if (chr.getEventInstance() != null) {
                                executeStandardPath = chr.getEventInstance().revivePlayer(chr);
                            }
                            if (executeStandardPath) {
                                chr.respawn(map.getReturnMapId());
                            }
                        }
                    } else {
                        if (chr.isGM()) {
                            MapleMap to = chr.getWarpMap(targetid);
                            chr.changeMap(to, to.getPortal(0));
                        } else {
                            final int divi = chr.getMapId() / 100;
                            boolean warp = false;
                            if (divi == 0) {
                                if (targetid == 10000) {
                                    warp = true;
                                }
                            } else if (divi == 20100) {
                                if (targetid == MapId.LITH_HARBOUR) {
                                    c.sendPacket(PacketCreator.lockUI(false));
                                    c.sendPacket(PacketCreator.disableUI(false));
                                    warp = true;
                                }
                            } else if (divi == 9130401) { // Only allow warp if player is already in Intro map, or else = hack
                                if (targetid == MapId.EREVE || targetid / 100 == 9130401) { // Cygnus introduction
                                    warp = true;
                                }
                            } else if (divi == 9140900) { // Aran Introduction
                                if (targetid == MapId.ARAN_TUTO_2 || targetid == MapId.ARAN_TUTO_3 || targetid == MapId.ARAN_TUTO_4 || targetid == MapId.ARAN_INTRO) {
                                    warp = true;
                                }
                            } else if (divi / 10 == 1020) { // Adventurer movie clip Intro
                                if (targetid == 1020000) {
                                    warp = true;
                                }
                            } else if (divi / 10 >= 980040 && divi / 10 <= 980045) {
                                if (targetid == MapId.WITCH_TOWER_ENTRANCE) {
                                    warp = true;
                                }
                            }
                            if (warp) {
                                final MapleMap to = chr.getWarpMap(targetid);
                                chr.changeMap(to, to.getPortal(0));
                            }
                        }
                    }
                }

                if (portal != null && !portal.getPortalStatus()) {
                    c.sendPacket(PacketCreator.blockedMessage(1));
                    c.sendPacket(PacketCreator.enableActions());
                    return;
                }

                if (chr.getMapId() == MapId.FITNESS_EVENT_LAST) {
                    chr.getFitness().resetTimes();
                } else if (chr.getMapId() == MapId.OLA_EVENT_LAST_1 || chr.getMapId() == MapId.OLA_EVENT_LAST_2) {
                    chr.getOla().resetTimes();
                }

                if (portal != null) {
                    if (portal.getPosition().distanceSq(chr.getPosition()) > 400000) {
                        c.sendPacket(PacketCreator.enableActions());
                        return;
                    }

                    portal.enterPortal(c);
                } else {
                    c.sendPacket(PacketCreator.enableActions());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}