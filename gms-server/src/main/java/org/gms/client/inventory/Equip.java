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
package org.gms.client.inventory;

import org.gms.client.Client;
import org.gms.config.GameConfig;
import org.gms.constants.game.ExpTable;
import org.gms.constants.inventory.ItemConstants;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;
import org.gms.util.Randomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.ItemInformationProvider;
import org.gms.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Equip extends Item {
    private static final Logger log = LoggerFactory.getLogger(Equip.class);

    public enum ScrollResult {

        FAIL(0), SUCCESS(1), CURSE(2);
        private int value = -1;

        ScrollResult(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public enum StatUpgrade {

        incDEX(0), incSTR(1), incINT(2), incLUK(3),
        incMHP(4), incMMP(5), incPAD(6), incMAD(7),
        incPDD(8), incMDD(9), incEVA(10), incACC(11),
        incSpeed(12), incJump(13), incVicious(14), incSlot(15);
        private int value = -1;

        StatUpgrade(int value) {
            this.value = value;
        }
    }

    private byte upgradeSlots;
    private byte level, itemLevel;
    private short flag;
    private short str, dex, _int, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, vicious;
    private float itemExp;
    private int ringid = -1;
    private boolean wear = false;
    private boolean isUpgradeable, isElemental = false;    // timeless or reverse, or any equip that could levelup on GMS for all effects

    public Equip(int id, short position) {
        this(id, position, 0);
    }

    public Equip(int id, short position, int slots) {
        super(id, position, (short) 1);
        this.upgradeSlots = (byte) slots;
        this.itemExp = 0;
        this.itemLevel = 1;

        this.isElemental = (ItemInformationProvider.getInstance().getEquipLevel(id, false) > 1);
    }

    @Override
    public Item copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getUpgradeSlots());
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.flag = flag;
        ret.vicious = vicious;
        ret.upgradeSlots = upgradeSlots;
        ret.itemLevel = itemLevel;
        ret.itemExp = itemExp;
        ret.level = level;
        ret.itemLog = new LinkedList<>(itemLog);
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());
        ret.setGiftFrom(getGiftFrom());
        return ret;
    }

    @Override
    public short getFlag() {
        return flag;
    }

    @Override
    public byte getItemType() {
        return 1;
    }

    public byte getUpgradeSlots() {
        return upgradeSlots;
    }

    public short getStr() {
        return str;
    }

    public short getDex() {
        return dex;
    }

    public short getInt() {
        return _int;
    }

    public short getLuk() {
        return luk;
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public short getWatk() {
        return watk;
    }

    public short getMatk() {
        return matk;
    }

    public short getWdef() {
        return wdef;
    }

    public short getMdef() {
        return mdef;
    }

    public short getAcc() {
        return acc;
    }

    public short getAvoid() {
        return avoid;
    }

    public short getHands() {
        return hands;
    }

    public short getSpeed() {
        return speed;
    }

    public short getJump() {
        return jump;
    }

    public short getVicious() {
        return vicious;
    }

    @Override
    public void setFlag(short flag) {
        this.flag = flag;
    }

    public void setStr(short str) {
        this.str = str;
    }

    public void setDex(short dex) {
        this.dex = dex;
    }

    public void setInt(short _int) {
        this._int = _int;
    }

    public void setLuk(short luk) {
        this.luk = luk;
    }

    public void setHp(short hp) {
        this.hp = hp;
    }

    public void setMp(short mp) {
        this.mp = mp;
    }

    public void setWatk(short watk) {
        this.watk = watk;
    }

    public void setMatk(short matk) {
        this.matk = matk;
    }

    public void setWdef(short wdef) {
        this.wdef = wdef;
    }

    public void setMdef(short mdef) {
        this.mdef = mdef;
    }

    public void setAcc(short acc) {
        this.acc = acc;
    }

    public void setAvoid(short avoid) {
        this.avoid = avoid;
    }

    public void setHands(short hands) {
        this.hands = hands;
    }

    public void setSpeed(short speed) {
        this.speed = speed;
    }

    public void setJump(short jump) {
        this.jump = jump;
    }

    public void setVicious(short vicious) {
        this.vicious = vicious;
    }

    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    public byte getLevel() {
        return level;
    }

    public void setLevel(byte level) {
        this.level = level;
    }

    private static int getStatModifier(boolean isAttribute) {
        // each set of stat points grants a chance for a bonus stat point upgrade at equip level up.

        if (GameConfig.getServerBoolean("use_equipment_level_up_power")) {
            if (isAttribute) {
                return 2;
            } else {
                return 4;
            }
        } else {
            if (isAttribute) {
                return 4;
            } else {
                return 16;
            }
        }
    }

    private static int randomizeStatUpgrade(int top) {
        int limit = Math.min(top, GameConfig.getServerInt("max_equipment_level_up_stat_up"));

        int poolCount = (limit * (limit + 1) / 2) + limit;
        int rnd = Randomizer.rand(0, poolCount);

        int stat = 0;
        if (rnd >= limit) {
            rnd -= limit;
            stat = 1 + (int) Math.floor((-1 + Math.sqrt((8 * rnd) + 1)) / 2);    // optimized randomizeStatUpgrade author: David A.
        }

        return stat;
    }

    private static boolean isPhysicalWeapon(int itemid) {
        Equip eqp = (Equip) ItemInformationProvider.getInstance().getEquipById(itemid);
        return eqp.getWatk() >= eqp.getMatk();
    }

    private boolean isNotWeaponAffinity(StatUpgrade name) {
        // Vcoc's idea - WATK/MATK expected gains lessens outside of weapon affinity (physical/magic)

        if (ItemConstants.isWeapon(this.getItemId())) {
            if (name.equals(StatUpgrade.incPAD)) {
                return !isPhysicalWeapon(this.getItemId());
            } else if (name.equals(StatUpgrade.incMAD)) {
                return isPhysicalWeapon(this.getItemId());
            }
        }

        return false;
    }

    private void getUnitStatUpgrade(List<Pair<StatUpgrade, Integer>> stats, StatUpgrade name, int curStat, boolean isAttribute) {
        isUpgradeable = true;

        int maxUpgrade = randomizeStatUpgrade((int) (1 + (curStat / (getStatModifier(isAttribute) * (isNotWeaponAffinity(name) ? 2.7 : 1)))));
        if (maxUpgrade == 0) {
            return;
        }

        stats.add(new Pair<>(name, maxUpgrade));
    }

    private static void getUnitSlotUpgrade(List<Pair<StatUpgrade, Integer>> stats, StatUpgrade name) {
        if (Math.random() < 0.1) {
            stats.add(new Pair<>(name, 1));  // 10% success on getting a slot upgrade.
        }
    }

    private void improveDefaultStats(List<Pair<StatUpgrade, Integer>> stats) {
        if (dex > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incDEX, dex, true);
        }
        if (str > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incSTR, str, true);
        }
        if (_int > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incINT, _int, true);
        }
        if (luk > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incLUK, luk, true);
        }
        if (hp > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incMHP, hp, false);
        }
        if (mp > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incMMP, mp, false);
        }
        if (watk > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incPAD, watk, false);
        }
        if (matk > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incMAD, matk, false);
        }
        if (wdef > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incPDD, wdef, false);
        }
        if (mdef > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incMDD, mdef, false);
        }
        if (avoid > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incEVA, avoid, false);
        }
        if (acc > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incACC, acc, false);
        }
        if (speed > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incSpeed, speed, false);
        }
        if (jump > 0) {
            getUnitStatUpgrade(stats, StatUpgrade.incJump, jump, false);
        }
    }

    public Map<StatUpgrade, Short> getStats() {
        Map<StatUpgrade, Short> stats = new HashMap<>(5);

        if (dex > 0) {
            stats.put(StatUpgrade.incDEX, dex);
        }
        if (str > 0) {
            stats.put(StatUpgrade.incSTR, str);
        }
        if (_int > 0) {
            stats.put(StatUpgrade.incINT, _int);
        }
        if (luk > 0) {
            stats.put(StatUpgrade.incLUK, luk);
        }
        if (hp > 0) {
            stats.put(StatUpgrade.incMHP, hp);
        }
        if (mp > 0) {
            stats.put(StatUpgrade.incMMP, mp);
        }
        if (watk > 0) {
            stats.put(StatUpgrade.incPAD, watk);
        }
        if (matk > 0) {
            stats.put(StatUpgrade.incMAD, matk);
        }
        if (wdef > 0) {
            stats.put(StatUpgrade.incPDD, wdef);
        }
        if (mdef > 0) {
            stats.put(StatUpgrade.incMDD, mdef);
        }
        if (avoid > 0) {
            stats.put(StatUpgrade.incEVA, avoid);
        }
        if (acc > 0) {
            stats.put(StatUpgrade.incACC, acc);
        }
        if (speed > 0) {
            stats.put(StatUpgrade.incSpeed, speed);
        }
        if (jump > 0) {
            stats.put(StatUpgrade.incJump, jump);
        }

        return stats;
    }

    /**
     * 装备升级时计算增加的属性值，值>0才显示，避免显示负数或者0，避免玩家以为属性被扣除了
     * 优化提示消息，使其更易懂
     * @param stats
     * @return
     */
    public Pair<String, Pair<Boolean, Boolean>> gainStats(List<Pair<StatUpgrade, Integer>> stats) {
        boolean gotSlot = false, gotVicious = false;
        String lvupStr = "";
        int statUp, maxStat = GameConfig.getServerInt("max_equipment_stat");
        for (Pair<StatUpgrade, Integer> stat : stats) {
            switch (stat.getLeft()) {
                case incDEX:
                    statUp = Math.min(stat.getRight(), maxStat - dex);
                    if(statUp > 0) {
                        dex += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message1") + "+" + statUp + "; ";
                    }
                    break;
                case incSTR:
                    statUp = Math.min(stat.getRight(), maxStat - str);
                    if(statUp > 0) {
                        str += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message2") + "; ";
                    }
                    break;
                case incINT:
                    statUp = Math.min(stat.getRight(), maxStat - _int);
                    if(statUp > 0) {
                        _int += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message3") + "+" + statUp + "; ";
                    }
                    break;
                case incLUK:
                    statUp = Math.min(stat.getRight(), maxStat - luk);
                    if(statUp > 0) {
                        luk += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message4") + "+" + statUp + "; ";
                    }
                    break;
                case incMHP:
                    statUp = Math.min(stat.getRight(), maxStat - hp);
                    if(statUp > 0) {
                        hp += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message5") + "+" + statUp + "; ";
                    }
                    break;
                case incMMP:
                    statUp = Math.min(stat.getRight(), maxStat - mp);
                    if(statUp > 0) {
                        mp += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message6") + "+" + statUp + "; ";
                    }
                    break;
                case incPAD:
                    statUp = Math.min(stat.getRight(), maxStat - watk);
                    if(statUp > 0) {
                        watk += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message7") + "+" + statUp + "; ";
                    }
                    break;
                case incMAD:
                    statUp = Math.min(stat.getRight(), maxStat - matk);
                    if(statUp > 0) {
                        matk += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message8") + "+" + statUp + "; ";
                    }
                    break;
                case incPDD:
                    statUp = Math.min(stat.getRight(), maxStat - wdef);
                    if(statUp > 0) {
                        wdef += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message9") + "+" + statUp + "; ";
                    }
                    break;
                case incMDD:
                    statUp = Math.min(stat.getRight(), maxStat - mdef);
                    if(statUp > 0) {
                        mdef += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message10") + "+" + statUp + "; ";
                    }
                    break;
                case incEVA:
                    statUp = Math.min(stat.getRight(), maxStat - avoid);
                    if(statUp > 0) {
                        avoid += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message11") + "+" + statUp + "; ";
                    }
                    break;
                case incACC:
                    statUp = Math.min(stat.getRight(), maxStat - acc);
                    if(statUp > 0) {
                        acc += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message12") + "+" + statUp + "; ";
                    }
                    break;
                case incSpeed:
                    statUp = Math.min(stat.getRight(), maxStat - speed);
                    if(statUp > 0) {
                        speed += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message13") + "+" + statUp + "; ";
                    }
                    break;
                case incJump:
                    statUp = Math.min(stat.getRight(), maxStat - jump);if(statUp > 0) {
                        jump += statUp;
                        lvupStr += I18nUtil.getMessage("Equip.gainStats.message14") + "+" + statUp + "; ";
                    }
                    break;

                case incVicious:
                    vicious -= stat.getRight();
                    gotVicious = true;
                    break;
                case incSlot:
                    upgradeSlots += stat.getRight();
                    gotSlot = true;
                    break;
            }
        }

        return new Pair<>(lvupStr, new Pair<>(gotSlot, gotVicious));
    }

    private void gainLevel(Client c) {
        List<Pair<StatUpgrade, Integer>> stats = new LinkedList<>();

        if (isElemental) {
            List<Pair<String, Integer>> elementalStats = ItemInformationProvider.getInstance().getItemLevelupStats(getItemId(), itemLevel);

            for (Pair<String, Integer> p : elementalStats) {
                if (p.getRight() > 0) {
                    stats.add(new Pair<>(StatUpgrade.valueOf(p.getLeft()), p.getRight()));
                }
            }
        }

        if (!stats.isEmpty()) {
            if (GameConfig.getServerBoolean("use_equipment_level_up_slots")) {
                if (vicious > 0) {
                    getUnitSlotUpgrade(stats, StatUpgrade.incVicious);
                }
                getUnitSlotUpgrade(stats, StatUpgrade.incSlot);
            }
        } else {
            isUpgradeable = false;

            improveDefaultStats(stats);
            if (GameConfig.getServerBoolean("use_equipment_level_up_slots")) {
                if (vicious > 0) {
                    getUnitSlotUpgrade(stats, StatUpgrade.incVicious);
                }
                getUnitSlotUpgrade(stats, StatUpgrade.incSlot);
            }

            if (isUpgradeable) {
                while (stats.isEmpty()) {
                    improveDefaultStats(stats);
                    if (GameConfig.getServerBoolean("use_equipment_level_up_slots")) {
                        if (vicious > 0) {
                            getUnitSlotUpgrade(stats, StatUpgrade.incVicious);
                        }
                        getUnitSlotUpgrade(stats, StatUpgrade.incSlot);
                    }
                }
            }
        }

        itemLevel++;

        String lvupStr = I18nUtil.getMessage("Equip.gainStats.message21", ItemInformationProvider.getInstance().getName(this.getItemId()), itemLevel) + "; ";
        String showStr = "#e'" + ItemInformationProvider.getInstance().getName(this.getItemId()) + I18nUtil.getMessage("Equip.gainStats.message16") + itemLevel + "#k#b!";

        Pair<String, Pair<Boolean, Boolean>> res = this.gainStats(stats);
        lvupStr += res.getLeft();
        boolean gotSlot = res.getRight().getLeft();
        boolean gotVicious = res.getRight().getRight();

        if (gotVicious) {
            //c.getPlayer().dropMessage(6, "A new Vicious Hammer opportunity has been found on the '" + ItemInformationProvider.getInstance().getName(getItemId()) + "'!");
            lvupStr += I18nUtil.getMessage("Equip.gainStats.message19");
        }
        if (gotSlot) {
            //c.getPlayer().dropMessage(6, "A new upgrade slot has been found on the '" + ItemInformationProvider.getInstance().getName(getItemId()) + "'!");
            lvupStr += I18nUtil.getMessage("Equip.gainStats.message20") + gotSlot + "; ";
        }

        c.getPlayer().equipChanged();

        showLevelupMessage(showStr, c); // thanks to Polaris dev team !
        c.getPlayer().dropMessage(6, lvupStr);

        c.sendPacket(PacketCreator.showEquipmentLevelUp());
        c.getPlayer().getMap().broadcastPacket(c.getPlayer(), PacketCreator.showForeignEffect(c.getPlayer().getId(), 15));
        c.getPlayer().forceUpdateItem(this);
    }

    public int getItemExp() {
        return (int) itemExp;
    }

    private static double normalizedMasteryExp(int reqLevel) {
        // Conversion factor between mob exp and equip exp gain. Through many calculations, the expected for equipment levelup
        // from level 1 to 2 is killing about 100~200 mobs of the same level range, on a 1x EXP rate scenario.

        if (reqLevel < 5) {
            return 42;
        } else if (reqLevel >= 78) {
            return Math.max((10413.648 * Math.exp(reqLevel * 0.03275)), 15);
        } else if (reqLevel >= 38) {
            return Math.max((4985.818 * Math.exp(reqLevel * 0.02007)), 15);
        } else if (reqLevel >= 18) {
            return Math.max((248.219 * Math.exp(reqLevel * 0.11093)), 15);
        } else {
            return Math.max(((1334.564 * Math.log(reqLevel)) - 1731.976), 15);
        }
    }

    public synchronized void gainItemExp(Client c, int gain) {  // Ronan's Equip Exp gain method
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        if (!ii.isUpgradeable(this.getItemId())) {
            return;
        }

        int equipMaxLevel = Math.min(30, Math.max(ii.getEquipLevel(this.getItemId(), true), GameConfig.getServerInt("use_equipment_level_up")));
        if (itemLevel >= equipMaxLevel) {
            return;
        }

        int reqLevel = ii.getEquipLevelReq(this.getItemId());

        float masteryModifier = (GameConfig.getServerFloat("equip_exp_rate") * ExpTable.getExpNeededForLevel(1)) / (float) normalizedMasteryExp(reqLevel);
        float elementModifier = (isElemental) ? 0.85f : 0.6f;

        float baseExpGain = gain * elementModifier * masteryModifier;

        itemExp += baseExpGain;
        int expNeeded = ExpTable.getEquipExpNeededForLevel(itemLevel);

        if (GameConfig.getServerBoolean("use_debug_show_eqp_exp")) {
            log.info("{} -> EXP Gain: {}, Mastery: {}, Base gain: {}, exp: {} / {}, Kills TNL: {}", ii.getName(getItemId()),
                    gain, masteryModifier, baseExpGain, itemExp, expNeeded, expNeeded / (baseExpGain / c.getPlayer().getExpRate()));
        }

        if (itemExp >= expNeeded) {
            while (itemExp >= expNeeded) {
                itemExp -= expNeeded;
                gainLevel(c);

                if (itemLevel >= equipMaxLevel) {
                    itemExp = 0.0f;
                    break;
                }

                expNeeded = ExpTable.getEquipExpNeededForLevel(itemLevel);
            }
        }

        c.getPlayer().forceUpdateItem(this);
        //if(GameConfig.getServerBoolean("use_debug")) c.getPlayer().dropMessage("'" + ii.getName(this.getItemId()) + "': " + itemExp + " / " + expNeeded);
    }

    private boolean reachedMaxLevel() {
        if (isElemental) {
            if (itemLevel < ItemInformationProvider.getInstance().getEquipLevel(getItemId(), true)) {
                return false;
            }
        }

        return itemLevel >= GameConfig.getServerInt("use_equipment_level_up");
    }

    public String showEquipFeatures(Client c) {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        if (!ii.isUpgradeable(this.getItemId())) {
            return "";
        }

        String eqpName = ii.getName(getItemId());
        String eqpInfo = reachedMaxLevel() ? " #e#rMAX LEVEL#k#n" : (" EXP: #e#b" + (int) itemExp + "#k#n / " + ExpTable.getEquipExpNeededForLevel(itemLevel));

        return "'" + eqpName + "' -> LV: #e#b" + itemLevel + "#k#n    " + eqpInfo + "\r\n";
    }

    private static void showLevelupMessage(String msg, Client c) {
        c.getPlayer().showHint(msg, 300);
    }

    public void setItemExp(int exp) {
        this.itemExp = exp;
    }

    public void setItemLevel(byte level) {
        this.itemLevel = level;
    }

    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    public void setUpgradeSlots(int i) {
        this.upgradeSlots = (byte) i;
    }

    public void setVicious(int i) {
        this.vicious = (short) i;
    }

    public int getRingId() {
        return ringid;
    }

    public void setRingId(int id) {
        this.ringid = id;
    }

    public boolean isWearing() {
        return wear;
    }

    public void wear(boolean yes) {
        wear = yes;
    }

    public byte getItemLevel() {
        return itemLevel;
    }
}