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
import org.gms.client.Job;
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.client.autoban.AutobanFactory;
import org.gms.client.status.MonsterStatus;
import org.gms.client.status.MonsterStatusEffect;
import org.gms.config.GameConfig;
import org.gms.constants.game.GameConstants;
import org.gms.constants.id.ItemId;
import org.gms.constants.id.MapId;
import org.gms.constants.id.MobId;
import org.gms.constants.skills.Aran;
import org.gms.constants.skills.Assassin;
import org.gms.constants.skills.Bandit;
import org.gms.constants.skills.Beginner;
import org.gms.constants.skills.Bishop;
import org.gms.constants.skills.BlazeWizard;
import org.gms.constants.skills.Bowmaster;
import org.gms.constants.skills.Brawler;
import org.gms.constants.skills.Buccaneer;
import org.gms.constants.skills.ChiefBandit;
import org.gms.constants.skills.Cleric;
import org.gms.constants.skills.Corsair;
import org.gms.constants.skills.Crossbowman;
import org.gms.constants.skills.Crusader;
import org.gms.constants.skills.DawnWarrior;
import org.gms.constants.skills.DragonKnight;
import org.gms.constants.skills.Evan;
import org.gms.constants.skills.FPArchMage;
import org.gms.constants.skills.FPMage;
import org.gms.constants.skills.FPWizard;
import org.gms.constants.skills.Fighter;
import org.gms.constants.skills.Gunslinger;
import org.gms.constants.skills.Hermit;
import org.gms.constants.skills.Hero;
import org.gms.constants.skills.Hunter;
import org.gms.constants.skills.ILArchMage;
import org.gms.constants.skills.ILMage;
import org.gms.constants.skills.Legend;
import org.gms.constants.skills.Marauder;
import org.gms.constants.skills.Marksman;
import org.gms.constants.skills.NightLord;
import org.gms.constants.skills.NightWalker;
import org.gms.constants.skills.Noblesse;
import org.gms.constants.skills.Outlaw;
import org.gms.constants.skills.Page;
import org.gms.constants.skills.Paladin;
import org.gms.constants.skills.Ranger;
import org.gms.constants.skills.Rogue;
import org.gms.constants.skills.Shadower;
import org.gms.constants.skills.Sniper;
import org.gms.constants.skills.Spearman;
import org.gms.constants.skills.SuperGM;
import org.gms.constants.skills.ThunderBreaker;
import org.gms.constants.skills.WhiteKnight;
import org.gms.constants.skills.WindArcher;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.PlayerBuffValueHolder;
import org.gms.scripting.AbstractPlayerInteraction;
import org.gms.server.StatEffect;
import org.gms.server.TimerManager;
import org.gms.server.life.Element;
import org.gms.server.life.ElementalEffectiveness;
import org.gms.server.life.MobSkill;
import org.gms.server.life.MobSkillFactory;
import org.gms.server.life.MobSkillId;
import org.gms.server.life.MobSkillType;
import org.gms.server.life.Monster;
import org.gms.server.life.MonsterDropEntry;
import org.gms.server.life.MonsterInformationProvider;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;
import org.gms.util.Randomizer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class AbstractDealDamageHandler extends AbstractPacketHandler {

    public static class AttackInfo {

        public int numAttacked, numDamage, numAttackedAndDamage, skill, skilllevel, stance, direction, rangedirection, charge, display;
        public Map<Integer, List<Integer>> allDamage;
        public boolean ranged, magic;
        public int speed = 4;
        public Point position = new Point();

        public StatEffect getAttackEffect(Character chr, Skill theSkill) {
            Skill mySkill = theSkill;
            if (mySkill == null) {
                mySkill = SkillFactory.getSkill(skill);
            }

            int skillLevel = chr.getSkillLevel(mySkill);
            if (skillLevel == 0 && GameConstants.isPqSkillMap(chr.getMapId()) && GameConstants.isPqSkill(mySkill.getId())) {
                skillLevel = 1;
            }

            if (skillLevel == 0) {
                return null;
            }
            if (display > 80) { //Hmm
                if (!mySkill.getAction()) {
                    AutobanFactory.FAST_ATTACK.autoban(chr, "WZ Edit; adding action to a skill: " + display);
                    return null;
                }
            }
            return mySkill.getEffect(skillLevel);
        }
    }

    protected void applyAttack(AttackInfo attack, final Character player, int attackCount) {
        final MapleMap map = player.getMap();
        if (map.isOwnershipRestricted(player)) {
            return;
        }

        Skill theSkill = null;
        StatEffect attackEffect = null;
        final int job = player.getJob().getId();
        try {
            if (player.isBanned()) {
                return;
            }
            if (attack.skill != 0) {
                theSkill = SkillFactory.getSkill(attack.skill); // thanks Conrad for noticing some Aran skills not consuming MP
                attackEffect = attack.getAttackEffect(player, theSkill); //returns back the player's attack effect so we are gucci
                if (attackEffect == null) {
                    player.sendPacket(PacketCreator.enableActions());
                    return;
                }

                if (player.getMp() < attackEffect.getMpCon()) {
                    AutobanFactory.MPCON.addPoint(player.getAutoBanManager(), "Skill: " + attack.skill + "; Player MP: " + player.getMp() + "; MP Needed: " + attackEffect.getMpCon());
                }

                int mobCount = attackEffect.getMobCount();
                if (attack.skill != Cleric.HEAL) {
                    if (player.isAlive()) {
                        if (attack.skill == Aran.BODY_PRESSURE || attack.skill == Marauder.ENERGY_CHARGE || attack.skill == ThunderBreaker.ENERGY_CHARGE) {  // thanks IxianMace for noticing Energy Charge skills refreshing on touch
                            // prevent touch dmg skills refreshing
                        } else if (attack.skill == DawnWarrior.FINAL_ATTACK || attack.skill == WindArcher.FINAL_ATTACK) {
                            // prevent cygnus FA refreshing
                            mobCount = 15;
                        } else if (attack.skill == NightWalker.POISON_BOMB) {// Poison Bomb
                            attackEffect.applyTo(player, new Point(attack.position.x, attack.position.y));
                        } else {
                            attackEffect.applyTo(player);

                            if (attack.skill == Page.FINAL_ATTACK_BW || attack.skill == Page.FINAL_ATTACK_SWORD || attack.skill == Fighter.FINAL_ATTACK_SWORD
                                    || attack.skill == Fighter.FINAL_ATTACK_AXE || attack.skill == Spearman.FINAL_ATTACK_SPEAR || attack.skill == Spearman.FINAL_ATTACK_POLEARM
                                    || attack.skill == Hunter.FINAL_ATTACK || attack.skill == Crossbowman.FINAL_ATTACK) {

                                mobCount = 15;//:(
                            } else if (attack.skill == Aran.HIDDEN_FULL_DOUBLE || attack.skill == Aran.HIDDEN_FULL_TRIPLE || attack.skill == Aran.HIDDEN_OVER_DOUBLE || attack.skill == Aran.HIDDEN_OVER_TRIPLE) {
                                mobCount = 12;
                            }
                        }
                    } else {
                        player.sendPacket(PacketCreator.enableActions());
                    }
                }

                if (attack.numAttacked > mobCount) {
                    AutobanFactory.MOB_COUNT.autoban(player, "Skill: " + attack.skill + "; Count: " + attack.numAttacked + " Max: " + attackEffect.getMobCount());
                    return;
                }
            }
            if (!player.isAlive()) {
                return;
            }

            //WTF IS THIS F3,1
            /*if (attackCount != attack.numDamage && attack.skill != ChiefBandit.MESO_EXPLOSION && attack.skill != NightWalker.VAMPIRE && attack.skill != WindArcher.WIND_SHOT && attack.skill != Aran.COMBO_SMASH && attack.skill != Aran.COMBO_FENRIR && attack.skill != Aran.COMBO_TEMPEST && attack.skill != NightLord.NINJA_AMBUSH && attack.skill != Shadower.NINJA_AMBUSH) {
                return;
            }*/

            int totDamage = 0;

            if (attack.skill == ChiefBandit.MESO_EXPLOSION) {
                int delay = 0;
                for (Integer oned : attack.allDamage.keySet()) {
                    MapObject mapobject = map.getMapObject(oned);
                    if (mapobject != null && mapobject.getType() == MapObjectType.ITEM) {
                        final MapItem mapitem = (MapItem) mapobject;
                        if (mapitem.getMeso() == 0) { //Maybe it is possible some how?
                            return;
                        }

                        mapitem.lockItem();
                        try {
                            if (mapitem.isPickedUp()) {
                                return;
                            }
                            TimerManager.getInstance().schedule(() -> {
                                mapitem.lockItem();
                                try {
                                    if (mapitem.isPickedUp()) {
                                        return;
                                    }
                                    map.pickItemDrop(PacketCreator.removeItemFromMap(mapitem.getObjectId(), 4, 0), mapitem);
                                } finally {
                                    mapitem.unlockItem();
                                }
                            }, delay);
                            delay += 100;
                        } finally {
                            mapitem.unlockItem();
                        }
                    } else if (mapobject != null && mapobject.getType() != MapObjectType.MONSTER) {
                        return;
                    }
                }
            }
            for (Integer oned : attack.allDamage.keySet()) {
                final Monster monster = map.getMonsterByOid(oned);
                if (monster != null) {
                    double distance = player.getPosition().distanceSq(monster.getPosition());
                    double distanceToDetect = 200000.0;

                    if (attack.ranged) {
                        distanceToDetect += 400000;
                    }

                    if (attack.magic) {
                        distanceToDetect += 200000;
                    }

                    if (player.getJob().isA(Job.ARAN1)) {
                        distanceToDetect += 200000; // Arans have extra range over normal warriors.
                    }

                    if (attack.skill == Aran.COMBO_SMASH || attack.skill == Aran.BODY_PRESSURE) {
                        distanceToDetect += 40000;
                    } else if (attack.skill == Bishop.GENESIS || attack.skill == ILArchMage.BLIZZARD || attack.skill == FPArchMage.METEOR_SHOWER) {
                        distanceToDetect += 275000;
                    } else if (attack.skill == Hero.BRANDISH || attack.skill == DragonKnight.SPEAR_CRUSHER || attack.skill == DragonKnight.POLE_ARM_CRUSHER) {
                        distanceToDetect += 40000;
                    } else if (attack.skill == DragonKnight.DRAGON_ROAR || attack.skill == SuperGM.SUPER_DRAGON_ROAR) {
                        distanceToDetect += 250000;
                    } else if (attack.skill == Shadower.BOOMERANG_STEP) {
                        distanceToDetect += 60000;
                    }

                    if (distance > distanceToDetect) {
                        AutobanFactory.DISTANCE_HACK.alert(player, "Distance Sq to monster: " + distance + " SID: " + attack.skill + " MID: " + monster.getId());
                        monster.refreshMobPosition();
                    }

                    int totDamageToOneMonster = 0;
                    List<Integer> onedList = attack.allDamage.get(oned);

                    if (attack.magic) { // thanks BHB, Alex (CanIGetaPR) for noticing no immunity status check here
                        if (monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY)) {
                            Collections.fill(onedList, 1);
                        }
                    } else {
                        if (monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY)) {
                            Collections.fill(onedList, 1);
                        }
                    }

                    if (MobId.isDojoBoss(monster.getId())) {
                        if (attack.skill == 1009 || attack.skill == 10001009 || attack.skill == 20001009) {
                            int dmgLimit = (int) Math.ceil(0.3 * monster.getMaxHp());
                            List<Integer> _onedList = new LinkedList<>();
                            for (Integer i : onedList) {
                                _onedList.add(i < dmgLimit ? i : dmgLimit);
                            }

                            onedList = _onedList;
                        }
                    }

                    for (Integer eachd : onedList) {
                        if (eachd < 0) {
                            eachd += Integer.MAX_VALUE;
                        }
                        totDamageToOneMonster += eachd;
                    }
                    totDamage += totDamageToOneMonster;
                    monster.aggroMonsterDamage(player, totDamageToOneMonster);
                    if (player.getBuffedValue(BuffStat.PICKPOCKET) != null && (attack.skill == 0 || attack.skill == Rogue.DOUBLE_STAB || attack.skill == Bandit.SAVAGE_BLOW || attack.skill == ChiefBandit.ASSAULTER || attack.skill == ChiefBandit.BAND_OF_THIEVES || attack.skill == Shadower.ASSASSINATE || attack.skill == Shadower.TAUNT || attack.skill == Shadower.BOOMERANG_STEP)) {
                        Skill pickpocket = SkillFactory.getSkill(ChiefBandit.PICKPOCKET);
                        int picklv = (player.isGM()) ? pickpocket.getMaxLevel() : player.getSkillLevel(pickpocket);
                        if (picklv > 0) {
                            int delay = 0;
                            final int maxmeso = player.getBuffedValue(BuffStat.PICKPOCKET);
                            for (Integer eachd : onedList) {
                                eachd += Integer.MAX_VALUE;

                                if (pickpocket.getEffect(picklv).makeChanceResult()) {
                                    final int eachdf;
                                    if (eachd < 0) {
                                        eachdf = eachd + Integer.MAX_VALUE;
                                    } else {
                                        eachdf = eachd;
                                    }

                                    TimerManager.getInstance().schedule(() -> map.spawnMesoDrop(Math.min((int) Math.max(((double) eachdf / (double) 20000) * (double) maxmeso, 1), maxmeso), new Point((int) (monster.getPosition().getX() + Randomizer.nextInt(100) - 50), (int) (monster.getPosition().getY())), monster, player, true, (byte) 2), delay);
                                    delay += 100;
                                }
                            }
                        }
                    } else if (attack.skill == Marauder.ENERGY_DRAIN || attack.skill == ThunderBreaker.ENERGY_DRAIN || attack.skill == NightWalker.VAMPIRE || attack.skill == Assassin.DRAIN) {
                        player.addHP(Math.min(monster.getMaxHp(), Math.min((int) ((double) totDamage * (double) SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(SkillFactory.getSkill(attack.skill))).getX() / 100.0), player.getCurrentMaxHp() / 2)));
                    } else if (attack.skill == Bandit.STEAL) {
                        Skill steal = SkillFactory.getSkill(Bandit.STEAL);
                        if (monster.getStolen().size() < 1) { // One steal per mob <3
                            if (steal.getEffect(player.getSkillLevel(steal)).makeChanceResult()) {
                                monster.addStolen(0);

                                MonsterInformationProvider mi = MonsterInformationProvider.getInstance();
                                List<Integer> dropPool = mi.retrieveDropPool(monster.getId());
                                if (dropPool != null && !dropPool.isEmpty()) {
                                    int rndPool = (int) Math.floor(Math.random() * dropPool.get(dropPool.size() - 1));

                                    int i = 0;
                                    while (rndPool >= dropPool.get(i)) {
                                        i++;
                                    }

                                    List<MonsterDropEntry> toSteal = new ArrayList<>();
                                    toSteal.add(mi.retrieveDrop(monster.getId()).get(i));

                                    map.dropItemsFromMonster(toSteal, player, monster);
                                    monster.addStolen(toSteal.get(0).itemId);
                                }
                            }
                        }
                    } else if (attack.skill == FPArchMage.FIRE_DEMON) {
                        long duration = SECONDS.toMillis(SkillFactory.getSkill(FPArchMage.FIRE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(FPArchMage.FIRE_DEMON))).getDuration());
                        monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, duration);
                    } else if (attack.skill == ILArchMage.ICE_DEMON) {
                        long duration = SECONDS.toMillis(SkillFactory.getSkill(ILArchMage.ICE_DEMON).getEffect(player.getSkillLevel(SkillFactory.getSkill(ILArchMage.ICE_DEMON))).getDuration());
                        monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, duration);
                    } else if (attack.skill == Outlaw.HOMING_BEACON || attack.skill == Corsair.BULLSEYE) {
                        StatEffect beacon = SkillFactory.getSkill(attack.skill).getEffect(player.getSkillLevel(attack.skill));
                        beacon.applyBeaconBuff(player, monster.getObjectId());
                    } else if (attack.skill == Outlaw.FLAME_THROWER) {
                        if (!monster.isBoss()) {
                            Skill type = SkillFactory.getSkill(Outlaw.FLAME_THROWER);
                            if (player.getSkillLevel(type) > 0) {
                                StatEffect DoT = type.getEffect(player.getSkillLevel(type));
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), type, null, false);
                                monster.applyStatus(player, monsterStatusEffect, true, DoT.getDuration(), false);
                            }
                        }
                    }

                    if (player.isAran()) {
                        if (player.getBuffedValue(BuffStat.WK_CHARGE) != null) {
                            Skill snowCharge = SkillFactory.getSkill(Aran.SNOW_CHARGE);
                            if (totDamageToOneMonster > 0) {
                                MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, snowCharge.getEffect(player.getSkillLevel(snowCharge)).getX()), snowCharge, null, false);
                                long duration = SECONDS.toMillis(snowCharge.getEffect(player.getSkillLevel(snowCharge)).getY());
                                monster.applyStatus(player, monsterStatusEffect, false, duration);
                            }
                        }
                    }
                    if (player.getBuffedValue(BuffStat.HAMSTRING) != null) {
                        Skill hamstring = SkillFactory.getSkill(Bowmaster.HAMSTRING);
                        if (hamstring.getEffect(player.getSkillLevel(hamstring)).makeChanceResult()) {
                            MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, hamstring.getEffect(player.getSkillLevel(hamstring)).getX()), hamstring, null, false);
                            long duration = SECONDS.toMillis(hamstring.getEffect(player.getSkillLevel(hamstring)).getY());
                            monster.applyStatus(player, monsterStatusEffect, false, duration);
                        }
                    }
                    if (player.getBuffedValue(BuffStat.SLOW) != null) {
                        Skill slow = SkillFactory.getSkill(Evan.SLOW);
                        if (slow.getEffect(player.getSkillLevel(slow)).makeChanceResult()) {
                            MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.SPEED, slow.getEffect(player.getSkillLevel(slow)).getX()), slow, null, false);
                            long duration = MINUTES.toMillis(slow.getEffect(player.getSkillLevel(slow)).getY());
                            monster.applyStatus(player, monsterStatusEffect, false, duration);
                        }
                    }
                    if (player.getBuffedValue(BuffStat.BLIND) != null) {
                        Skill blind = SkillFactory.getSkill(Marksman.BLIND);
                        if (blind.getEffect(player.getSkillLevel(blind)).makeChanceResult()) {
                            MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.ACC, blind.getEffect(player.getSkillLevel(blind)).getX()), blind, null, false);
                            long duration = SECONDS.toMillis(blind.getEffect(player.getSkillLevel(blind)).getY());
                            monster.applyStatus(player, monsterStatusEffect, false, duration);
                        }
                    }
                    if (job == 121 || job == 122) {
                        for (int charge = 1211005; charge < 1211007; charge++) {
                            Skill chargeSkill = SkillFactory.getSkill(charge);
                            if (player.isBuffFrom(BuffStat.WK_CHARGE, chargeSkill)) {
                                if (totDamageToOneMonster > 0) {
                                    if (charge == WhiteKnight.BW_ICE_CHARGE || charge == WhiteKnight.SWORD_ICE_CHARGE) {
                                        monster.setTempEffectiveness(Element.ICE, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
                                        // 修复冰技能不冰怪的问题，关键是冰和火都没有对应的异常状态，对应的异常只有冻结。如果这里把ICE改了，那火怎么办？所以，还是先注释掉。
//                                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.FREEZE, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getX()), chargeSkill, null, false);
//                                        long duration = SECONDS.toMillis(chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY());
//                                        monster.applyStatus(player, monsterStatusEffect, false, duration);
                                        break;
                                    }
                                    if (charge == WhiteKnight.BW_FIRE_CHARGE || charge == WhiteKnight.SWORD_FIRE_CHARGE) {
                                        monster.setTempEffectiveness(Element.FIRE, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
                                        break;
                                    }
                                }
                            }
                        }
                        if (job == 122) {
                            for (int charge = 1221003; charge < 1221004; charge++) {
                                Skill chargeSkill = SkillFactory.getSkill(charge);
                                if (player.isBuffFrom(BuffStat.WK_CHARGE, chargeSkill)) {
                                    if (totDamageToOneMonster > 0) {
                                        monster.setTempEffectiveness(Element.HOLY, ElementalEffectiveness.WEAK, chargeSkill.getEffect(player.getSkillLevel(chargeSkill)).getY() * 1000);
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (player.getBuffedValue(BuffStat.COMBO_DRAIN) != null) {
                        Skill skill;
                        if (player.getBuffedValue(BuffStat.COMBO_DRAIN) != null) {
                            skill = SkillFactory.getSkill(21100005);
                            player.addHP(((totDamage * skill.getEffect(player.getSkillLevel(skill)).getX()) / 100));
                        }
                    } else if (job == 412 || job == 422 || job == 1411) {
                        Skill type = SkillFactory.getSkill(player.getJob().getId() == 412 ? 4120005 : (player.getJob().getId() == 1411 ? 14110004 : 4220005));
                        if (player.getSkillLevel(type) > 0) {
                            StatEffect venomEffect = type.getEffect(player.getSkillLevel(type));
                            for (int i = 0; i < attackCount; i++) {
                                if (venomEffect.makeChanceResult()) {
                                    if (monster.getVenomMulti() < 3) {
                                        monster.setVenomMulti((monster.getVenomMulti() + 1));
                                        MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(Collections.singletonMap(MonsterStatus.POISON, 1), type, null, false);
                                        monster.applyStatus(player, monsterStatusEffect, false, venomEffect.getDuration(), true);
                                    }
                                }
                            }
                        }
                    } else if (job >= 311 && job <= 322) {
                        if (!monster.isBoss()) {
                            Skill mortalBlow;
                            if (job == 311 || job == 312) {
                                mortalBlow = SkillFactory.getSkill(Ranger.MORTAL_BLOW);
                            } else {
                                mortalBlow = SkillFactory.getSkill(Sniper.MORTAL_BLOW);
                            }

                            int skillLevel = player.getSkillLevel(mortalBlow);
                            if (skillLevel > 0) {
                                StatEffect mortal = mortalBlow.getEffect(skillLevel);
                                if (monster.getHp() <= (monster.getStats().getHp() * mortal.getX()) / 100) {
                                    if (Randomizer.rand(1, 100) <= mortal.getY()) {
                                        map.damageMonster(player, monster, Integer.MAX_VALUE);  // thanks Conrad for noticing reduced EXP gain from skill kill
                                    }
                                }
                            }
                        }
                    }
                    if (attack.skill != 0) {
                        if (attackEffect.getFixDamage() != -1) {
                            if (totDamageToOneMonster != attackEffect.getFixDamage() && totDamageToOneMonster != 0) {
                                AutobanFactory.FIX_DAMAGE.autoban(player, totDamageToOneMonster + " damage");
                            }

                            int threeSnailsId = player.getJobType() * 10000000 + 1000;
                            if (attack.skill == threeSnailsId) {
                                if (GameConfig.getServerBoolean("use_ultra_three_snails")) {
                                    int skillLv = player.getSkillLevel(threeSnailsId);

                                    if (skillLv > 0) {
                                        AbstractPlayerInteraction api = player.getAbstractPlayerInteraction();

                                        int shellId = switch (skillLv) {
                                            case 1 -> ItemId.SNAIL_SHELL;
                                            case 2 -> ItemId.BLUE_SNAIL_SHELL;
                                            default -> ItemId.RED_SNAIL_SHELL;
                                        };

                                        if (api.haveItem(shellId, 1)) {
                                            api.gainItem(shellId, (short) -1, false);
                                            totDamageToOneMonster *= player.getLevel();
                                        } else {
                                            player.dropMessage(5, "You have ran out of shells to activate the hidden power of Three Snails.");
                                        }
                                    } else {
                                        totDamageToOneMonster = 0;
                                    }
                                }
                            }
                        }
                    }
                    if (totDamageToOneMonster > 0 && attackEffect != null) {
                        Map<MonsterStatus, Integer> attackEffectStati = attackEffect.getMonsterStati();
                        if (!attackEffectStati.isEmpty()) {
                            if (attackEffect.makeChanceResult()) {
                                monster.applyStatus(player, new MonsterStatusEffect(attackEffectStati, theSkill, null, false), attackEffect.isPoison(), attackEffect.getDuration());
                            }
                        }
                    }
                    if (attack.skill == Paladin.HEAVENS_HAMMER) {
                        if (!monster.isBoss()) {
                            damageMonsterWithSkill(player, map, monster, monster.getHp() - 1, attack.skill, 1777);
                        } else {
                            int HHDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Paladin.HEAVENS_HAMMER).getEffect(player.getSkillLevel(SkillFactory.getSkill(Paladin.HEAVENS_HAMMER))).getDamage() / 100));
                            damageMonsterWithSkill(player, map, monster, (int) (Math.floor(Math.random() * (HHDmg / 5) + HHDmg * .8)), attack.skill, 1777);
                        }
                    } else if (attack.skill == Aran.COMBO_TEMPEST) {
                        if (!monster.isBoss()) {
                            damageMonsterWithSkill(player, map, monster, monster.getHp(), attack.skill, 0);
                        } else {
                            int TmpDmg = (player.calculateMaxBaseDamage(player.getTotalWatk()) * (SkillFactory.getSkill(Aran.COMBO_TEMPEST).getEffect(player.getSkillLevel(SkillFactory.getSkill(Aran.COMBO_TEMPEST))).getDamage() / 100));
                            damageMonsterWithSkill(player, map, monster, (int) (Math.floor(Math.random() * (TmpDmg / 5) + TmpDmg * .8)), attack.skill, 0);
                        }
                    } else {
                        if (attack.skill == Aran.BODY_PRESSURE) {
                            map.broadcastMessage(PacketCreator.damageMonster(monster.getObjectId(), totDamageToOneMonster));
                        }

                        map.damageMonster(player, monster, totDamageToOneMonster);
                    }
                    if (monster.isBuffed(MonsterStatus.WEAPON_REFLECT) && !attack.magic) {
                        for (MobSkillId msId : monster.getSkills()) {
                            if (msId.type() == MobSkillType.PHYSICAL_AND_MAGIC_COUNTER) {
                                MobSkill toUse = MobSkillFactory.getMobSkillOrThrow(MobSkillType.PHYSICAL_AND_MAGIC_COUNTER, msId.level());
                                player.addHP(-toUse.getX());
                                map.broadcastMessage(player, PacketCreator.damagePlayer(0, monster.getId(), player.getId(), toUse.getX(), 0, 0, false, 0, true, monster.getObjectId(), 0, 0), true);
                            }
                        }
                    }
                    if (monster.isBuffed(MonsterStatus.MAGIC_REFLECT) && attack.magic) {
                        for (MobSkillId msId : monster.getSkills()) {
                            if (msId.type() == MobSkillType.PHYSICAL_AND_MAGIC_COUNTER) {
                                MobSkill toUse = MobSkillFactory.getMobSkillOrThrow(MobSkillType.PHYSICAL_AND_MAGIC_COUNTER, msId.level());
                                player.addHP(-toUse.getY());
                                map.broadcastMessage(player, PacketCreator.damagePlayer(0, monster.getId(), player.getId(), toUse.getY(), 0, 0, false, 0, true, monster.getObjectId(), 0, 0), true);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void damageMonsterWithSkill(final Character attacker, final MapleMap map, final Monster monster, final int damage, int skillid, int fixedTime) {
        int animationTime;

        if (fixedTime == 0) {
            animationTime = SkillFactory.getSkill(skillid).getAnimationTime();
        } else {
            animationTime = fixedTime;
        }

        if (animationTime > 0) { // be sure to only use LIMITED ATTACKS with animation time here
            TimerManager.getInstance().schedule(() -> {
                map.broadcastMessage(PacketCreator.damageMonster(monster.getObjectId(), damage), monster.getPosition());
                map.damageMonster(attacker, monster, damage);
            }, animationTime);
        } else {
            map.broadcastMessage(PacketCreator.damageMonster(monster.getObjectId(), damage), monster.getPosition());
            map.damageMonster(attacker, monster, damage);
        }
    }

    protected AttackInfo parseDamage(InPacket p, Character chr, boolean ranged, boolean magic) {
        //2C 00 00 01 91 A1 12 00 A5 57 62 FC E2 75 99 10 00 47 80 01 04 01 C6 CC 02 DD FF 5F 00
        AttackInfo ret = new AttackInfo();
        p.readByte();
        ret.numAttackedAndDamage = p.readByte();
        ret.numAttacked = (ret.numAttackedAndDamage >>> 4) & 0xF;
        ret.numDamage = ret.numAttackedAndDamage & 0xF;
        ret.allDamage = new HashMap<>();
        ret.skill = p.readInt();
        ret.ranged = ranged;
        ret.magic = magic;

        if (ret.skill > 0) {
            ret.skilllevel = chr.getSkillLevel(ret.skill);
            if (ret.skilllevel == 0 && GameConstants.isPqSkillMap(chr.getMapId()) && GameConstants.isPqSkill(ret.skill)) {
                ret.skilllevel = 1;
            }
        }

        if (ret.skill == Evan.ICE_BREATH || ret.skill == Evan.FIRE_BREATH || ret.skill == FPArchMage.BIG_BANG || ret.skill == ILArchMage.BIG_BANG || ret.skill == Bishop.BIG_BANG || ret.skill == Gunslinger.GRENADE || ret.skill == Brawler.CORKSCREW_BLOW || ret.skill == ThunderBreaker.CORKSCREW_BLOW || ret.skill == NightWalker.POISON_BOMB) {
            ret.charge = p.readInt();
        } else {
            ret.charge = 0;
        }

        p.skip(8);
        ret.display = p.readByte();
        ret.direction = p.readByte();
        ret.stance = p.readByte();
        if (ret.skill == ChiefBandit.MESO_EXPLOSION) {
            if (ret.numAttackedAndDamage == 0) {
                p.skip(10);
                int bullets = p.readByte();
                for (int j = 0; j < bullets; j++) {
                    int mesoid = p.readInt();
                    p.skip(1);
                    ret.allDamage.put(mesoid, null);
                }
                return ret;
            } else {
                p.skip(6);
            }
            for (int i = 0; i < ret.numAttacked + 1; i++) {
                int oid = p.readInt();
                if (i < ret.numAttacked) {
                    p.skip(12);
                    int bullets = p.readByte();
                    List<Integer> allDamageNumbers = new ArrayList<>();
                    for (int j = 0; j < bullets; j++) {
                        int damage = p.readInt();
                        allDamageNumbers.add(damage);
                    }
                    ret.allDamage.put(oid, allDamageNumbers);
                    p.skip(4);
                } else {
                    int bullets = p.readByte();
                    for (int j = 0; j < bullets; j++) {
                        int mesoid = p.readInt();
                        p.skip(1);
                        ret.allDamage.put(mesoid, null);
                    }
                }
            }
            return ret;
        }
        if (ranged) {
            p.readByte();
            ret.speed = p.readByte();
            p.readByte();
            ret.rangedirection = p.readByte();
            p.skip(7);
            if (ret.skill == Bowmaster.HURRICANE || ret.skill == Marksman.PIERCING_ARROW || ret.skill == Corsair.RAPID_FIRE || ret.skill == WindArcher.HURRICANE) {
                p.skip(4);
            }
        } else {
            p.readByte();
            ret.speed = p.readByte();
            p.skip(4);
        }

        // Find the base damage to base futher calculations on.
        // Several skills have their own formula in this section.
        long calcDmgMax;

        if (magic && ret.skill != 0) {   // thanks onechord for noticing a few false positives stemming from maxdmg as 0
            calcDmgMax = (long) (Math.ceil((chr.getTotalMagic() * Math.ceil(chr.getTotalMagic() / 1000.0) + chr.getTotalMagic()) / 30.0) + Math.ceil(chr.getTotalInt() / 200.0));
        } else if (ret.skill == Rogue.LUCKY_SEVEN || ret.skill == NightWalker.LUCKY_SEVEN || ret.skill == NightLord.TRIPLE_THROW) {
            calcDmgMax = (long) ((chr.getTotalLuk() * 5) * Math.ceil(chr.getTotalWatk() / 100.0));
        } else if (ret.skill == DragonKnight.DRAGON_ROAR) {
            calcDmgMax = (long) ((chr.getTotalStr() * 4 + chr.getTotalDex()) * Math.ceil(chr.getTotalWatk() / 100.0));
        } else if (ret.skill == NightLord.VENOMOUS_STAR || ret.skill == Shadower.VENOMOUS_STAB) {
            calcDmgMax = (long) (Math.ceil((18.5 * (chr.getTotalStr() + chr.getTotalLuk()) + chr.getTotalDex() * 2) / 100.0) * chr.calculateMaxBaseDamage(chr.getTotalWatk()));
        } else {
            calcDmgMax = chr.calculateMaxBaseDamage(chr.getTotalWatk());
        }

        StatEffect effect = null;
        if (ret.skill != 0) {
            Skill skill = SkillFactory.getSkill(ret.skill);
            effect = skill.getEffect(ret.skilllevel);

            if (magic) {
                // Since the skill is magic based, use the magic formula
                if (chr.getJob() == Job.IL_ARCHMAGE || chr.getJob() == Job.IL_MAGE) {
                    int skillLvl = chr.getSkillLevel(ILMage.ELEMENT_AMPLIFICATION);
                    if (skillLvl > 0) {
                        calcDmgMax = calcDmgMax * SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                    }
                } else if (chr.getJob() == Job.FP_ARCHMAGE || chr.getJob() == Job.FP_MAGE) {
                    int skillLvl = chr.getSkillLevel(FPMage.ELEMENT_AMPLIFICATION);
                    if (skillLvl > 0) {
                        calcDmgMax = calcDmgMax * SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                    }
                } else if (chr.getJob() == Job.BLAZEWIZARD3 || chr.getJob() == Job.BLAZEWIZARD4) {
                    int skillLvl = chr.getSkillLevel(BlazeWizard.ELEMENT_AMPLIFICATION);
                    if (skillLvl > 0) {
                        calcDmgMax = calcDmgMax * SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                    }
                } else if (chr.getJob() == Job.EVAN7 || chr.getJob() == Job.EVAN8 || chr.getJob() == Job.EVAN9 || chr.getJob() == Job.EVAN10) {
                    int skillLvl = chr.getSkillLevel(Evan.MAGIC_AMPLIFICATION);
                    if (skillLvl > 0) {
                        calcDmgMax = calcDmgMax * SkillFactory.getSkill(Evan.MAGIC_AMPLIFICATION).getEffect(skillLvl).getY() / 100;
                    }
                }

                calcDmgMax *= effect.getMatk();
                if (ret.skill == Cleric.HEAL) {
                    // This formula is still a bit wonky, but it is fairly accurate.
                    calcDmgMax = (int) Math.round((chr.getTotalInt() * 4.8 + chr.getTotalLuk() * 4) * chr.getTotalMagic() / 1000);
                    calcDmgMax = calcDmgMax * effect.getHp() / 100;

                    ret.speed = 7;
                }
            } else if (ret.skill == Hermit.SHADOW_MESO) {
                // Shadow Meso also has its own formula
                calcDmgMax = effect.getMoneyCon() * 10;
                calcDmgMax = (int) Math.floor(calcDmgMax * 1.5);
            } else {
                // Normal damage formula for skills
                calcDmgMax = calcDmgMax * effect.getDamage() / 100;
            }
        }

        Integer comboBuff = chr.getBuffedValue(BuffStat.COMBO);
        if (comboBuff != null && comboBuff > 0) {
            int oid = chr.isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
            int advcomboid = chr.isCygnus() ? DawnWarrior.ADVANCED_COMBO : Hero.ADVANCED_COMBO;

            if (comboBuff > 6) {
                // Advanced Combo
                StatEffect ceffect = SkillFactory.getSkill(advcomboid).getEffect(chr.getSkillLevel(advcomboid));
                calcDmgMax = (long) Math.floor(calcDmgMax * (ceffect.getDamage() + 50) / 100 + 0.20 + (comboBuff - 5) * 0.04);
            } else {
                // Normal Combo
                int skillLv = chr.getSkillLevel(oid);
                if (skillLv <= 0 || chr.isGM()) {
                    skillLv = SkillFactory.getSkill(oid).getMaxLevel();
                }

                if (skillLv > 0) {
                    StatEffect ceffect = SkillFactory.getSkill(oid).getEffect(skillLv);
                    calcDmgMax = (long) Math.floor(calcDmgMax * (ceffect.getDamage() + 50) / 100 + Math.floor((comboBuff - 1) * (skillLv / 6)) / 100);
                }
            }

            if (GameConstants.isFinisherSkill(ret.skill)) {
                // Finisher skills do more damage based on how many orbs the player has.
                int orbs = comboBuff - 1;
                if (orbs == 2) {
                    calcDmgMax *= 1.2;
                } else if (orbs == 3) {
                    calcDmgMax *= 1.54;
                } else if (orbs == 4) {
                    calcDmgMax *= 2;
                } else if (orbs >= 5) {
                    calcDmgMax *= 2.5;
                }
            }
        }

        if (chr.getEnergyBar() == 15000) {
            int energycharge = chr.isCygnus() ? ThunderBreaker.ENERGY_CHARGE : Marauder.ENERGY_CHARGE;
            StatEffect ceffect = SkillFactory.getSkill(energycharge).getEffect(chr.getSkillLevel(energycharge));
            calcDmgMax *= (100 + ceffect.getDamage()) / 100;
        }

        int bonusDmgBuff = 100;
        for (PlayerBuffValueHolder pbvh : chr.getAllBuffs()) {
            int bonusDmg = pbvh.effect.getDamage() - 100;
            bonusDmgBuff += bonusDmg;
        }

        if (bonusDmgBuff != 100) {
            float dmgBuff = bonusDmgBuff / 100.0f;
            calcDmgMax = (long) Math.ceil(calcDmgMax * dmgBuff);
        }

        if (chr.getMapId() >= MapId.ARAN_TUTORIAL_START && chr.getMapId() <= MapId.ARAN_TUTORIAL_MAX) {
            calcDmgMax += 80000; // Aran Tutorial.
        }

        boolean canCrit = chr.getJob().isA((Job.BOWMAN)) || chr.getJob().isA(Job.THIEF) || chr.getJob().isA(Job.NIGHTWALKER1) || chr.getJob().isA(Job.WINDARCHER1) || chr.getJob() == Job.ARAN3 || chr.getJob() == Job.ARAN4 || chr.getJob() == Job.MARAUDER || chr.getJob() == Job.BUCCANEER;

        StatEffect sharpEyesEffect = chr.getBuffEffect(BuffStat.SHARP_EYES);
        if (sharpEyesEffect != null) {
            // Any class that has sharp eyes can crit. Also, since it stacks with normal crit go ahead
            // and calc it in.
            canCrit = true;
            // 精确火眼按照当前等级计算伤害，而不是直接粗暴的取满级1.4，如果技改了wz，也能完全适配
            calcDmgMax = (long) Math.ceil(sharpEyesEffect.getY() / 100.0 * calcDmgMax);
        }

        boolean shadowPartner = chr.getBuffEffect(BuffStat.SHADOWPARTNER) != null;

        if (ret.skill != 0) {
            int fixed = ret.getAttackEffect(chr, SkillFactory.getSkill(ret.skill)).getFixDamage();
            if (fixed > 0) {
                calcDmgMax = fixed;
            }
        }
        for (int i = 0; i < ret.numAttacked; i++) {
            int oid = p.readInt();
            p.skip(14);
            List<Integer> allDamageNumbers = new ArrayList<>();
            Monster monster = chr.getMap().getMonsterByOid(oid);

            if (chr.getBuffEffect(BuffStat.WK_CHARGE) != null) {
                // Charge, so now we need to check elemental effectiveness
                int sourceID = chr.getBuffSource(BuffStat.WK_CHARGE);
                int level = chr.getBuffedValue(BuffStat.WK_CHARGE);
                if (monster != null) {
                    if (sourceID == WhiteKnight.BW_FIRE_CHARGE || sourceID == WhiteKnight.SWORD_FIRE_CHARGE) {
                        if (monster.getStats().getEffectiveness(Element.FIRE) == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.05 + level * 0.015;
                        }
                    } else if (sourceID == WhiteKnight.BW_ICE_CHARGE || sourceID == WhiteKnight.SWORD_ICE_CHARGE) {
                        if (monster.getStats().getEffectiveness(Element.ICE) == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.05 + level * 0.015;
                        }
                    } else if (sourceID == WhiteKnight.BW_LIT_CHARGE || sourceID == WhiteKnight.SWORD_LIT_CHARGE) {
                        if (monster.getStats().getEffectiveness(Element.LIGHTING) == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.05 + level * 0.015;
                        }
                    } else if (sourceID == Paladin.BW_HOLY_CHARGE || sourceID == Paladin.SWORD_HOLY_CHARGE) {
                        if (monster.getStats().getEffectiveness(Element.HOLY) == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.2 + level * 0.015;
                        }
                    }
                } else {
                    // Since we already know the skill has an elemental attribute, but we dont know if the monster is weak or not, lets
                    // take the safe approach and just assume they are weak.
                    calcDmgMax *= 1.5;
                }
            }

            if (ret.skill != 0) {
                Skill skill = SkillFactory.getSkill(ret.skill);
                if (skill.getElement() != Element.NEUTRAL && chr.getBuffedValue(BuffStat.ELEMENTAL_RESET) == null) {
                    // The skill has an element effect, so we need to factor that in.
                    if (monster != null) {
                        ElementalEffectiveness eff = monster.getElementalEffectiveness(skill.getElement());
                        if (eff == ElementalEffectiveness.WEAK) {
                            calcDmgMax *= 1.5;
                        } else if (eff == ElementalEffectiveness.STRONG) {
                            //calcDmgMax *= 0.5;
                        }
                    } else {
                        // Since we already know the skill has an elemental attribute, but we dont know if the monster is weak or not, lets
                        // take the safe approach and just assume they are weak.
                        calcDmgMax *= 1.5;
                    }
                }
                if (ret.skill == FPWizard.POISON_BREATH || ret.skill == FPMage.POISON_MIST || ret.skill == FPArchMage.FIRE_DEMON || ret.skill == ILArchMage.ICE_DEMON) {
                    if (monster != null) {
                        // Turns out poison is completely server side, so I don't know why I added this. >.<
                        //calcDmgMax = monster.getHp() / (70 - chr.getSkillLevel(skill));
                    }
                } else if (ret.skill == Hermit.SHADOW_WEB) {
                    if (monster != null) {
                        calcDmgMax = monster.getHp() / (50 - chr.getSkillLevel(skill));
                    }
                } else if (ret.skill == Hermit.SHADOW_MESO) {
                    if (monster != null) {
                        monster.debuffMob(Hermit.SHADOW_MESO);
                    }
                } else if (ret.skill == Aran.BODY_PRESSURE) {
                    if (monster != null) {
                        int bodyPressureDmg = (int) Math.ceil(monster.getMaxHp() * SkillFactory.getSkill(Aran.BODY_PRESSURE).getEffect(ret.skilllevel).getDamage() / 100.0);
                        if (bodyPressureDmg > calcDmgMax) {
                            calcDmgMax = bodyPressureDmg;
                        }
                    }
                }
            }

            for (int j = 0; j < ret.numDamage; j++) {
                int damage = p.readInt();
                long hitDmgMax = calcDmgMax;
                if (ret.skill == Buccaneer.BARRAGE || ret.skill == ThunderBreaker.BARRAGE) {
                    if (j > 3) {
                        hitDmgMax *= Math.pow(2, (j - 3));
                    }
                }
                if (shadowPartner) {
                    // For shadow partner, the second half of the hits only do 50% damage. So calc that
                    // in for the crit effects.
                    if (j >= ret.numDamage / 2) {
                        hitDmgMax *= 0.5;
                    }
                }

                if (ret.skill == Marksman.SNIPE) {
                    damage = 195000 + Randomizer.nextInt(5000);
                    hitDmgMax = 200000;
                } else if (ret.skill == Beginner.BAMBOO_RAIN || ret.skill == Noblesse.BAMBOO_RAIN || ret.skill == Evan.BAMBOO_THRUST || ret.skill == Legend.BAMBOO_THRUST) {
                    hitDmgMax = 82569000; // 30% of Max HP of strongest Dojo boss
                }

                long maxWithCrit = hitDmgMax;
                if (canCrit) // They can crit, so up the max.
                {
                    maxWithCrit *= 2;
                }

                // Warn if the damage is over 1.5x what we calculated above.
                if (damage > maxWithCrit * 1.5) {
                    AutobanFactory.DAMAGE_HACK.alert(chr, "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " + ret.skill + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapName() + " (" + chr.getMapId() + ")");
                }

                // Add a ab point if its over 5x what we calculated.
                if (damage > maxWithCrit * 5) {
                    AutobanFactory.DAMAGE_HACK.addPoint(chr.getAutoBanManager(), "DMG: " + damage + " MaxDMG: " + maxWithCrit + " SID: " + ret.skill + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapName() + " (" + chr.getMapId() + ")");
                }

                if (ret.skill == Marksman.SNIPE || (canCrit && damage > hitDmgMax)) {
                    // If the skill is a crit, inverse the damage to make it show up on clients.
                    damage = -Integer.MAX_VALUE + damage - 1;
                }

                if(effect != null) {
                    int maxattack = Math.max(effect.getBulletCount(), effect.getAttackCount());
                    if (shadowPartner) {
                        maxattack = maxattack * 2;
                    }
                    if (ret.numDamage > maxattack) {
                        AutobanFactory.DAMAGE_HACK.addPoint(chr.getAutoBanManager(), "Too many lines: " + ret.numDamage + " Max lines: " + maxattack + " SID: " + ret.skill + " MobID: " + (monster != null ? monster.getId() : "null") + " Map: " + chr.getMap().getMapName() + " (" + chr.getMapId() + ")");
                    }
                }

                allDamageNumbers.add(damage);
            }
            if (ret.skill != Corsair.RAPID_FIRE || ret.skill != Aran.HIDDEN_FULL_DOUBLE || ret.skill != Aran.HIDDEN_FULL_TRIPLE || ret.skill != Aran.HIDDEN_OVER_DOUBLE || ret.skill != Aran.HIDDEN_OVER_TRIPLE) {
                p.skip(4);
            }
            ret.allDamage.put(oid, allDamageNumbers);
        }
        if (ret.skill == NightWalker.POISON_BOMB) { // Poison Bomb
            p.skip(4);
            ret.position.setLocation(p.readShort(), p.readShort());
        }
        return ret;
    }

    private static int rand(int l, int u) {
        return (int) ((Math.random() * (u - l + 1)) + l);
    }
}
