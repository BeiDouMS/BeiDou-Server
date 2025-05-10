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

import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
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
    @Getter
    private byte level, itemLevel;
    private short flag;
    private short str, dex, _int, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, vicious;
    private float itemExp;
    private int ringid = -1;
    private boolean wear = false;
    private boolean isUpgradeable, isElemental = false;    // timeless or reverse, or any equip that could levelup on GMS for all effects
    private static ItemInformationProvider ii = ItemInformationProvider.getInstance();

    public Equip(int id, short position) {
        this(id, position, 0);
    }

    public Equip(int id, short position, int slots) {
        super(id, position, (short) 1);
        this.upgradeSlots = (byte) slots;
        this.itemExp = 0;
        this.itemLevel = 1;

        this.isElemental = (ii.getEquipLevel(id, false) > 1);
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
        Equip eqp = (Equip) ii.getEquipById(itemid);
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

    /**
     * 尝试为单位插槽添加升级属性（默认10%成功率）
     * @private
     * @static
     * @param {List<Pair<StatUpgrade, Integer>>} stats - 存储升级属性的列表（需传入引用）
     * @param {StatUpgrade} name - 要尝试升级的属性类型
     * @description 调用重载方法时默认使用10%的成功概率
     */
    private static void getUnitSlotUpgrade(List<Pair<StatUpgrade, Integer>> stats, StatUpgrade name) {
        getUnitSlotUpgrade(stats, name, 0.1);  // 默认10%成功率的快捷调用
    }
    /**
     * 尝试为单位插槽添加升级属性（可配置概率）
     * @private
     * @static
     * @param {List<Pair<StatUpgrade, Integer>>} stats - 存储升级属性的列表（需传入引用）
     * @param {StatUpgrade} name - 要尝试升级的属性类型
     * @param {double} chance - 成功概率值（范围0.0\~1.0）
     * @description 通过随机数判断是否成功添加属性升级项
     */
    private static void getUnitSlotUpgrade(List<Pair<StatUpgrade, Integer>> stats, StatUpgrade name, double chance) {
        if (Math.random() <= chance) {  // 概率判定核心逻辑
            stats.add(new Pair<>(name, 1));  // 成功时添加新属性项
        }
    }
    /**
     * 判断是否需要增加砸卷次数或者减少金锤子已使用次数
     */
    private void UpgradeSlotProcessing(List<Pair<StatUpgrade, Integer>> stats,int equipLevel) {
        if (GameConfig.getServerBoolean("use_equipment_level_up_slots")) {// 处理可砸卷次数逻辑
            getUnitSlotUpgrade(stats, StatUpgrade.incSlot); // 增加升级槽
        }
        if (GameConfig.getServerBoolean("use_equipment_level_up_vicious") && vicious > 0) { // 金锤子已使用次数大于0时
            double[][] chanceList = {{0, 255, 0.1}};
            String chanceParam = GameConfig.getServerString("use_equipment_level_up_vicious_levelrange_chance");
            if(chanceParam != null) {
                try {
                    chanceList = JSONObject.parseObject(chanceParam, double[][].class);
                } catch (Throwable e) {
                    log.warn("金锤子装备等级范围概率参数解析失败，请检查是否正确");
                }
            }
            for(double[] obj : chanceList) {
                double minLevel = obj[0],maxLevel = obj[1], chance = obj[2];
                if(equipLevel >= minLevel && equipLevel <= maxLevel) {
                    getUnitSlotUpgrade(stats, StatUpgrade.incVicious,chance); // 减少金锤子
                    break;
                }
            }
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
     * @param stats 属性升级列表，包含属性类型和增加值
     * @return 返回一个 Pair，包含提示消息和两个布尔值（是否增加升级槽、是否减少金锤子）
     */
    public Pair<String, Pair<Boolean, Boolean>> gainStats(List<Pair<StatUpgrade, Integer>> stats) {
        boolean gotSlot = false, gotVicious = false; // 标记是否增加了升级槽或减少了金锤子
        StringBuilder lvupStr = new StringBuilder(); // 使用 StringBuilder 提高字符串拼接效率
        int maxStat = GameConfig.getServerInt("max_equipment_stat"); // 获取属性最大值

        for (Pair<StatUpgrade, Integer> stat : stats) { // 遍历属性升级列表
            StatUpgrade type = stat.getLeft(); // 属性类型
            int value = stat.getRight(); // 属性增加值

            switch (type) {
                case incVicious: // 减少金锤子
                    vicious -= value;
                    gotVicious = true;
                    break;
                case incSlot: // 增加升级槽
                    upgradeSlots += value;
                    gotSlot = true;
                    break;
                default: // 处理普通属性
                    int statUp = handleStatUpgrade(type, value, maxStat);
                    if (statUp > 0) {
                        lvupStr.append(getStatMessage(type, statUp)).append("; ");
                    }
                    break;
            }
        }

        return new Pair<>(lvupStr.toString(), new Pair<>(gotSlot, gotVicious));
    }

    /**
     * 处理普通属性的升级逻辑
     * @param type 属性类型
     * @param value 属性增加值
     * @param maxStat 属性最大值
     * @return 实际增加的属性值
     */
    private int handleStatUpgrade(StatUpgrade type, int value, int maxStat) {
        int currentStat = getCurrentStat(type); // 获取当前属性值
        int statUp = Math.min(value, maxStat - currentStat); // 计算实际增加值，不超过最大值
        if (statUp > 0) {
            setCurrentStat(type, currentStat + statUp); // 更新属性值
        }
        return statUp;
    }

    /**
     * 获取当前属性值
     * @param type 属性类型
     * @return 当前属性值
     */
    private int getCurrentStat(StatUpgrade type) {
        switch (type) {
            case incDEX: return dex;
            case incSTR: return str;
            case incINT: return _int;
            case incLUK: return luk;
            case incMHP: return hp;
            case incMMP: return mp;
            case incPAD: return watk;
            case incMAD: return matk;
            case incPDD: return wdef;
            case incMDD: return mdef;
            case incEVA: return avoid;
            case incACC: return acc;
            case incSpeed: return speed;
            case incJump: return jump;
            default: return 0;
        }
    }

    /**
     * 设置当前属性值
     * @param type 属性类型
     * @param value 新的属性值
     */
    private void setCurrentStat(StatUpgrade type, int value) {
        switch (type) {
            case incDEX: dex = (short) value; break;
            case incSTR: str = (short) value; break;
            case incINT: _int = (short) value; break;
            case incLUK: luk = (short) value; break;
            case incMHP: hp = (short) value; break;
            case incMMP: mp = (short) value; break;
            case incPAD: watk = (short) value; break;
            case incMAD: matk = (short) value; break;
            case incPDD: wdef = (short) value; break;
            case incMDD: mdef = (short) value; break;
            case incEVA: avoid = (short) value; break;
            case incACC: acc = (short) value; break;
            case incSpeed: speed = (short) value; break;
            case incJump: jump = (short) value; break;
            default: break;
        }
    }

    /**
     * 获取属性提升的提示消息
     * @param type 属性类型
     * @param value 属性增加值
     * @return 提示消息
     */
    private String getStatMessage(StatUpgrade type, int value) {
        String messageKey = "Equip.gainStats." + type.name().substring(3); // 从 incDEX 中提取 DEX
        return I18nUtil.getMessage(messageKey) + "+" + value;
    }

    /**
     * 处理装备升级的逻辑，包括属性提升、升级槽增加、金锤子减少等，并通知客户端更新装备状态
     * @param c 触发升级的客户端
     */
    private void gainLevel(Client c) {
        List<Pair<StatUpgrade, Integer>> stats = new LinkedList<>(); // 初始化属性升级列表
        int equipLevel = ii.getEquipLevelReq(getItemId()); // 获取装备要求等级

        if (isElemental) {// 如果是元素装备，从配置中获取元素属性升级列表
            List<Pair<String, Integer>> elementalStats = ii.getItemLevelupStats(getItemId(), itemLevel);
            for (Pair<String, Integer> p : elementalStats) {
                if (p.getRight() > 0) { // 只有增加值大于0时才添加到列表
                    stats.add(new Pair<>(StatUpgrade.valueOf(p.getLeft()), p.getRight()));
                }
            }
        }

        if (stats.isEmpty()) {// 如果属性列表为空，则生成默认属性升级列表
            isUpgradeable = false; // 标记装备不可升级
            improveDefaultStats(stats); // 生成默认属性升级列表
        }
        UpgradeSlotProcessing(stats,equipLevel);    // 砸卷次数和减少金锤子次数判断
        if (isUpgradeable && stats.isEmpty()) {// 如果装备仍可升级且属性列表为空，则继续生成属性升级列表
            while (stats.isEmpty()) {
                improveDefaultStats(stats);// 生成默认属性升级列表
                UpgradeSlotProcessing(stats,equipLevel);// 砸卷次数和减少金锤子次数判断
            }
        }

        itemLevel++; // 提升装备等级

        String lvupStr = I18nUtil.getMessage("Equip.gainStats.lvupStr", ii.getName(this.getItemId()), itemLevel) + "; ";  // 生成等级提升的提示消息

        Pair<String, Pair<Boolean, Boolean>> res = this.gainStats(stats);    // 调用 gainStats 计算属性提升和生成提示消息
        lvupStr += res.getLeft(); // 拼接属性提升的提示消息
        boolean gotSlot = res.getRight().getLeft(); // 是否增加了升级槽
        boolean gotVicious = res.getRight().getRight(); // 是否减少了金锤子

        if (gotVicious) {// 如果减少了金锤子，追加提示消息
            lvupStr += I18nUtil.getMessage("Equip.gainStats.Vicious","-1")  + "; ";
        }

        if (gotSlot) {// 如果增加了升级槽，追加提示消息
            lvupStr += I18nUtil.getMessage("Equip.gainStats.UPGSLOT","+1")  + "; ";
        }

        // 通知客户端更新装备状态
        c.getPlayer().equipChanged();
        c.getPlayer().showHint(I18nUtil.getMessage("Equip.gainStats.showHint", ii.getName(this.getItemId()), itemLevel), 300); // 显示等级提升的消息
        c.getPlayer().dropMessage(6, lvupStr); // 显示属性提升的消息

        // 发送装备升级的效果包
        c.sendPacket(PacketCreator.showEquipmentLevelUp());
        c.getPlayer().getMap().broadcastPacket(c.getPlayer(), PacketCreator.showForeignEffect(c.getPlayer().getId(), 15));
        c.getPlayer().forceUpdateItem(this); // 强制更新装备状态
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

    /**
     * 处理装备经验值的增加逻辑（Ronan 的装备经验值获取方法）
     * @param c 客户端对象
     * @param gain 获得的经验值
     */
    public synchronized void gainItemExp(Client c, int gain) {
        if (!ii.isUpgradeable(this.getItemId())) {// 检查装备是否可升级
            return;
        }

        int equipMaxLevel = Math.min(30, Math.max(ii.getEquipLevel(this.getItemId(), true), GameConfig.getServerInt("use_equipment_level_up")));// 计算装备的最大等级
        if (itemLevel >= equipMaxLevel) {
            return;
        }

        int reqLevel = ii.getEquipLevelReq(this.getItemId());// 获取装备的需求等级

        // 计算经验值修正因子
        float masteryModifier = (GameConfig.getServerFloat("equip_exp_rate") * ExpTable.getExpNeededForLevel(1)) / (float) normalizedMasteryExp(reqLevel);
        float elementModifier = (isElemental) ? 0.85f : 0.6f;

        float baseExpGain = gain * elementModifier * masteryModifier;// 计算实际获得的经验值

        itemExp += baseExpGain;// 更新装备经验值
        int expNeeded = ExpTable.getEquipExpNeededForLevel(itemLevel);

        // 调试信息：显示经验值获取详情
        if (GameConfig.getServerBoolean("use_debug_show_eqp_exp")) {
            log.info("{} -> EXP Gain: {}, Mastery: {}, Base gain: {}, exp: {} / {}, Kills TNL: {}", ii.getName(getItemId()),
                    gain, masteryModifier, baseExpGain, itemExp, expNeeded, expNeeded / (baseExpGain / c.getPlayer().getExpRate()));
        }


        if (itemExp >= expNeeded) {// 判断是否需要升级
            while (itemExp >= expNeeded) {
                itemExp -= expNeeded;
                gainLevel(c); // 升级装备

                if (itemLevel >= equipMaxLevel || !GameConfig.getServerBoolean("use_equipment_level_up_continuous")) {// 如果达到最大等级或者不允许连续升级，重置经验值并退出循环
                    itemExp = 0.0f;
                    break;
                }

                expNeeded = ExpTable.getEquipExpNeededForLevel(itemLevel);// 更新升级所需经验值
            }
        }

        c.getPlayer().forceUpdateItem(this);// 通知客户端更新装备状态
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

}