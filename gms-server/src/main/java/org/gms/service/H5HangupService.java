package org.gms.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.gms.client.Character;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.constants.inventory.ItemConstants;
import org.gms.server.ItemInformationProvider;
import org.gms.util.DatabaseConnection;
import org.gms.config.GameConfig;
import org.gms.dao.entity.CharactersDO;
import org.gms.dao.entity.H5HangupMapDO;
import org.gms.dao.entity.H5HangupRewardDO;
import org.gms.dao.entity.H5HangupStateDO;
import org.gms.dao.mapper.CharactersMapper;
import org.gms.dao.mapper.H5HangupRewardMapper;
import org.gms.dao.mapper.H5HangupStateMapper;
import org.gms.exception.BizException;
import org.gms.model.dto.H5DropRtnDTO;
import org.gms.model.dto.H5CharacterRtnDTO;
import org.gms.model.dto.H5HangupClaimRtnDTO;
import org.gms.model.dto.H5HangupRtnDTO;
import org.gms.model.dto.H5ItemRtnDTO;
import org.gms.model.dto.H5MapDetailRtnDTO;
import org.gms.model.dto.H5MapRtnDTO;
import org.gms.model.dto.H5WhitelistRtnDTO;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.util.I18nUtil;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * H5 挂机收益结算。
 * <p>
 * 所有收益以服务端数据库为唯一数据源：离线时长取自 {@code characters.lastLogoutTime}，
 * 战斗力取自在线角色或 {@code h5_hangup_stat} 快照，地图与怪物数据取自 {@code h5_hangup_map} 目录，
 * 掉落取自服务端权威的 {@code drop_data}，结果落 {@code h5_hangup_reward} 待发放，
 * 由端游登录流程或在线领取时通过原有游戏逻辑发放。
 *
 * @since 2026-11-01
 */
@Service
@Slf4j
public class H5HangupService {
    private static final long SECONDS_PER_HOUR = 3600L;
    /** 每日时长上限（小时），<=0 表示不限（封闭测试阶段默认不限） */
    private static final int DEFAULT_DAILY_CAP_HOURS = 0;
    /** 不限每日时长时返回的剩余秒数标记 */
    private static final long UNLIMITED_DAILY_REMAIN = -1L;
    private static final int DEFAULT_MAX_GAP_HOURS = 12;
    private static final int MAX_ITEM_QUANTITY_PER_CLAIM = Short.MAX_VALUE;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 按角色 ID 分段加锁，避免同一角色并发结算重复发放 */
    private static final Object[] LOCKS = new Object[64];

    static {
        for (int i = 0; i < LOCKS.length; i++) {
            LOCKS[i] = new Object();
        }
    }

    private final CharactersMapper charactersMapper;
    private final H5HangupStateMapper stateMapper;
    private final H5HangupRewardMapper rewardMapper;
    private final H5CatalogService catalogService;

    public H5HangupService(CharactersMapper charactersMapper, H5HangupStateMapper stateMapper,
                           H5HangupRewardMapper rewardMapper, H5CatalogService catalogService) {
        this.charactersMapper = charactersMapper;
        this.stateMapper = stateMapper;
        this.rewardMapper = rewardMapper;
        this.catalogService = catalogService;
    }

    /**
     * 结算入口：accountId 与 characterId 二选一，mapId 可选。
     */
    public H5HangupRtnDTO settle(Integer accountId, Integer characterId, Integer mapId) {
        List<CharactersDO> targets = new ArrayList<>();
        if (characterId != null && characterId > 0) {
            CharactersDO chr = charactersMapper.selectOneById(characterId);
            if (chr == null) {
                throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_CHARACTER_NOT_FOUND"));
            }
            if (accountId != null && accountId > 0 && !accountId.equals(chr.getAccountid())) {
                throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_CHARACTER_NOT_IN_ACCOUNT"));
            }
            targets.add(chr);
        } else if (accountId != null && accountId > 0) {
            targets.addAll(charactersMapper.selectListByQuery(QueryWrapper.create().eq("accountid", accountId)));
            if (targets.isEmpty()) {
                throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_CHARACTER_NOT_FOUND"));
            }
        } else {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_PARAM_REQUIRED"));
        }

        List<H5HangupRtnDTO.H5HangupCharRtnDTO> result = new ArrayList<>();
        long totalMeso = 0L;
        long totalExp = 0L;
        for (CharactersDO chr : targets) {
            H5HangupRtnDTO.H5HangupCharRtnDTO rtn = settleCharacter(chr, true);
            if (rtn == null) {
                continue;
            }
            result.add(rtn);
            totalMeso += Optional.ofNullable(rtn.getPendingMeso()).orElse(0L);
            totalExp += Optional.ofNullable(rtn.getPendingExp()).orElse(0L);
        }

        return H5HangupRtnDTO.builder()
                .characters(result)
                .pendingMeso(totalMeso)
                .pendingExp(totalExp)
                .build();
    }

    /**
     * 累计一次挂机收益。stop=true 表示结算并停止本次挂机，false 表示仅刷新（挂机继续）。
     */
    protected H5HangupRtnDTO.H5HangupCharRtnDTO settleCharacter(CharactersDO chr, boolean stop) {
        synchronized (LOCKS[Math.floorMod(chr.getId(), LOCKS.length)]) {
            int charId = chr.getId();
            long now = System.currentTimeMillis();

            Character onlineChr = findOnlineCharacter(chr.getWorld(), charId);

            H5HangupStateDO state = stateMapper.selectOneById(charId);
            LocalDate today = LocalDate.now();
            Timestamp lastSettleTime;
            int dailySeconds;
            long totalSeconds;
            int mapId;
            boolean active;
            Long sessionStartTime;
            double sessionKills;

            if (state == null) {
                lastSettleTime = Optional.ofNullable(chr.getLastLogoutTime()).orElse(new Timestamp(now));
                dailySeconds = 0;
                totalSeconds = 0L;
                mapId = 0;
                active = false;
                sessionStartTime = null;
                sessionKills = 0D;
            } else {
                lastSettleTime = Optional.ofNullable(state.getLastSettleTime()).orElse(new Timestamp(now));
                dailySeconds = Optional.ofNullable(state.getDailySeconds()).orElse(0);
                totalSeconds = Optional.ofNullable(state.getTotalSeconds()).orElse(0L);
                if (state.getDailyDate() == null || !today.equals(state.getDailyDate().toLocalDate())) {
                    dailySeconds = 0;
                }
                mapId = Optional.ofNullable(state.getMapId()).orElse(0);
                active = Boolean.TRUE.equals(state.getActive());
                sessionStartTime = state.getSessionStartTime();
                sessionKills = Optional.ofNullable(state.getSessionKills()).orElse(0D);
            }

            H5HangupMapDO map = mapId > 0 ? catalogService.getMap(mapId) : null;
            if (map == null && active) {
                throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_MAP_SELECT_REQUIRED"));
            }

            // 只有挂机中的会话才累计；开始挂机后按 lastSettleTime 到现在的时长计
            long elapsedSeconds = 0L;
            if (active && map != null && onlineChr == null) {
                long from = lastSettleTime.getTime();
                Timestamp logoutTime = chr.getLastLogoutTime();
                if (logoutTime != null && logoutTime.getTime() > from) {
                    from = logoutTime.getTime();
                }
                if (sessionStartTime != null && sessionStartTime > from) {
                    from = sessionStartTime;
                }
                if (from > now) {
                    // 时间回拨：不加收益，直接把基准时间拉回服务器时间
                    from = now;
                }
                elapsedSeconds = (now - from) / 1000L;
            }
            int dailyCapSeconds = dailyCapSeconds();
            int maxGapSeconds = hoursToSeconds(serverInt("h5_hangup_max_gap_hours", DEFAULT_MAX_GAP_HOURS));

            long rewardSeconds = Math.min(elapsedSeconds, maxGapSeconds);
            rewardSeconds = Math.min(rewardSeconds, Math.max(0L, dailyCapSeconds - dailySeconds));
            rewardSeconds = Math.max(0L, rewardSeconds);

            H5CatalogService.Power power = catalogService.resolvePower(chr);
            H5CatalogService.Economy economy = map == null ? null : catalogService.project(
                    map, power, catalogService.expRate(chr.getWorld()), catalogService.mesoRate(chr.getWorld()));

            long gainedMeso = 0L;
            long gainedExp = 0L;
            double gainedKills = 0D;
            List<H5ItemRtnDTO> gainedItems = List.of();
            if (rewardSeconds > 0 && economy != null) {
                double hours = (double) rewardSeconds / SECONDS_PER_HOUR;
                gainedMeso = (long) (economy.mesoPerHour * hours);
                gainedExp = (long) (economy.expPerHour * hours);
                gainedKills = economy.killsPerHour * hours;
                gainedItems = rollItems(charId, mapId, economy.killsPerHour, hours);

                Map<Integer, Long> itemStacks = new LinkedHashMap<>();
                for (H5ItemRtnDTO item : gainedItems) {
                    itemStacks.merge(item.getItemId(), item.getQuantity(), Long::sum);
                }
                accumulateReward(chr, gainedMeso, gainedExp, itemStacks);
                dailySeconds += (int) rewardSeconds;
                totalSeconds += rewardSeconds;
                sessionKills += gainedKills;
            }

            if (stop) {
                active = false;
            }
            saveState(chr, now, dailySeconds, totalSeconds, today, mapId, active, sessionStartTime, sessionKills);

            // 在线角色结算后立即通过游戏逻辑发放，避免收益滞留在待发放表
            if (onlineChr != null) {
                H5HangupClaimRtnDTO claimed = claim(onlineChr, charId);
                gainedMeso += Optional.ofNullable(claimed.getGivenMeso()).orElse(0L);
                gainedExp += Optional.ofNullable(claimed.getGivenExp()).orElse(0L);
            }

            H5HangupRewardDO reward = rewardMapper.selectOneById(charId);
            long pendingMeso = reward == null ? 0L : Optional.ofNullable(reward.getMeso()).orElse(0L);
            long pendingExp = reward == null ? 0L : Optional.ofNullable(reward.getExp()).orElse(0L);
            List<H5ItemRtnDTO> pendingItems = reward == null ? List.of() : parseItems(reward.getItemsJson());

            if (gainedMeso > 0 || gainedExp > 0) {
                log.info(I18nUtil.getLogMessage("H5.Hangup.Settle.info1", charId, chr.getName(), rewardSeconds, gainedMeso, gainedExp));
            }

            return H5HangupRtnDTO.H5HangupCharRtnDTO.builder()
                    .characterId(charId)
                    .characterName(chr.getName())
                    .level(chr.getLevel())
                    .world(chr.getWorld())
                    .online(onlineChr != null)
                    .settledSeconds(rewardSeconds)
                    .dailyUsedSeconds((long) dailySeconds)
                    .dailyRemainSeconds(dailyRemainSeconds(dailyCapSeconds, dailySeconds))
                    .gainedMeso(gainedMeso)
                    .gainedExp(gainedExp)
                    .pendingMeso(pendingMeso)
                    .pendingExp(pendingExp)
                    .mapId(mapId)
                    .mapName(map == null ? null : map.getName())
                    .gainedItems(gainedItems)
                    .pendingItems(pendingItems)
                    .gainedKills(gainedKills)
                    .sessionKills(sessionKills)
                    .active(active)
                    .sessionStartTime(sessionStartTime)
                    .build();
        }
    }

    /**
     * 按地图真实掉率和白名单估算本次掉落，白名单未勾选的物品不会产出。
     */
    private List<H5ItemRtnDTO> rollItems(int characterId, int mapId, double killsPerHour, double hours) {
        List<Integer> whitelist = catalogService.getWhitelist(characterId);
        if (whitelist.isEmpty() || killsPerHour <= 0 || hours <= 0) {
            return List.of();
        }
        List<H5DropRtnDTO> drops = catalogService.drops(mapId, killsPerHour, whitelist);
        List<H5ItemRtnDTO> items = new ArrayList<>();
        for (H5DropRtnDTO drop : drops) {
            if (!Boolean.TRUE.equals(drop.getWhitelisted())) {
                continue;
            }
            long quantity = (long) Math.floor(Optional.ofNullable(drop.getExpectedPerHour()).orElse(0D) * hours);
            if (quantity <= 0) {
                continue;
            }
            items.add(H5ItemRtnDTO.builder()
                    .itemId(drop.getItemId())
                    .itemName(drop.getItemName())
                    .quantity(quantity)
                    .build());
        }
        return items;
    }

    /**
     * 把待发放收益发放给在线角色。离线角色不发放，收益保留到下次登录。
     */
    public H5HangupClaimRtnDTO claim(Character chr, int characterId) {
        H5HangupClaimRtnDTO result = H5HangupClaimRtnDTO.builder()
                .characterId(characterId)
                .online(chr != null)
                .givenMeso(0L)
                .givenExp(0L)
                .pendingMeso(0L)
                .pendingExp(0L)
                .givenItems(List.of())
                .pendingItems(List.of())
                .build();
        if (chr == null) {
            H5HangupRewardDO pending = rewardMapper.selectOneById(characterId);
            if (pending != null) {
                result.setPendingMeso(Optional.ofNullable(pending.getMeso()).orElse(0L));
                result.setPendingExp(Optional.ofNullable(pending.getExp()).orElse(0L));
                result.setPendingItems(parseItems(pending.getItemsJson()));
            }
            return result;
        }

        synchronized (LOCKS[Math.floorMod(characterId, LOCKS.length)]) {
            H5HangupRewardDO reward = rewardMapper.selectOneById(characterId);
            if (reward == null) {
                return result;
            }
            long meso = Optional.ofNullable(reward.getMeso()).orElse(0L);
            long exp = Optional.ofNullable(reward.getExp()).orElse(0L);
            long givenMeso = Math.min(meso, Integer.MAX_VALUE);
            long givenExp = Math.min(exp, Integer.MAX_VALUE);

            if (givenMeso > 0) {
                chr.gainMeso((int) givenMeso);
            }
            if (givenExp > 0) {
                chr.gainExp((int) givenExp);
            }

            List<H5ItemRtnDTO> pendingItems = parseItems(reward.getItemsJson());
            List<H5ItemRtnDTO> givenItems = new ArrayList<>();
            List<H5ItemRtnDTO> leftItems = new ArrayList<>();
            if (!pendingItems.isEmpty()) {
                grantItems(chr, pendingItems, givenItems, leftItems);
            }

            if (leftItems.isEmpty()) {
                rewardMapper.deleteById(characterId);
            } else {
                reward.setItemsJson(writeItems(leftItems));
                reward.setUpdateAt(new Timestamp(System.currentTimeMillis()));
                rewardMapper.update(reward);
            }

            chr.message(I18nUtil.getMessage("H5.Hangup.Claim", givenMeso, givenExp));
            log.info(I18nUtil.getLogMessage("H5.Hangup.Claim.info1", chr.getId(), chr.getName(), givenMeso, givenExp));

            result.setGivenMeso(givenMeso);
            result.setGivenExp(givenExp);
            result.setGivenItems(givenItems);
            result.setPendingItems(leftItems);
            return result;
        }
    }

    /**
     * 背包放不下的物品保留为待发放，避免物品丢失。
     */
    private void grantItems(Character chr, List<H5ItemRtnDTO> items, List<H5ItemRtnDTO> given, List<H5ItemRtnDTO> left) {
        if (chr.getClient() == null) {
            left.addAll(items);
            return;
        }
        for (H5ItemRtnDTO item : items) {
            long remain = Optional.ofNullable(item.getQuantity()).orElse(0L);
            long granted = 0L;
            while (remain > 0) {
                short chunk = (short) Math.min(remain, MAX_ITEM_QUANTITY_PER_CLAIM);
                if (!InventoryManipulator.checkSpace(chr.getClient(), item.getItemId(), chunk, "")) {
                    break;
                }
                // expiration 必须传 -1（无期限）；传 0 会被客户端当成 1970 年到期，登录时按「现金道具已过期」清除
                InventoryManipulator.addById(chr.getClient(), item.getItemId(), chunk, "", -1, (short) 0, -1L);
                granted += chunk;
                remain -= chunk;
            }
            if (granted > 0) {
                given.add(H5ItemRtnDTO.builder()
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .quantity(granted)
                        .build());
            }
            if (remain > 0) {
                left.add(H5ItemRtnDTO.builder()
                        .itemId(item.getItemId())
                        .itemName(item.getItemName())
                        .quantity(remain)
                        .build());
            }
        }
    }

    /**
     * 按角色 ID 领取：在线走游戏逻辑发放，离线直接写数据库入账（背包/经验/金币）。
     */
    public H5HangupClaimRtnDTO claimByCharacterId(int characterId) {
        CharactersDO chr = charactersMapper.selectOneById(characterId);
        if (chr == null) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_CHARACTER_NOT_FOUND"));
        }
        Character online = findOnlineCharacter(chr.getWorld(), characterId);
        if (online != null) {
            return claim(online, characterId);
        }
        return claimOffline(chr);
    }

    /**
     * 离线发放：金币/经验直接累加到 characters 表，物品按在线发放相同的白板规则写入背包表。
     * 升级判断无需处理：登录时 exp 原样加载，玩家获得任意经验时服务端会自动循环升级。
     */
    private H5HangupClaimRtnDTO claimOffline(CharactersDO chr) {
        H5HangupClaimRtnDTO result = H5HangupClaimRtnDTO.builder()
                .characterId(chr.getId())
                .online(false)
                .givenMeso(0L)
                .givenExp(0L)
                .pendingMeso(0L)
                .pendingExp(0L)
                .givenItems(List.of())
                .pendingItems(List.of())
                .build();
        synchronized (LOCKS[Math.floorMod(chr.getId(), LOCKS.length)]) {
            H5HangupRewardDO reward = rewardMapper.selectOneById(chr.getId());
            if (reward == null) {
                return result;
            }
            long pendingMeso = Optional.ofNullable(reward.getMeso()).orElse(0L);
            long pendingExp = Optional.ofNullable(reward.getExp()).orElse(0L);
            List<H5ItemRtnDTO> pendingItems = parseItems(reward.getItemsJson());
            if (pendingMeso <= 0 && pendingExp <= 0 && pendingItems.isEmpty()) {
                return result;
            }

            long givenMeso = 0L;
            long givenExp = 0L;
            List<H5ItemRtnDTO> givenItems = new ArrayList<>();
            List<H5ItemRtnDTO> leftItems = new ArrayList<>();
            try (Connection con = DatabaseConnection.getConnection()) {
                boolean ownTransaction = con.getAutoCommit();
                if (ownTransaction) {
                    con.setAutoCommit(false);
                }
                try {
                    // 金币：离线直加 characters.meso，上限 int 最大值
                    if (pendingMeso > 0) {
                        long room = Integer.MAX_VALUE - Optional.ofNullable(chr.getMeso()).orElse(0);
                        givenMeso = Math.max(0L, Math.min(pendingMeso, room));
                        if (givenMeso > 0) {
                            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET meso = meso + ? WHERE id = ?")) {
                                ps.setLong(1, givenMeso);
                                ps.setInt(2, chr.getId());
                                ps.executeUpdate();
                            }
                        }
                    }
                    // 经验：离线直加 characters.exp，超上限部分保留待发放
                    if (pendingExp > 0) {
                        long room = Integer.MAX_VALUE - Optional.ofNullable(chr.getExp()).orElse(0);
                        givenExp = Math.max(0L, Math.min(pendingExp, room));
                        if (givenExp > 0) {
                            try (PreparedStatement ps = con.prepareStatement("UPDATE characters SET exp = exp + ? WHERE id = ?")) {
                                ps.setLong(1, givenExp);
                                ps.setInt(2, chr.getId());
                                ps.executeUpdate();
                            }
                        }
                    }
                    // 物品：写入背包表，放不下的保留待发放
                    if (!pendingItems.isEmpty()) {
                        grantItemsOffline(con, chr, pendingItems, givenItems, leftItems);
                    }

                    long leftMeso = pendingMeso - givenMeso;
                    long leftExp = pendingExp - givenExp;
                    if (givenMeso <= 0 && givenExp <= 0 && givenItems.isEmpty()) {
                        // 什么都没发出去（背包满/超上限），保留原待发放记录
                        con.rollback();
                    } else if (leftMeso <= 0 && leftExp <= 0 && leftItems.isEmpty()) {
                        try (PreparedStatement ps = con.prepareStatement("DELETE FROM h5_hangup_reward WHERE character_id = ?")) {
                            ps.setInt(1, chr.getId());
                            ps.executeUpdate();
                        }
                        con.commit();
                    } else {
                        try (PreparedStatement ps = con.prepareStatement(
                                "UPDATE h5_hangup_reward SET meso = ?, exp = ?, items_json = ?, update_at = NOW() WHERE character_id = ?")) {
                            ps.setLong(1, leftMeso);
                            ps.setLong(2, leftExp);
                            ps.setString(3, writeItems(leftItems));
                            ps.setInt(4, chr.getId());
                            ps.executeUpdate();
                        }
                        con.commit();
                    }
                } catch (SQLException | RuntimeException e) {
                    try {
                        con.rollback();
                    } catch (SQLException ignored) {
                    }
                    throw e;
                } finally {
                    if (ownTransaction) {
                        con.setAutoCommit(true);
                    }
                }
            } catch (SQLException e) {
                log.warn("H5 offline claim failed char={}: {}", chr.getId(), e.getMessage());
                result.setPendingMeso(pendingMeso);
                result.setPendingExp(pendingExp);
                result.setPendingItems(pendingItems);
                return result;
            }

            log.info("H5 offline claim char={} name={} meso={} exp={} items={}",
                    chr.getId(), chr.getName(), givenMeso, givenExp, givenItems.size());
            result.setGivenMeso(givenMeso);
            result.setGivenExp(givenExp);
            result.setGivenItems(givenItems);
            result.setPendingMeso(pendingMeso - givenMeso);
            result.setPendingExp(pendingExp - givenExp);
            result.setPendingItems(leftItems);
            return result;
        }
    }

    /**
     * 离线写背包：优先补满现有未满堆叠，再找空格新放；装备与在线发放同样走 WZ 构造（白板）。
     */
    private void grantItemsOffline(Connection con, CharactersDO chr, List<H5ItemRtnDTO> items,
                                   List<H5ItemRtnDTO> given, List<H5ItemRtnDTO> left) throws SQLException {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        for (H5ItemRtnDTO dto : items) {
            long remain = Optional.ofNullable(dto.getQuantity()).orElse(0L);
            long granted = 0L;
            InventoryType mit = ItemConstants.getInventoryType(dto.getItemId());
            // 宠物需要 inventorypet 行与唯一 petid，离线不发放，保留到在线领取
            if (mit == null || mit == InventoryType.UNDEFINED || mit == InventoryType.CASH || ItemConstants.isPet(dto.getItemId())) {
                left.add(dto);
                continue;
            }
            short slotMax = ii.getSlotMax(null, dto.getItemId());
            byte slotLimit = slotLimitOf(chr, mit);

            // 1) 补满现有未满堆（仅可堆叠物品）
            if (slotMax > 1 && remain > 0) {
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT position, quantity FROM inventoryitems WHERE type = 1 AND characterid = ? AND inventorytype = ? AND itemid = ? AND quantity > 0 AND quantity < ? ORDER BY position")) {
                    ps.setString(1, String.valueOf(chr.getId()));
                    ps.setInt(2, mit.getType());
                    ps.setInt(3, dto.getItemId());
                    ps.setShort(4, slotMax);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (remain > 0 && rs.next()) {
                            long room = slotMax - rs.getShort("quantity");
                            if (room <= 0) {
                                continue;
                            }
                            long add = Math.min(room, remain);
                            try (PreparedStatement ups = con.prepareStatement(
                                    "UPDATE inventoryitems SET quantity = quantity + ? WHERE type = 1 AND characterid = ? AND inventorytype = ? AND position = ?")) {
                                ups.setInt(1, (int) add);
                                ups.setString(2, String.valueOf(chr.getId()));
                                ups.setInt(3, mit.getType());
                                ups.setInt(4, rs.getInt("position"));
                                ups.executeUpdate();
                            }
                            remain -= add;
                            granted += add;
                        }
                    }
                }
            }

            // 2) 找空格新放（与 addById 一致：普通物品 new Item，装备走 WZ 构造）
            TreeSet<Integer> used = occupiedOfflinePositions(con, chr, mit);
            int position = 1;
            while (remain > 0) {
                while (used.contains(position)) {
                    position++;
                }
                if (position > slotLimit) {
                    break;
                }
                long chunk = Math.min(remain, slotMax);
                Item item = mit == InventoryType.EQUIP
                        ? ii.getEquipById(dto.getItemId())
                        : new Item(dto.getItemId(), (short) 0, (short) chunk, 0);
                item.setPosition((short) position);
                item.setOwner("");
                item.setQuantity(mit == InventoryType.EQUIP ? (short) 1 : (short) chunk);
                insertOfflineItem(con, chr, mit, item);
                used.add(position);
                position++;
                remain -= chunk;
                granted += chunk;
            }

            if (granted > 0) {
                given.add(H5ItemRtnDTO.builder()
                        .itemId(dto.getItemId())
                        .itemName(dto.getItemName())
                        .quantity(granted)
                        .build());
            }
            if (remain > 0) {
                left.add(H5ItemRtnDTO.builder()
                        .itemId(dto.getItemId())
                        .itemName(dto.getItemName())
                        .quantity(remain)
                        .build());
            }
        }
    }

    /** 背包格子数：与登录加载一致（CASH 96，其余取角色表扩展槽位，默认 24） */
    private byte slotLimitOf(CharactersDO chr, InventoryType type) {
        return switch (type) {
            case EQUIP -> (byte) Math.min(96, Optional.ofNullable(chr.getEquipslots()).orElse(24));
            case USE -> (byte) Math.min(96, Optional.ofNullable(chr.getUseslots()).orElse(24));
            case SETUP -> (byte) Math.min(96, Optional.ofNullable(chr.getSetupslots()).orElse(24));
            case ETC -> (byte) Math.min(96, Optional.ofNullable(chr.getEtcslots()).orElse(24));
            default -> 96;
        };
    }

    private TreeSet<Integer> occupiedOfflinePositions(Connection con, CharactersDO chr, InventoryType mit) throws SQLException {
        TreeSet<Integer> used = new TreeSet<>();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT position FROM inventoryitems WHERE type = 1 AND characterid = ? AND inventorytype = ? AND position > 0")) {
            ps.setString(1, String.valueOf(chr.getId()));
            ps.setInt(2, mit.getType());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    used.add(rs.getInt(1));
                }
            }
        }
        return used;
    }

    /** 写入背包表，装备附加 inventoryequipment 行；列序与 {@code ItemFactory.saveItems} 一致 */
    private void insertOfflineItem(Connection con, CharactersDO chr, InventoryType mit, Item item) throws SQLException {
        long inventoryItemId;
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO inventoryitems VALUES (DEFAULT, ?, ?, NULL, ?, ?, ?, ?, ?, 0, 0, 0, '')",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, 1); // type: 玩家背包（ItemFactory.INVENTORY）
            ps.setString(2, String.valueOf(chr.getId()));
            ps.setInt(3, item.getItemId());
            ps.setInt(4, mit.getType());
            ps.setInt(5, item.getPosition());
            ps.setInt(6, item.getQuantity());
            ps.setString(7, item.getOwner());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("Inserting offline item failed (no generated key)");
                }
                inventoryItemId = rs.getLong(1);
            }
        }
        if (item instanceof Equip equip) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO inventoryequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?)")) {
                ps.setLong(1, inventoryItemId);
                ps.setInt(2, equip.getUpgradeSlots());
                ps.setInt(3, equip.getLevel());
                ps.setInt(4, equip.getStr());
                ps.setInt(5, equip.getDex());
                ps.setInt(6, equip.getInt());
                ps.setInt(7, equip.getLuk());
                ps.setInt(8, equip.getHp());
                ps.setInt(9, equip.getMp());
                ps.setInt(10, equip.getWatk());
                ps.setInt(11, equip.getMatk());
                ps.setInt(12, equip.getWdef());
                ps.setInt(13, equip.getMdef());
                ps.setInt(14, equip.getAcc());
                ps.setInt(15, equip.getAvoid());
                ps.setInt(16, equip.getHands());
                ps.setInt(17, equip.getSpeed());
                ps.setInt(18, equip.getJump());
                ps.setInt(19, equip.getVicious());
                ps.setInt(20, equip.getItemLevel());
                ps.setInt(21, equip.getItemExp());
                ps.setInt(22, equip.getRingId());
                ps.executeUpdate();
            }
        }
    }

    /**
     * 登录时自动发放待领取的挂机收益。
     */
    public void claimOnLogin(Character chr) {
        if (chr == null) {
            return;
        }
        try {
            claim(chr, chr.getId());
        } catch (Exception e) {
            log.error(I18nUtil.getLogMessage("H5.Hangup.ClaimOnLogin.error1", chr.getId(), e.getMessage()), e);
        }
    }

    /**
     * 登出时记录战斗力快照。
     */
    public void snapshotOnLogout(Character chr) {
        if (chr == null) {
            return;
        }
        try {
            catalogService.snapshot(chr);
        } catch (Exception e) {
            log.error("H5 snapshot on logout failed: characterId={}, error={}", chr.getId(), e.getMessage(), e);
        }
    }

    /**
     * 开始挂机：锁定地图并开启本次挂机会话。
     */
    public H5HangupRtnDTO.H5HangupCharRtnDTO start(int characterId, Integer mapId) {
        CharactersDO chr = requireCharacter(characterId);
        synchronized (LOCKS[Math.floorMod(characterId, LOCKS.length)]) {
            H5HangupStateDO state = stateMapper.selectOneById(characterId);
            if (state != null && Boolean.TRUE.equals(state.getActive())) {
                return stateSummary(characterId);
            }
            int targetMap = mapId != null && mapId > 0 ? mapId
                    : (state == null ? 0 : Optional.ofNullable(state.getMapId()).orElse(0));
            H5HangupMapDO map = targetMap > 0 ? catalogService.getMap(targetMap) : null;
            if (map == null) {
                throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_MAP_SELECT_REQUIRED"));
            }
            long now = System.currentTimeMillis();
            LocalDate today = LocalDate.now();
            int dailySeconds = 0;
            long totalSeconds = 0L;
            if (state != null) {
                totalSeconds = Optional.ofNullable(state.getTotalSeconds()).orElse(0L);
                if (state.getDailyDate() != null && today.equals(state.getDailyDate().toLocalDate())) {
                    dailySeconds = Optional.ofNullable(state.getDailySeconds()).orElse(0);
                }
            }
            int dailyCapSeconds = dailyCapSeconds();
            if (dailySeconds >= dailyCapSeconds) {
                throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_DAILY_LIMIT"));
            }
            H5HangupStateDO next = H5HangupStateDO.builder()
                    .characterId(characterId)
                    .accountId(chr.getAccountid())
                    .world(chr.getWorld())
                    .mapId(map.getMapId())
                    .lastSettleTime(new Timestamp(now))
                    .dailyDate(Date.valueOf(today))
                    .dailySeconds(dailySeconds)
                    .totalSeconds(totalSeconds)
                    .active(true)
                    .sessionStartTime(now)
                    .sessionKills(0D)
                    .updateAt(new Timestamp(now))
                    .build();
            if (state == null) {
                stateMapper.insert(next);
            } else {
                stateMapper.update(next);
            }
            log.info("H5 hangup start char={} name={} map={}", characterId, chr.getName(), map.getName());
            return stateSummary(characterId);
        }
    }

    /**
     * 挂机中每分钟刷新一次收益，不停止挂机。
     */
    public H5HangupRtnDTO.H5HangupCharRtnDTO progress(int characterId) {
        CharactersDO chr = requireCharacter(characterId);
        return settleCharacter(chr, false);
    }

    /**
     * 选择挂机地图（不重置离线计时，收益从上次结算点继续累计）。
     */
    public H5HangupStateDO selectMap(int characterId, int mapId) {
        CharactersDO chr = charactersMapper.selectOneById(characterId);
        if (chr == null) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_CHARACTER_NOT_FOUND"));
        }
        H5HangupMapDO map = catalogService.getMap(mapId);
        H5HangupStateDO state = stateMapper.selectOneById(characterId);
        // 挂机中锁图，必须先结算收益停止本次挂机
        if (state != null && Boolean.TRUE.equals(state.getActive())) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_MAP_LOCKED"));
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (state == null) {
            state = H5HangupStateDO.builder()
                    .characterId(characterId)
                    .accountId(chr.getAccountid())
                    .world(chr.getWorld())
                    .mapId(map.getMapId())
                    .lastSettleTime(Optional.ofNullable(chr.getLastLogoutTime()).orElse(now))
                    .dailyDate(Date.valueOf(LocalDate.now()))
                    .dailySeconds(0)
                    .totalSeconds(0L)
                    .updateAt(now)
                    .build();
            stateMapper.insert(state);
            return state;
        }
        state.setMapId(mapId);
        state.setUpdateAt(now);
        stateMapper.update(state);
        return state;
    }

    public H5HangupStateDO getState(int characterId) {
        return stateMapper.selectOneById(characterId);
    }

    /**
     * 丢弃待领取战利品中的指定物品（按物品 ID），金币与经验不受影响。
     */
    public H5HangupRtnDTO.H5HangupCharRtnDTO discardItems(int characterId, List<Integer> itemIds) {
        if (itemIds != null && !itemIds.isEmpty()) {
            synchronized (LOCKS[Math.floorMod(characterId, LOCKS.length)]) {
                H5HangupRewardDO reward = rewardMapper.selectOneById(characterId);
                if (reward != null) {
                    Set<Integer> discard = new HashSet<>(itemIds);
                    List<H5ItemRtnDTO> left = new ArrayList<>();
                    for (H5ItemRtnDTO item : parseItems(reward.getItemsJson())) {
                        if (!discard.contains(item.getItemId())) {
                            left.add(item);
                        }
                    }
                    reward.setItemsJson(writeItems(left));
                    reward.setUpdateAt(new Timestamp(System.currentTimeMillis()));
                    rewardMapper.update(reward);
                }
            }
        }
        return stateSummary(characterId);
    }

    /**
     * 查询角色当前挂机状态（不产生结算）。
     */
    public H5HangupRtnDTO.H5HangupCharRtnDTO stateSummary(int characterId) {
        CharactersDO chr = charactersMapper.selectOneById(characterId);
        if (chr == null) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_CHARACTER_NOT_FOUND"));
        }
        H5HangupStateDO state = stateMapper.selectOneById(characterId);
        int dailyCapSeconds = dailyCapSeconds();
        int dailySeconds = 0;
        int mapId = 0;
        if (state != null) {
            mapId = Optional.ofNullable(state.getMapId()).orElse(0);
            if (state.getDailyDate() != null && LocalDate.now().equals(state.getDailyDate().toLocalDate())) {
                dailySeconds = Optional.ofNullable(state.getDailySeconds()).orElse(0);
            }
        }
        H5HangupMapDO map = mapId > 0 ? catalogService.getMap(mapId) : null;
        H5HangupRewardDO reward = rewardMapper.selectOneById(characterId);
        return H5HangupRtnDTO.H5HangupCharRtnDTO.builder()
                .characterId(characterId)
                .characterName(chr.getName())
                .level(chr.getLevel())
                .world(chr.getWorld())
                .online(findOnlineCharacter(chr.getWorld(), characterId) != null)
                .settledSeconds(0L)
                .dailyUsedSeconds((long) dailySeconds)
                .dailyRemainSeconds(dailyRemainSeconds(dailyCapSeconds, dailySeconds))
                .gainedMeso(0L)
                .gainedExp(0L)
                .pendingMeso(reward == null ? 0L : Optional.ofNullable(reward.getMeso()).orElse(0L))
                .pendingExp(reward == null ? 0L : Optional.ofNullable(reward.getExp()).orElse(0L))
                .mapId(mapId)
                .mapName(map == null ? null : map.getName())
                .gainedItems(List.of())
                .pendingItems(reward == null ? List.of() : parseItems(reward.getItemsJson()))
                .gainedKills(0D)
                .sessionKills(state == null ? 0D : Optional.ofNullable(state.getSessionKills()).orElse(0D))
                .active(state != null && Boolean.TRUE.equals(state.getActive()))
                .sessionStartTime(state == null ? null : state.getSessionStartTime())
                .build();
    }

    /**
     * 账号下角色列表（含战斗力和已选地图）。
     */
    public List<H5CharacterRtnDTO> listCharacters(int accountId) {
        List<CharactersDO> chars = charactersMapper.selectListByQuery(QueryWrapper.create().eq("accountid", accountId));
        List<H5CharacterRtnDTO> result = new ArrayList<>();
        for (CharactersDO chr : chars) {
            H5HangupStateDO state = stateMapper.selectOneById(chr.getId());
            int mapId = state == null ? 0 : Optional.ofNullable(state.getMapId()).orElse(0);
            result.add(catalogService.toCharacter(chr, catalogService.resolvePower(chr), mapId));
        }
        return result;
    }

    /** 按地区分类的全部野外地图 */
    public H5MapRtnDTO maps(int characterId, String region) {
        CharactersDO chr = requireCharacter(characterId);
        H5HangupStateDO state = stateMapper.selectOneById(characterId);
        int mapId = state == null ? 0 : Optional.ofNullable(state.getMapId()).orElse(0);
        return catalogService.listMaps(chr, catalogService.resolvePower(chr), region, mapId);
    }

    /** 地图详情：怪物、掉落、收益估算 */
    public H5MapDetailRtnDTO mapDetail(int characterId, int mapId) {
        CharactersDO chr = requireCharacter(characterId);
        H5HangupStateDO state = stateMapper.selectOneById(characterId);
        int selected = state == null ? 0 : Optional.ofNullable(state.getMapId()).orElse(0);
        return catalogService.detail(chr, catalogService.resolvePower(chr), mapId, selected,
                catalogService.getWhitelist(characterId));
    }

    /** 地图可掉落物品 + 当前白名单状态；mapId 为 0 时只返回已启用的物品 */
    public H5WhitelistRtnDTO whitelist(int characterId, int mapId) {
        CharactersDO chr = requireCharacter(characterId);
        List<H5DropRtnDTO> items = mapId > 0
                ? catalogService.mapDropsForCharacter(chr, catalogService.resolvePower(chr), mapId, characterId)
                : List.of();
        return H5WhitelistRtnDTO.builder()
                .characterId(characterId)
                .mapId(mapId)
                .items(items)
                .whitelistedItems(catalogService.whitelistItems(characterId))
                .build();
    }

    /** 覆写白名单，勾选数量不设上限 */
    public H5WhitelistRtnDTO saveWhitelist(int characterId, List<Integer> itemIds, int mapId) {
        requireCharacter(characterId);
        catalogService.saveWhitelist(characterId, itemIds);
        return whitelist(characterId, mapId);
    }

    /** 选择地图并返回角色摘要 */
    public H5CharacterRtnDTO selectMapRtn(int characterId, int mapId) {
        CharactersDO chr = requireCharacter(characterId);
        selectMap(characterId, mapId);
        return catalogService.toCharacter(chr, catalogService.resolvePower(chr), mapId);
    }

    private CharactersDO requireCharacter(int characterId) {
        CharactersDO chr = charactersMapper.selectOneById(characterId);
        if (chr == null) {
            throw new BizException(I18nUtil.getExceptionMessage("H5_HANGUP_CHARACTER_NOT_FOUND"));
        }
        return chr;
    }

    private void accumulateReward(CharactersDO chr, long meso, long exp, Map<Integer, Long> itemStacks) {
        H5HangupRewardDO reward = rewardMapper.selectOneById(chr.getId());
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (reward == null) {
            rewardMapper.insert(H5HangupRewardDO.builder()
                    .characterId(chr.getId())
                    .accountId(chr.getAccountid())
                    .world(chr.getWorld())
                    .meso(meso)
                    .exp(exp)
                    .itemsJson(itemStacks.isEmpty() ? null : writeItems(mergeItems(itemStacks)))
                    .updateAt(now)
                    .build());
            return;
        }
        reward.setMeso(Optional.ofNullable(reward.getMeso()).orElse(0L) + meso);
        reward.setExp(Optional.ofNullable(reward.getExp()).orElse(0L) + exp);
        reward.setAccountId(chr.getAccountid());
        reward.setWorld(chr.getWorld());
        if (!itemStacks.isEmpty()) {
            Map<Integer, Long> merged = new LinkedHashMap<>();
            for (H5ItemRtnDTO item : parseItems(reward.getItemsJson())) {
                merged.merge(item.getItemId(), item.getQuantity(), Long::sum);
            }
            itemStacks.forEach((itemId, qty) -> merged.merge(itemId, qty, Long::sum));
            reward.setItemsJson(writeItems(mergeItems(merged)));
        }
        reward.setUpdateAt(now);
        rewardMapper.update(reward);
    }

    private List<H5ItemRtnDTO> mergeItems(Map<Integer, Long> stacks) {
        List<H5ItemRtnDTO> items = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : stacks.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            items.add(H5ItemRtnDTO.builder()
                    .itemId(entry.getKey())
                    .itemName(catalogService.itemName(entry.getKey()))
                    .quantity(entry.getValue())
                    .build());
        }
        return items;
    }

    private List<H5ItemRtnDTO> parseItems(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> raw = MAPPER.readValue(json, new TypeReference<>() {
            });
            List<H5ItemRtnDTO> items = new ArrayList<>();
            for (Map<String, Object> row : raw) {
                Integer itemId = (Integer) row.get("itemId");
                Number qty = (Number) row.get("quantity");
                if (itemId == null || qty == null) {
                    continue;
                }
                items.add(H5ItemRtnDTO.builder()
                        .itemId(itemId)
                        .itemName(catalogService.itemName(itemId))
                        .quantity(qty.longValue())
                        .build());
            }
            return items;
        } catch (Exception e) {
            log.error("H5 pending item json parse failed: {}", json, e);
            return List.of();
        }
    }

    private String writeItems(List<H5ItemRtnDTO> items) {
        try {
            List<Map<String, Object>> raw = new ArrayList<>();
            for (H5ItemRtnDTO item : items) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("itemId", item.getItemId());
                row.put("quantity", item.getQuantity());
                raw.add(row);
            }
            return MAPPER.writeValueAsString(raw);
        } catch (Exception e) {
            log.error("H5 pending item json write failed", e);
            return null;
        }
    }

    private void saveState(CharactersDO chr, long now, int dailySeconds, long totalSeconds, LocalDate today,
                           int mapId, boolean active, Long sessionStartTime, double sessionKills) {
        H5HangupStateDO state = H5HangupStateDO.builder()
                .characterId(chr.getId())
                .accountId(chr.getAccountid())
                .world(chr.getWorld())
                .mapId(mapId)
                .lastSettleTime(new Timestamp(now))
                .dailyDate(Date.valueOf(today))
                .dailySeconds(dailySeconds)
                .totalSeconds(totalSeconds)
                .active(active)
                .sessionStartTime(sessionStartTime)
                .sessionKills(sessionKills)
                .updateAt(new Timestamp(now))
                .build();
        if (stateMapper.selectOneById(chr.getId()) == null) {
            stateMapper.insert(state);
        } else {
            stateMapper.update(state);
        }
    }

    private Character findOnlineCharacter(int world, int characterId) {
        World w = Server.getInstance().getWorld(world);
        if (w == null) {
            return null;
        }
        return w.getPlayerStorage().getCharacterById(characterId);
    }

    private int serverInt(String key, int defaultValue) {
        Integer value = GameConfig.getInteger(key);
        return value == null ? defaultValue : value;
    }

    private int hoursToSeconds(int hours) {
        return (int) Math.min(Integer.MAX_VALUE / 2L, Math.max(0, hours) * SECONDS_PER_HOUR);
    }

    /** 每日挂机时长上限（秒），配置 <=0 表示不限 */
    private int dailyCapSeconds() {
        int hours = serverInt("h5_hangup_daily_cap_hours", DEFAULT_DAILY_CAP_HOURS);
        return hours <= 0 ? Integer.MAX_VALUE / 2 : hoursToSeconds(hours);
    }

    /** 当日剩余可挂机秒数，不限时返回 -1（前端按「不限」展示） */
    private long dailyRemainSeconds(int dailyCapSeconds, int dailySeconds) {
        if (dailyCapSeconds >= Integer.MAX_VALUE / 2) {
            return UNLIMITED_DAILY_REMAIN;
        }
        return Math.max(0L, (long) dailyCapSeconds - dailySeconds);
    }
}
