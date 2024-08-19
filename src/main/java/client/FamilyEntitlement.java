package client;

public enum FamilyEntitlement {
    FAMILY_REUINION(1, 300, "特殊传送", "[目标] 我\\n[效果] 传送到你选择的成员处。"),
    SUMMON_FAMILY(1, 500, "成员召唤", "[目标] 1 名成员\\n[效果] 传送你选择成员到你身边。"),
    SELF_DROP_1_5(1, 700, "爆率加成 1.5x (15 分钟)", "[目标] 我\\n[时间] 15 分钟\\n[效果] 怪物爆率提升 #c1.5x#\\n*  如果有同类活动进行，\\n这个效果将被取消。"),
    SELF_EXP_1_5(1, 800, "经验加成 1.5x (15 分钟)", "[目标] 我\\n[时间] 15 分钟\\n[效果] 狩猎获得的经验提升 #c1.5x#\\n* 如果有同类活动进行，\\n这个效果将被取消。"),
    FAMILY_BONDING(1, 1000, "超级加成 (30 分钟)", "[目标] 至少有 6 名下级成员在线\\n[时间] 30 分钟\\n[效果] 狩猎经验和爆率提升 #c2x#\\n* 如果有同类活动进行，\\n这个效果将被取消。"),
    SELF_DROP_2(1, 1200, "爆率加成 2x (15 分钟)", "[目标] 我\\n[时间] 15 分钟\\n[效果] 怪物爆率提升 #c2x#\\n* 如果有同类活动进行，\\n这个效果将被取消。"),
    SELF_EXP_2(1, 1500, "经验加成 2x (15 分钟)", "[目标] 我\\n[时间] 15 分钟\\n[效果] 狩猎经验提升 #c2x#\\n* 如果有同类活动进行，\\n这个效果将被取消。"),
    SELF_DROP_2_30MIN(1, 2000, "爆率加成 2x (30 分钟)", "[目标] 我\\n[时间] 30 分钟\\n[效果] 怪物爆率提升 #c2x#\\n* 如果有同类活动进行，\\n这个效果将被取消。"),
    SELF_EXP_2_30MIN(1, 2500, "经验加成 2x (30 分钟)", "[目标] 我\\n[时间] 30 分钟\\n[效果] 狩猎经验提升 #c2x#\\n* 如果有同类活动进行，\\n这个效果将被取消。"),
    PARTY_DROP_2_30MIN(1, 4000, "团队爆率加成 2x (30 分钟)", "[目标] 我的队伍\\n[时间] 30 分钟\\n[效果] 怪物爆率提升 #c2x#\\n* 如果有同类活动进行，\\n这个效果将被取消。"),
    PARTY_EXP_2_30MIN(1, 5000, "团队经验加成 2x (30 分钟)", "[目标] 我的队伍\\n[时间] 30 分钟\\n[效果] 狩猎经验提升 #c2x#\\n* 如果有同类活动进行，\\n这个效果将被取消。");

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
