package org.gms.client;

import org.gms.util.I18nUtil;

public enum FamilyEntitlement {
    FAMILY_REUINION(1, 300, I18nUtil.getMessage("FamilyEntitlement.message1"), I18nUtil.getMessage("FamilyEntitlement.message2")),
    SUMMON_FAMILY(1, 500, I18nUtil.getMessage("FamilyEntitlement.message3"), I18nUtil.getMessage("FamilyEntitlement.message4")),
    SELF_DROP_1_5(1, 700, I18nUtil.getMessage("FamilyEntitlement.message5"), I18nUtil.getMessage("FamilyEntitlement.message6")),
    SELF_EXP_1_5(1, 800, I18nUtil.getMessage("FamilyEntitlement.message7"), I18nUtil.getMessage("FamilyEntitlement.message8")),
    FAMILY_BONDING(1, 1000, I18nUtil.getMessage("FamilyEntitlement.message9"), I18nUtil.getMessage("FamilyEntitlement.message10")),
    SELF_DROP_2(1, 1200, I18nUtil.getMessage("FamilyEntitlement.message11"), I18nUtil.getMessage("FamilyEntitlement.message12")),
    SELF_EXP_2(1, 1500, I18nUtil.getMessage("FamilyEntitlement.message13"), I18nUtil.getMessage("FamilyEntitlement.message14")),
    SELF_DROP_2_30MIN(1, 2000, I18nUtil.getMessage("FamilyEntitlement.message15"), I18nUtil.getMessage("FamilyEntitlement.message16")),
    SELF_EXP_2_30MIN(1, 2500, I18nUtil.getMessage("FamilyEntitlement.message17"), I18nUtil.getMessage("FamilyEntitlement.message18")),
    PARTY_DROP_2_30MIN(1, 4000, I18nUtil.getMessage("FamilyEntitlement.message19"), I18nUtil.getMessage("FamilyEntitlement.message20")),
    PARTY_EXP_2_30MIN(1, 5000, I18nUtil.getMessage("FamilyEntitlement.message21"), I18nUtil.getMessage("FamilyEntitlement.message22"));

    private final int usageLimit, repCost;
    private final String name, description;

    FamilyEntitlement(int usageLimit, int repCost, String name, String description) {
        this.usageLimit = usageLimit;
        this.repCost = repCost;
        this.name = name;
        this.description = description;
    }

    public int getUsageLimit() {
        return usageLimit;
    }

    public int getRepCost() {
        return repCost;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
