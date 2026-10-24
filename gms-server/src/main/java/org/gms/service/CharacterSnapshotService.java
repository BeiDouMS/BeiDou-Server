package org.gms.service;

import lombok.extern.slf4j.Slf4j;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.ItemFactory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.snapshot.CharacterSnapshot;
import org.gms.client.snapshot.SnapshotCodecs;
import org.gms.util.I18nUtil;
import org.gms.util.Pair;
import java.sql.*;
import java.util.*;

/** 只写入入队前取得的私有快照；连接与事务由账号存档执行者统一管理。 */
@Slf4j
public final class CharacterSnapshotService {
    private CharacterSnapshotService() {}

    public static void save(Connection con, CharacterSnapshot snap) throws SQLException {
        if (!restoreCharacters(con, snap) || !restoreQuests(con, snap)) {
            throw new SQLException(I18nUtil.getExceptionMessage("CHARACTER_SAVE_FAILED"));
        }
        restoreMonsterbookLegacy(con, snap);
        restoreKeymap(con, snap);
        restoreQuickslot(con, snap);
        restoreSkillmacros(con, snap);
        restoreInventory(con, snap);
        restoreSkills(con, snap);
        restoreSavedlocations(con, snap);
        restoreTrock(con, snap);
        restoreBuddies(con, snap);
        restoreAreaInfo(con, snap);
        if (snap.eventstats != null) restoreEventstats(con, snap);
        restoreFamily(con, snap);
        restoreCashshop(con, snap);
        restoreStorage(con, snap);
    }
    private static boolean restoreCharacters(Connection con, CharacterSnapshot snap) throws SQLException {
        CharacterSnapshot.CharStats s = snap.stats;
        String sql = "UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, gachaexp = ?, "
                + "hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, "
                + "map = ?, meso = ?, hpMpUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, messengerid = ?, messengerposition = ?, "
                + "mountlevel = ?, mountexp = ?, mounttiredness= ?, equipslots = ?, useslots = ?, setupslots = ?, etcslots = ?, "
                + "monsterbookcover = ?, vanquisherStage = ?, dojoPoints = ?, lastDojoStage = ?, finishedDojoTutorial = ?, vanquisherKills = ?, "
                + "matchcardwins = ?, matchcardlosses = ?, matchcardties = ?, omokwins = ?, omoklosses = ?, omokties = ?, dataString = ?, "
                + "fquest = ?, jailexpire = ?, partnerId = ?, marriageItemId = ?, lastExpGainTime = ?, ariantPoints = ?, partySearch = ?, world = ? "
                + "WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, s.level);
            ps.setInt(2, s.fame);
            ps.setInt(3, s.str);
            ps.setInt(4, s.dex);
            ps.setInt(5, s.luk);
            ps.setInt(6, s.int_);
            ps.setInt(7, s.exp);
            ps.setInt(8, s.gachaexp);
            ps.setInt(9, s.hp);
            ps.setInt(10, s.mp);
            ps.setInt(11, s.maxhp);
            ps.setInt(12, s.maxmp);
            ps.setString(13, s.sp);
            ps.setInt(14, s.ap);
            ps.setInt(15, s.gm);
            ps.setInt(16, s.skincolor);
            ps.setInt(17, s.gender);
            ps.setInt(18, s.job);
            ps.setInt(19, s.hair);
            ps.setInt(20, s.face);
            ps.setInt(21, s.map);
            ps.setInt(22, s.meso);
            ps.setInt(23, s.hpMpUsed);
            ps.setInt(24, s.spawnpoint);
            ps.setInt(25, s.party);
            ps.setInt(26, s.buddyCapacity);
            ps.setInt(27, s.messengerid);
            ps.setInt(28, s.messengerposition);
            ps.setInt(29, s.mountlevel);
            ps.setInt(30, s.mountexp);
            ps.setInt(31, s.mounttiredness);
            ps.setInt(32, s.equipslots);
            ps.setInt(33, s.useslots);
            ps.setInt(34, s.setupslots);
            ps.setInt(35, s.etcslots);
            ps.setInt(36, s.monsterbookcover);
            ps.setInt(37, s.vanquisherStage);
            ps.setInt(38, s.dojoPoints);
            ps.setInt(39, s.lastDojoStage);
            ps.setInt(40, s.finishedDojoTutorial);
            ps.setInt(41, s.vanquisherKills);
            ps.setInt(42, s.matchcardwins);
            ps.setInt(43, s.matchcardlosses);
            ps.setInt(44, s.matchcardties);
            ps.setInt(45, s.omokwins);
            ps.setInt(46, s.omoklosses);
            ps.setInt(47, s.omokties);
            ps.setString(48, s.dataString);
            ps.setInt(49, s.fquest);
            ps.setLong(50, s.jailexpire);
            ps.setInt(51, s.partnerId);
            ps.setInt(52, s.marriageItemId);
            ps.setTimestamp(53, new Timestamp(s.lastExpGainTime));
            ps.setInt(54, s.ariantPoints);
            ps.setBoolean(55, s.partySearch);
            ps.setInt(56, snap.worldId);
            ps.setInt(57, snap.charId);
            int rows = ps.executeUpdate();
            if (rows < 1) {
                log.warn(I18nUtil.getLogMessage("CharacterSave.missingCharacter", snap.charId));
                return false;
            }
        }
        return true;
    }

    private static void restoreMonsterbookLegacy(Connection con, CharacterSnapshot snap) throws SQLException {
        if (snap.monsterbookCards == null || snap.monsterbookCards.isEmpty()) return;
        String sql = "INSERT INTO monsterbook (charid, cardid, level) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE level = VALUES(level)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (var e : snap.monsterbookCards.entrySet()) {
                ps.setInt(1, snap.charId);
                ps.setInt(2, e.getKey());
                ps.setInt(3, e.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void restoreKeymap(Connection con, CharacterSnapshot snap) throws SQLException {
        List<CharacterSnapshot.KeyBindingInfo> bindings = snap.keymap == null ? List.of() : snap.keymap;
        Set<Integer> keys = new HashSet<>();
        for (var binding : bindings) {
            if (!keys.add(binding.key)) {
                throw new SQLException(I18nUtil.getExceptionMessage("CHARACTER_SAVE_FAILED"));
            }
        }
        // 调用方已在本事务更新 characters 行，同角色写入由该行锁串行化。
        Map<Integer, Long> existing = new HashMap<>();
        try (PreparedStatement ps = con.prepareStatement("SELECT id, `key` FROM keymap WHERE characterid = ?")) {
            ps.setInt(1, snap.charId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int key = rs.getInt("key");
                    if (existing.put(key, rs.getLong("id")) != null) {
                        throw new SQLException(I18nUtil.getExceptionMessage("CHARACTER_SAVE_FAILED"));
                    }
                }
            }
        }
        try (PreparedStatement update = con.prepareStatement("UPDATE keymap SET `type` = ?, `action` = ? WHERE id = ?");
             PreparedStatement insert = con.prepareStatement(
                     "INSERT INTO keymap (characterid, `key`, `type`, `action`) VALUES (?, ?, ?, ?)");
             PreparedStatement delete = con.prepareStatement("DELETE FROM keymap WHERE id = ?")) {
            for (var binding : bindings) {
                Long id = existing.remove(binding.key);
                if (id != null) {
                    update.setInt(1, binding.type);
                    update.setInt(2, binding.action);
                    update.setLong(3, id);
                    update.addBatch();
                } else {
                    insert.setInt(1, snap.charId);
                    insert.setInt(2, binding.key);
                    insert.setInt(3, binding.type);
                    insert.setInt(4, binding.action);
                    insert.addBatch();
                }
            }
            for (long id : existing.values()) {
                delete.setLong(1, id);
                delete.addBatch();
            }
            update.executeBatch();
            insert.executeBatch();
            delete.executeBatch();
        }
    }

    private static void restoreQuickslot(Connection con, CharacterSnapshot snap) throws SQLException {
        if (snap.quickslot == null) {
            return;
        }
        String sql = "INSERT INTO quickslotkeymapped (accountid, keymap) VALUES (?, ?) ON DUPLICATE KEY UPDATE keymap = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, snap.accountId);
            ps.setLong(2, snap.quickslot);
            ps.setLong(3, snap.quickslot);
            ps.executeUpdate();
        }
    }

    private static void restoreSkillmacros(Connection con, CharacterSnapshot snap) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM skillmacros WHERE characterid = ?")) {
            ps.setInt(1, snap.charId);
            ps.executeUpdate();
        }
        if (snap.skillmacros == null || snap.skillmacros.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            for (var m : snap.skillmacros) {
                ps.setInt(1, snap.charId);
                ps.setInt(2, m.skill1);
                ps.setInt(3, m.skill2);
                ps.setInt(4, m.skill3);
                ps.setString(5, m.name);
                ps.setInt(6, m.shout);
                ps.setInt(7, m.position);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void restoreInventory(Connection con, CharacterSnapshot snap) throws SQLException {
        if (snap.inventory == null) {
            return;
        }
        List<Pair<Item, InventoryType>> items = new ArrayList<>();
        for (var info : snap.inventory) {
            InventoryType type = SnapshotCodecs.toInventoryType(info.inventorytype);
            items.add(new Pair<>(SnapshotCodecs.decodeItem(info), type));
        }
        ItemFactory.INVENTORY.saveItems(items, snap.charId, con);
    }

    private static void restoreSkills(Connection con, CharacterSnapshot snap) throws SQLException {
        if (snap.skills == null || snap.skills.isEmpty()) {
            return;
        }
        // 与原 REPLACE 一致：同一快照重复的技能取最后一份，未包含的旧技能不删除。
        Map<Integer, CharacterSnapshot.SkillInfo> skills = new LinkedHashMap<>();
        for (var skill : snap.skills) {
            skills.put(skill.skillid, skill);
        }
        Map<Integer, Long> existing = new HashMap<>();
        // 用已有 skillpair(skillid, characterid) 索引查询本次技能，避免按角色全表扫描。
        try (PreparedStatement ps = con.prepareStatement("SELECT id, skillid FROM skills WHERE skillid IN ("
                + String.join(",", Collections.nCopies(skills.size(), "?")) + ") AND characterid = ?")) {
            int parameter = 1;
            for (int skillId : skills.keySet()) {
                ps.setInt(parameter++, skillId);
            }
            ps.setInt(parameter, snap.charId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    existing.put(rs.getInt("skillid"), rs.getLong("id"));
                }
            }
        }
        // characters 行锁仍由外层事务持有；仅修改非索引属性，新增技能才执行 INSERT。
        try (PreparedStatement update = con.prepareStatement(
                "UPDATE skills SET skilllevel = ?, masterlevel = ?, expiration = ? WHERE id = ?");
             PreparedStatement insert = con.prepareStatement(
                     "INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)")) {
            for (var skill : skills.values()) {
                Long id = existing.get(skill.skillid);
                if (id != null) {
                    update.setInt(1, skill.skilllevel);
                    update.setInt(2, skill.masterlevel);
                    update.setLong(3, skill.expiration);
                    update.setLong(4, id);
                    update.addBatch();
                } else {
                    insert.setInt(1, snap.charId);
                    insert.setInt(2, skill.skillid);
                    insert.setInt(3, skill.skilllevel);
                    insert.setInt(4, skill.masterlevel);
                    insert.setLong(5, skill.expiration);
                    insert.addBatch();
                }
            }
            update.executeBatch();
            insert.executeBatch();
        }
    }

    private static void restoreSavedlocations(Connection con, CharacterSnapshot snap) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM savedlocations WHERE characterid = ?")) {
            ps.setInt(1, snap.charId);
            ps.executeUpdate();
        }
        if (snap.savedlocations == null || snap.savedlocations.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO savedlocations (characterid, `locationtype`, `map`, `portal`) VALUES (?, ?, ?, ?)")) {
            for (var l : snap.savedlocations) {
                ps.setInt(1, snap.charId);
                ps.setString(2, l.locationtype);
                ps.setInt(3, l.map);
                ps.setInt(4, l.portal);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void restoreTrock(Connection con, CharacterSnapshot snap) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM trocklocations WHERE characterid = ?")) {
            ps.setInt(1, snap.charId);
            ps.executeUpdate();
        }
        if (snap.trocklocations == null || snap.trocklocations.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO trocklocations (characterid, mapid, vip) VALUES (?, ?, ?)")) {
            for (var t : snap.trocklocations) {
                ps.setInt(1, snap.charId);
                ps.setInt(2, t.mapid);
                ps.setInt(3, t.vip);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void restoreBuddies(Connection con, CharacterSnapshot snap) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM buddies WHERE characterid = ? AND pending = 0")) {
            ps.setInt(1, snap.charId);
            ps.executeUpdate();
        }
        if (snap.buddies == null || snap.buddies.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO buddies (characterid, `buddyid`, `pending`, `group`) VALUES (?, ?, 0, ?)")) {
            for (var b : snap.buddies) {
                ps.setInt(1, snap.charId);
                ps.setInt(2, b.buddyid);
                ps.setString(3, b.group);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void restoreAreaInfo(Connection con, CharacterSnapshot snap) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM area_info WHERE charid = ?")) {
            ps.setInt(1, snap.charId);
            ps.executeUpdate();
        }
        if (snap.areaInfo == null || snap.areaInfo.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO area_info (id, charid, area, info) VALUES (DEFAULT, ?, ?, ?)")) {
            for (var a : snap.areaInfo) {
                ps.setInt(1, snap.charId);
                ps.setShort(2, a.area);
                ps.setString(3, a.info);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static void restoreEventstats(Connection con, CharacterSnapshot snap) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM eventstats WHERE characterid = ?")) {
            ps.setInt(1, snap.charId);
            ps.executeUpdate();
        }
        if (snap.eventstats == null || snap.eventstats.isEmpty()) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO eventstats (characterid, name, info) VALUES (?, ?, ?)")) {
            for (var e : snap.eventstats) {
                ps.setInt(1, snap.charId);
                ps.setString(2, e.name);
                ps.setInt(3, e.info);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static boolean restoreQuests(Connection con, CharacterSnapshot snap) throws SQLException {
        for (String sql : new String[]{
                "DELETE FROM medalmaps WHERE characterid = ?",
                "DELETE FROM questprogress WHERE characterid = ?",
                "DELETE FROM queststatus WHERE characterid = ?"}) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, snap.charId);
                ps.executeUpdate();
            }
        }
        if (snap.quests == null || snap.quests.isEmpty()) {
            return true;
        }
        boolean complete = true;
        try (PreparedStatement psStatus = con.prepareStatement(
                "INSERT INTO queststatus (`characterid`, `quest`, `status`, `time`, `expires`, `forfeited`, `completed`) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
             PreparedStatement psProgress = con.prepareStatement(
                     "INSERT INTO questprogress VALUES (DEFAULT, ?, ?, ?, ?)");
             PreparedStatement psMedal = con.prepareStatement(
                     "INSERT INTO medalmaps VALUES (DEFAULT, ?, ?, ?)")) {
            for (var q : snap.quests) {
                psStatus.setInt(1, snap.charId);
                psStatus.setInt(2, q.quest);
                psStatus.setInt(3, q.status);
                psStatus.setLong(4, q.time);
                psStatus.setLong(5, q.expires);
                psStatus.setInt(6, q.forfeited);
                psStatus.setInt(7, q.completed);
                psStatus.executeUpdate();
                try (ResultSet rs = psStatus.getGeneratedKeys()) {
                    if (!rs.next()) {
                        complete = false;
                        continue;
                    }
                    long queststatusid = rs.getLong(1);
                    if (q.progress != null) {
                        for (var p : q.progress.entrySet()) {
                            psProgress.setInt(1, snap.charId);
                            psProgress.setLong(2, queststatusid);
                            psProgress.setInt(3, p.getKey());
                            psProgress.setString(4, p.getValue());
                            psProgress.addBatch();
                        }
                    }
                    if (q.medalmaps != null) {
                        for (int mapid : q.medalmaps) {
                            psMedal.setInt(1, snap.charId);
                            psMedal.setLong(2, queststatusid);
                            psMedal.setInt(3, mapid);
                            psMedal.addBatch();
                        }
                    }
                }
            }
            psProgress.executeBatch();
            psMedal.executeBatch();
        }
        return complete;
    }

    private static void restoreFamily(Connection con, CharacterSnapshot snap) throws SQLException {
        if (snap.family == null) {
            return;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE family_character SET reputation = ?, todaysrep = ?, totalreputation = ?, reptosenior = ? WHERE cid = ?")) {
            ps.setInt(1, snap.family.reputation);
            ps.setInt(2, snap.family.todaysrep);
            ps.setInt(3, snap.family.totalreputation);
            ps.setInt(4, snap.family.reptosenior);
            ps.setInt(5, snap.charId);
            ps.executeUpdate();
        }
    }

    private static void restoreStorage(Connection con, CharacterSnapshot snap) throws SQLException {
        if (snap.storage == null) {
            return;
        }
        var st = snap.storage;
        try (PreparedStatement ps = con.prepareStatement("UPDATE storages SET slots = ?, meso = ? WHERE storageid = ?")) {
            ps.setInt(1, st.slots);
            ps.setInt(2, st.meso);
            ps.setInt(3, st.storageid);
            if (ps.executeUpdate() != 1) throw new SQLException(I18nUtil.getExceptionMessage("CHARACTER_SAVE_FAILED"));
        }
        if (st.items != null) {
            List<Pair<Item, InventoryType>> items = new ArrayList<>();
            for (var info : st.items) {
                items.add(new Pair<>(SnapshotCodecs.decodeItem(info),
                        SnapshotCodecs.toInventoryType(info.inventorytype)));
            }
            ItemFactory.STORAGE.saveItems(items, st.storageid, con);
        }
    }
    private static void restoreCashshop(Connection con, CharacterSnapshot snap) throws SQLException {
        CharacterSnapshot.CashShopInfo cash = snap.cashshop;
        if (cash == null) return;
        try (PreparedStatement ps = con.prepareStatement("UPDATE accounts SET nxCredit=?, maplePoint=?, nxPrepaid=? WHERE id=?")) {
            ps.setInt(1, cash.nxCredit); ps.setInt(2, cash.maplePoint); ps.setInt(3, cash.nxPrepaid); ps.setInt(4, snap.accountId);
            if (ps.executeUpdate() != 1) throw new SQLException(I18nUtil.getExceptionMessage("CHARACTER_SAVE_FAILED"));
        }
        ItemFactory factory = Arrays.stream(ItemFactory.values()).filter(value -> value.getValue() == cash.factoryValue)
                .findFirst().orElseThrow(() -> new SQLException(I18nUtil.getExceptionMessage("CHARACTER_SAVE_FAILED")));
        List<Pair<Item, InventoryType>> items = new ArrayList<>();
        for (var item : cash.items) items.add(new Pair<>(SnapshotCodecs.decodeItem(item), InventoryType.getByType(item.inventorytype)));
        factory.saveItems(items, snap.accountId, con);
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM wishlists WHERE charid=?")) {
            ps.setInt(1, cash.characterId); ps.executeUpdate();
        }
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO wishlists VALUES (DEFAULT,?,?)")) {
            ps.setInt(1, cash.characterId);
            for (int sn : cash.wishlists) { ps.setInt(2, sn); ps.addBatch(); }
            ps.executeBatch();
        }
    }
}
