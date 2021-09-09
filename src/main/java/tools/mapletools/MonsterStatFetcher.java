package tools.mapletools;

import provider.*;
import provider.wz.DataType;
import provider.wz.WZFiles;
import server.life.Element;
import server.life.ElementalEffectiveness;
import server.life.LifeFactory.BanishInfo;
import server.life.LifeFactory.loseItem;
import server.life.LifeFactory.selfDestruction;
import server.life.MonsterStats;
import tools.Pair;

import java.util.*;

public class MonsterStatFetcher {
    private static final DataProvider data = DataProviderFactory.getDataProvider(WZFiles.MOB);
    private static final DataProvider stringDataWZ = DataProviderFactory.getDataProvider(WZFiles.STRING);
    private static final Data mobStringData = stringDataWZ.getData("Mob.img");
    private static final Map<Integer, MonsterStats> monsterStats = new HashMap<>();

    static Map<Integer, MonsterStats> getAllMonsterStats() {
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
                MonsterStats stats = new MonsterStats();
                stats.setHp(DataTool.getIntConvert("maxHP", monsterInfoData));
                stats.setFriendly(DataTool.getIntConvert("damagedByMob", monsterInfoData, 0) == 1);
                stats.setPADamage(DataTool.getIntConvert("PADamage", monsterInfoData));
                stats.setPDDamage(DataTool.getIntConvert("PDDamage", monsterInfoData));
                stats.setMADamage(DataTool.getIntConvert("MADamage", monsterInfoData));
                stats.setMDDamage(DataTool.getIntConvert("MDDamage", monsterInfoData));
                stats.setMp(DataTool.getIntConvert("maxMP", monsterInfoData, 0));
                stats.setExp(DataTool.getIntConvert("exp", monsterInfoData, 0));
                stats.setLevel(DataTool.getIntConvert("level", monsterInfoData));
                stats.setRemoveAfter(DataTool.getIntConvert("removeAfter", monsterInfoData, 0));
                stats.setBoss(DataTool.getIntConvert("boss", monsterInfoData, 0) > 0);
                stats.setExplosiveReward(DataTool.getIntConvert("explosiveReward", monsterInfoData, 0) > 0);
                stats.setFfaLoot(DataTool.getIntConvert("publicReward", monsterInfoData, 0) > 0);
                stats.setUndead(DataTool.getIntConvert("undead", monsterInfoData, 0) > 0);
                stats.setName(DataTool.getString(mid + "/name", mobStringData, "MISSINGNO"));
                stats.setBuffToGive(DataTool.getIntConvert("buff", monsterInfoData, -1));
                stats.setCP(DataTool.getIntConvert("getCP", monsterInfoData, 0));
                stats.setRemoveOnMiss(DataTool.getIntConvert("removeOnMiss", monsterInfoData, 0) > 0);

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
                stats.setDropPeriod(DataTool.getIntConvert("dropItemPeriod", monsterInfoData, 0) * 10000);

                stats.setTagColor(DataTool.getIntConvert("hpTagColor", monsterInfoData, 0));
                stats.setTagBgColor(DataTool.getIntConvert("hpTagBgcolor", monsterInfoData, 0));

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
                Data monsterSkillData = monsterInfoData.getChildByPath("skill");
                if (monsterSkillData != null) {
                    int i = 0;
                    List<Pair<Integer, Integer>> skills = new ArrayList<>();
                    while (monsterSkillData.getChildByPath(Integer.toString(i)) != null) {
                        skills.add(new Pair<>(DataTool.getInt(i + "/skill", monsterSkillData, 0), DataTool.getInt(i + "/level", monsterSkillData, 0)));
                        i++;
                    }
                    stats.setSkills(skills);
                }
                Data banishData = monsterInfoData.getChildByPath("ban");
                if (banishData != null) {
                    stats.setBanishInfo(new BanishInfo(DataTool.getString("banMsg", banishData), DataTool.getInt("banMap/0/field", banishData, -1), DataTool.getString("banMap/0/portal", banishData, "sp")));
                }

                monsterStats.put(mid, stats);
            } catch (NullPointerException npe) {
                //System.out.println("[SEVERE] " + mFile.getName() + " failed to load. Issue: " + npe.getMessage() + "\n\n");
            }
        }

        System.out.println("Done parsing mob stats!");
        return monsterStats;
    }

    private static int getMonsterId(String fileName) {
        return Integer.parseInt(fileName.substring(0, 7));
    }

    private static void decodeElementalString(MonsterStats stats, String elemAttr) {
        for (int i = 0; i < elemAttr.length(); i += 2) {
            stats.setEffectiveness(Element.getFromChar(elemAttr.charAt(i)), ElementalEffectiveness.getByNumber(Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
        }
    }

}
