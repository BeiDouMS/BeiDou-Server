package org.gms.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * H5 角色信息页：属性、技能、装备栏、道具栏。
 * 数据全部取自服务端数据库与 WZ 资源，换算方式与挂机收益模型一致。
 *
 * @since 2026-11-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class H5CharacterInfoRtnDTO {
    private Integer characterId;
    private String characterName;
    private Integer level;
    private Integer job;
    private String jobName;
    private Integer world;
    private Integer gender;
    private Integer fame;
    private Integer exp;
    private Long meso;
    private Integer hp;
    private Integer mp;
    private Integer maxHp;
    private Integer maxMp;
    /** 剩余能力点 */
    private Integer ap;
    /** 剩余技能点 */
    private Integer sp;
    /** physical / magic */
    private String attackType;
    private Double dps;
    /** 属性汇总，含装备加成 */
    private List<H5StatRtnDTO> stats;
    private List<H5SkillRtnDTO> skills;
    /** 已装备（position < 0） */
    private List<H5EquipRtnDTO> equipped;
    /** 装备栏内未穿戴的装备 */
    private List<H5EquipRtnDTO> equips;
    private List<H5ItemEntryRtnDTO> useItems;
    private List<H5ItemEntryRtnDTO> setupItems;
    private List<H5ItemEntryRtnDTO> etcItems;
    private List<H5ItemEntryRtnDTO> cashItems;

    /** 单条属性：基础值 + 装备加成 = 合计 */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class H5StatRtnDTO {
        private String key;
        private String label;
        private Integer base;
        private Integer bonus;
        private Integer total;
    }

    /** 已学技能 */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class H5SkillRtnDTO {
        private Integer skillId;
        private String name;
        private Integer level;
        private Integer masterLevel;
    }

    /** 装备（含卷轴强化后的实际属性） */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class H5EquipRtnDTO {
        private Integer itemId;
        private String itemName;
        private Integer position;
        private Boolean equipped;
        /** 剩余可升级次数 */
        private Integer upgradeSlots;
        /** 装备等级（0 为未升级） */
        private Integer level;
        private Integer reqLevel;
        private Integer reqJob;
        private Integer str;
        private Integer dex;
        private Integer intValue;
        private Integer luk;
        private Integer hp;
        private Integer mp;
        private Integer watk;
        private Integer matk;
        private Integer wdef;
        private Integer mdef;
        private Integer acc;
        private Integer avoid;
        private Integer hands;
        private Integer speed;
        private Integer jump;
        private Integer vicious;
        private Integer itemLevel;
        private Integer itemExp;
        private Long expiration;
    }

    /** 消耗/设置/其它/现金栏物品 */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class H5ItemEntryRtnDTO {
        private Integer itemId;
        private String itemName;
        /** 1 装备 2 消耗 3 设置 4 其它 5 现金 */
        private Integer inventoryType;
        private Integer position;
        private Long quantity;
        private Long expiration;
    }
}
