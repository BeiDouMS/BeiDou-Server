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
package org.gms.server.maps;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.util.PacketCreator;

import lombok.Getter;

import java.awt.*;

/**
 * @author Jan
 */
public class Summon extends AbstractAnimatedMapObject {
    private final Character owner;
    private final byte skillLevel;
    private final int skill;
    private int hp;
    private final SummonMovementType movementType;
    @Getter 
    private final SummonAssistantType assistantType;

    public Summon(Character owner, int skill, Point pos, SummonMovementType movementType) {
        this.owner = owner;
        this.skill = skill;
        Skill skillData = SkillFactory.getSkill(skill);
        this.skillLevel = owner.getSkillLevel(skillData);
        if (skillLevel == 0) {
            throw new RuntimeException();
        }

        this.movementType = movementType;
        this.assistantType = SummonAssistantType.getFromSummonData(skillData.getSummonNode());
        setPosition(pos);
    }

    @Override
    public void sendSpawnData(Client client) {
        client.sendPacket(PacketCreator.spawnSummon(this, false));
    }

    @Override
    public void sendDestroyData(Client client) {
        client.sendPacket(PacketCreator.removeSummon(this, true));
    }

    public Character getOwner() {
        return owner;
    }

    public int getSkill() {
        return skill;
    }

    public int getHP() {
        return hp;
    }

    public void addHP(int delta) {
        this.hp += delta;
    }

    public SummonMovementType getMovementType() {
        return movementType;
    }

    public boolean isStationary() {
        return movementType == SummonMovementType.STATIONARY;
    }

    public byte getSkillLevel() {
        return skillLevel;
    }

    @Override
    public MapObjectType getType() {
        return MapObjectType.SUMMON;
    }

    public final boolean isPuppet() {
        return isPuppet(skill);
    }

    public static boolean isPuppet(int skill) {
        switch (skill) {
            case 3111002:
            case 3211002:
            case 13111004:
                return true;
        }
        return false;
    }
}
