package server.partyquest;

import client.Disease;
import provider.Data;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import provider.wz.WZFiles;
import server.life.MobSkill;
import server.life.MobSkillFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
    *@author Drago (Dragohe4rt)
*/
public class MapleCarnivalFactory {

    private final static MapleCarnivalFactory instance = new MapleCarnivalFactory();
    private final Map<Integer, MCSkill> skills = new HashMap<>();
    private final Map<Integer, MCSkill> guardians = new HashMap<>();
    private final MapleDataProvider dataRoot = MapleDataProviderFactory.getDataProvider(WZFiles.SKILL);
    
    private final List<Integer> singleTargetedSkills = new ArrayList<>();
    private final List<Integer> multiTargetedSkills = new ArrayList<>();

    public MapleCarnivalFactory() {
        //whoosh
	initialize();
    }

    public static final MapleCarnivalFactory getInstance() {
        return instance;
    }

    private void initialize() {
        if (skills.size() != 0) {
            return;
        }
        for (Data z : dataRoot.getData("MCSkill.img")) {
            Integer id = Integer.parseInt(z.getName());
            MCSkill ms = new MCSkill(MapleDataTool.getInt("spendCP", z, 0), MapleDataTool.getInt("mobSkillID", z, 0), MapleDataTool.getInt("level", z, 0), MapleDataTool.getInt("target", z, 1) > 1);
            
            skills.put(id, ms);
            if (ms.targetsAll) {
                multiTargetedSkills.add(id);
            } else {
                singleTargetedSkills.add(id);
            }
        }
        for (Data z : dataRoot.getData("MCGuardian.img")) {
            guardians.put(Integer.parseInt(z.getName()), new MCSkill(MapleDataTool.getInt("spendCP", z, 0), MapleDataTool.getInt("mobSkillID", z, 0), MapleDataTool.getInt("level", z, 0), true));
        }
    }

    private MCSkill randomizeSkill(boolean multi) {
        if (multi) {
            return skills.get(multiTargetedSkills.get((int) (Math.random() * multiTargetedSkills.size())));
        } else {
            return skills.get(singleTargetedSkills.get((int) (Math.random() * singleTargetedSkills.size())));
        }
    }
    
    public MCSkill getSkill(final int id) {
        MCSkill skill = skills.get(id);
        if (skill != null && skill.skillid <= 0) {
            return randomizeSkill(skill.targetsAll);
        } else {
            return skill;
        }
    }

    public MCSkill getGuardian(final int id) {
        return guardians.get(id);
    }

    public static class MCSkill {

        public int cpLoss, skillid, level;
        public boolean targetsAll;

        public MCSkill(int _cpLoss, int _skillid, int _level, boolean _targetsAll) {
            cpLoss = _cpLoss;
            skillid = _skillid;
            level = _level;
            targetsAll = _targetsAll;
        }

        public MobSkill getSkill() {
            return getMobSkill(skillid, level);
        }
        
        public static MobSkill getMobSkill(int skillid, int level) {
            return MobSkillFactory.getMobSkill(skillid, level);
        }

        public Disease getDisease() {
            return Disease.getBySkill(skillid);
        }
    }
}
