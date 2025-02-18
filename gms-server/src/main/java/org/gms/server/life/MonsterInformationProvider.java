/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gms.server.life;

import org.gms.config.GameConfig;
import org.gms.constants.inventory.ItemConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.provider.Data;
import org.gms.provider.DataProvider;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;
import org.gms.server.ItemInformationProvider;
import org.gms.util.DatabaseConnection;
import org.gms.util.Pair;
import org.gms.util.Randomizer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MonsterInformationProvider {
    private static final Logger log = LoggerFactory.getLogger(MonsterInformationProvider.class);
    // Author : LightPepsi

    private static final MonsterInformationProvider instance = new MonsterInformationProvider();

    public static MonsterInformationProvider getInstance() {
        return instance;
    }

    private final Map<Integer, List<MonsterDropEntry>> drops = new HashMap<>();
    private final List<MonsterGlobalDropEntry> globaldrops = new ArrayList<>();
    private final Map<Integer, List<MonsterGlobalDropEntry>> continentdrops = new HashMap<>();

    private final Map<Integer, List<Integer>> dropsChancePool = new HashMap<>();    // thanks to ronan
    private final Set<Integer> hasNoMultiEquipDrops = new HashSet<>();
    private final Map<Integer, List<MonsterDropEntry>> extraMultiEquipDrops = new HashMap<>();

    private final Map<Pair<Integer, Integer>, Integer> mobAttackAnimationTime = new HashMap<>();
    private final Map<MobSkill, Integer> mobSkillAnimationTime = new HashMap<>();

    private final Map<Integer, Pair<Integer, Integer>> mobAttackInfo = new HashMap<>();

    private final Map<Integer, Boolean> mobBossCache = new HashMap<>();
    private final Map<Integer, String> mobNameCache = new HashMap<>();

    protected MonsterInformationProvider() {
        retrieveGlobal();
    }

    public final List<MonsterGlobalDropEntry> getRelevantGlobalDrops(int mapid) {
        int continentid = mapid / 100000000;

        List<MonsterGlobalDropEntry> contiItems = continentdrops.get(continentid);
        if (contiItems == null) {   // continent separated global drops found thanks to marcuswoon
            contiItems = new ArrayList<>();

            for (MonsterGlobalDropEntry e : globaldrops) {
                if (e.continentid < 0 || e.continentid == continentid) {
                    contiItems.add(e);
                }
            }

            continentdrops.put(continentid, contiItems);
        }

        return contiItems;
    }

    private void retrieveGlobal() {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data_global WHERE chance > 0");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                globaldrops.add(new MonsterGlobalDropEntry(
                        rs.getInt("itemid"),
                        rs.getInt("chance"),
                        rs.getByte("continent"),
                        rs.getInt("minimum_quantity"),
                        rs.getInt("maximum_quantity"),
                        rs.getShort("questid")));
            }
        } catch (SQLException e) {
            log.error("Error retrieving global drops", e);
        }
    }

    public List<MonsterDropEntry> retrieveEffectiveDrop(final int monsterId) {
        // this reads the drop entries searching for multi-equip, properly processing them

        List<MonsterDropEntry> list = retrieveDrop(monsterId);
        if (hasNoMultiEquipDrops.contains(monsterId) || !GameConfig.getServerBoolean("use_multiple_same_equip_drop")) {
            return list;
        }

        List<MonsterDropEntry> multiDrops = extraMultiEquipDrops.get(monsterId), extra = new LinkedList<>();
        if (multiDrops == null) {
            multiDrops = new LinkedList<>();

            for (MonsterDropEntry mde : list) {
                if (ItemConstants.isEquipment(mde.itemId) && mde.Maximum > 1) {
                    multiDrops.add(mde);

                    int rnd = Randomizer.rand(mde.Minimum, mde.Maximum);
                    for (int i = 0; i < rnd - 1; i++) {
                        extra.add(mde);   // this passes copies of the equips' MDE with min/max quantity > 1, but idc on equips they are unused anyways
                    }
                }
            }

            if (!multiDrops.isEmpty()) {
                extraMultiEquipDrops.put(monsterId, multiDrops);
            } else {
                hasNoMultiEquipDrops.add(monsterId);
            }
        } else {
            for (MonsterDropEntry mde : multiDrops) {
                int rnd = Randomizer.rand(mde.Minimum, mde.Maximum);
                for (int i = 0; i < rnd - 1; i++) {
                    extra.add(mde);
                }
            }
        }

        List<MonsterDropEntry> ret = new LinkedList<>(list);
        ret.addAll(extra);

        return ret;
    }

    public final List<MonsterDropEntry> retrieveDrop(final int monsterId) {
        if (drops.containsKey(monsterId)) {
            return drops.get(monsterId);
        }
        final List<MonsterDropEntry> ret = new LinkedList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT itemid, chance, minimum_quantity, maximum_quantity, questid FROM drop_data WHERE dropperid = ?")) {
            ps.setInt(1, monsterId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ret.add(new MonsterDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getShort("questid")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ret;
        }

        drops.put(monsterId, ret);
        return ret;
    }

    public final List<Integer> retrieveDropPool(final int monsterId) {  // ignores Quest and Party Quest items
        if (dropsChancePool.containsKey(monsterId)) {
            return dropsChancePool.get(monsterId);
        }

        ItemInformationProvider ii = ItemInformationProvider.getInstance();

        List<MonsterDropEntry> dropList = retrieveDrop(monsterId);
        List<Integer> ret = new ArrayList<>();

        int accProp = 0;
        for (MonsterDropEntry mde : dropList) {
            if (
                  GameConfig.getServerBoolean("allow_steal_quest_item") ||
                  !ii.isQuestItem(mde.itemId) && !ii.isPartyQuestItem(mde.itemId)
            ) {
                accProp += mde.chance;
            }
            ret.add(accProp);
        }

        if (accProp == 0) {
            ret.clear();    // don't accept mobs dropping no relevant items
        }
        dropsChancePool.put(monsterId, ret);
        return ret;
    }

    public final void setMobAttackAnimationTime(int monsterId, int attackPos, int animationTime) {
        mobAttackAnimationTime.put(new Pair<>(monsterId, attackPos), animationTime);
    }

    public final Integer getMobAttackAnimationTime(int monsterId, int attackPos) {
        Integer time = mobAttackAnimationTime.get(new Pair<>(monsterId, attackPos));
        return time == null ? 0 : time;
    }

    public final void setMobSkillAnimationTime(MobSkill skill, int animationTime) {
        mobSkillAnimationTime.put(skill, animationTime);
    }

    public final Integer getMobSkillAnimationTime(MobSkill skill) {
        Integer time = mobSkillAnimationTime.get(skill);
        return time == null ? 0 : time;
    }

    public final void setMobAttackInfo(int monsterId, int attackPos, int mpCon, int coolTime) {
        mobAttackInfo.put((monsterId << 3) + attackPos, new Pair<>(mpCon, coolTime));
    }

    public final Pair<Integer, Integer> getMobAttackInfo(int monsterId, int attackPos) {
        if (attackPos < 0 || attackPos > 7) {
            return null;
        }
        return mobAttackInfo.get((monsterId << 3) + attackPos);
    }

    public static ArrayList<Pair<Integer, String>> getMobsIDsFromName(String search) {
        DataProvider dataProvider = DataProviderFactory.getDataProvider(WZFiles.STRING);
        ArrayList<Pair<Integer, String>> retMobs = new ArrayList<>();
        Data data = dataProvider.getData("Mob.img");
        List<Pair<Integer, String>> mobPairList = new LinkedList<>();
        for (Data mobIdData : data.getChildren()) {
            int mobIdFromData = Integer.parseInt(mobIdData.getName());
            String mobNameFromData = DataTool.getString(mobIdData.getChildByPath("name"), "NO-NAME");
            mobPairList.add(new Pair<>(mobIdFromData, mobNameFromData));
        }
        for (Pair<Integer, String> mobPair : mobPairList) {
            if (mobPair.getRight().toLowerCase().contains(search.toLowerCase())) {
                retMobs.add(mobPair);
            }
        }
        return retMobs;
    }

    public boolean isBoss(int id) {
        Boolean boss = mobBossCache.get(id);
        if (boss == null) {
            try {
                boss = LifeFactory.getMonster(id).isBoss();
            } catch (NullPointerException npe) {
                boss = false;
            } catch (Exception e) {   //nonexistant mob
                boss = false;

                log.warn("Non-existent mob id {}", id, e);
            }

            mobBossCache.put(id, boss);
        }

        return boss;
    }

    public String getMobNameFromId(int id) {
        String mobName = mobNameCache.get(id);
        if (mobName == null) {
            DataProvider dataProvider = DataProviderFactory.getDataProvider(WZFiles.STRING);
            Data mobData = dataProvider.getData("Mob.img");

            mobName = DataTool.getString(mobData.getChildByPath(id + "/name"), "");
            mobNameCache.put(id, mobName);
        }

        return mobName;
    }

    public final void clearDrops() {
        drops.clear();
        hasNoMultiEquipDrops.clear();
        extraMultiEquipDrops.clear();
        dropsChancePool.clear();
        globaldrops.clear();
        continentdrops.clear();
        retrieveGlobal();
    }
}
