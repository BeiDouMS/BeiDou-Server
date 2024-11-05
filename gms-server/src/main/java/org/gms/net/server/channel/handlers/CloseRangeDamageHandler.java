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

import org.gms.client.BuffStat;
import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Job;
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.constants.id.MapId;
import org.gms.constants.skills.Crusader;
import org.gms.constants.skills.DawnWarrior;
import org.gms.constants.skills.DragonKnight;
import org.gms.constants.skills.Hero;
import org.gms.constants.skills.NightWalker;
import org.gms.constants.skills.Rogue;
import org.gms.constants.skills.WindArcher;
import org.gms.net.packet.InPacket;
import org.gms.server.StatEffect;
import org.gms.util.PacketCreator;
import org.gms.util.Pair;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class CloseRangeDamageHandler extends AbstractDealDamageHandler {

    @Override
    public final void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();
        
        /*long timeElapsed = currentServerTime() - chr.getAutobanManager().getLastSpam(8);
        if(timeElapsed < 300) {
                AutobanFactory.FAST_ATTACK.alert(chr, "Time: " + timeElapsed);
        }
        chr.getAutobanManager().spam(8);*/

        AttackInfo attack = parseDamage(p, chr, false, false);
        if (chr.getBuffEffect(BuffStat.MORPH) != null) {
            if (chr.getBuffEffect(BuffStat.MORPH).isMorphWithoutAttack()) {
                // How are they attacking when the client won't let them?
                chr.getClient().disconnect(false, false);
                return;
            }
        }

        if (chr.getDojoEnergy() < 10000 && (attack.skill == 1009 || attack.skill == 10001009 || attack.skill == 20001009)) // PE hacking or maybe just lagging
        {
            return;
        }
        if (MapId.isDojo(chr.getMap().getId()) && attack.numAttacked > 0) {
            chr.setDojoEnergy(chr.getDojoEnergy() + GameConfig.getServerInt("dojo_energy_atk"));
            c.sendPacket(PacketCreator.getEnergy("energy", chr.getDojoEnergy()));
        }

        chr.getMap().broadcastMessage(chr, PacketCreator.closeRangeAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, attack.allDamage, attack.speed, attack.direction, attack.display), false, true);
        int numFinisherOrbs = 0;
        Integer comboBuff = chr.getBuffedValue(BuffStat.COMBO);
        if (GameConstants.isFinisherSkill(attack.skill)) {
            if (comboBuff != null) {
                numFinisherOrbs = comboBuff - 1;
            }
            chr.handleOrbconsume();
        } else if (attack.numAttacked > 0) {
            if (attack.skill != 1111008 && comboBuff != null) {
                int orbcount = chr.getBuffedValue(BuffStat.COMBO);
                int oid = chr.isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
                int advcomboid = chr.isCygnus() ? DawnWarrior.ADVANCED_COMBO : Hero.ADVANCED_COMBO;
                Skill combo = SkillFactory.getSkill(oid);
                Skill advcombo = SkillFactory.getSkill(advcomboid);
                StatEffect ceffect;
                int advComboSkillLevel = chr.getSkillLevel(advcombo);
                if (advComboSkillLevel > 0) {
                    ceffect = advcombo.getEffect(advComboSkillLevel);
                } else {
                    int comboLv = chr.getSkillLevel(combo);
                    if (comboLv <= 0 || chr.isGM()) {
                        comboLv = SkillFactory.getSkill(oid).getMaxLevel();
                    }

                    if (comboLv > 0) {
                        ceffect = combo.getEffect(comboLv);
                    } else {
                        ceffect = null;
                    }
                }
                if (ceffect != null) {
                    if (orbcount < ceffect.getX() + 1) {
                        int neworbcount = orbcount + 1;
                        if (advComboSkillLevel > 0 && ceffect.makeChanceResult()) {
                            if (neworbcount <= ceffect.getX()) {
                                neworbcount++;
                            }
                        }

                        int olv = chr.getSkillLevel(oid);
                        if (olv <= 0) {
                            olv = SkillFactory.getSkill(oid).getMaxLevel();
                        }

                        int duration = combo.getEffect(olv).getDuration();
                        List<Pair<BuffStat, Integer>> stat = Collections.singletonList(new Pair<>(BuffStat.COMBO, neworbcount));
                        chr.setBuffedValue(BuffStat.COMBO, neworbcount);
                        duration -= (int) (currentServerTime() - chr.getBuffedStarttime(BuffStat.COMBO));
                        c.sendPacket(PacketCreator.giveBuff(oid, duration, stat));
                        chr.getMap().broadcastMessage(chr, PacketCreator.giveForeignBuff(chr.getId(), stat), false);
                    }
                }
            } else if (chr.getSkillLevel(chr.isCygnus() ? SkillFactory.getSkill(15100004) : SkillFactory.getSkill(5110001)) > 0 && (chr.getJob().isA(Job.MARAUDER) || chr.getJob().isA(Job.THUNDERBREAKER2))) {
                for (int i = 0; i < attack.numAttacked; i++) {
                    chr.handleEnergyChargeGain();
                }
            }
        }
        if (attack.numAttacked > 0 && attack.skill == DragonKnight.SACRIFICE) {
            int totDamageToOneMonster = 0; // sacrifice attacks only 1 mob with 1 attack
            final Iterator<List<Integer>> dmgIt = attack.allDamage.values().iterator();
            if (dmgIt.hasNext()) {
                totDamageToOneMonster = dmgIt.next().get(0);
            }

            chr.safeAddHP(-1 * totDamageToOneMonster * attack.getAttackEffect(chr, null).getX() / 100);
        }
        if (attack.numAttacked > 0 && attack.skill == 1211002) {
            boolean advcharge_prob = false;
            int advcharge_level = chr.getSkillLevel(SkillFactory.getSkill(1220010));
            if (advcharge_level > 0) {
                advcharge_prob = SkillFactory.getSkill(1220010).getEffect(advcharge_level).makeChanceResult();
            }
            if (!advcharge_prob) {
                chr.cancelEffectFromBuffStat(BuffStat.WK_CHARGE);
            }
        }
        int attackCount = 1;
        if (attack.skill != 0) {
            attackCount = attack.getAttackEffect(chr, null).getAttackCount();
        }
        if (numFinisherOrbs == 0 && GameConstants.isFinisherSkill(attack.skill)) {
            return;
        }
        if (attack.skill % 10000000 == 1009) { // bamboo
            if (chr.getDojoEnergy() < 10000) { // PE hacking or maybe just lagging
                return;
            }

            chr.setDojoEnergy(0);
            c.sendPacket(PacketCreator.getEnergy("energy", chr.getDojoEnergy()));
            c.sendPacket(PacketCreator.serverNotice(5, "As you used the secret skill, your energy bar has been reset."));
        } else if (attack.skill > 0) {
            Skill skill = SkillFactory.getSkill(attack.skill);
            StatEffect effect_ = skill.getEffect(chr.getSkillLevel(skill));
            if (effect_.getCooldown() > 0) {
                if (chr.skillIsCooling(attack.skill)) {
                    return;
                } else {
                    c.sendPacket(PacketCreator.skillCooldown(attack.skill, effect_.getCooldown()));
                    chr.addCooldown(attack.skill, currentServerTime(), SECONDS.toMillis(effect_.getCooldown()));
                }
            }
        }
        if ((chr.getSkillLevel(SkillFactory.getSkill(NightWalker.VANISH)) > 0 || chr.getSkillLevel(SkillFactory.getSkill(Rogue.DARK_SIGHT)) > 0) && chr.getBuffedValue(BuffStat.DARKSIGHT) != null) {// && chr.getBuffSource(BuffStat.DARKSIGHT) != 9101004
            chr.cancelEffectFromBuffStat(BuffStat.DARKSIGHT);
            chr.cancelBuffStats(BuffStat.DARKSIGHT);
        } else if (chr.getSkillLevel(SkillFactory.getSkill(WindArcher.WIND_WALK)) > 0 && chr.getBuffedValue(BuffStat.WIND_WALK) != null) {
            chr.cancelEffectFromBuffStat(BuffStat.WIND_WALK);
            chr.cancelBuffStats(BuffStat.WIND_WALK);
        }

        applyAttack(attack, chr, attackCount);
    }
}
