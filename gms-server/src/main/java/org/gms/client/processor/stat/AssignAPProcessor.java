/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2019 RonanLana (HeavenMS)

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
package org.gms.client.processor.stat;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Job;
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.client.Stat;
import org.gms.client.autoban.AutobanFactory;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.config.GameConfig;
import org.gms.constants.skills.BlazeWizard;
import org.gms.constants.skills.Brawler;
import org.gms.constants.skills.DawnWarrior;
import org.gms.constants.skills.Magician;
import org.gms.constants.skills.ThunderBreaker;
import org.gms.constants.skills.Warrior;
import org.gms.net.packet.InPacket;
import org.gms.util.PacketCreator;
import org.gms.util.Randomizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author RonanLana - synchronization of AP transaction modules
 */
public class AssignAPProcessor {

    public static void APAutoAssignAction(InPacket inPacket, Client c) {
        Character chr = c.getPlayer();
        if (chr.getRemainingAp() < 1) {
            return;
        }

        Collection<Item> equippedC = chr.getInventory(InventoryType.EQUIPPED).list();

        c.lockClient();
        try {
            int[] statGain = new int[4];
            int[] statUpdate = new int[4];
            statGain[0] = 0;
            statGain[1] = 0;
            statGain[2] = 0;
            statGain[3] = 0;

            int remainingAp = chr.getRemainingAp();
            inPacket.skip(8);

            if (GameConfig.getServerBoolean("use_server_auto_assigner")) {
                // --------- Ronan Lana's AUTOASSIGNER ---------
                // This method excels for assigning APs in such a way to cover all equipments AP requirements.
                byte opt = inPacket.readByte();     // useful for pirate autoassigning

                int str = 0, dex = 0, luk = 0, int_ = 0;
                List<Short> eqpStrList = new ArrayList<>();
                List<Short> eqpDexList = new ArrayList<>();
                List<Short> eqpLukList = new ArrayList<>();

                Equip nEquip;

                for (Item item : equippedC) {   //selecting the biggest AP value of each stat from each equipped item.
                    nEquip = (Equip) item;
                    if (nEquip.getStr() > 0) {
                        eqpStrList.add(nEquip.getStr());
                    }
                    str += nEquip.getStr();

                    if (nEquip.getDex() > 0) {
                        eqpDexList.add(nEquip.getDex());
                    }
                    dex += nEquip.getDex();

                    if (nEquip.getLuk() > 0) {
                        eqpLukList.add(nEquip.getLuk());
                    }
                    luk += nEquip.getLuk();

                    //if(nEquip.getInt() > 0) eqpIntList.add(nEquip.getInt()); //not needed...
                    int_ += nEquip.getInt();
                }

                statUpdate[0] = chr.getStr();
                statUpdate[1] = chr.getDex();
                statUpdate[2] = chr.getLuk();
                statUpdate[3] = chr.getInt();

                eqpStrList.sort(Collections.reverseOrder());
                eqpDexList.sort(Collections.reverseOrder());
                eqpLukList.sort(Collections.reverseOrder());

                //Autoassigner looks up the 1st/2nd placed equips for their stats to calculate the optimal upgrade.
                int eqpStr = getNthHighestStat(eqpStrList, (short) 0) + getNthHighestStat(eqpStrList, (short) 1);
                int eqpDex = getNthHighestStat(eqpDexList, (short) 0) + getNthHighestStat(eqpDexList, (short) 1);
                int eqpLuk = getNthHighestStat(eqpLukList, (short) 0) + getNthHighestStat(eqpLukList, (short) 1);

                //c.getPlayer().message("----------------------------------------");
                //c.getPlayer().message("SDL: s" + eqpStr + " d" + eqpDex + " l" + eqpLuk + " BASE STATS --> STR: " + chr.getStr() + " DEX: " + chr.getDex() + " INT: " + chr.getInt() + " LUK: " + chr.getLuk());
                //c.getPlayer().message("SUM EQUIP STATS -> STR: " + str + " DEX: " + dex + " LUK: " + luk + " INT: " + int_);

                Job stance = c.getPlayer().getJobStyle(opt);
                int prStat = 0, scStat = 0, trStat = 0, temp, tempAp = remainingAp, CAP;
                if (tempAp < 1) {
                    return;
                }

                Stat primary, secondary, tertiary = Stat.LUK;
                switch (stance) {
                    case MAGICIAN:
                        CAP = 165;
                        scStat = (chr.getLevel() + 3) - (chr.getLuk() + luk - eqpLuk);
                        if (scStat < 0) {
                            scStat = 0;
                        }
                        scStat = Math.min(scStat, tempAp);

                        if (tempAp > scStat) {
                            tempAp -= scStat;
                        } else {
                            tempAp = 0;
                        }

                        prStat = tempAp;
                        int_ = prStat;
                        luk = scStat;
                        str = 0;
                        dex = 0;

                        if (GameConfig.getServerBoolean("use_auto_assign_secondary_cap") && luk + chr.getLuk() > CAP) {
                            temp = luk + chr.getLuk() - CAP;
                            scStat -= temp;
                            prStat += temp;
                        }

                        primary = Stat.INT;
                        secondary = Stat.LUK;
                        tertiary = Stat.DEX;

                        break;

                    case BOWMAN:
                        CAP = 125;
                        scStat = (chr.getLevel() + 5) - (chr.getStr() + str - eqpStr);
                        if (scStat < 0) {
                            scStat = 0;
                        }
                        scStat = Math.min(scStat, tempAp);

                        if (tempAp > scStat) {
                            tempAp -= scStat;
                        } else {
                            tempAp = 0;
                        }

                        prStat = tempAp;
                        dex = prStat;
                        str = scStat;
                        int_ = 0;
                        luk = 0;

                        if (GameConfig.getServerBoolean("use_auto_assign_secondary_cap") && str + chr.getStr() > CAP) {
                            temp = str + chr.getStr() - CAP;
                            scStat -= temp;
                            prStat += temp;
                        }

                        primary = Stat.DEX;
                        secondary = Stat.STR;

                        break;

                    case GUNSLINGER:
                    case CROSSBOWMAN:
                        CAP = 120;
                        scStat = chr.getLevel() - (chr.getStr() + str - eqpStr);
                        if (scStat < 0) {
                            scStat = 0;
                        }
                        scStat = Math.min(scStat, tempAp);

                        if (tempAp > scStat) {
                            tempAp -= scStat;
                        } else {
                            tempAp = 0;
                        }

                        prStat = tempAp;
                        dex = prStat;
                        str = scStat;
                        int_ = 0;
                        luk = 0;

                        if (GameConfig.getServerBoolean("use_auto_assign_secondary_cap") && str + chr.getStr() > CAP) {
                            temp = str + chr.getStr() - CAP;
                            scStat -= temp;
                            prStat += temp;
                        }

                        primary = Stat.DEX;
                        secondary = Stat.STR;

                        break;

                    case THIEF:
                        CAP = 160;

                        scStat = 0;
                        if (chr.getDex() < 80) {
                            scStat = (2 * chr.getLevel()) - (chr.getDex() + dex - eqpDex);
                            if (scStat < 0) {
                                scStat = 0;
                            }

                            scStat = Math.min(80 - chr.getDex(), scStat);
                            scStat = Math.min(tempAp, scStat);
                            tempAp -= scStat;
                        }

                        temp = (chr.getLevel() + 40) - Math.max(80, scStat + chr.getDex() + dex - eqpDex);
                        if (temp < 0) {
                            temp = 0;
                        }
                        temp = Math.min(tempAp, temp);
                        scStat += temp;
                        tempAp -= temp;

                        // thieves will upgrade STR as well only if a level-based threshold is reached.
                        if (chr.getStr() >= Math.max(13, (int) (0.4 * chr.getLevel()))) {
                            if (chr.getStr() < 50) {
                                trStat = (chr.getLevel() - 10) - (chr.getStr() + str - eqpStr);
                                if (trStat < 0) {
                                    trStat = 0;
                                }

                                trStat = Math.min(50 - chr.getStr(), trStat);
                                trStat = Math.min(tempAp, trStat);
                                tempAp -= trStat;
                            }

                            temp = (20 + (chr.getLevel() / 2)) - Math.max(50, trStat + chr.getStr() + str - eqpStr);
                            if (temp < 0) {
                                temp = 0;
                            }
                            temp = Math.min(tempAp, temp);
                            trStat += temp;
                            tempAp -= temp;
                        }

                        prStat = tempAp;
                        luk = prStat;
                        dex = scStat;
                        str = trStat;
                        int_ = 0;

                        if (GameConfig.getServerBoolean("use_auto_assign_secondary_cap") && dex + chr.getDex() > CAP) {
                            temp = dex + chr.getDex() - CAP;
                            scStat -= temp;
                            prStat += temp;
                        }
                        if (GameConfig.getServerBoolean("use_auto_assign_secondary_cap") && str + chr.getStr() > CAP) {
                            temp = str + chr.getStr() - CAP;
                            trStat -= temp;
                            prStat += temp;
                        }

                        primary = Stat.LUK;
                        secondary = Stat.DEX;
                        tertiary = Stat.STR;

                        break;

                    case BRAWLER:
                    default:    //warrior, beginner, ...
                        CAP = 300;

                        boolean highDex = false;    // thanks lucasziron & Vcoc for finding out DEX autoassigning poorly for STR-based characters
                        if (chr.getLevel() < 40) {
                            if (chr.getDex() >= (2 * chr.getLevel()) + 2) {
                                highDex = true;
                            }
                        } else {
                            if (chr.getDex() >= chr.getLevel() + 42) {
                                highDex = true;
                            }
                        }

                        // other classes will start favoring more DEX only if a level-based threshold is reached.
                        if (!highDex) {
                            scStat = 0;
                            if (chr.getDex() < 80) {
                                scStat = (2 * chr.getLevel()) - (chr.getDex() + dex - eqpDex);
                                if (scStat < 0) {
                                    scStat = 0;
                                }

                                scStat = Math.min(80 - chr.getDex(), scStat);
                                scStat = Math.min(tempAp, scStat);
                                tempAp -= scStat;
                            }

                            temp = (chr.getLevel() + 40) - Math.max(80, scStat + chr.getDex() + dex - eqpDex);
                            if (temp < 0) {
                                temp = 0;
                            }
                            temp = Math.min(tempAp, temp);
                            scStat += temp;
                            tempAp -= temp;
                        } else {
                            scStat = 0;
                            if (chr.getDex() < 96) {
                                scStat = (int) (2.4 * chr.getLevel()) - (chr.getDex() + dex - eqpDex);
                                if (scStat < 0) {
                                    scStat = 0;
                                }

                                scStat = Math.min(96 - chr.getDex(), scStat);
                                scStat = Math.min(tempAp, scStat);
                                tempAp -= scStat;
                            }

                            temp = 96 + (int) (1.2 * (chr.getLevel() - 40)) - Math.max(96, scStat + chr.getDex() + dex - eqpDex);
                            if (temp < 0) {
                                temp = 0;
                            }
                            temp = Math.min(tempAp, temp);
                            scStat += temp;
                            tempAp -= temp;
                        }

                        prStat = tempAp;
                        str = prStat;
                        dex = scStat;
                        int_ = 0;
                        luk = 0;

                        if (GameConfig.getServerBoolean("use_auto_assign_secondary_cap") && dex + chr.getDex() > CAP) {
                            temp = dex + chr.getDex() - CAP;
                            scStat -= temp;
                            prStat += temp;
                        }

                        primary = Stat.STR;
                        secondary = Stat.DEX;
                }

                //-------------------------------------------------------------------------------------

                int extras = 0;

                extras = gainStatByType(primary, statGain, prStat + extras, statUpdate);
                extras = gainStatByType(secondary, statGain, scStat + extras, statUpdate);
                extras = gainStatByType(tertiary, statGain, trStat + extras, statUpdate);

                if (extras > 0) {    //redistribute surplus in priority order
                    extras = gainStatByType(primary, statGain, extras, statUpdate);
                    extras = gainStatByType(secondary, statGain, extras, statUpdate);
                    extras = gainStatByType(tertiary, statGain, extras, statUpdate);
                    gainStatByType(getQuaternaryStat(stance), statGain, extras, statUpdate);
                }

                chr.assignStrDexIntLuk(statGain[0], statGain[1], statGain[3], statGain[2]);
                c.sendPacket(PacketCreator.enableActions());

                //----------------------------------------------------------------------------------------

                c.sendPacket(PacketCreator.serverNotice(1, "Better AP applications detected:\r\nSTR: +" + statGain[0] + "\r\nDEX: +" + statGain[1] + "\r\nINT: +" + statGain[3] + "\r\nLUK: +" + statGain[2]));
            } else {
                if (inPacket.available() < 16) {
                    AutobanFactory.PACKET_EDIT.alert(chr, "Didn't send full packet for Auto Assign.");

                    c.disconnect(true, false);
                    return;
                }

                for (int i = 0; i < 2; i++) {
                    int type = inPacket.readInt();
                    int tempVal = inPacket.readInt();
                    if (tempVal < 0 || tempVal > remainingAp) {
                        return;
                    }

                    gainStatByType(Stat.getBy5ByteEncoding(type), statGain, tempVal, statUpdate);
                }

                chr.assignStrDexIntLuk(statGain[0], statGain[1], statGain[3], statGain[2]);
                c.sendPacket(PacketCreator.enableActions());
            }
        } finally {
            c.unlockClient();
        }
    }

    private static int getNthHighestStat(List<Short> statList, short rank) {    // ranks from 0
        return (statList.size() <= rank ? 0 : statList.get(rank));
    }

    private static int gainStatByType(Stat type, int[] statGain, int gain, int[] statUpdate) {
        if (gain <= 0) {
            return 0;
        }

        int newVal = 0;
        switch (type) {
        case STR:
            newVal = statUpdate[0] + gain;
            if (newVal > GameConfig.getServerInt("max_ap")) {
                statGain[0] += (gain - (newVal - GameConfig.getServerInt("max_ap")));
                statUpdate[0] = GameConfig.getServerInt("max_ap");
            } else {
                statGain[0] += gain;
                statUpdate[0] = newVal;
            }
            break;
        case INT:
            newVal = statUpdate[3] + gain;
            if (newVal > GameConfig.getServerInt("max_ap")) {
                statGain[3] += (gain - (newVal - GameConfig.getServerInt("max_ap")));
                statUpdate[3] = GameConfig.getServerInt("max_ap");
            } else {
                statGain[3] += gain;
                statUpdate[3] = newVal;
            }
            break;
        case LUK:
            newVal = statUpdate[2] + gain;
            if (newVal > GameConfig.getServerInt("max_ap")) {
                statGain[2] += (gain - (newVal - GameConfig.getServerInt("max_ap")));
                statUpdate[2] = GameConfig.getServerInt("max_ap");
            } else {
                statGain[2] += gain;
                statUpdate[2] = newVal;
            }
            break;
        case DEX:
            newVal = statUpdate[1] + gain;
            if (newVal > GameConfig.getServerInt("max_ap")) {
                statGain[1] += (gain - (newVal - GameConfig.getServerInt("max_ap")));
                statUpdate[1] = GameConfig.getServerInt("max_ap");
            } else {
                statGain[1] += gain;
                statUpdate[1] = newVal;
            }
            break;
        }

        if (newVal > GameConfig.getServerInt("max_ap")) {
            return newVal - GameConfig.getServerInt("max_ap");
        }
        return 0;
    }

    private static Stat getQuaternaryStat(Job stance) {
        if (stance != Job.MAGICIAN) {
            return Stat.INT;
        }
        return Stat.STR;
    }

    public static boolean APResetAction(Client c, int APFrom, int APTo) {
        c.lockClient();
        try {
            Character player = c.getPlayer();

            switch (APFrom) {
                case 64: // str
                    if (player.getStr() < 5) {
                        player.message("You don't have the minimum STR required to swap.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }
                    if (!player.assignStr(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }
                    break;
                case 128: // dex
                    if (player.getDex() < 5) {
                        player.message("You don't have the minimum DEX required to swap.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }
                    if (!player.assignDex(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }
                    break;
                case 256: // int
                    if (player.getInt() < 5) {
                        player.message("You don't have the minimum INT required to swap.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }
                    if (!player.assignInt(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }
                    break;
                case 512: // luk
                    if (player.getLuk() < 5) {
                        player.message("You don't have the minimum LUK required to swap.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }
                    if (!player.assignLuk(-1)) {
                        player.message("Couldn't execute AP reset operation.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }
                    break;
                case 2048: // HP
                    if (GameConfig.getServerBoolean("use_enforce_hpmp_swap")) {
                        if (APTo != 8192) {
                            player.message("You can only swap HP ability points to MP.");
                            c.sendPacket(PacketCreator.enableActions());
                            return false;
                        }
                    }

                    if (player.getHpMpApUsed() < 1) {
                        player.message("You don't have enough HPMP stat points to spend on AP Reset.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }

                    int hp = player.getMaxHp();
                    int level_ = player.getLevel();
                    if (hp < level_ * 14 + 148) {
                        player.message("You don't have the minimum HP pool required to swap.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }

                    int curHp = player.getHp();
                    int hplose = -takeHp(player.getJob());
                    player.assignHP(hplose, -1);
                    if (!GameConfig.getServerBoolean("use_fixed_ratio_hpmp_update")) {
                        player.updateHp(Math.max(1, curHp + hplose));
                    }

                    break;
                case 8192: // MP
                    if (GameConfig.getServerBoolean("use_enforce_hpmp_swap")) {
                        if (APTo != 2048) {
                            player.message("You can only swap MP ability points to HP.");
                            c.sendPacket(PacketCreator.enableActions());
                            return false;
                        }
                    }

                    if (player.getHpMpApUsed() < 1) {
                        player.message("You don't have enough HPMP stat points to spend on AP Reset.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }

                    int mp = player.getMaxMp();
                    int level = player.getLevel();
                    Job job = player.getJob();

                    boolean canWash = true;
                    if (job.isA(Job.SPEARMAN) && mp < 4 * level + 156) {
                        canWash = false;
                    } else if ((job.isA(Job.FIGHTER) || job.isA(Job.ARAN1)) && mp < 4 * level + 56) {
                        canWash = false;
                    } else if (job.isA(Job.THIEF) && job.getId() % 100 > 0 && mp < level * 14 - 4) {
                        canWash = false;
                    } else if (mp < level * 14 + 148) {
                        canWash = false;
                    }

                    if (!canWash) {
                        player.message("You don't have the minimum MP pool required to swap.");
                        c.sendPacket(PacketCreator.enableActions());
                        return false;
                    }

                    int curMp = player.getMp();
                    int mplose = -takeMp(job);
                    player.assignMP(mplose, -1);
                    if (!GameConfig.getServerBoolean("use_fixed_ratio_hpmp_update")) {
                        player.updateMp(Math.max(0, curMp + mplose));
                    }
                    break;
                default:
                    c.sendPacket(PacketCreator.updatePlayerStats(PacketCreator.EMPTY_STATUPDATE, true, player));
                    return false;
            }

            addStat(player, APTo, true);
            return true;
        } finally {
            c.unlockClient();
        }
    }

    public static void APAssignAction(Client c, int num) {
        c.lockClient();
        try {
            addStat(c.getPlayer(), num, false);
        } finally {
            c.unlockClient();
        }
    }

    private static boolean addStat(Character chr, int apTo, boolean usedAPReset) {
        switch (apTo) {
            case 64:
                if (!chr.assignStr(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(PacketCreator.enableActions());
                    return false;
                }
                break;
            case 128: // Dex
                if (!chr.assignDex(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(PacketCreator.enableActions());
                    return false;
                }
                break;
            case 256: // Int
                if (!chr.assignInt(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(PacketCreator.enableActions());
                    return false;
                }
                break;
            case 512: // Luk
                if (!chr.assignLuk(1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(PacketCreator.enableActions());
                    return false;
                }
                break;
            case 2048:
                if (!chr.assignHP(calcHpChange(chr, usedAPReset), 1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(PacketCreator.enableActions());
                    return false;
                }
                break;
            case 8192:
                if (!chr.assignMP(calcMpChange(chr, usedAPReset), 1)) {
                    chr.message("Couldn't execute AP assign operation.");
                    chr.sendPacket(PacketCreator.enableActions());
                    return false;
                }
                break;
            default:
                chr.sendPacket(PacketCreator.updatePlayerStats(PacketCreator.EMPTY_STATUPDATE, true, chr));
                return false;
        }
        return true;
    }

    private static int calcHpChange(Character player, boolean usedAPReset) {
        Job job = player.getJob();
        int MaxHP = 0;

        if (job.isA(Job.WARRIOR) || job.isA(Job.DAWNWARRIOR1)) {
            if (!usedAPReset) {
                Skill increaseHP = SkillFactory.getSkill(job.isA(Job.DAWNWARRIOR1) ? DawnWarrior.MAX_HP_INCREASE : Warrior.IMPROVED_MAXHP);
                int sLvl = player.getSkillLevel(increaseHP);

                if (sLvl > 0) {
                    MaxHP += increaseHP.getEffect(sLvl).getY();
                }
            }

            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (usedAPReset) {
                    MaxHP += 20;
                } else {
                    MaxHP += Randomizer.rand(18, 22);
                }
            } else {
                MaxHP += 20;
            }
        } else if (job.isA(Job.ARAN1)) {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (usedAPReset) {
                    MaxHP += 20;
                } else {
                    MaxHP += Randomizer.rand(26, 30);
                }
            } else {
                MaxHP += 28;
            }
        } else if (job.isA(Job.MAGICIAN) || job.isA(Job.BLAZEWIZARD1)) {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (usedAPReset) {
                    MaxHP += 6;
                } else {
                    MaxHP += Randomizer.rand(5, 9);
                }
            } else {
                MaxHP += 6;
            }
        } else if (job.isA(Job.THIEF) || job.isA(Job.NIGHTWALKER1)) {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (usedAPReset) {
                    MaxHP += 16;
                } else {
                    MaxHP += Randomizer.rand(14, 18);
                }
            } else {
                MaxHP += 16;
            }
        } else if (job.isA(Job.BOWMAN) || job.isA(Job.WINDARCHER1)) {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (usedAPReset) {
                    MaxHP += 16;
                } else {
                    MaxHP += Randomizer.rand(14, 18);
                }
            } else {
                MaxHP += 16;
            }
        } else if (job.isA(Job.PIRATE) || job.isA(Job.THUNDERBREAKER1)) {
            if (!usedAPReset) {
                Skill increaseHP = SkillFactory.getSkill(job.isA(Job.PIRATE) ? Brawler.IMPROVE_MAX_HP : ThunderBreaker.IMPROVE_MAX_HP);
                int sLvl = player.getSkillLevel(increaseHP);

                if (sLvl > 0) {
                    MaxHP += increaseHP.getEffect(sLvl).getY();
                }
            }

            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (usedAPReset) {
                    MaxHP += 18;
                } else {
                    MaxHP += Randomizer.rand(16, 20);
                }
            } else {
                MaxHP += 18;
            }
        } else if (usedAPReset) {
            MaxHP += 8;
        } else {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                MaxHP += Randomizer.rand(8, 12);
            } else {
                MaxHP += 10;
            }
        }

        return MaxHP;
    }

    private static int calcMpChange(Character player, boolean usedAPReset) {
        Job job = player.getJob();
        int MaxMP = 0;

        if (job.isA(Job.WARRIOR) || job.isA(Job.DAWNWARRIOR1) || job.isA(Job.ARAN1)) {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (!usedAPReset) {
                    MaxMP += (Randomizer.rand(2, 4) + (player.getInt() / 10));
                } else {
                    MaxMP += 2;
                }
            } else {
                MaxMP += 3;
            }
        } else if (job.isA(Job.MAGICIAN) || job.isA(Job.BLAZEWIZARD1)) {
            if (!usedAPReset) {
                Skill increaseMP = SkillFactory.getSkill(job.isA(Job.BLAZEWIZARD1) ? BlazeWizard.INCREASING_MAX_MP : Magician.IMPROVED_MAX_MP_INCREASE);
                int sLvl = player.getSkillLevel(increaseMP);

                if (sLvl > 0) {
                    MaxMP += increaseMP.getEffect(sLvl).getY();
                }
            }

            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (!usedAPReset) {
                    MaxMP += (Randomizer.rand(12, 16) + (player.getInt() / 20));
                } else {
                    MaxMP += 18;
                }
            } else {
                MaxMP += 18;
            }
        } else if (job.isA(Job.BOWMAN) || job.isA(Job.WINDARCHER1)) {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (!usedAPReset) {
                    MaxMP += (Randomizer.rand(6, 8) + (player.getInt() / 10));
                } else {
                    MaxMP += 10;
                }
            } else {
                MaxMP += 10;
            }
        } else if (job.isA(Job.THIEF) || job.isA(Job.NIGHTWALKER1)) {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (!usedAPReset) {
                    MaxMP += (Randomizer.rand(6, 8) + (player.getInt() / 10));
                } else {
                    MaxMP += 10;
                }
            } else {
                MaxMP += 10;
            }
        } else if (job.isA(Job.PIRATE) || job.isA(Job.THUNDERBREAKER1)) {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (!usedAPReset) {
                    MaxMP += (Randomizer.rand(7, 9) + (player.getInt() / 10));
                } else {
                    MaxMP += 14;
                }
            } else {
                MaxMP += 14;
            }
        } else {
            if (GameConfig.getServerBoolean("use_randomize_hpmp_gain")) {
                if (!usedAPReset) {
                    MaxMP += (Randomizer.rand(4, 6) + (player.getInt() / 10));
                } else {
                    MaxMP += 6;
                }
            } else {
                MaxMP += 6;
            }
        }

        return MaxMP;
    }

    private static int takeHp(Job job) {
        int MaxHP = 0;

        if (job.isA(Job.WARRIOR) || job.isA(Job.DAWNWARRIOR1) || job.isA(Job.ARAN1)) {
            MaxHP += 54;
        } else if (job.isA(Job.MAGICIAN) || job.isA(Job.BLAZEWIZARD1)) {
            MaxHP += 10;
        } else if (job.isA(Job.THIEF) || job.isA(Job.NIGHTWALKER1)) {
            MaxHP += 20;
        } else if (job.isA(Job.BOWMAN) || job.isA(Job.WINDARCHER1)) {
            MaxHP += 20;
        } else if (job.isA(Job.PIRATE) || job.isA(Job.THUNDERBREAKER1)) {
            MaxHP += 42;
        } else {
            MaxHP += 12;
        }

        return MaxHP;
    }

    private static int takeMp(Job job) {
        int MaxMP = 0;

        if (job.isA(Job.WARRIOR) || job.isA(Job.DAWNWARRIOR1) || job.isA(Job.ARAN1)) {
            MaxMP += 4;
        } else if (job.isA(Job.MAGICIAN) || job.isA(Job.BLAZEWIZARD1)) {
            MaxMP += 31;
        } else if (job.isA(Job.BOWMAN) || job.isA(Job.WINDARCHER1)) {
            MaxMP += 12;
        } else if (job.isA(Job.THIEF) || job.isA(Job.NIGHTWALKER1)) {
            MaxMP += 12;
        } else if (job.isA(Job.PIRATE) || job.isA(Job.THUNDERBREAKER1)) {
            MaxMP += 16;
        } else {
            MaxMP += 8;
        }

        return MaxMP;
    }

}
