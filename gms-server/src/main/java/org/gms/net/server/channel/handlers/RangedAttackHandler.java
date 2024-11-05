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
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.WeaponType;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.config.GameConfig;
import org.gms.constants.id.ItemId;
import org.gms.constants.id.MapId;
import org.gms.constants.inventory.ItemConstants;
import org.gms.constants.skills.Aran;
import org.gms.constants.skills.Buccaneer;
import org.gms.constants.skills.NightLord;
import org.gms.constants.skills.NightWalker;
import org.gms.constants.skills.Shadower;
import org.gms.constants.skills.ThunderBreaker;
import org.gms.constants.skills.WindArcher;
import org.gms.net.packet.InPacket;
import org.gms.net.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.ItemInformationProvider;
import org.gms.server.StatEffect;
import org.gms.util.PacketCreator;
import org.gms.util.Randomizer;

import static java.util.concurrent.TimeUnit.SECONDS;


public final class RangedAttackHandler extends AbstractDealDamageHandler {
    private static final Logger log = LoggerFactory.getLogger(RangedAttackHandler.class);

    @Override
    public void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();
        
        /*long timeElapsed = currentServerTime() - chr.getAutobanManager().getLastSpam(8);
        if(timeElapsed < 300) {
            AutobanFactory.FAST_ATTACK.alert(chr, "Time: " + timeElapsed);
        }
        chr.getAutobanManager().spam(8);*/

        AttackInfo attack = parseDamage(p, chr, true, false);

        if (chr.getBuffEffect(BuffStat.MORPH) != null) {
            if (chr.getBuffEffect(BuffStat.MORPH).isMorphWithoutAttack()) {
                // How are they attacking when the client won't let them?
                chr.getClient().disconnect(false, false);
                return;
            }
        }

        if (MapId.isDojo(chr.getMap().getId()) && attack.numAttacked > 0) {
            chr.setDojoEnergy(chr.getDojoEnergy() + GameConfig.getServerInt("dojo_energy_atk"));
            c.sendPacket(PacketCreator.getEnergy("energy", chr.getDojoEnergy()));
        }

        if (attack.skill == Buccaneer.ENERGY_ORB || attack.skill == ThunderBreaker.SPARK || attack.skill == Shadower.TAUNT || attack.skill == NightLord.TAUNT) {
            chr.getMap().broadcastMessage(chr, PacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, 0, attack.allDamage, attack.speed, attack.direction, attack.display), false);
            applyAttack(attack, chr, 1);
        } else if (attack.skill == ThunderBreaker.SHARK_WAVE && chr.getSkillLevel(ThunderBreaker.SHARK_WAVE) > 0) {
            chr.getMap().broadcastMessage(chr, PacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, 0, attack.allDamage, attack.speed, attack.direction, attack.display), false);
            applyAttack(attack, chr, 1);

            for (int i = 0; i < attack.numAttacked; i++) {
                chr.handleEnergyChargeGain();
            }
        } else if (attack.skill == Aran.COMBO_SMASH || attack.skill == Aran.COMBO_FENRIR || attack.skill == Aran.COMBO_TEMPEST) {
            chr.getMap().broadcastMessage(chr, PacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, 0, attack.allDamage, attack.speed, attack.direction, attack.display), false);
            if (attack.skill == Aran.COMBO_SMASH && chr.getCombo() >= 30) {
                chr.setCombo((short) 0);
                applyAttack(attack, chr, 1);
            } else if (attack.skill == Aran.COMBO_FENRIR && chr.getCombo() >= 100) {
                chr.setCombo((short) 0);
                applyAttack(attack, chr, 2);
            } else if (attack.skill == Aran.COMBO_TEMPEST && chr.getCombo() >= 200) {
                chr.setCombo((short) 0);
                applyAttack(attack, chr, 4);
            }
        } else {
            Item weapon = chr.getInventory(InventoryType.EQUIPPED).getItem((short) -11);
            WeaponType type = ItemInformationProvider.getInstance().getWeaponType(weapon.getItemId());
            if (type == WeaponType.NOT_A_WEAPON) {
                return;
            }
            short slot = -1;
            int projectile = 0;
            short bulletCount = 1;
            short supplement = 0;   //用于补充平衡之怒的变量
            StatEffect effect = null;
            if (attack.skill != 0) {
                effect = attack.getAttackEffect(chr, null);
                bulletCount = effect.getBulletCount();
                if (effect.getCooldown() > 0) {
                    c.sendPacket(PacketCreator.skillCooldown(attack.skill, effect.getCooldown()));
                }

                if (attack.skill == 4111004) {   // shadow meso
                    bulletCount = 0;

                    int money = effect.getMoneyCon();
                    if (money != 0) {
                        int moneyMod = money / 2;
                        money += Randomizer.nextInt(moneyMod);
                        if (money > chr.getMeso()) {
                            money = chr.getMeso();
                        }
                        chr.gainMeso(-money, false);
                    }
                }
            }
            boolean hasShadowPartner = chr.getBuffedValue(BuffStat.SHADOWPARTNER) != null;
            if (hasShadowPartner) {
                bulletCount *= 2;
            }
            Inventory inv = chr.getInventory(InventoryType.USE);
            for (short i = 1; i <= inv.getSlotLimit(); i++) {
                Item item = inv.getItem(i);
                if (item != null) {
                    int id = item.getItemId();
                    slot = item.getPosition();

                    boolean bow = ItemConstants.isArrowForBow(id);
                    boolean cbow = ItemConstants.isArrowForCrossBow(id);

                    if (id == ItemId.BALANCED_FURY && (item.getQuantity() - bulletCount) <= 10) {   //平衡之怒低于10，则自动补充，如果设置数值过低时，会造成出拳平A
                        supplement = (short) -ItemInformationProvider.getInstance().getSlotMax(c,id);  //设定补充到限制的最高数值
                    }

                    if (item.getQuantity() >= bulletCount) { //Fixes the bug where you can't use your last arrow.
                        if (type == WeaponType.CLAW && ItemConstants.isThrowingStar(id) && weapon.getItemId() != ItemId.MAGICAL_MITTEN) {
                            //这段判断不知道干啥用的，里面又没有内容，看样子是判定 物品ID = 月牙镖 或 平衡之怒 且 等级小于70，或 月牙镖 且 等级小于50
                            if (((id == ItemId.HWABI_THROWING_STARS || id == ItemId.BALANCED_FURY) && chr.getLevel() < 70) || (id == ItemId.CRYSTAL_ILBI_THROWING_STARS && chr.getLevel() < 50)) {
                            } else {
                                projectile = id;
                                break;
                            }
                        } else if ((type == WeaponType.GUN && ItemConstants.isBullet(id))) {
                            if (id == ItemId.BLAZE_CAPSULE || id == ItemId.GLAZE_CAPSULE) {
                                if (chr.getLevel() >= 70) {
                                    projectile = id;
                                    break;
                                }
                            } else if (chr.getLevel() > (id % 10) * 20 + 9) {
                                projectile = id;
                                break;
                            }
                        } else if ((type == WeaponType.BOW && bow) || (type == WeaponType.CROSSBOW && cbow) || (weapon.getItemId() == ItemId.MAGICAL_MITTEN && (bow || cbow))) {
                            projectile = id;
                            break;
                        }
                    }
                }
            }
            boolean soulArrow = chr.getBuffedValue(BuffStat.SOULARROW) != null;
            boolean shadowClaw = chr.getBuffedValue(BuffStat.SHADOW_CLAW) != null;
            if (projectile != 0) {
                if (!soulArrow && !shadowClaw && attack.skill != 11101004 && attack.skill != 15111007 && attack.skill != 14101006) {
                    short bulletConsume = bulletCount;

                    if (effect != null && effect.getBulletConsume() != 0) {
                        bulletConsume = (byte) (effect.getBulletConsume() * (hasShadowPartner ? 2 : 1));
                    }
                    if (supplement < 0 ) {
                        bulletConsume = supplement;     //将消耗飞镖设为补充飞镖
                    }
                    if (slot < 0) {
                        log.warn("<ERROR> Projectile to use was unable to be found.");
                    } else {
                        InventoryManipulator.removeFromSlot(c, InventoryType.USE, slot, bulletConsume, false, true);    //减去消耗品指定栏位物品数量
                    }
                }
            }

            if (projectile != 0 || soulArrow || attack.skill == 11101004 || attack.skill == 15111007 || attack.skill == 14101006 || attack.skill == 4111004 || attack.skill == 13101005) {
                int visProjectile = projectile; //visible projectile sent to players
                if (ItemConstants.isThrowingStar(projectile)) {
                    Inventory cash = chr.getInventory(InventoryType.CASH);
                    for (int i = 1; i <= cash.getSlotLimit(); i++) { // impose order...
                        Item item = cash.getItem((short) i);
                        if (item != null) {
                            if (item.getItemId() / 1000 == 5021) {
                                visProjectile = item.getItemId();
                                break;
                            }
                        }
                    }
                } else if (soulArrow || attack.skill == 3111004 || attack.skill == 3211004 || attack.skill == 11101004 || attack.skill == 15111007 || attack.skill == 14101006 || attack.skill == 13101005) {
                    visProjectile = 0;
                }

                final Packet packet;
                switch (attack.skill) {
                    case 3121004: // Hurricane
                    case 3221001: // Pierce
                    case 5221004: // Rapid Fire
                    case 13111002: // KoC Hurricane
                        packet = PacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.rangedirection, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed, attack.direction, attack.display);
                        break;
                    default:
                        packet = PacketCreator.rangedAttack(chr, attack.skill, attack.skilllevel, attack.stance, attack.numAttackedAndDamage, visProjectile, attack.allDamage, attack.speed, attack.direction, attack.display);
                        break;
                }
                chr.getMap().broadcastMessage(chr, packet, false, true);

                if (attack.skill != 0) {
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

                if (chr.getSkillLevel(SkillFactory.getSkill(NightWalker.VANISH)) > 0 && chr.getBuffedValue(BuffStat.DARKSIGHT) != null && attack.numAttacked > 0 && chr.getBuffSource(BuffStat.DARKSIGHT) != 9101004) {
                    chr.cancelEffectFromBuffStat(BuffStat.DARKSIGHT);
                    chr.cancelBuffStats(BuffStat.DARKSIGHT);
                } else if (chr.getSkillLevel(SkillFactory.getSkill(WindArcher.WIND_WALK)) > 0 && chr.getBuffedValue(BuffStat.WIND_WALK) != null && attack.numAttacked > 0) {
                    chr.cancelEffectFromBuffStat(BuffStat.WIND_WALK);
                    chr.cancelBuffStats(BuffStat.WIND_WALK);
                }

                applyAttack(attack, chr, bulletCount);
            }
        }
    }
}
