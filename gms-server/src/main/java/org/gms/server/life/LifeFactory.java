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
package org.gms.server.life;

import org.gms.util.RequireUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.provider.Data;
import org.gms.provider.DataProvider;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.DataType;
import org.gms.provider.wz.WZFiles;
import org.gms.util.Pair;
import org.gms.util.StringUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LifeFactory {
    private static final Logger log = LoggerFactory.getLogger(LifeFactory.class);
    private static final DataProvider data = DataProviderFactory.getDataProvider(WZFiles.MOB);
    private final static DataProvider stringDataWZ = DataProviderFactory.getDataProvider(WZFiles.STRING);
    private static final Data mobStringData = stringDataWZ.getData("Mob.img");
    private static final Data npcStringData = stringDataWZ.getData("Npc.img");
    private static final Map<Integer, MonsterStats> monsterStats = new HashMap<>();
    private static final Set<Integer> hpbarBosses = getHpBarBosses();
    private static final Map<Integer, String> npcNames = new HashMap<>();

    private static Set<Integer> getHpBarBosses() {
        Set<Integer> ret = new HashSet<>();

        DataProvider uiDataWZ = DataProviderFactory.getDataProvider(WZFiles.UI);
        for (Data bossData : uiDataWZ.getData("UIWindow.img").getChildByPath("MobGage/Mob").getChildren()) {
            ret.add(Integer.valueOf(bossData.getName()));
        }

        return ret;
    }

    public static AbstractLoadedLife getLife(int id, String type) {
        if (type.equalsIgnoreCase("n")) {
            return getNPC(id);
        } else if (type.equalsIgnoreCase("m")) {
            return getMonster(id);
        } else {
            log.warn("Unknown Life type: {}", type);
            return null;
        }
    }

    private static class MobAttackInfoHolder {
        protected int attackPos;
        protected int mpCon;
        protected int coolTime;
        protected int animationTime;

        protected MobAttackInfoHolder(int attackPos, int mpCon, int coolTime, int animationTime) {
            this.attackPos = attackPos;
            this.mpCon = mpCon;
            this.coolTime = coolTime;
            this.animationTime = animationTime;
        }
    }

    private static final class BoundingBox {
        private int minX = Integer.MAX_VALUE;
        private int minY = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private int maxY = Integer.MIN_VALUE;
        private boolean valid = false;

        /**
         * 合并单帧的碰撞框到当前包围盒
         *
         * @param lt 单帧左上角
         * @param rb 单帧右下角
         */
        public void update(Point lt, Point rb) {
            if (lt == null || rb == null) {
                return;
            }
            minX = Math.min(minX, lt.x);
            minY = Math.min(minY, lt.y);
            maxX = Math.max(maxX, rb.x);
            maxY = Math.max(maxY, rb.y);
            valid = true;
        }

        public boolean isValid() {
            return valid;
        }

        public int getMinX() {
            return minX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMaxY() {
            return maxY;
        }
    }

    /**
     * 解析 UOL 引用，返回最终的数据节点
     *
     * @param data 原始数据节点
     * @return 解析后的节点（可能与入参相同）
     */
    private static Data resolveUol(Data data) {
        if (data == null) {
            return null;
        }
        if (data.getType() == DataType.UOL) {
            Object path = data.getData();
            if (path instanceof String) {
                Data resolved = data.getChildByPath((String) path);
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return data;
    }

    /**
     * 判断动作名是否参与碰撞框计算
     *
     * @param name 动作名
     * @return 是否参与
     */
    private static boolean isBboxAction(String name) {
        return name.startsWith("stand") || name.startsWith("move") || name.startsWith("fly")
                || name.startsWith("jump") || name.startsWith("attack") || name.startsWith("hit");
    }

    /**
     * 计算怪物多动作帧的最大包围碰撞框
     *
     * @param mid       怪物 ID
     * @param mobName   怪物名称
     * @param monsterData 怪物数据根节点
     * @return 最大包围碰撞框
     */
    private static BoundingBox buildMonsterBoundingBox(int mid, String mobName, Data monsterData) {
        BoundingBox bbox = new BoundingBox();
        int actionCount = 0;
        int frameCount = 0;
        int frameWithLtRb = 0;
        int frameFallback = 0;
        int frameSkipped = 0;
        for (Data action : monsterData.getChildren()) {
            String actionName = action.getName();
            if (actionName.equals("info") || actionName.startsWith("die") || !isBboxAction(actionName)) {
                continue;
            }
            // 处理 UOL 指向的动作节点
            Data actionData = resolveUol(action);
            if (actionData == null) {
                continue;
            }
            actionCount++;
            for (Data frame : actionData.getChildren()) {
                // 处理 UOL 指向的帧节点
                Data frameData = resolveUol(frame);
                if (frameData == null || frameData.getType() != DataType.CANVAS) {
                    frameSkipped++;
                    continue;
                }
                frameCount++;
                Point lt = DataTool.getPoint("lt", frameData, null);
                Point rb = DataTool.getPoint("rb", frameData, null);
                boolean usedFallback = false;
                Point origin = null;
                int width = -1;
                int height = -1;
                if (lt == null || rb == null) {
                    // lt/rb 缺失时，使用 origin + 宽高推算
                    origin = DataTool.getPoint("origin", frameData, null);
                    width = DataTool.getAttributeValueInt(frameData, "width", -1);
                    height = DataTool.getAttributeValueInt(frameData, "height", -1);
                    if (origin != null && width > 0 && height > 0) {
                        lt = new Point(-origin.x, -origin.y);
                        rb = new Point(width - origin.x, height - origin.y);
                        usedFallback = true;
                    }
                }
                if (lt != null && rb != null) {
                    if (usedFallback) {
                        frameFallback++;
                    } else {
                        frameWithLtRb++;
                    }
                } else {
                    frameSkipped++;
                }
                // 合并到整体包围框
                bbox.update(lt, rb);
            }
        }
        return bbox;
    }

    /**
     * 读取指定怪物的 WZ 根节点。
     */
    private static Data getMonsterData(int mid) {
        return data.getData(StringUtil.getLeftPaddedStr(mid + ".img", '0', 11));
    }

    /**
     * 解析当前怪物真正应当用于可视判定的数据节点。
     *
     * <p>部分怪物只保留自身属性，动作帧、lt/rb、origin 等视觉信息全部挂在 link 怪身上。
     * 如果这里不沿着 link 查找，后续 bbox 会退化为“没有碰撞数据”，DISTANCE_HACK 就会误判。</p>
     */
    private static Data resolveMonsterVisualData(int mid, Data monsterData) {
        if (monsterData == null) {
            return null;
        }

        Set<Integer> visitedMobs = new HashSet<>();
        Data currentMonsterData = monsterData;
        int currentMid = mid;
        while (currentMonsterData != null && visitedMobs.add(currentMid)) {
            if (hasVisualBoundingBoxSource(currentMonsterData)) {
                return currentMonsterData;
            }

            Data currentInfoData = currentMonsterData.getChildByPath("info");
            int linkMid = currentInfoData == null ? 0 : DataTool.getIntConvert("link", currentInfoData, 0);
            if (linkMid == 0) {
                break;
            }

            currentMonsterData = getMonsterData(linkMid);
            currentMid = linkMid;
        }
        return monsterData;
    }

    /**
     * 判断一个怪物节点是否已经具备可用于距离判定的视觉来源。
     *
     * <p>只要存在主可视帧，或者任意动作帧能解析出 bbox，即视为可用。</p>
     */
    private static boolean hasVisualBoundingBoxSource(Data monsterData) {
        if (monsterData == null) {
            return false;
        }
        if (resolvePrimaryVisualFrame(monsterData) != null) {
            return true;
        }
        return buildMonsterBoundingBox(0, "", monsterData).isValid();
    }

    /**
     * 获取怪物的主可视帧。
     *
     * <p>保持与原逻辑一致，优先使用 fly/0，其次 stand/0。</p>
     */
    private static Data resolvePrimaryVisualFrame(Data monsterData) {
        if (monsterData == null) {
            return null;
        }

        Data flyFrame = resolveVisualFrame(monsterData.getChildByPath("fly/0"));
        if (flyFrame != null) {
            return flyFrame;
        }
        return resolveVisualFrame(monsterData.getChildByPath("stand/0"));
    }

    /**
     * 将帧节点解析到最终的 Canvas 节点。
     */
    private static Data resolveVisualFrame(Data frameData) {
        Data resolvedFrame = resolveUol(frameData);
        if (resolvedFrame == null || resolvedFrame.getType() != DataType.CANVAS) {
            return null;
        }
        return resolvedFrame;
    }

    /**
     * 从最终视觉来源中回填怪物图片尺寸、移动类型和碰撞框。
     */
    private static void applyMonsterVisualStats(int mid, MonsterStats stats, Data visualMonsterData) {
        if (stats == null || visualMonsterData == null) {
            return;
        }

        Data flyFrame = resolveVisualFrame(visualMonsterData.getChildByPath("fly/0"));
        Data standFrame = resolveVisualFrame(visualMonsterData.getChildByPath("stand/0"));
        Data primaryFrame = flyFrame != null ? flyFrame : standFrame;

        if (flyFrame != null) {
            stats.setMovetype(1);
            stats.setImgwidth(DataTool.getAttributeValueInt(flyFrame, "width", -1));
            stats.setImgheight(DataTool.getAttributeValueInt(flyFrame, "height", -1));
        } else if (standFrame != null) {
            stats.setMovetype(0);
            stats.setImgwidth(DataTool.getAttributeValueInt(standFrame, "width", -1));
            stats.setImgheight(DataTool.getAttributeValueInt(standFrame, "height", -1));
        }

        BoundingBox bbox = buildMonsterBoundingBox(mid, stats.getName(), visualMonsterData);
        if (bbox.isValid()) {
            stats.setBbox(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
            return;
        }

        if (primaryFrame == null) {
            return;
        }

        Point origin = DataTool.getPoint("origin", primaryFrame, null);
        if (origin != null && stats.getImgwidth() > 0 && stats.getImgheight() > 0) {
            stats.setBbox(-origin.x, -origin.y, stats.getImgwidth() - origin.x, stats.getImgheight() - origin.y);
        }
    }

    private static void setMonsterAttackInfo(int mid, List<MobAttackInfoHolder> attackInfos) {
        if (!attackInfos.isEmpty()) {
            MonsterInformationProvider mi = MonsterInformationProvider.getInstance();

            for (MobAttackInfoHolder attackInfo : attackInfos) {
                mi.setMobAttackInfo(mid, attackInfo.attackPos, attackInfo.mpCon, attackInfo.coolTime);
                mi.setMobAttackAnimationTime(mid, attackInfo.attackPos, attackInfo.animationTime);
            }
        }
    }

    private static Pair<MonsterStats, List<MobAttackInfoHolder>> getMonsterStats(int mid) {
        Data monsterData = getMonsterData(mid);
        if (monsterData == null) {
            return null;
        }
        Data monsterInfoData = monsterData.getChildByPath("info");

        List<MobAttackInfoHolder> attackInfos = new LinkedList<>();
        MonsterStats stats = new MonsterStats();

        int linkMid = DataTool.getIntConvert("link", monsterInfoData, 0);
        if (linkMid != 0) {
            Pair<MonsterStats, List<MobAttackInfoHolder>> linkStats = getMonsterStats(linkMid);
            if (linkStats == null) {
                return null;
            }

            // thanks resinate for noticing non-propagable infos such as revives getting retrieved
            attackInfos.addAll(linkStats.getRight());
        }

        stats.setHp(DataTool.getIntConvert("maxHP", monsterInfoData));
        stats.setFriendly(DataTool.getIntConvert("damagedByMob", monsterInfoData, stats.isFriendly() ? 1 : 0) == 1);
        stats.setPADamage(DataTool.getIntConvert("PADamage", monsterInfoData));
        stats.setPDDamage(DataTool.getIntConvert("PDDamage", monsterInfoData));
        stats.setMADamage(DataTool.getIntConvert("MADamage", monsterInfoData));
        stats.setMDDamage(DataTool.getIntConvert("MDDamage", monsterInfoData));
        stats.setMp(DataTool.getIntConvert("maxMP", monsterInfoData, stats.getMp()));
        stats.setExp(DataTool.getIntConvert("exp", monsterInfoData, stats.getExp()));
        stats.setLevel(DataTool.getIntConvert("level", monsterInfoData));
        stats.setRemoveAfter(DataTool.getIntConvert("removeAfter", monsterInfoData, stats.removeAfter()));
        stats.setBoss(DataTool.getIntConvert("boss", monsterInfoData, stats.isBoss() ? 1 : 0) > 0);
        stats.setExplosiveReward(DataTool.getIntConvert("explosiveReward", monsterInfoData, stats.isExplosiveReward() ? 1 : 0) > 0);
        stats.setFfaLoot(DataTool.getIntConvert("publicReward", monsterInfoData, stats.isFfaLoot() ? 1 : 0) > 0);
        stats.setUndead(DataTool.getIntConvert("undead", monsterInfoData, stats.isUndead() ? 1 : 0) > 0);
        stats.setName(DataTool.getString(mid + "/name", mobStringData, "MISSINGNO"));
        stats.setBuffToGive(DataTool.getIntConvert("buff", monsterInfoData, stats.getBuffToGive()));
        stats.setCP(DataTool.getIntConvert("getCP", monsterInfoData, stats.getCP()));
        stats.setRemoveOnMiss(DataTool.getIntConvert("removeOnMiss", monsterInfoData, stats.removeOnMiss() ? 1 : 0) > 0);

        Data special = monsterInfoData.getChildByPath("coolDamage");
        if (special != null) {
            int coolDmg = DataTool.getIntConvert("coolDamage", monsterInfoData);
            int coolProb = DataTool.getIntConvert("coolDamageProb", monsterInfoData, 0);
            stats.setCool(new Pair<>(coolDmg, coolProb));
        }
        special = monsterInfoData.getChildByPath("loseItem");
        if (special != null) {
            for (Data liData : special.getChildren()) {
                stats.addLoseItem(new loseItem(DataTool.getInt(liData.getChildByPath("id")), (byte) DataTool.getInt(liData.getChildByPath("prop")), (byte) DataTool.getInt(liData.getChildByPath("x"))));
            }
        }
        special = monsterInfoData.getChildByPath("selfDestruction");
        if (special != null) {
            stats.setSelfDestruction(new selfDestruction((byte) DataTool.getInt(special.getChildByPath("action")), DataTool.getIntConvert("removeAfter", special, -1), DataTool.getIntConvert("hp", special, -1)));
        }
        Data firstAttackData = monsterInfoData.getChildByPath("firstAttack");
        int firstAttack = 0;
        if (firstAttackData != null) {
            if (firstAttackData.getType() == DataType.FLOAT) {
                firstAttack = Math.round(DataTool.getFloat(firstAttackData));
            } else {
                firstAttack = DataTool.getInt(firstAttackData);
            }
        }
        stats.setFirstAttack(firstAttack > 0);
        stats.setDropPeriod(DataTool.getIntConvert("dropItemPeriod", monsterInfoData, stats.getDropPeriod() / 10000) * 10000);

        // thanks yuxaij, Riizade, Z1peR, Anesthetic for noticing some bosses crashing players due to missing requirements
        boolean hpbarBoss = stats.isBoss() && hpbarBosses.contains(mid);
        stats.setTagColor(hpbarBoss ? DataTool.getIntConvert("hpTagColor", monsterInfoData, 0) : 0);
        stats.setTagBgColor(hpbarBoss ? DataTool.getIntConvert("hpTagBgcolor", monsterInfoData, 0) : 0);

        for (Data idata : monsterData) {
            if (!idata.getName().equals("info")) {
                int delay = 0;
                for (Data pic : idata.getChildren()) {
                    delay += DataTool.getIntConvert("delay", pic, 0);
                }
                stats.setAnimationTime(idata.getName(), delay);
            }
        }
        Data reviveInfo = monsterInfoData.getChildByPath("revive");
        if (reviveInfo != null) {
            List<Integer> revives = new LinkedList<>();
            for (Data data_ : reviveInfo) {
                revives.add(DataTool.getInt(data_));
            }
            stats.setRevives(revives);
        }
        decodeElementalString(stats, DataTool.getString("elemAttr", monsterInfoData, ""));

        MonsterInformationProvider mi = MonsterInformationProvider.getInstance();
        Data monsterSkillInfoData = monsterInfoData.getChildByPath("skill");
        if (monsterSkillInfoData != null) {
            int i = 0;
            Set<MobSkillId> skills = new HashSet<>();
            while (monsterSkillInfoData.getChildByPath(Integer.toString(i)) != null) {
                int skillId = DataTool.getInt(i + "/skill", monsterSkillInfoData, 0);
                int skillLv = DataTool.getInt(i + "/level", monsterSkillInfoData, 0);
                MobSkillType type = MobSkillType.from(skillId).orElseThrow();
                skills.add(new MobSkillId(type, skillLv));

                Data monsterSkillData = monsterData.getChildByPath("skill" + (i + 1));
                if (monsterSkillData != null) {
                    int animationTime = 0;
                    for (Data effectEntry : monsterSkillData.getChildren()) {
                        animationTime += DataTool.getIntConvert("delay", effectEntry, 0);
                    }

                    MobSkill skill = MobSkillFactory.getMobSkillOrThrow(type, skillLv);
                    mi.setMobSkillAnimationTime(skill, animationTime);
                }

                i++;
            }
            stats.setSkills(skills);
        }

        int i = 0;
        Data monsterAttackData;
        while ((monsterAttackData = monsterData.getChildByPath("attack" + (i + 1))) != null) {
            int animationTime = 0;
            for (Data effectEntry : monsterAttackData.getChildren()) {
                animationTime += DataTool.getIntConvert("delay", effectEntry, 0);
            }

            int mpCon = DataTool.getIntConvert("info/conMP", monsterAttackData, 0);
            int coolTime = DataTool.getIntConvert("info/attackAfter", monsterAttackData, 0);
            attackInfos.add(new MobAttackInfoHolder(i, mpCon, coolTime, animationTime));
            i++;
        }

        Data banishData = monsterInfoData.getChildByPath("ban");
        if (banishData != null) {
            stats.setBanishInfo(new BanishInfo(DataTool.getString("banMsg", banishData), DataTool.getInt("banMap/0/field", banishData, -1), DataTool.getString("banMap/0/portal", banishData, "sp")));
        }

        Data visualMonsterData = resolveMonsterVisualData(mid, monsterData);
        int noFlip = DataTool.getInt("noFlip", monsterInfoData, 0);
        if (noFlip > 0) {
            Data fixedStanceFrame = resolveVisualFrame(visualMonsterData.getChildByPath("stand/0"));
            if (fixedStanceFrame == null) {
                fixedStanceFrame = resolvePrimaryVisualFrame(visualMonsterData);
            }
            Point origin = fixedStanceFrame == null ? null : DataTool.getPoint("origin", fixedStanceFrame, null);
            if (origin != null) {
                stats.setFixedStance(origin.getX() < 1 ? 5 : 4);    // fixed left/right
            }
        }
        // 统一从最终视觉来源回填尺寸与碰撞框，避免 link 怪重复走本地旧逻辑。
        applyMonsterVisualStats(mid, stats, visualMonsterData);
        /* 已废弃：以下旧的本地 visual 解析路径仅作留档，当前统一走上面的 link 可视数据解析。
        Data fly = monsterData.getChildByPath("fly/0");
        if (fly != null) {
            stats.setMovetype(1);   //设定怪物类型为：fly
            fly = fly.getType() == DataType.UOL ? fly.getChildByPath((String) fly.getData()) : fly;  //呼叫转移...
            if (fly != null) {
                stats.setImgwidth(DataTool.getAttributeValueInt(fly,"width",-1));
                stats.setImgheight(DataTool.getAttributeValueInt(fly,"height",-1));
            }
        } else if (stand != null) {
            stats.setMovetype(0);   //设定怪物类型为：stand
            stand = stand.getType() == DataType.UOL ? stand.getChildByPath((String) stand.getData()) : stand;  //呼叫转移...
            if (stand != null) {
                stats.setImgwidth(DataTool.getAttributeValueInt(stand,"width",-1));
                stats.setImgheight(DataTool.getAttributeValueInt(stand,"height",-1));
            }

        }
        // 计算怪物碰撞框（多动作帧合并），用于后续距离判定和大体型识别
        BoundingBox bbox = buildMonsterBoundingBox(mid, stats.getName(), monsterData);
        if (bbox.isValid()) {
            stats.setBbox(bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY());
        } else {
            Point origin = DataTool.getPoint("stand/0/origin", monsterData, null);
            if (origin == null) {
                origin = DataTool.getPoint("fly/0/origin", monsterData, null);
            }
            if (origin != null && stats.getImgwidth() > 0 && stats.getImgheight() > 0) {
                stats.setBbox(-origin.x, -origin.y, stats.getImgwidth() - origin.x, stats.getImgheight() - origin.y);
            }
        }
        */
        return new Pair<>(stats, attackInfos);
    }

    public static Monster getMonster(int mid) {
        try {
            MonsterStats stats = monsterStats.get(mid);
            if (stats == null) {
                Pair<MonsterStats, List<MobAttackInfoHolder>> mobStats = getMonsterStats(mid);
                stats = mobStats.getLeft();
                setMonsterAttackInfo(mid, mobStats.getRight());

                monsterStats.put(mid, stats);
            }
            return new Monster(mid, stats);
        } catch (NullPointerException npe) {
            log.error("[SEVERE] MOB {} failed to load.", mid, npe);
            return null;
        }
    }

    public static int getMonsterLevel(int mid) {
        try {
            MonsterStats stats = monsterStats.get(mid);
            if (stats == null) {
                Data monsterData = data.getData(StringUtil.getLeftPaddedStr(mid + ".img", '0', 11));
                if (monsterData == null) {
                    return -1;
                }
                Data monsterInfoData = monsterData.getChildByPath("info");
                return DataTool.getIntConvert("level", monsterInfoData);
            } else {
                return stats.getLevel();
            }
        } catch (NullPointerException npe) {
            log.error("[SEVERE] MOB {} failed to load.", mid, npe);
        }

        return -1;
    }

    private static void decodeElementalString(MonsterStats stats, String elemAttr) {
        for (int i = 0; i < elemAttr.length(); i += 2) {
            stats.setEffectiveness(Element.getFromChar(elemAttr.charAt(i)), ElementalEffectiveness.getByNumber(Integer.parseInt(String.valueOf(elemAttr.charAt(i + 1)))));
        }
    }

    public static NPC getNPC(int nid) {
        String name = npcNames.get(nid);
        if (RequireUtil.isEmpty(name)) {
            name = DataTool.getString(nid + "/name", npcStringData, "MISSINGNO");
            npcNames.put(nid, name);
        }
        return new NPC(nid, new NPCStats(name));
    }

    public static String getNPCName(int nid) {
        return getNPC(nid).getName();
    }

    public static String getNPCDefaultTalk(int nid) {
        return DataTool.getString(nid + "/d0", npcStringData, "(...)");
    }

    public static class BanishInfo {

        private final int map;
        private final String portal;
        private final String msg;

        public BanishInfo(String msg, int map, String portal) {
            this.msg = msg;
            this.map = map;
            this.portal = portal;
        }

        public int getMap() {
            return map;
        }

        public String getPortal() {
            return portal;
        }

        public String getMsg() {
            return msg;
        }
    }

    public static class loseItem {

        private final int id;
        private final byte chance;
        private final byte x;

        public loseItem(int id, byte chance, byte x) {
            this.id = id;
            this.chance = chance;
            this.x = x;
        }

        public int getId() {
            return id;
        }

        public byte getChance() {
            return chance;
        }

        public byte getX() {
            return x;
        }
    }

    public static class selfDestruction {

        private final byte action;
        private final int removeAfter;
        private final int hp;

        public selfDestruction(byte action, int removeAfter, int hp) {
            this.action = action;
            this.removeAfter = removeAfter;
            this.hp = hp;
        }

        public int getHp() {
            return hp;
        }

        public byte getAction() {
            return action;
        }

        public int removeAfter() {
            return removeAfter;
        }
    }
}
