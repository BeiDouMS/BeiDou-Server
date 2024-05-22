package org.gms.server.partyquest;

import org.gms.client.Disease;
import org.gms.provider.Data;
import org.gms.provider.DataProvider;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;
import org.gms.server.life.MobSkill;
import org.gms.server.life.MobSkillFactory;
import org.gms.server.life.MobSkillType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Drago (Dragohe4rt)
 */
public class CarnivalFactory {

    private final static CarnivalFactory instance = new CarnivalFactory();
    private final Map<Integer, MCSkill> skills = new HashMap<>();
    private final Map<Integer, MCSkill> guardians = new HashMap<>();
    private final DataProvider dataRoot = DataProviderFactory.getDataProvider(WZFiles.SKILL);

    private final List<Integer> singleTargetedSkills = new ArrayList<>();
    private final List<Integer> multiTargetedSkills = new ArrayList<>();

    public CarnivalFactory() {
        //whoosh
        initialize();
    }

    public static final CarnivalFactory getInstance() {
        return instance;
    }

    private void initialize() {
        if (skills.size() != 0) {
            return;
        }
        for (Data z : dataRoot.getData("MCSkill.img")) {
            Integer id = Integer.parseInt(z.getName());
            int spendCp = DataTool.getInt("spendCP", z, 0);
            int mobSkillId = DataTool.getInt("mobSkillID", z, 0);
            MobSkillType mobSkillType = null;
            if (mobSkillId != 0) {
                mobSkillType = MobSkillType.from(mobSkillId).orElseThrow();
            }
            int level = DataTool.getInt("level", z, 0);
            boolean isMultiTarget = DataTool.getInt("target", z, 1) > 1;
            MCSkill ms = new MCSkill(spendCp, mobSkillType, level, isMultiTarget);

            skills.put(id, ms);
            if (ms.targetsAll) {
                multiTargetedSkills.add(id);
            } else {
                singleTargetedSkills.add(id);
            }
        }
        for (Data z : dataRoot.getData("MCGuardian.img")) {
            int spendCp = DataTool.getInt("spendCP", z, 0);
            int mobSkillId = DataTool.getInt("mobSkillID", z, 0);
            MobSkillType mobSkillType = MobSkillType.from(mobSkillId).orElseThrow();
            int level = DataTool.getInt("level", z, 0);
            guardians.put(Integer.parseInt(z.getName()), new MCSkill(spendCp, mobSkillType, level, true));
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
        if (skill != null && skill.mobSkillType == null) {
            return randomizeSkill(skill.targetsAll);
        } else {
            return skill;
        }
    }

    public MCSkill getGuardian(final int id) {
        return guardians.get(id);
    }

    public record MCSkill(int cpLoss, MobSkillType mobSkillType, int level, boolean targetsAll) {
        public MobSkill getSkill() {
            return MobSkillFactory.getMobSkillOrThrow(mobSkillType, level);
        }

        public Disease getDisease() {
            return Disease.getBySkill(mobSkillType);
        }
    }
}
