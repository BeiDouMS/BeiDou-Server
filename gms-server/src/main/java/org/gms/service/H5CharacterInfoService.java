package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.Job;
import org.gms.dao.entity.CharactersDO;
import org.gms.dao.entity.InventoryequipmentDO;
import org.gms.dao.entity.InventoryitemsDO;
import org.gms.dao.entity.SkillsDO;
import org.gms.dao.mapper.CharactersMapper;
import org.gms.dao.mapper.InventoryequipmentMapper;
import org.gms.dao.mapper.InventoryitemsMapper;
import org.gms.dao.mapper.SkillsMapper;
import org.gms.exception.BizException;
import org.gms.model.dto.H5CharacterInfoRtnDTO;
import org.gms.provider.Data;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;
import org.gms.server.ItemInformationProvider;
import org.gms.util.I18nUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.gms.dao.entity.table.InventoryequipmentDOTableDef.INVENTORYEQUIPMENT_D_O;

/**
 * H5 角色信息页数据。
 * <p>
 * 属性取自 {@code characters}（基础点）与 {@code inventoryequipment}（装备实装属性，已含卷轴强化），
 * 装备与道具取自 {@code inventoryitems}，技能取自 {@code skills}，技能名取自客户端 String.wz。
 *
 * @since 2026-11-01
 */
@Service
@Slf4j
public class H5CharacterInfoService {
    /** 装备栏（背包内） */
    private static final int INV_EQUIP = 1;
    /** 已穿戴 */
    private static final int INV_EQUIPPED = -1;
    private static final int INV_USE = 2;
    private static final int INV_SETUP = 3;
    private static final int INV_ETC = 4;
    private static final int INV_CASH = 5;

    private final CharactersMapper charactersMapper;
    private final InventoryitemsMapper inventoryitemsMapper;
    private final InventoryequipmentMapper inventoryequipmentMapper;
    private final SkillsMapper skillsMapper;
    private final H5CatalogService catalogService;

    private volatile Map<Integer, String> skillNameCache;

    public H5CharacterInfoService(CharactersMapper charactersMapper,
                                  InventoryitemsMapper inventoryitemsMapper,
                                  InventoryequipmentMapper inventoryequipmentMapper,
                                  SkillsMapper skillsMapper,
                                  H5CatalogService catalogService) {
        this.charactersMapper = charactersMapper;
        this.inventoryitemsMapper = inventoryitemsMapper;
        this.inventoryequipmentMapper = inventoryequipmentMapper;
        this.skillsMapper = skillsMapper;
        this.catalogService = catalogService;
    }

    public H5CharacterInfoRtnDTO info(int characterId) {
        CharactersDO chr = charactersMapper.selectOneById(characterId);
        if (chr == null) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_CHARACTER_NOT_FOUND"));
        }
        H5CatalogService.Power power = catalogService.resolvePower(chr);

        List<InventoryitemsDO> items = inventoryitemsMapper.selectListByQuery(
                QueryWrapper.create().eq("characterid", characterId));
        Map<Long, InventoryequipmentDO> equipStats = loadEquipStats(items);

        List<H5CharacterInfoRtnDTO.H5EquipRtnDTO> equipped = new ArrayList<>();
        List<H5CharacterInfoRtnDTO.H5EquipRtnDTO> equips = new ArrayList<>();
        List<H5CharacterInfoRtnDTO.H5ItemEntryRtnDTO> useItems = new ArrayList<>();
        List<H5CharacterInfoRtnDTO.H5ItemEntryRtnDTO> setupItems = new ArrayList<>();
        List<H5CharacterInfoRtnDTO.H5ItemEntryRtnDTO> etcItems = new ArrayList<>();
        List<H5CharacterInfoRtnDTO.H5ItemEntryRtnDTO> cashItems = new ArrayList<>();

        int str = 0, dex = 0, intValue = 0, luk = 0;
        int watk = 0, matk = 0, wdef = 0, mdef = 0, acc = 0, avoid = 0, hands = 0, speed = 0, jump = 0;

        for (InventoryitemsDO item : items) {
            if (item == null || item.getItemid() == null) {
                continue;
            }
            int type = orZero(item.getInventorytype());
            int position = orZero(item.getPosition());
            if (type == INV_EQUIP || type == INV_EQUIPPED) {
                InventoryequipmentDO stats = equipStats.get(item.getInventoryitemid());
                H5CharacterInfoRtnDTO.H5EquipRtnDTO dto = toEquip(item, stats);
                if (position < 0) {
                    equipped.add(dto);
                    str += orZero(dto.getStr());
                    dex += orZero(dto.getDex());
                    intValue += orZero(dto.getIntValue());
                    luk += orZero(dto.getLuk());
                    watk += orZero(dto.getWatk());
                    matk += orZero(dto.getMatk());
                    wdef += orZero(dto.getWdef());
                    mdef += orZero(dto.getMdef());
                    acc += orZero(dto.getAcc());
                    avoid += orZero(dto.getAvoid());
                    hands += orZero(dto.getHands());
                    speed += orZero(dto.getSpeed());
                    jump += orZero(dto.getJump());
                } else {
                    equips.add(dto);
                }
                continue;
            }
            H5CharacterInfoRtnDTO.H5ItemEntryRtnDTO entry = H5CharacterInfoRtnDTO.H5ItemEntryRtnDTO.builder()
                    .itemId(item.getItemid())
                    .itemName(catalogService.itemName(item.getItemid()))
                    .inventoryType(type)
                    .position(position)
                    .quantity(Optional.ofNullable(item.getQuantity()).map(Integer::longValue).orElse(0L))
                    .expiration(item.getExpiration())
                    .build();
            switch (type) {
                case INV_USE -> useItems.add(entry);
                case INV_SETUP -> setupItems.add(entry);
                case INV_ETC -> etcItems.add(entry);
                case INV_CASH -> cashItems.add(entry);
                default -> {
                    // 未知栏位不展示
                }
            }
        }

        equipped.sort(Comparator.comparing(i -> orZero(i.getPosition())));
        equips.sort(Comparator.comparing(i -> orZero(i.getPosition())));
        sortByPosition(useItems);
        sortByPosition(setupItems);
        sortByPosition(etcItems);
        sortByPosition(cashItems);

        int baseStr = orZero(chr.getAttrStr());
        int baseDex = orZero(chr.getAttrDex());
        int baseInt = orZero(chr.getAttrInt());
        int baseLuk = orZero(chr.getAttrLuk());

        List<H5CharacterInfoRtnDTO.H5StatRtnDTO> stats = new ArrayList<>();
        stats.add(stat("str", "力量", baseStr, str));
        stats.add(stat("dex", "敏捷", baseDex, dex));
        stats.add(stat("int", "智力", baseInt, intValue));
        stats.add(stat("luk", "幸运", baseLuk, luk));
        stats.add(stat("watk", "攻击力", 0, watk));
        stats.add(stat("matk", "魔力", 0, matk));
        stats.add(stat("wdef", "物理防御", 0, wdef));
        stats.add(stat("mdef", "魔法防御", 0, mdef));
        stats.add(stat("acc", "命中", 0, acc));
        stats.add(stat("avoid", "回避", 0, avoid));
        stats.add(stat("hands", "手技", 0, hands));
        stats.add(stat("speed", "移动速度", 0, speed));
        stats.add(stat("jump", "跳跃", 0, jump));

        Job job = Job.getById(orZero(chr.getJob()));
        return H5CharacterInfoRtnDTO.builder()
                .characterId(chr.getId())
                .characterName(chr.getName())
                .level(Optional.ofNullable(chr.getLevel()).orElse(1))
                .job(orZero(chr.getJob()))
                .jobName(job.getName())
                .world(orZero(chr.getWorld()))
                .gender(orZero(chr.getGender()))
                .fame(orZero(chr.getFame()))
                .exp(orZero(chr.getExp()))
                .meso(Optional.ofNullable(chr.getMeso()).map(Integer::longValue).orElse(0L))
                .hp(orZero(chr.getHp()))
                .mp(orZero(chr.getMp()))
                .maxHp(orZero(chr.getMaxhp()))
                .maxMp(orZero(chr.getMaxmp()))
                .ap(orZero(chr.getAp()))
                .sp(remainingSp(chr.getSp()))
                .attackType(power.mage ? "magic" : "physical")
                .dps(power.dps)
                .stats(stats)
                .skills(skills(chr.getId()))
                .equipped(equipped)
                .equips(equips)
                .useItems(useItems)
                .setupItems(setupItems)
                .etcItems(etcItems)
                .cashItems(cashItems)
                .build();
    }

    private Map<Long, InventoryequipmentDO> loadEquipStats(List<InventoryitemsDO> items) {
        List<Long> ids = new ArrayList<>();
        for (InventoryitemsDO item : items) {
            int type = orZero(item.getInventorytype());
            if (item.getInventoryitemid() != null && (type == INV_EQUIP || type == INV_EQUIPPED)) {
                ids.add(item.getInventoryitemid());
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<InventoryequipmentDO> rows = inventoryequipmentMapper.selectListByQuery(
                QueryWrapper.create().where(INVENTORYEQUIPMENT_D_O.INVENTORYITEMID.in(ids)));
        Map<Long, InventoryequipmentDO> map = new HashMap<>();
        if (rows != null) {
            rows.forEach(row -> map.put(row.getInventoryitemid(), row));
        }
        return map;
    }

    private H5CharacterInfoRtnDTO.H5EquipRtnDTO toEquip(InventoryitemsDO item, InventoryequipmentDO stats) {
        H5CharacterInfoRtnDTO.H5EquipRtnDTO dto = H5CharacterInfoRtnDTO.H5EquipRtnDTO.builder()
                .itemId(item.getItemid())
                .itemName(catalogService.itemName(item.getItemid()))
                .position(orZero(item.getPosition()))
                .equipped(orZero(item.getPosition()) < 0)
                .expiration(item.getExpiration())
                .build();
        if (stats == null) {
            return dto;
        }
        dto.setUpgradeSlots(orZero(stats.getUpgradeslots()));
        dto.setLevel(orZero(stats.getLevel()));
        dto.setReqLevel(equipStat(item.getItemid(), "reqLevel"));
        dto.setReqJob(equipStat(item.getItemid(), "reqJob"));
        dto.setStr(orZero(stats.getStr()));
        dto.setDex(orZero(stats.getDex()));
        dto.setIntValue(orZero(stats.getInte()));
        dto.setLuk(orZero(stats.getLuk()));
        dto.setHp(orZero(stats.getHp()));
        dto.setMp(orZero(stats.getMp()));
        dto.setWatk(orZero(stats.getWatk()));
        dto.setMatk(orZero(stats.getMatk()));
        dto.setWdef(orZero(stats.getWdef()));
        dto.setMdef(orZero(stats.getMdef()));
        dto.setAcc(orZero(stats.getAcc()));
        dto.setAvoid(orZero(stats.getAvoid()));
        dto.setHands(orZero(stats.getHands()));
        dto.setSpeed(orZero(stats.getSpeed()));
        dto.setJump(orZero(stats.getJump()));
        dto.setVicious(orZero(stats.getVicious()));
        dto.setItemLevel(orZero(stats.getItemlevel()));
        dto.setItemExp(orZero(stats.getItemexp()));
        return dto;
    }

    private int equipStat(int itemId, String key) {
        Map<String, Integer> stats = ItemInformationProvider.getInstance().getEquipStats(itemId);
        return stats == null ? 0 : Optional.ofNullable(stats.get(key)).orElse(0);
    }

    private List<H5CharacterInfoRtnDTO.H5SkillRtnDTO> skills(int characterId) {
        List<SkillsDO> rows = skillsMapper.selectListByQuery(
                QueryWrapper.create().eq("characterid", characterId));
        List<H5CharacterInfoRtnDTO.H5SkillRtnDTO> skills = new ArrayList<>();
        if (rows == null) {
            return skills;
        }
        Map<Integer, String> names = skillNames();
        for (SkillsDO row : rows) {
            if (row.getSkillid() == null) {
                continue;
            }
            skills.add(H5CharacterInfoRtnDTO.H5SkillRtnDTO.builder()
                    .skillId(row.getSkillid())
                    .name(Optional.ofNullable(names.get(row.getSkillid()))
                            .orElse(String.valueOf(row.getSkillid())))
                    .level(orZero(row.getSkilllevel()))
                    .masterLevel(orZero(row.getMasterlevel()))
                    .build());
        }
        skills.sort(Comparator.comparing(H5CharacterInfoRtnDTO.H5SkillRtnDTO::getSkillId));
        return skills;
    }

    /**
     * 技能名来自客户端 String.wz/Skill.img，服务端首次查询时缓存。
     */
    private Map<Integer, String> skillNames() {
        Map<Integer, String> local = skillNameCache;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (skillNameCache != null) {
                return skillNameCache;
            }
            Map<Integer, String> names = new HashMap<>();
            try {
                Data skillData = DataProviderFactory.getDataProvider(WZFiles.STRING).getData("Skill.img");
                if (skillData != null) {
                    for (Data child : skillData.getChildren()) {
                        String id = child.getName();
                        if (id == null || !id.matches("\\d+")) {
                            continue;
                        }
                        String name = DataTool.getString("name", child, null);
                        if (name != null && !name.isEmpty()) {
                            names.put(Integer.parseInt(id), name);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("H5 skill name load failed: {}", e.getMessage());
            }
            skillNameCache = names;
            return names;
        }
    }

    private int remainingSp(String sp) {
        if (sp == null || sp.isBlank()) {
            return 0;
        }
        String first = sp.split(",")[0].trim();
        try {
            return Integer.parseInt(first);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private H5CharacterInfoRtnDTO.H5StatRtnDTO stat(String key, String label, int base, int bonus) {
        return H5CharacterInfoRtnDTO.H5StatRtnDTO.builder()
                .key(key)
                .label(label)
                .base(base)
                .bonus(bonus)
                .total(base + bonus)
                .build();
    }

    private void sortByPosition(List<H5CharacterInfoRtnDTO.H5ItemEntryRtnDTO> items) {
        items.sort(Comparator.comparing(i -> orZero(i.getPosition())));
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }
}
