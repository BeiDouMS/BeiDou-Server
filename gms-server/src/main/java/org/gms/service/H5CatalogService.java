package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.client.Skill;
import org.gms.client.SkillFactory;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.WeaponType;
import org.gms.config.GameConfig;
import org.gms.dao.entity.CharactersDO;
import org.gms.dao.entity.DropDataDO;
import org.gms.dao.entity.H5HangupMapDO;
import org.gms.dao.entity.H5HangupMapMobDO;
import org.gms.dao.entity.H5HangupMobDO;
import org.gms.dao.entity.H5HangupStatDO;
import org.gms.dao.entity.H5HangupWhitelistDO;
import org.gms.dao.entity.InventoryitemsDO;
import org.gms.dao.entity.SkillsDO;
import org.gms.dao.mapper.DropDataMapper;
import org.gms.dao.mapper.H5HangupMapMapper;
import org.gms.dao.mapper.H5HangupMapMobMapper;
import org.gms.dao.mapper.H5HangupMobMapper;
import org.gms.dao.mapper.H5HangupStatMapper;
import org.gms.dao.mapper.H5HangupWhitelistMapper;
import org.gms.dao.mapper.InventoryitemsMapper;
import org.gms.dao.mapper.SkillsMapper;
import org.gms.exception.BizException;
import org.gms.model.dto.H5CharacterRtnDTO;
import org.gms.model.dto.H5DropRtnDTO;
import org.gms.model.dto.H5MapDetailRtnDTO;
import org.gms.model.dto.H5MapRtnDTO;
import org.gms.model.dto.H5MapRtnItemDTO;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.server.ItemInformationProvider;
import org.gms.server.StatEffect;
import org.gms.server.life.LifeFactory;
import org.gms.server.life.Monster;
import org.gms.util.I18nUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.gms.dao.entity.table.DropDataDOTableDef.DROP_DATA_D_O;

/**
 * H5 挂机地图目录与收益估算。
 * <p>
 * 地图目录由客户端 WZ 解析后写入 {@code h5_hangup_map}/{@code h5_hangup_map_mob}/{@code h5_hangup_mob}，
 * 掉落直接读取服务端权威的 {@code drop_data}，收益按战斗力模型估算，全部由服务端计算。
 *
 * @since 2026-11-01
 */
@Service
@Slf4j
public class H5CatalogService {
    public static final double DEFAULT_ATTACK_SPEED = 1.6D;
    /** 挂机击杀上限：每秒最多 2 只，即每小时 7200 只 */
    private static final double MAX_KILLS_PER_HOUR = 2D * 3600D;
    /** 客户端实际伤害相对服务端权威公式的倍率（私服客户端伤害放大，按 luckyS 实测 80/段 ÷ 公式 40/段 校准） */
    public static final double DEFAULT_DAMAGE_MULTIPLIER = 2.0D;
    /** 魔法伤害相对 v83 魔法公式的倍率（按 luckyS 魔法力101/魔法双击Lv20(mad=40) 实测 146/段 ÷ 公式 163/段 校准） */
    public static final double DEFAULT_MAGIC_DAMAGE_MULTIPLIER = 0.9D;
    private static final double DEFAULT_WEAPON_MULTIPLIER = 4.0D;
    private static final double MIN_ATTACK_VALUE = 10D;
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;

    private final H5HangupMapMapper mapMapper;
    private final H5HangupMapMobMapper mapMobMapper;
    private final H5HangupMobMapper mobMapper;
    private final H5HangupWhitelistMapper whitelistMapper;
    private final H5HangupStatMapper statMapper;
    private final DropDataMapper dropDataMapper;
    private final InventoryitemsMapper inventoryitemsMapper;
    private final SkillsMapper skillsMapper;

    private final Object cacheLock = new Object();
    private final Map<Integer, Integer> mobAttackCache = new ConcurrentHashMap<>();
    private volatile Catalog catalog;
    private volatile long catalogLoadedAt;

    public H5CatalogService(H5HangupMapMapper mapMapper,
                            H5HangupMapMobMapper mapMobMapper,
                            H5HangupMobMapper mobMapper,
                            H5HangupWhitelistMapper whitelistMapper,
                            H5HangupStatMapper statMapper,
                            DropDataMapper dropDataMapper,
                            InventoryitemsMapper inventoryitemsMapper,
                            SkillsMapper skillsMapper) {
        this.mapMapper = mapMapper;
        this.mapMobMapper = mapMobMapper;
        this.mobMapper = mobMapper;
        this.whitelistMapper = whitelistMapper;
        this.statMapper = statMapper;
        this.dropDataMapper = dropDataMapper;
        this.inventoryitemsMapper = inventoryitemsMapper;
        this.skillsMapper = skillsMapper;
    }

    /** 地图目录内存缓存 */
    public static class Catalog {
        public final Map<Integer, H5HangupMapDO> maps = new HashMap<>();
        public final Map<Integer, H5HangupMobDO> mobs = new HashMap<>();
        public final Map<Integer, List<H5HangupMapMobDO>> mapMobs = new HashMap<>();
        public final Map<String, String> regionNames = new LinkedHashMap<>();
        public final Map<String, Integer> regionOrders = new HashMap<>();
    }

    /** 角色战斗力 */
    public static class Power {
        public int level;
        public int job;
        public int str;
        public int dex;
        public int intValue;
        public int luk;
        public int watk;
        public int matk;
        public int wdef;
        public int mdef;
        public int maxHp;
        public int maxMp;
        public boolean mage;
        public double dps;
    }

    /** 角色用于估算的最强攻击技能（魔法取 mad，物理取 damage%） */
    private static class AttackSkill {
        final int score;
        final double multiplier;
        final int attackCount;

        AttackSkill(int score, double multiplier, int attackCount) {
            this.score = score;
            this.multiplier = multiplier;
            this.attackCount = Math.max(1, attackCount);
        }
    }

    /** 单次收益估算 */
    public static class Economy {
        public double levelGap;
        public double decay;
        public double killsPerHour;
        public long expPerHour;
        public long mesoPerHour;
    }

    public double attackSpeed() {
        Double v = GameConfig.getDouble("h5_hangup_attack_speed");
        return v == null || v <= 0 ? DEFAULT_ATTACK_SPEED : v;
    }

    public double damageMultiplier() {
        Double v = GameConfig.getDouble("h5_hangup_damage_multiplier");
        return v == null || v <= 0 ? DEFAULT_DAMAGE_MULTIPLIER : v;
    }

    public double magicDamageMultiplier() {
        Double v = GameConfig.getDouble("h5_hangup_magic_damage_multiplier");
        return v == null || v <= 0 ? DEFAULT_MAGIC_DAMAGE_MULTIPLIER : v;
    }

    public Catalog getCatalog() {
        long now = System.currentTimeMillis();
        Catalog local = catalog;
        if (local != null && now - catalogLoadedAt < CACHE_TTL_MS) {
            return local;
        }
        synchronized (cacheLock) {
            if (catalog != null && now - catalogLoadedAt < CACHE_TTL_MS) {
                return catalog;
            }
            Catalog built = new Catalog();
            mapMapper.selectAll().forEach(m -> built.maps.put(m.getMapId(), m));
            mobMapper.selectAll().forEach(m -> built.mobs.put(m.getMobId(), m));
            mapMobMapper.selectAll().forEach(m ->
                    built.mapMobs.computeIfAbsent(m.getMapId(), k -> new ArrayList<>()).add(m));
            built.maps.values().stream().sorted(Comparator.comparing(H5HangupMapDO::getRegionOrder))
                    .forEach(m -> built.regionNames.putIfAbsent(m.getRegion(), m.getRegionName()));
            built.maps.values().forEach(m -> built.regionOrders.putIfAbsent(m.getRegion(), m.getRegionOrder()));
            catalog = built;
            catalogLoadedAt = now;
            log.info("H5 map catalog loaded: {} maps, {} mobs", built.maps.size(), built.mobs.size());
            return built;
        }
    }

    public void reload() {
        synchronized (cacheLock) {
            catalog = null;
        }
        getCatalog();
    }

    public H5HangupMapDO getMap(int mapId) {
        H5HangupMapDO map = getCatalog().maps.get(mapId);
        if (map == null) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_MAP_NOT_FOUND"));
        }
        return map;
    }

    public boolean isMageJob(int job) {
        return (job >= 200 && job < 300) || (job >= 1200 && job < 1300) || (job >= 2200 && job < 2300);
    }

    public Power resolvePower(CharactersDO chr) {
        Character online = findOnlineCharacter(chr.getWorld(), chr.getId());
        if (online != null) {
            return finish(fromCharacter(online), chr.getId());
        }
        H5HangupStatDO stat = statMapper.selectOneById(chr.getId());
        if (stat != null) {
            Power power = new Power();
            power.level = orZero(stat.getLevel());
            power.job = orZero(stat.getJob());
            power.str = orZero(stat.getStr());
            power.dex = orZero(stat.getDex());
            power.intValue = orZero(stat.getIntValue());
            power.luk = orZero(stat.getLuk());
            power.watk = orZero(stat.getWatk());
            power.matk = orZero(stat.getMatk());
            power.maxHp = Math.max(1, orZero(chr.getMaxhp()));
            power.maxMp = Math.max(0, orZero(chr.getMaxmp()));
            fillEquipStats(power, chr.getId(), false);
            return finish(power, chr.getId());
        }
        Power power = new Power();
        power.level = Optional.ofNullable(chr.getLevel()).orElse(1);
        power.job = orZero(chr.getJob());
        power.str = orZero(chr.getAttrStr());
        power.dex = orZero(chr.getAttrDex());
        power.intValue = orZero(chr.getAttrInt());
        power.luk = orZero(chr.getAttrLuk());
        power.maxHp = Math.max(1, orZero(chr.getMaxhp()));
        power.maxMp = Math.max(0, orZero(chr.getMaxmp()));
        fillEquipStats(power, chr.getId(), true);
        return finish(power, chr.getId());
    }

    private Power fromCharacter(Character chr) {
        Power power = new Power();
        power.level = chr.getLevel();
        power.job = chr.getJob() == null ? 0 : chr.getJob().getId();
        power.str = chr.getStr();
        power.dex = chr.getDex();
        power.intValue = chr.getInt();
        power.luk = chr.getLuk();
        power.watk = chr.getTotalWatk();
        // totalMagic = baseInt + 2x装备INT + 装备MATK（equipInt 计入 localint_ 与 equipmagic 两处），
        // matk 统一存"装备魔法贡献"，finish 里 magic = intValue + matk 还原 totalMagic
        power.matk = Math.max(0, chr.getTotalMagic() - chr.getInt());
        power.maxHp = Math.max(1, chr.getCurrentMaxHp());
        power.maxMp = Math.max(0, chr.getCurrentMaxMp());
        for (Item item : chr.getInventory(InventoryType.EQUIPPED).list()) {
            if (item instanceof Equip equip) {
                power.wdef += Math.max(0, equip.getWdef());
                power.mdef += Math.max(0, equip.getMdef());
            }
        }
        return power;
    }

    private Power finish(Power power, int characterId) {
        power.level = Math.max(1, power.level);
        power.mage = isMageJob(power.job);
        Map<Integer, Integer> learned = learnedSkills(characterId);
        AttackSkill atk = bestAttackSkill(learned, power.mage);
        double perCast;
        double clientFactor;
        if (power.mage) {
            // v83 魔法伤害公式：MAX = ((魔法力²/1000 + 魔法力)/30 + INT/200) × 技能 mad
            double magic = Math.max(1D, power.intValue + power.matk);
            double maxBase = (magic * magic / 1000.0 + magic) / 30.0 + power.intValue / 200.0;
            perCast = maxBase * (atk == null ? 1.0D : atk.multiplier) * magicAmplification(power, learned);
            clientFactor = magicDamageMultiplier();
        } else {
            // 物理公式同 Character.calculateMaxBaseDamage：ceil((武器倍率*主属性+副属性)/100 * watk)，再乘技能 damage%
            WeaponType weaponType = weaponType(characterId);
            perCast = physicalMaxBase(power, weaponType) * (atk == null ? 1.0D : atk.multiplier / 100.0D);
            clientFactor = damageMultiplier();
        }
        power.dps = perCast * (atk == null ? 1 : atk.attackCount) * clientFactor * attackSpeed();
        return power;
    }

    /** 角色已学技能表：skillId -> skillLevel */
    private Map<Integer, Integer> learnedSkills(int characterId) {
        List<SkillsDO> rows = skillsMapper.selectListByQuery(QueryWrapper.create()
                .eq("characterid", characterId).gt("skilllevel", 0));
        Map<Integer, Integer> learned = new HashMap<>();
        if (rows != null) {
            for (SkillsDO row : rows) {
                if (row == null || row.getSkillid() == null || row.getSkilllevel() == null || row.getSkilllevel() <= 0) {
                    continue;
                }
                learned.put(row.getSkillid(), row.getSkilllevel());
            }
        }
        return learned;
    }

    private AttackSkill bestAttackSkill(Map<Integer, Integer> learned, boolean magic) {
        AttackSkill best = null;
        for (Map.Entry<Integer, Integer> entry : learned.entrySet()) {
            Skill skill = SkillFactory.getSkill(entry.getKey());
            if (skill == null || skill.isBeginnerSkill()) {
                continue;
            }
            int level = Math.min(Math.max(1, entry.getValue()), skill.getMaxLevel());
            if (level <= 0) {
                continue;
            }
            StatEffect eff = skill.getEffect(level);
            if (eff == null || eff.getDuration() > 0) {
                // buff/增益技能（如冥想法 mad>0）不参与伤害估算
                continue;
            }
            if (magic) {
                int mad = eff.getMatk();
                if (mad <= 0) {
                    continue;
                }
                int score = mad * Math.max(1, eff.getAttackCount());
                if (best == null || score > best.score) {
                    best = new AttackSkill(score, mad, eff.getAttackCount());
                }
            } else {
                int damage = eff.getDamage();
                if (damage <= 100 && eff.getAttackCount() <= 1) {
                    // damage=100 视为无倍率（普通攻击）
                    continue;
                }
                int score = damage * Math.max(1, eff.getAttackCount());
                if (best == null || score > best.score) {
                    best = new AttackSkill(score, damage, eff.getAttackCount());
                }
            }
        }
        return best;
    }

    /** 法师元素放大（服务端同款：effect.getY()/100），无放大技能时为 1 */
    private double magicAmplification(Power power, Map<Integer, Integer> learned) {
        int job = power.job;
        int ampSkillId;
        if (job == 211 || job == 212) {
            ampSkillId = 2110001;   // FPMage.ELEMENT_AMPLIFICATION
        } else if (job == 221 || job == 222) {
            ampSkillId = 2210001;   // ILMage.ELEMENT_AMPLIFICATION
        } else if (job == 1211 || job == 1212) {
            ampSkillId = 12110001;  // BlazeWizard.ELEMENT_AMPLIFICATION
        } else if (job >= 2210 && job <= 2218) {
            ampSkillId = 22150000;  // Evan.MAGIC_AMPLIFICATION
        } else {
            return 1.0D;
        }
        Integer lvl = learned.get(ampSkillId);
        if (lvl == null || lvl <= 0) {
            return 1.0D;
        }
        Skill amp = SkillFactory.getSkill(ampSkillId);
        if (amp == null) {
            return 1.0D;
        }
        StatEffect eff = amp.getEffect(Math.min(Math.max(1, lvl), amp.getMaxLevel()));
        return eff == null || eff.getY() <= 0 ? 1.0D : eff.getY() / 100.0D;
    }

    /** 已装备武器（position=-11）的武器类型，未装备武器时返回 null */
    private WeaponType weaponType(int characterId) {
        List<InventoryitemsDO> items = inventoryitemsMapper.selectListByQuery(QueryWrapper.create()
                .eq("characterid", characterId).eq("position", -11));
        if (items == null || items.isEmpty() || items.get(0) == null || items.get(0).getItemid() == null) {
            return null;
        }
        return ItemInformationProvider.getInstance().getWeaponType(items.get(0).getItemid());
    }

    /**
     * 物理最大基础伤害：与 Character.calculateMaxBaseDamage 同口径。
     * 弓/弩/枪以 DEX 为主属性，拳套/盗贼短刀以 LUK 为主属性，其余以 STR 为主属性。
     */
    private double physicalMaxBase(Power power, WeaponType type) {
        double watk = Math.max(MIN_ATTACK_VALUE, power.watk);
        boolean thief = (power.job >= 400 && power.job < 500) || (power.job >= 1400 && power.job < 1500);
        if (type == WeaponType.DAGGER_OTHER && thief) {
            type = WeaponType.DAGGER_THIEVES;
        }
        int mainstat;
        int secondarystat;
        if (type == WeaponType.BOW || type == WeaponType.CROSSBOW || type == WeaponType.GUN) {
            mainstat = power.dex;
            secondarystat = power.str;
        } else if (type == WeaponType.CLAW || type == WeaponType.DAGGER_THIEVES) {
            mainstat = power.luk;
            secondarystat = power.dex + power.str;
        } else {
            mainstat = power.str;
            secondarystat = power.dex;
        }
        double multiplier = type == null ? DEFAULT_WEAPON_MULTIPLIER : type.getMaxDamageMultiplier();
        return Math.ceil((multiplier * mainstat + secondarystat) / 100.0 * watk);
    }

    private void fillEquipStats(Power power, int characterId, boolean includeAttack) {
        // 已穿戴装备在 inventoryitems 中的 inventorytype = -1，position < 0
        List<InventoryitemsDO> items = inventoryitemsMapper.selectListByQuery(
                QueryWrapper.create().eq("characterid", characterId));
        if (items == null) {
            return;
        }
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        for (InventoryitemsDO item : items) {
            if (item == null || item.getPosition() == null || item.getPosition() >= 0 || item.getItemid() == null) {
                continue;
            }
            Map<String, Integer> stats = ii.getEquipStats(item.getItemid());
            if (stats == null) {
                continue;
            }
            if (includeAttack) {
                // WZ inc* 节点剥离前缀后的键：incPAD->PAD、incMAD->MAD、incINT->INT
                power.watk += Optional.ofNullable(stats.get("PAD")).orElse(0);
                // totalMagic 中装备 INT 计入两处（localint_ 与 equipmagic），故乘 2
                power.matk += Optional.ofNullable(stats.get("MAD")).orElse(0)
                        + 2 * Optional.ofNullable(stats.get("INT")).orElse(0);
            }
            power.wdef += Math.max(0, Optional.ofNullable(stats.get("WDEF")).orElse(0));
            power.mdef += Math.max(0, Optional.ofNullable(stats.get("MDEF")).orElse(0));
            power.maxHp += Math.max(0, Optional.ofNullable(stats.get("HP")).orElse(0));
            power.maxMp += Math.max(0, Optional.ofNullable(stats.get("MP")).orElse(0));
        }
    }

    public Economy project(H5HangupMapDO map, Power power, double expRate, double mesoRate) {
        Economy economy = new Economy();
        Catalog cat = getCatalog();
        List<H5HangupMapMobDO> spawns = cat.mapMobs.getOrDefault(map.getMapId(), List.of());

        double totalSpawn = 0D;
        double totalHp = 0D;
        int mobTypes = 0;
        Set<Integer> seenMobs = new HashSet<>();
        double weightedExp = 0D;
        double weightedLevel = 0D;
        int maxMobAttack = 0;
        for (H5HangupMapMobDO spawn : spawns) {
            H5HangupMobDO mob = cat.mobs.get(spawn.getMobId());
            if (mob == null) {
                continue;
            }
            double weight = Math.max(1, Optional.ofNullable(spawn.getSpawnCount()).orElse(1));
            totalSpawn += weight;
            // 同一怪物可能按刷新时间拆分多条记录，按怪物种类去重
            if (seenMobs.add(spawn.getMobId())) {
                totalHp += Math.max(1, Optional.ofNullable(mob.getHp()).orElse(1));
                mobTypes++;
                maxMobAttack = Math.max(maxMobAttack, mobAttackPower(spawn.getMobId()));
            }
            weightedExp += Optional.ofNullable(mob.getExp()).orElse(0) * weight;
            weightedLevel += Optional.ofNullable(mob.getLevel()).orElse(1) * weight;
        }
        // 按怪物种类取平均血量，不考虑各怪刷新数量
        double avgHp = mobTypes <= 0 ? 0D : totalHp / mobTypes;
        double avgExp = totalSpawn <= 0 ? 0D : weightedExp / totalSpawn;
        double avgLevel = totalSpawn <= 0 ? 0D : weightedLevel / totalSpawn;

        economy.levelGap = avgLevel - power.level;
        economy.decay = levelDecay(economy.levelGap);
        if (avgHp <= 0 || power.dps <= 0) {
            return economy;
        }

        // 生存惩罚：地图中攻击力最高的怪物单次可打掉角色的血量占比，击杀数按 1-占比 折算
        double defense = Math.max(power.wdef, power.mdef);
        double effectiveHp = Math.max(1D, power.maxHp + (power.mage ? Math.max(0, power.maxMp) : 0D));
        double hitDamage = maxMobAttack <= 0 ? 0D : Math.max(0D, maxMobAttack - defense);
        double penalty = Math.min(1D, hitDamage / effectiveHp);

        // 每秒击杀 = DPS / 平均血量，每小时 ×3600，上限 2 只/秒
        economy.killsPerHour = Math.min(power.dps / avgHp * 3600D, MAX_KILLS_PER_HOUR) * (1D - penalty);
        economy.expPerHour = (long) (economy.killsPerHour * avgExp * expRate * economy.decay);
        economy.mesoPerHour = (long) (economy.killsPerHour * (avgExp * 1.5D + 30D) * mesoRate);
        return economy;
    }

    private int mobAttackPower(int mobId) {
        return mobAttackCache.computeIfAbsent(mobId, id -> {
            try {
                Monster monster = LifeFactory.getMonster(id);
                if (monster == null) {
                    return 0;
                }
                return Math.max(0, Math.max(monster.getPADamage(), monster.getStats().getMADamage()));
            } catch (Exception e) {
                log.warn("H5 hangup mob attack load failed: {}", id, e);
                return 0;
            }
        });
    }

    public double levelDecay(double levelGap) {
        if (levelGap >= -4) {
            return 1.0D;
        }
        return Math.max(0.1D, 1.0D - 0.1D * (Math.abs(levelGap) - 4D));
    }

    public H5MapRtnItemDTO toMapItem(H5HangupMapDO map, Power power, double expRate, double mesoRate, int selectedMapId) {
        Economy economy = project(map, power, expRate, mesoRate);
        return H5MapRtnItemDTO.builder()
                .mapId(map.getMapId())
                .name(map.getName())
                .streetName(map.getStreetName())
                .region(map.getRegion())
                .regionName(map.getRegionName())
                .subRegion(map.getSubRegion())
                .subRegionName(map.getSubRegionName())
                .subRegionOrder(map.getSubRegionOrder())
                .minLevel(map.getMinLevel())
                .maxLevel(map.getMaxLevel())
                .spawnCount(map.getSpawnCount())
                .respawnSeconds(map.getRespawnSeconds())
                .hasBoss(Optional.ofNullable(map.getHasBoss()).orElse(0) > 0)
                .levelGap((int) Math.round(economy.levelGap))
                .decay(economy.decay)
                .killsPerHour(round(economy.killsPerHour))
                .expPerHour(economy.expPerHour)
                .mesoPerHour(economy.mesoPerHour)
                .selected(map.getMapId() == selectedMapId)
                .build();
    }

    public H5MapRtnDTO listMaps(CharactersDO chr, Power power, String regionFilter, int selectedMapId) {
        Catalog cat = getCatalog();
        double expRate = expRate(chr.getWorld());
        double mesoRate = mesoRate(chr.getWorld());
        List<H5HangupMapDO> maps = cat.maps.values().stream()
                .filter(m -> regionFilter == null || regionFilter.isEmpty() || regionFilter.equals(m.getRegion()))
                .sorted(Comparator.comparing((H5HangupMapDO m) -> Optional.ofNullable(m.getSubRegionOrder()).orElse(0))
                        .thenComparing(H5HangupMapDO::getMinLevel))
                .collect(Collectors.toList());

        // 两级分组：地区 -> 二级分区（归属城镇）-> 地图
        Map<String, Map<String, List<H5MapRtnItemDTO>>> grouped = new LinkedHashMap<>();
        for (H5HangupMapDO map : maps) {
            String sub = Optional.ofNullable(map.getSubRegion()).orElse("");
            grouped.computeIfAbsent(map.getRegion(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(sub, k -> new ArrayList<>())
                    .add(toMapItem(map, power, expRate, mesoRate, selectedMapId));
        }
        List<H5MapRtnDTO.H5MapRegionRtnDTO> regions = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<H5MapRtnItemDTO>>> entry : grouped.entrySet()) {
            List<H5MapRtnItemDTO> items = new ArrayList<>();
            List<H5MapRtnDTO.H5MapSubRegionRtnDTO> subRegions = new ArrayList<>();
            for (Map.Entry<String, List<H5MapRtnItemDTO>> sub : entry.getValue().entrySet()) {
                List<H5MapRtnItemDTO> subMaps = sub.getValue();
                H5MapRtnItemDTO first = subMaps.get(0);
                items.addAll(subMaps);
                subRegions.add(H5MapRtnDTO.H5MapSubRegionRtnDTO.builder()
                        .subRegion(sub.getKey())
                        .subRegionName(Optional.ofNullable(first.getSubRegionName()).orElse(first.getRegionName()))
                        .order(Optional.ofNullable(first.getSubRegionOrder()).orElse(0))
                        .mapCount(subMaps.size())
                        .maps(subMaps)
                        .build());
            }
            regions.add(H5MapRtnDTO.H5MapRegionRtnDTO.builder()
                    .region(entry.getKey())
                    .regionName(items.get(0).getRegionName())
                    .order(cat.regionOrders.getOrDefault(entry.getKey(), 0))
                    .mapCount(items.size())
                    .maps(items)
                    .subRegions(subRegions)
                    .build());
        }
        return H5MapRtnDTO.builder()
                .regions(regions)
                .character(toCharacter(chr, power, selectedMapId))
                .build();
    }

    public H5CharacterRtnDTO toCharacter(CharactersDO chr, Power power, int selectedMapId) {
        Catalog cat = getCatalog();
        H5HangupMapDO map = cat.maps.get(selectedMapId);
        return H5CharacterRtnDTO.builder()
                .characterId(chr.getId())
                .characterName(chr.getName())
                .level(power.level)
                .job(power.job)
                .attackType(power.mage ? "magic" : "physical")
                .str(power.str)
                .dex(power.dex)
                .intValue(power.intValue)
                .luk(power.luk)
                .watk(power.watk)
                // 前端展示用总魔法力（基础 INT + 装备贡献）
                .matk(power.mage ? power.intValue + power.matk : power.matk)
                .dps(power.dps)
                .mapId(selectedMapId)
                .mapName(map == null ? null : map.getName())
                .build();
    }

    public H5MapDetailRtnDTO detail(CharactersDO chr, Power power, int mapId, int selectedMapId, List<Integer> whitelist) {
        H5HangupMapDO map = getMap(mapId);
        double expRate = expRate(chr.getWorld());
        double mesoRate = mesoRate(chr.getWorld());
        Economy economy = project(map, power, expRate, mesoRate);

        List<H5MapDetailRtnDTO.MobInfo> mobs = new ArrayList<>();
        Catalog cat = getCatalog();
        for (H5HangupMapMobDO spawn : cat.mapMobs.getOrDefault(mapId, List.of())) {
            H5HangupMobDO mob = cat.mobs.get(spawn.getMobId());
            if (mob == null) {
                continue;
            }
            mobs.add(H5MapDetailRtnDTO.MobInfo.builder()
                    .mobId(mob.getMobId())
                    .name(mob.getName())
                    .level(mob.getLevel())
                    .hp(mob.getHp())
                    .exp(mob.getExp())
                    .spawnCount(spawn.getSpawnCount())
                    .boss(Optional.ofNullable(mob.getBoss()).orElse(0) > 0)
                    .build());
        }
        return H5MapDetailRtnDTO.builder()
                .map(toMapItem(map, power, expRate, mesoRate, selectedMapId))
                .character(toCharacter(chr, power, selectedMapId))
                .mobs(mobs)
                .drops(drops(mapId, economy.killsPerHour, whitelist))
                .build();
    }

    /**
     * 按地图刷怪权重汇总掉落，并按当前击杀速度估算每小时产出。
     */
    public List<H5DropRtnDTO> drops(int mapId, double killsPerHour, List<Integer> whitelist) {
        Catalog cat = getCatalog();
        List<H5HangupMapMobDO> spawns = cat.mapMobs.getOrDefault(mapId, List.of());
        if (spawns.isEmpty()) {
            return List.of();
        }
        double totalSpawn = spawns.stream()
                .mapToDouble(s -> Math.max(1, Optional.ofNullable(s.getSpawnCount()).orElse(1))).sum();
        Map<Integer, Double> weights = new HashMap<>();
        for (H5HangupMapMobDO spawn : spawns) {
            weights.merge(spawn.getMobId(),
                    Math.max(1, Optional.ofNullable(spawn.getSpawnCount()).orElse(1)) / totalSpawn, Double::sum);
        }
        Collection<Integer> mobIds = weights.keySet();
        List<DropDataDO> rows = dropDataMapper.selectListByQuery(
                QueryWrapper.create().where(DROP_DATA_D_O.DROPPERID.in(mobIds)));
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        Set<Integer> picked = whitelist == null ? Set.of() : Set.copyOf(whitelist);
        Map<Integer, H5DropRtnDTO> aggregated = new LinkedHashMap<>();
        for (DropDataDO row : rows) {
            int itemId = Optional.ofNullable(row.getItemid()).orElse(0);
            if (itemId <= 0) {
                // itemid = 0 表示金币掉落，金币走独立收益公式
                continue;
            }
            double weight = weights.getOrDefault(row.getDropperid(), 0D);
            if (weight <= 0) {
                continue;
            }
            double chancePpm = Optional.ofNullable(row.getChance()).orElse(0).doubleValue() * weight;
            int minQty = Math.max(1, Optional.ofNullable(row.getMinimumQuantity()).orElse(1));
            int maxQty = Math.max(minQty, Optional.ofNullable(row.getMaximumQuantity()).orElse(minQty));
            double expected = killsPerHour * chancePpm / 1_000_000D * ((minQty + maxQty) / 2D);

            H5DropRtnDTO dto = aggregated.get(itemId);
            if (dto == null) {
                dto = H5DropRtnDTO.builder()
                        .itemId(itemId)
                        .itemName(itemName(itemId))
                        .chancePercent(chancePpm / 10_000D)
                        .minQuantity(minQty)
                        .maxQuantity(maxQty)
                        .expectedPerHour(expected)
                        .build();
                aggregated.put(itemId, dto);
            } else {
                dto.setChancePercent(dto.getChancePercent() + chancePpm / 10_000D);
                dto.setExpectedPerHour(dto.getExpectedPerHour() + expected);
            }
            dto.setWhitelisted(picked.contains(itemId));
        }
        return aggregated.values().stream()
                .sorted(Comparator.comparing(H5DropRtnDTO::getExpectedPerHour).reversed())
                .peek(d -> {
                    d.setChancePercent(round(d.getChancePercent()));
                    d.setExpectedPerHour(round(d.getExpectedPerHour()));
                })
                .collect(Collectors.toList());
    }

    public List<H5DropRtnDTO> mapDropsForCharacter(CharactersDO chr, Power power, int mapId, int characterId) {
        H5HangupMapDO map = getMap(mapId);
        Economy economy = project(map, power, expRate(chr.getWorld()), mesoRate(chr.getWorld()));
        return drops(mapId, economy.killsPerHour, getWhitelist(characterId));
    }

    /** 角色已启用的全部拾取物品，用于白名单管理页展示 */
    public List<H5DropRtnDTO> whitelistItems(int characterId) {
        return getWhitelist(characterId).stream()
                .sorted()
                .map(itemId -> H5DropRtnDTO.builder()
                        .itemId(itemId)
                        .itemName(itemName(itemId))
                        .whitelisted(true)
                        .build())
                .collect(Collectors.toList());
    }

    public List<Integer> getWhitelist(int characterId) {
        return whitelistMapper.selectListByQuery(QueryWrapper.create().eq("character_id", characterId)).stream()
                .map(H5HangupWhitelistDO::getItemId)
                .collect(Collectors.toList());
    }

    /**
     * 覆写白名单。勾选数量不设上限，传空列表即清空。
     */
    public void saveWhitelist(int characterId, List<Integer> itemIds) {
        whitelistMapper.deleteByQuery(QueryWrapper.create().eq("character_id", characterId));
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        for (Integer itemId : itemIds) {
            if (itemId == null || itemId <= 0) {
                continue;
            }
            whitelistMapper.insert(H5HangupWhitelistDO.builder()
                    .characterId(characterId)
                    .itemId(itemId)
                    .updateAt(now)
                    .build());
        }
    }

    public String itemName(int itemId) {
        String name = ItemInformationProvider.getInstance().getName(itemId);
        return name == null || name.isEmpty() ? String.valueOf(itemId) : name;
    }

    public double expRate(int world) {
        World w = Server.getInstance().getWorld(world);
        if (w == null) {
            return 1.0D;
        }
        float rate = w.getExpRate();
        return rate <= 0 ? 1.0D : rate;
    }

    public double mesoRate(int world) {
        World w = Server.getInstance().getWorld(world);
        if (w == null) {
            return 1.0D;
        }
        float rate = w.getMesoRate();
        return rate <= 0 ? 1.0D : rate;
    }

    /** 登出时写入战斗力快照 */
    public void snapshot(Character chr) {
        if (chr == null || chr.getId() <= 0) {
            return;
        }
        Power power = fromCharacter(chr);
        H5HangupStatDO stat = H5HangupStatDO.builder()
                .characterId(chr.getId())
                .level(power.level)
                .job(power.job)
                .str(power.str)
                .dex(power.dex)
                .intValue(power.intValue)
                .luk(power.luk)
                .watk(power.watk)
                .matk(power.matk)
                .updateAt(new java.sql.Timestamp(System.currentTimeMillis()))
                .build();
        if (statMapper.selectOneById(chr.getId()) == null) {
            statMapper.insert(stat);
        } else {
            statMapper.update(stat);
        }
    }

    private Character findOnlineCharacter(int world, int characterId) {
        World w = Server.getInstance().getWorld(world);
        if (w == null) {
            return null;
        }
        return w.getPlayerStorage().getCharacterById(characterId);
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static double round(double value) {
        return Math.round(value * 100D) / 100D;
    }
}
