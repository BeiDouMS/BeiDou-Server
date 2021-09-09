package tools.mapletools;

import provider.*;
import provider.wz.DataType;
import provider.wz.WZFiles;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.MapleLifeFactory.BanishInfo;
import server.life.MapleLifeFactory.loseItem;
import server.life.MapleLifeFactory.selfDestruction;
import server.life.MapleMonsterStats;
import tools.Pair;

import java.util.*;

public class MonsterStatFetcher {
    private static final DataProvider data = DataProviderFactory.getDataProvider(WZFiles.MOB);
    private static final DataProvider stringDataWZ = DataProviderFactory.getDataProvider(WZFiles.STRING);
    private static final Data mobStringData = stringDataWZ.getData("Mob.img");
    private static final Map<Integer, MapleMonsterStats> monsterStats = new HashMap<>();

    static Map<Integer, MapleMonsterStats> getAllMonsterStats() {
        DataDirectoryEntry root = data.getRoot();

        System.out.print("Parsing mob stats... ");
        for (DataFileEntry mFile : root.getFiles()) {
            try {
                String fileName = mFile.getName();

                //System.out.println("Parsing '" + fileName + "'");
                Data monsterData = data.getData(fileName);
                if (monsterData == null) {
                    continue;
                }

                Integer mid = getMonsterId(fileName);

                Data monsterInfoData = monsterData.getChildByPath("info");
                MapleMonsterStats stats = new MapleMonsterStats();
                stats.setHp(MapleDataTool.getIntConvert("maxHP", monsterInfoData));
                stats.setFriendly(MapleDataTool.getIntConvert("damagedByMob", monsterInfoData, 0) == 1);
                stats.setPADamage(MapleDataTool.getIntConvert("PADamage", monsterInfoData));
                stats.setPDDamage(MapleDataTool.getIntConvert("PDDamage", monsterInfoData));
                stats.setMADamage(MapleDataTool.getIntConvert("MADamage", monsterInfoData));
                stats.setMDDamage(MapleDataTool.getIntConvert("MDDamage", monsterInfoData));
                stats.setMp(MapleDataTool.getIntConvert("maxMP", monsterInfoData, 0));
                stats.setExp(MapleDataTool.getIntConvert("exp", monsterInfoData, 0));
                stats.setLevel(MapleDataTool.getIntConvert("level", monsterInfoData));
                stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", monsterInfoData, 0));
                stats.setBoss(MapleDataTool.getIntConvert("boss", monsterInfoData, 0) > 0);
                stats.setExplosiveReward(MapleDataTool.getIntConvert("explosiveReward", monsterInfoData, 0) > 0);
                stats.setFfaLoot(MapleDataTool.getIntConvert("publicReward", monsterInfoData, 0) > 0);
                stats.setUndead(MapleDataTool.getIntConvert("undead", monsterInfoData, 0) > 0);
                stats.setName(MapleDataTool.getString(mid + "/name", mobStringData, "MISSINGNO"));
                stats.setBuffToGive(MapleDataTool.getIntConvert("buff", monsterInfoData, -1));
                stats.setCP(MapleDataTool.getIntConvert("getCP", monsterInfoData, 0));
                stats.setRemoveOnMiss(MapleDataTool.getIntConvert("removeOnMiss", monsterInfoData, 0) > 0);

                Data special = monsterInfoData.getChildByPath("coolDamage");
                if (special != null) {
                    int coolDmg = MapleDataTool.getIntConvert("coolDamage", monsterInfoData);
                    int coolProb = MapleDataTool.getIntConvert("coolDamageProb", monsterInfoData, 0);
                    stats.setCool(new Pair<>(coolDmg, coolProb));
                }
                special = monsterInfoData.getChildByPath("loseItem");
                if (special != null) {
                    for (Data liData : special.getChildren()) {
                        stats.addLoseItem(new loseItem(MapleDataTool.getInt(liData.getChildByPath("id")), (byte) MapleDataTool.getInt(liData.getChildByPath("prop")), (byte) MapleDataTool.getInt(liData.getChildByPath("x"))));
                    }
                }
                special = monsterInfoData.getChildByPath("selfDestruction");
                if (special != null) {
                    stats.setSelfDestruction(new selfDestruction((byte) MapleDataTool.getInt(special.getChildByPath("action")), MapleDataTool.getIntConvert("removeAfter", special, -1), MapleDataTool.getIntConvert("hp", special, -1)));
                }
                Data firstAttackData = monsterInfoData.getChildByPath("firstAttack");
                int firstAttack = 0;
                if (firstAttackData != null) {
                    if (firstAttackData.getType() == DataType.FLOAT) {
                        firstAttack = Math.round(MapleDataTool.getFloat(firstAttackData));
                    } else {
                        firstAttack = MapleDataTool.getInt(firstAttackData);
                    }
                }
                stats.setFirstAttack(firstAttack > 0);
                stats.setDropPeriod(MapleDataTool.getIntConvert("dropItemPeriod", monsterInfoData, 0) * 10000);

                stats.setTagColor(MapleDataTool.getIntConvert("hpTagColor", monsterInfoData, 0));
                stats.setTagBgColor(MapleDataTool.getIntConvert("hpTagBgcolor", monsterInfoData, 0));

                for (Data idata : monsterData) {
                    if (!idata.getName().equals("info")) {
                        int delay = 0;
                        for (Data pic : idata.getChildren()) {
                            delay += MapleDataTool.getIntConvert("delay", pic, 0);
                        }
                        stats.setAnimationTime(idata.getName(), delay);
                    }
                }
                Data reviveInfo = monsterInfoData.getChildByPath("revive");
                if (reviveInfo != null) {
                    List<Integer> revives = new LinkedList<>();
                    for (Data data_ : reviveInfo) {
                        revives.add(MapleDataTool.getInt(data_));
                    }
                    stats.setRevives(revives);
                }
                decodeElementalString(stats, MapleDataTool.getString("elemAttr", monsterInfoData, ""));
                Data monsterSkillData = monsterInfoData.getChildByPath("skill");
                if (monsterSkillData != null) {
                    int i = 0;
                    List<Pair<Integer, Integer>> skills = new ArrayList<>();
                    while (monsterSkillData.getChildByPath(Integer.toString(i)) != null) {
                        skills.add(new Pair<>(MapleDataTool.getInt(i + "/skill", monsterSkillData, 0), MapleDataTool.getInt(i + "/level", monsterSkillData, 0)));
                        i++;
                    }
                    stats.setSkills(skills);
                }
                Data banishData = monsterInfoData.getChildByPath("ban");
                if (banishData != null) {
                    stats.setBanishInfo(new BanishInfo(MapleDataTool.getString("banMsg", banishData), MapleDataTool.getInt("banMap/0/field", banishData, -1), MapleDataTool.getString("banMap/0/portal", banishData, "sp")));
                }

                monsterStats.put(mid, stats);
            } catch(NullPointerException npe) {
                //System.out.println("[SEVERE] " + mFile.getName() + " failed to load. Issue: " + npe.getMessage() + "\n\n");
            }
        }

        System.out.println("Done parsing mob stats!");
        return monsterStats;
    }

    private static int getMonsterId(String fileName) {
        return Integer.parseInt(fileName.substring(0, 7));
    }

    private static void decodeElementalString(MapleMonsterStats stats, String elemAttr) {
        for (int i = 0; i < elemAttr.length(); i += 2) {
            stats.setEffectiveness(Element.getFromChar(elemAttr.charAt(i)), ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
        }
    }

}
