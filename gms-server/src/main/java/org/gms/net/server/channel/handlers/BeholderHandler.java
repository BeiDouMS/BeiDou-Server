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
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.constants.skills.DarkKnight;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.server.StatEffect;
import org.gms.server.maps.Summon;
import org.gms.util.PacketCreator;

import java.util.Collection;

/**
 * @author BubblesDev
 */
public final class BeholderHandler extends AbstractPacketHandler {//Summon Skills noobs

    @Override
    public final void handlePacket(InPacket p, Client c) {
        //System.out.println(slea.toString());
        Character chr = c.getPlayer();
        Collection<Summon> summons = c.getPlayer().getSummonsValues();
        int oid = p.readInt();
        Summon summon = null;
        for (Summon sum : summons) {
            if (sum.getObjectId() == oid) {
                summon = sum;
            }
        }
        if (summon != null) {
            int skillId = p.readInt();
            int chrSkillLevel = chr.getSkillLevel(skillId);
            if (chrSkillLevel <= 0) {
                return;
            }
            Skill skill = SkillFactory.getSkill(skillId);
            if (skill == null) {
                return;
            }
            StatEffect skillEffect = skill.getEffect(chrSkillLevel);
            skillEffect.applyTo(chr);
            byte stance = p.readByte();
            
            chr.getMap().broadcastMessage(PacketCreator.summonSkillEffect(chr.getId(), oid, stance));
            chr.sendPacket(PacketCreator.showOwnBuffEffect(summon.getSkill(), 2));
            chr.getMap().broadcastMessage(chr, PacketCreator.showBuffEffect(chr.getId(), summon.getSkill(), 2), false);
        } else {
            chr.clearSummons();
        }
    }
}
