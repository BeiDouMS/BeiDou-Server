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

import org.gms.client.*;
import org.gms.client.Character;
import org.gms.config.GameConfig;
import org.gms.constants.id.MapId;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.coordinator.world.InviteCoordinator;
import org.gms.net.server.coordinator.world.InviteCoordinator.InviteType;
import org.gms.net.server.world.PartyCharacter;
import org.gms.server.maps.FieldLimit;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;

import java.util.Objects;

/**
 * @author Moogra
 * @author Ubaware
 */
public final class FamilyUseHandler extends AbstractPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        if (!GameConfig.getServerBoolean("use_family_system")) {
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
                            }
                        }
                    }
                } else {
                    c.sendPacket(PacketCreator.sendFamilyMessage(67, 0));
                }
            }

        } else {
            if (c.getPlayer().isFamilyBuff()) {
                c.getPlayer().message("你已经有BUFF");
                return;
            }

            switch (type) {
                case PARTY_EXP_2_30MIN:
                    applyPartyBuff(c, 2, 10, 30, FamilyEntitlement.PARTY_EXP_2_30MIN);
                    break;
                case PARTY_DROP_2_30MIN:
                    applyPartyBuff(c, 3, 9, 30, FamilyEntitlement.PARTY_DROP_2_30MIN);
                    break;
                case SELF_DROP_2_30MIN:
                    applySelfBuff(c, 3, 7, 30, 2, FamilyEntitlement.SELF_DROP_2_30MIN);
                    break;
                case SELF_DROP_2:
                    applySelfBuff(c, 3, 5, 15, 2, FamilyEntitlement.SELF_DROP_2);
                    break;
                case SELF_DROP_1_5:
                    applySelfBuff(c, 3, 2, 15, 1.5f, FamilyEntitlement.SELF_DROP_1_5);
                    break;
                case SELF_EXP_2_30MIN:
                    applySelfBuff(c, 2, 8, 30, 2, FamilyEntitlement.SELF_EXP_2_30MIN);
                    break;
                case SELF_EXP_2:
                    applySelfBuff(c, 2, 6, 15, 2, FamilyEntitlement.SELF_EXP_2);
                    break;
                case SELF_EXP_1_5:
                    applySelfBuff(c, 2, 3, 15, 1.5f, FamilyEntitlement.SELF_EXP_1_5);
                    break;
                case FAMILY_BONDING:
                    if (useEntitlement(entry, FamilyEntitlement.FAMILY_BONDING)) {
                        Family family = c.getPlayer().getFamily();
                        family.Familybuff(30);
                    }
                    break;
                default:
                    // Handle unknown entitlement type
                    break;
            }


        }
    }

    /**
     * 警告：此处只针对buff相关的做了处理，一日只一次
     * 非buff相关的如传送到别人位置或将别人传送到自己位置都没有限制一日一次，因为从2019年开始这俩就没限
     *
     * @param entry 学院实体类
     * @param entitlement 功能类型
     * @return 是否使用成功
     */
    private boolean useEntitlement(FamilyEntry entry, FamilyEntitlement entitlement) {
        if (entry.useEntitlement(entitlement)) {
            entry.gainReputation(-entitlement.getRepCost(), false);
            entry.getChr().sendPacket(PacketCreator.getFamilyInfo(entry));
            return true;
        }
        return false;
    }

    private void applyPartyBuff(Client c, int type, int effect, int duration, FamilyEntitlement entitlement) {
        Character player = c.getPlayer();
        FamilyEntry familyEntry = player.getFamilyEntry();
        if (player.getParty() != null) {
            // 只扣减使用者次数
            if (!useEntitlement(familyEntry, entitlement)) {
                return;
            }
            for (PartyCharacter mpc : player.getParty().getMembers()) {
                FamilyEntry mpcEntry = mpc.getPlayer().getFamilyEntry();
                // 没有学院的不享受
                if (mpcEntry == null) {
                    continue;
                }
                // 不是同学的不享受
                if (!Objects.equals(mpcEntry.getFamily().getID(), familyEntry.getFamily().getID())) {
                    continue;
                }
                mpc.getPlayer().sendPacket(PacketCreator.familyBuff(type, effect, 1, duration * 60000));
                if (type == 2) {
                    mpc.getPlayer().setFamilyBuff(true, 2, 1);
                } else {
                    mpc.getPlayer().setFamilyBuff(true, 1, 2);
                }
                mpc.getPlayer().startFamilyBuffTimer(duration * 60000);
                // 不扣减其他人次数
//                useEntitlement(mpcEntry, entitlement);
            }
        }
    }

    private void applySelfBuff(Client c, int type, int effect, int duration, float rate, FamilyEntitlement entitlement) {
        if (!useEntitlement(c.getPlayer().getFamilyEntry(), entitlement)) {
            return;
        }
        c.sendPacket(PacketCreator.familyBuff(type, effect, 1, duration * 60000));
        if (type == 2) {
            c.getPlayer().setFamilyBuff(true, rate, 1);
        } else {
            c.getPlayer().setFamilyBuff(true, 1, rate);
        }
        c.getPlayer().startFamilyBuffTimer(duration * 60000);
    }
}
