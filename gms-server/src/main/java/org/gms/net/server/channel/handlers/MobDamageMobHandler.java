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
import org.gms.client.status.MonsterStatus;
import org.gms.client.status.MonsterStatusEffect;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.life.Monster;
import org.gms.server.life.MonsterInformationProvider;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;

import java.util.Map;
import java.util.Optional;

/**
 * @author Jay Estrella
 * @author Ronan
 */
public final class MobDamageMobHandler extends AbstractPacketHandler {
    private static final Logger log = LoggerFactory.getLogger(MobDamageMobHandler.class);

    @Override
    public void handlePacket(InPacket p, Client c) {
        int from = p.readInt();
        p.readInt();
        int to = p.readInt();
        boolean magic = p.readByte() == 0;
        int dmg = p.readInt();
        Character chr = c.getPlayer();

        MapleMap map = chr.getMap();
        Monster attacker = map.getMonsterByOid(from);
        Monster damaged = map.getMonsterByOid(to);

        if (attacker != null && damaged != null) {
            Character damageChr = null;
            // 这里不能单从controller判断，因为仇恨可以被吸引转移
            if (attacker.getController() != null) {
                MonsterStatusEffect hadAttHypnotized = attacker.getStati(MonsterStatus.INERTMOB);
                if (hadAttHypnotized != null) {
                    // 心灵控制的怪物攻击其他怪物，伤害算作控制者的伤害
                    damageChr = attacker.getController();
                } else if (damaged.getController() != null) {
                    MonsterStatusEffect hadDamHypnotized = damaged.getStati(MonsterStatus.INERTMOB);
                    if (hadDamHypnotized != null) {
                        // 心灵控制的怪物被其他怪物攻击，伤害算作被控制者的伤害
                        damageChr = damaged.getController();
                    }
                }
            } else if (damaged.getController() != null) {
                MonsterStatusEffect hadDamHypnotized = damaged.getStati(MonsterStatus.INERTMOB);
                if (hadDamHypnotized != null) {
                    // 心灵控制的怪物被其他怪物攻击，伤害算作被控制者的伤害
                    damageChr = damaged.getController();
                }
            }
            if (damageChr == null) {
                // 2个怪物都没有心灵控制，互殴？
                log.warn("A mob from controller {} attacked another mob from controller {} without any hypnotized monsters.",
                        Optional.ofNullable(attacker.getController()).map(ac -> String.valueOf(ac.getId())).orElse("null"),
                        Optional.ofNullable(damaged.getController()).map(dc -> String.valueOf(dc.getId())).orElse("null"));
                // 此次攻击无效
                return;
            }

            int maxDmg = calcMaxDamage(attacker, damaged, magic);     // thanks Darter (YungMoozi) for reporting unchecked dmg

            if (dmg > maxDmg) {
                // 伤害计算有差异，StatEffect获取的时候，damage如果不存在默认为100，如果客户端也是这个逻辑，客户端是不是算上了这个damage导致实际数值比服务端高
//                AutobanFactory.DAMAGE_HACK.alert(damageChr, "Possible packet editing hypnotize damage exploit.");   // thanks Rien dev team
                String attackerName = MonsterInformationProvider.getInstance().getMobNameFromId(attacker.getId());
                String damagedName = MonsterInformationProvider.getInstance().getMobNameFromId(damaged.getId());
                log.warn("Chr {} had hypnotized {} to attack {} with damage {} (max: {})", damageChr.getName(),
                        attackerName, damagedName, dmg, maxDmg);
                dmg = maxDmg;
            }

            map.damageMonster(damageChr, damaged, dmg);
            map.broadcastMessage(damageChr, PacketCreator.damageMonster(to, dmg), false);
        }
    }

    private static int calcMaxDamage(Monster attacker, Monster damaged, boolean magic) {
        int attackerAtk, damagedDef, attackerLevel = attacker.getLevel();
        double maxDamage;
        if (magic) {
            int atkRate = calcModifier(attacker, MonsterStatus.MAGIC_ATTACK_UP, MonsterStatus.MATK);
            attackerAtk = (attacker.getStats().getMADamage() * atkRate) / 100;

            int defRate = calcModifier(damaged, MonsterStatus.MAGIC_DEFENSE_UP, MonsterStatus.MDEF);
            damagedDef = (damaged.getStats().getMDDamage() * defRate) / 100;

            maxDamage = ((attackerAtk * (1.15 + (0.025 * attackerLevel))) - (0.75 * damagedDef)) * (Math.log(Math.abs(damagedDef - attackerAtk)) / Math.log(12));
        } else {
            int atkRate = calcModifier(attacker, MonsterStatus.WEAPON_ATTACK_UP, MonsterStatus.WATK);
            attackerAtk = (attacker.getStats().getPADamage() * atkRate) / 100;

            int defRate = calcModifier(damaged, MonsterStatus.WEAPON_DEFENSE_UP, MonsterStatus.WDEF);
            damagedDef = (damaged.getStats().getPDDamage() * defRate) / 100;

            maxDamage = ((attackerAtk * (1.15 + (0.025 * attackerLevel))) - (0.75 * damagedDef)) * (Math.log(Math.abs(damagedDef - attackerAtk)) / Math.log(17));
        }

        return (int) maxDamage;
    }

    private static int calcModifier(Monster monster, MonsterStatus buff, MonsterStatus nerf) {
        int atkModifier;
        final Map<MonsterStatus, MonsterStatusEffect> monsterStati = monster.getStati();

        MonsterStatusEffect atkBuff = monsterStati.get(buff);
        if (atkBuff != null) {
            atkModifier = atkBuff.getStati().get(buff);
        } else {
            atkModifier = 100;
        }

        MonsterStatusEffect atkNerf = monsterStati.get(nerf);
        if (atkNerf != null) {
            atkModifier -= atkNerf.getStati().get(nerf);
        }

        return atkModifier;
    }
}
