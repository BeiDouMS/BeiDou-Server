package org.gms.client;

public enum FamilyEntitlement {
    FAMILY_REUINION(1, 300, "直接移动到学院成员身边", "[对象] 我\\n[效果] 直接移动到学院成员身边."),
    SUMMON_FAMILY(1, 500, "直接召唤学院成员", "[对象] 学院成员 1名\\n[效果] 直接可以召唤指定的学院成员到现在\\n的地图."),
    SELF_DROP_1_5(1, 700, "我的爆率 1.5倍 (15 分钟)", "[对象] 我\\n[时间] 15 分钟.\\n[效果] 打怪爆率增加到 #c1.5倍#.\\n*  与爆率活动重叠时失效."),
    SELF_EXP_1_5(1, 800, "我的经验值 1.5倍 (15 分钟)", "[对象] 我\\n[时间] 15 分钟.\\n[效果] 打怪经验值增加到 #c1.5倍#.\\n* 与经验活动重叠时失效."),
    FAMILY_BONDING(1, 1000, "我的学院 (30 分钟)", "[对象] 至少有 6 位在线家庭成员在我的家里\\n[时间] 30 分钟.\\n[效果] 打怪爆率和打怪经验值增加到 #c2倍#. \\n* 与经验活动重叠时失效."),
    SELF_DROP_2(1, 1200, "我的爆率 2倍 (15 分钟)", "[对象] 我\\n[时间] 15 分钟.\\n[效果] 打怪爆率增加到 #c2倍#.\\n* 与爆率活动重叠时失效."),
    SELF_EXP_2(1, 1500, "我的经验值 2倍 (15 分钟)", "[对象] 我\\n[时间] 15 分钟.\\n[效果] 打怪经验值增加到 #c2倍#.\\n* 与经验活动重叠时失效."),
    SELF_DROP_2_30MIN(1, 2000, "我的爆率 2倍 (30 分钟)", "[对象] 我\\n[时间] 30 分钟.\\n[效果] 打怪爆率增加到 #c2倍#.\\n* 与爆率活动重叠时失效."),
    SELF_EXP_2_30MIN(1, 2500, "我的经验值 2倍 (30 分钟)", "[对象] 我\\n[时间] 30 分钟.\\n[效果] 打怪经验值增加到 #c2倍#. \\n* 与经验活动重叠时失效."),
    PARTY_DROP_2_30MIN(1, 4000, "我的组队爆率 2倍 (30 分钟)", "[对象] 我的组队\\n[时间] 30 分钟.\\n[效果] 打怪爆率增加到 #c2倍#.\\n* 与爆率活动重叠时失效."),
    PARTY_EXP_2_30MIN(1, 5000, "我的组队经验值 2倍 (30 分钟)", "[对象] 我的组队\\n[时间] 30 分钟.\\n[效果] 打怪经验值增加到 #c2倍#.\\n* 与经验活动重叠时失效.");

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
