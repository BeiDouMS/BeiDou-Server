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
import org.gms.client.FamilyEntitlement;
import org.gms.client.FamilyEntry;
import org.gms.config.YamlConfig;
import org.gms.constants.id.MapId;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.coordinator.world.InviteCoordinator;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteType;
import org.gms.server.maps.FieldLimit;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;

/**
 * @author Moogra
 * @author Ubaware
 */
public final class FamilyUseHandler extends AbstractPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        if (!YamlConfig.config.server.USE_FAMILY_SYSTEM) {
            return;
        }
        FamilyEntitlement type = FamilyEntitlement.values()[p.readInt()];
        int cost = type.getRepCost();
        FamilyEntry entry = c.getPlayer().getFamilyEntry();
        if (entry.getReputation() < cost || entry.isEntitlementUsed(type)) {
            return; // shouldn't even be able to request it
        }
        c.sendPacket(PacketCreator.getFamilyInfo(entry));
        Character victim;
        if (type == FamilyEntitlement.FAMILY_REUINION || type == FamilyEntitlement.SUMMON_FAMILY) {
            victim = c.getChannelServer().getPlayerStorage().getCharacterByName(p.readString());
            if (victim != null && victim != c.getPlayer()) {
                if (victim.getFamily() == c.getPlayer().getFamily()) {
                    MapleMap targetMap = victim.getMap();
                    MapleMap ownMap = c.getPlayer().getMap();
                    if (targetMap != null) {
                        if (type == FamilyEntitlement.FAMILY_REUINION) {
                            if (!FieldLimit.CANNOTMIGRATE.check(ownMap.getFieldLimit()) && !FieldLimit.CANNOTVIPROCK.check(targetMap.getFieldLimit())
                                    && (targetMap.getForcedReturnId() == MapId.NONE || MapId.isMapleIsland(targetMap.getId())) && targetMap.getEventInstance() == null) {

                                c.getPlayer().changeMap(victim.getMap(), victim.getMap().getPortal(0));
                                useEntitlement(entry, type);
                            } else {
                                c.sendPacket(PacketCreator.sendFamilyMessage(75, 0)); // wrong message, but close enough. (client should check this first anyway)
                                return;
                            }
                        } else {
                            if (!FieldLimit.CANNOTMIGRATE.check(targetMap.getFieldLimit()) && !FieldLimit.CANNOTVIPROCK.check(ownMap.getFieldLimit())
                                    && (ownMap.getForcedReturnId() == MapId.NONE || MapId.isMapleIsland(ownMap.getId())) && ownMap.getEventInstance() == null) {

                                if (InviteCoordinator.hasInvite(InviteType.FAMILY_SUMMON, victim.getId())) {
                                    c.sendPacket(PacketCreator.sendFamilyMessage(74, 0));
                                    return;
                                }
                                InviteCoordinator.createInvite(InviteType.FAMILY_SUMMON, c.getPlayer(), victim, victim.getId(), c.getPlayer().getMap());
                                victim.sendPacket(PacketCreator.sendFamilySummonRequest(c.getPlayer().getFamily().getName(), c.getPlayer().getName()));
                                useEntitlement(entry, type);
                            } else {
                                c.sendPacket(PacketCreator.sendFamilyMessage(75, 0));
                                return;
                            }
                        }
                    }
                } else {
                    c.sendPacket(PacketCreator.sendFamilyMessage(67, 0));
                }
            }
        } else if (type == FamilyEntitlement.FAMILY_BONDING) {
            c.getPlayer().message("暂时关闭");
            //not implemented
        } else {
            if (c.getPlayer().isFamilybuff()) {
                c.getPlayer().message("你已经有BUFF");
                return;
            }
            float rate = 1.5f;
            int duration = 15;

            do {
                switch (type) {
                    case PARTY_EXP_2_30MIN:
                        c.getPlayer().message("暂时关闭");
                        break;

                    case PARTY_DROP_2_30MIN:
                        c.getPlayer().message("暂时关闭");
                        break;

                    case SELF_DROP_2_30MIN:
                        duration = 30;
                        rate = 2;
                        c.sendPacket(PacketCreator.familyBuff(3, 7, 1, duration  * 60000));
                        c.getPlayer().setFamilybuff(true,rate,1);
                        c.getPlayer().startFamilyBuffTimer(duration  * 60000);
                        useEntitlement(entry,FamilyEntitlement.SELF_DROP_2_30MIN);
                        break;

                    case SELF_DROP_2:
                        duration = 15;
                        rate = 2;
                        c.sendPacket(PacketCreator.familyBuff(3, 5, 1, duration  * 60000));
                        c.getPlayer().setFamilybuff(true,1,rate);
                        c.getPlayer().startFamilyBuffTimer(duration  * 60000);
                        useEntitlement(entry,FamilyEntitlement.SELF_DROP_2);
                        break;

                    case SELF_DROP_1_5:
                        duration = 15;
                        rate = 1.5f;
                        c.sendPacket(PacketCreator.familyBuff(3, 2, 1,duration  * 60000));
                        c.getPlayer().setFamilybuff(true,1,rate);
                        c.getPlayer().startFamilyBuffTimer(duration  * 60000);
                        useEntitlement(entry,FamilyEntitlement.SELF_DROP_1_5);
                        break;

                    case SELF_EXP_2_30MIN:
                        duration = 1;
                        rate = 2;
                        c.sendPacket(PacketCreator.familyBuff(2, 8, 1, duration * 60000));
                        c.getPlayer().setFamilybuff(true,rate,1);
                        c.getPlayer().startFamilyBuffTimer(duration  * 60000);
                        useEntitlement(entry,FamilyEntitlement.SELF_EXP_2_30MIN);
                        break;

                    case SELF_EXP_2:
                        duration = 15;
                        rate = 2;
                        c.sendPacket(PacketCreator.familyBuff(2, 6, 1, duration * 60000));
                        c.getPlayer().setFamilybuff(true,rate,1);
                        c.getPlayer().startFamilyBuffTimer(duration  * 60000);
                        useEntitlement(entry,FamilyEntitlement.SELF_EXP_2);
                        break;

                    case SELF_EXP_1_5:
                        duration = 15;
                        rate = 1.5f;
                        c.sendPacket(PacketCreator.familyBuff(2, 3, 1, duration * 60000));
                        c.getPlayer().setFamilybuff(true,rate,1);
                        c.getPlayer().startFamilyBuffTimer(duration  * 60000);
                        useEntitlement(entry,FamilyEntitlement.SELF_EXP_1_5);
                        break;

                    default:
                        // 默认处理逻辑
                        break;
                }
                break;
            } while (true);
            //not implemented
        }
    }

    private boolean useEntitlement(FamilyEntry entry, FamilyEntitlement entitlement) {
        if (entry.useEntitlement(entitlement)) {
            entry.gainReputation(-entitlement.getRepCost(), false);
            entry.getChr().sendPacket(PacketCreator.getFamilyInfo(entry));
            return true;
        }
        return false;
    }
}
