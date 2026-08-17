package soloMapling.itemPool;

import org.gms.constants.inventory.EquipType;
import org.gms.provider.Data;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;
import org.gms.provider.wz.XMLDomMapleData;
import org.gms.server.ItemInformationProvider;

import java.io.FileInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * One-time startup cache of all equip metadata parsed from WZ.
 * After {@link #initialize()} completes, every query is a pure in-memory
 * lookup — no WZ access, no iteration over ID ranges.
 *
 * <p>Usage:
 * <pre>
 *   EquipMetadataCache.initialize();              // once, after WZ is ready
 *   List&lt;EquipEntry&gt; nxCaps = EquipMetadataCache.get()
 *       .query(EquipType.CAP).cashOnly().asList();
 * </pre>
 */
public class EquipMetadataCache {

    // ── Entry ────────────────────────────────────────────────────────────

    public static class EquipEntry {
        public final int id;
        public final EquipType equipType;
        public final int gender;      // 0=male, 1=female, 2=unisex (4th-digit convention)
        public final int reqLevel;
        public final int reqJob;       // bitmask: 0=beginner/all, 1=warrior, 2=mage, 4=bow, 8=thief, 16=pirate
        public final boolean cash;
        public final boolean untradeable;
        public final boolean quest;
        public final int wzPrice;
        public final String name;

        EquipEntry(int id, EquipType equipType, int gender, int reqLevel,
                   int reqJob, boolean cash, boolean untradeable, boolean quest,
                   int wzPrice, String name) {
            this.id = id;
            this.equipType = equipType;
            this.gender = gender;
            this.reqLevel = reqLevel;
            this.reqJob = reqJob;
            this.cash = cash;
            this.untradeable = untradeable;
            this.quest = quest;
            this.wzPrice = wzPrice;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + " " + name + " [" + equipType + " lv" + reqLevel
                    + (cash ? " NX" : "") + "]";
        }
    }

    // ── ID ranges to scan ────────────────────────────────────────────────
    // Covers every equip category in v83. Ranges are inclusive.
    // Body equips use the standard equipTypeRanges from ItemInformationProviderUtilities.
    // We also add face accessories and rings which lack EquipType range entries.

    private static final Map<EquipType, int[]> EQUIP_RANGES = new LinkedHashMap<>();

    static {
        // Body / armor
        EQUIP_RANGES.put(EquipType.CAP,       new int[]{1000000, 1003073});
        EQUIP_RANGES.put(EquipType.FACE,       new int[]{1012000, 1012200});  // face accessories
        EQUIP_RANGES.put(EquipType.ACCESSORY,  new int[]{1022000, 1022104});  // eye accessories
        EQUIP_RANGES.put(EquipType.EARRING,    new int[]{1032000, 1032075});
        EQUIP_RANGES.put(EquipType.COAT,       new int[]{1040000, 1049000});
        EQUIP_RANGES.put(EquipType.LONGCOAT,   new int[]{1050000, 1052234});
        EQUIP_RANGES.put(EquipType.PANTS,      new int[]{1060000, 1062119});
        EQUIP_RANGES.put(EquipType.SHOES,      new int[]{1070000, 1072437});
        EQUIP_RANGES.put(EquipType.GLOVES,     new int[]{1080000, 1082262});
        EQUIP_RANGES.put(EquipType.SHIELD,     new int[]{1092000, 1092062});
        EQUIP_RANGES.put(EquipType.CAPE,       new int[]{1102000, 1102236});
        EQUIP_RANGES.put(EquipType.RING,       new int[]{1112000, 1112400});
        // Weapons
        EQUIP_RANGES.put(EquipType.SWORD,      new int[]{1302000, 1302133});
        EQUIP_RANGES.put(EquipType.AXE,        new int[]{1312000, 1312046});
        EQUIP_RANGES.put(EquipType.MACE,       new int[]{1322000, 1322074});
        EQUIP_RANGES.put(EquipType.DAGGER,     new int[]{1332000, 1332088});
        EQUIP_RANGES.put(EquipType.WAND,       new int[]{1372000, 1372046});
        EQUIP_RANGES.put(EquipType.STAFF,      new int[]{1382000, 1382062});
        EQUIP_RANGES.put(EquipType.SWORD_2H,   new int[]{1402000, 1402072});
        EQUIP_RANGES.put(EquipType.AXE_2H,     new int[]{1412000, 1412046});
        EQUIP_RANGES.put(EquipType.MACE_2H,    new int[]{1422000, 1422047});
        EQUIP_RANGES.put(EquipType.SPEAR,      new int[]{1432000, 1432061});
        EQUIP_RANGES.put(EquipType.POLEARM,    new int[]{1442000, 1442103});
        EQUIP_RANGES.put(EquipType.BOW,        new int[]{1452000, 1452085});
        EQUIP_RANGES.put(EquipType.CROSSBOW,   new int[]{1462000, 1462075});
        EQUIP_RANGES.put(EquipType.CLAW,       new int[]{1472000, 1472100});
        EQUIP_RANGES.put(EquipType.KNUCKLER,   new int[]{1482000, 1482046});
        EQUIP_RANGES.put(EquipType.PISTOL,     new int[]{1492000, 1492048});
    }

    // ── Singleton ────────────────────────────────────────────────────────

    private static volatile EquipMetadataCache instance;
    private static volatile boolean initialized = false;

    private final List<EquipEntry> allEquips;
    private final Map<EquipType, List<EquipEntry>> byType;
    private final Map<EquipType, List<EquipEntry>> cashByType;

    private EquipMetadataCache(List<EquipEntry> allEquips,
                               Map<EquipType, List<EquipEntry>> byType,
                               Map<EquipType, List<EquipEntry>> cashByType) {
        this.allEquips = allEquips;
        this.byType = byType;
        this.cashByType = cashByType;
    }

    public static EquipMetadataCache get() {
        if (!initialized) {
            throw new IllegalStateException("[EquipMetadataCache] Not initialized — call initialize() first");
        }
        return instance;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    // ── Initialization ───────────────────────────────────────────────────

    // Character.wz subdirectories that contain the equips we care about.
    // Names double as the category names inside String.wz Eqp.img.
    private static final List<String> CHARACTER_WZ_DIRS = List.of(
            "Accessory", "Cap", "Cape", "Coat", "Glove", "Longcoat",
            "Pants", "Ring", "Shield", "Shoes", "Weapon");

    public static synchronized void initialize() {
        if (initialized) return;

        System.out.println("[EquipMetadataCache] Initializing - scanning WZ equip data...");
        long start = System.currentTimeMillis();

        List<EquipEntry> all;
        try {
            all = scanCharacterWzDirectories();
        } catch (Exception e) {
            System.err.println("[EquipMetadataCache] Directory scan failed: " + e);
            all = new ArrayList<>();
        }
        if (all.isEmpty()) {
            System.err.println("[EquipMetadataCache] Directory scan found nothing - "
                    + "falling back to id-range probing via ItemInformationProvider");
            all = scanViaItemInformationProvider();
        }

        // selectWeightedRandom relies on ascending id order to bias towards
        // classic (low-id) equips, so sort before grouping.
        all.sort(Comparator.comparingInt(e -> e.id));

        Map<EquipType, List<EquipEntry>> typeMap = new ConcurrentHashMap<>();
        Map<EquipType, List<EquipEntry>> cashMap = new ConcurrentHashMap<>();
        for (EquipType eqType : EQUIP_RANGES.keySet()) {
            typeMap.put(eqType, new ArrayList<>());
            cashMap.put(eqType, new ArrayList<>());
        }
        for (EquipEntry entry : all) {
            typeMap.get(entry.equipType).add(entry);
            if (entry.cash) {
                cashMap.get(entry.equipType).add(entry);
            }
        }
        typeMap.replaceAll((k, v) -> Collections.unmodifiableList(v));
        cashMap.replaceAll((k, v) -> Collections.unmodifiableList(v));

        instance = new EquipMetadataCache(
                Collections.unmodifiableList(all),
                Collections.unmodifiableMap(typeMap),
                Collections.unmodifiableMap(cashMap)
        );
        initialized = true;

        long elapsed = System.currentTimeMillis() - start;
        long cashTotal = all.stream().filter(e -> e.cash).count();
        System.out.println("[EquipMetadataCache] Initialized in " + elapsed + "ms — "
                + all.size() + " equips cached (" + cashTotal + " cash/NX)");
    }

    // ── Fast path: enumerate Character.wz directories directly ──────────
    // The filename IS the item id, so only equips that actually exist get
    // parsed - no probing of 30k candidate ids, no linear directory searches
    // per id (which is what made the ItemInformationProvider path take ~20s).
    // Each file parse is self-contained, so directories scan in parallel.

    /** Intermediate scan result; name is resolved afterwards from String.wz. */
    private record ScannedEquip(int id, EquipType type, int reqLevel, int reqJob,
                                boolean cash, boolean untradeable, boolean quest,
                                int price) {
    }

    private static List<EquipEntry> scanCharacterWzDirectories() throws Exception {
        Path charWzRoot = WZFiles.CHARACTER.getFile();

        List<CompletableFuture<List<ScannedEquip>>> futures = new ArrayList<>();
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            for (String dir : CHARACTER_WZ_DIRS) {
                Path dirPath = charWzRoot.resolve(dir);
                futures.add(CompletableFuture.supplyAsync(() -> scanEquipDirectory(dirPath), pool));
            }
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }

        Map<Integer, String> names = buildNameTable();

        List<EquipEntry> all = new ArrayList<>();
        for (CompletableFuture<List<ScannedEquip>> future : futures) {
            for (ScannedEquip s : future.get()) {
                String name = names.get(s.id);
                all.add(new EquipEntry(s.id, s.type, deriveGender(s.id), s.reqLevel,
                        s.reqJob, s.cash, s.untradeable, s.quest, s.price,
                        name != null ? name : "?"));
            }
        }
        return all;
    }

    private static List<ScannedEquip> scanEquipDirectory(Path dir) {
        List<ScannedEquip> entries = new ArrayList<>();
        if (!Files.isDirectory(dir)) {
            return entries;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.img.xml")) {
            for (Path file : stream) {
                String fileName = file.getFileName().toString();
                int id;
                try {
                    id = Integer.parseInt(fileName.substring(0, fileName.length() - ".img.xml".length()));
                } catch (NumberFormatException e) {
                    continue;
                }
                EquipType eqType = classify(id);
                if (eqType == null) {
                    continue; // outside every range we track (e.g. taming mobs)
                }
                try (FileInputStream fis = new FileInputStream(file.toFile())) {
                    Data itemData = new XMLDomMapleData(fis, dir);
                    Data info = itemData.getChildByPath("info");
                    if (info == null) continue;
                    entries.add(new ScannedEquip(id, eqType,
                            DataTool.getInt("reqLevel", info, 0),
                            DataTool.getInt("reqJob", info, 0),
                            DataTool.getInt("cash", info, 0) == 1,
                            DataTool.getIntConvert("tradeBlock", info, 0) == 1,
                            DataTool.getIntConvert("quest", info, 0) == 1,
                            DataTool.getInt("price", info, 0)));
                } catch (Exception e) {
                    System.err.println("[EquipMetadataCache] Failed to parse " + fileName + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[EquipMetadataCache] Failed to scan " + dir + ": " + e.getMessage());
        }
        return entries;
    }

    /** One pass over String.wz Eqp.img building an id → name table. */
    private static Map<Integer, String> buildNameTable() {
        Map<Integer, String> names = new HashMap<>();
        try {
            Data eqpStrings = DataProviderFactory.getDataProvider(WZFiles.STRING).getData("Eqp.img");
            Data eqp = eqpStrings != null ? eqpStrings.getChildByPath("Eqp") : null;
            if (eqp == null) {
                return names;
            }
            for (Data category : eqp.getChildren()) {
                for (Data item : category.getChildren()) {
                    try {
                        int id = Integer.parseInt(item.getName());
                        String name = DataTool.getString("name", item, null);
                        if (name != null) {
                            names.put(id, name);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[EquipMetadataCache] Failed to load names from String.wz: " + e.getMessage());
        }
        return names;
    }

    private static EquipType classify(int id) {
        for (Map.Entry<EquipType, int[]> e : EQUIP_RANGES.entrySet()) {
            int[] range = e.getValue();
            if (id >= range[0] && id <= range[1]) {
                return e.getKey();
            }
        }
        return null;
    }

    // ── Fallback: original id-range probing (slow, ~20s) ────────────────

    private static List<EquipEntry> scanViaItemInformationProvider() {
        ItemInformationProvider ii = ItemInformationProvider.getInstance();
        List<EquipEntry> all = new ArrayList<>();

        for (Map.Entry<EquipType, int[]> rangeEntry : EQUIP_RANGES.entrySet()) {
            EquipType eqType = rangeEntry.getKey();
            int[] range = rangeEntry.getValue();

            for (int id = range[0]; id <= range[1]; id++) {
                Map<String, Integer> stats = ii.getEquipStats(id);
                if (stats == null) continue;  // item doesn't exist in WZ

                String name = ii.getName(id);
                all.add(new EquipEntry(id, eqType, deriveGender(id),
                        stats.getOrDefault("reqLevel", 0),
                        stats.getOrDefault("reqJob", 0),
                        stats.getOrDefault("cash", 0) == 1,
                        ii.isUntradeableRestricted(id),
                        ii.isQuestItem(id),
                        ii.getWholePrice(id),
                        name != null ? name : "?"));
            }
        }
        return all;
    }

    // ── Gender derivation (4th digit convention) ─────────────────────────

    private static int deriveGender(int itemId) {
        String s = Integer.toString(itemId);
        if (s.length() >= 4) {
            int digit = Character.getNumericValue(s.charAt(3));
            if (digit >= 0 && digit <= 2) return digit;
        }
        return 2; // default unisex
    }

    // ── Query API ────────────────────────────────────────────────────────

    /** Get all cached equips. */
    public List<EquipEntry> all() {
        return allEquips;
    }

    /** Get all equips of a given type. */
    public List<EquipEntry> getByType(EquipType type) {
        return byType.getOrDefault(type, Collections.emptyList());
    }

    /** Get all cash/NX equips of a given type (pre-filtered, instant). */
    public List<EquipEntry> getCashByType(EquipType type) {
        return cashByType.getOrDefault(type, Collections.emptyList());
    }

    /** Start a fluent filtered query. */
    public EquipQuery query(EquipType type) {
        return new EquipQuery(getByType(type));
    }

    /** Start a fluent query pre-filtered to cash items. */
    public EquipQuery queryCash(EquipType type) {
        return new EquipQuery(getCashByType(type));
    }

    // ── Fluent query builder ─────────────────────────────────────────────

    public static class EquipQuery {
        private List<EquipEntry> source;

        EquipQuery(List<EquipEntry> source) {
            this.source = source;
        }

        public EquipQuery cashOnly() {
            List<EquipEntry> filtered = new ArrayList<>();
            for (EquipEntry e : source) {
                if (e.cash) filtered.add(e);
            }
            source = filtered;
            return this;
        }

        public EquipQuery nonCash() {
            List<EquipEntry> filtered = new ArrayList<>();
            for (EquipEntry e : source) {
                if (!e.cash) filtered.add(e);
            }
            source = filtered;
            return this;
        }

        /** Filter to items matching the given bot gender (includes unisex). */
        public EquipQuery forGender(int gender) {
            List<EquipEntry> filtered = new ArrayList<>();
            for (EquipEntry e : source) {
                if (e.gender == 2 || e.gender == gender) filtered.add(e);
            }
            source = filtered;
            return this;
        }

        /** Filter to items with reqLevel <= maxLevel. */
        public EquipQuery maxLevel(int maxLevel) {
            List<EquipEntry> filtered = new ArrayList<>();
            for (EquipEntry e : source) {
                if (e.reqLevel <= maxLevel) filtered.add(e);
            }
            source = filtered;
            return this;
        }

        /** Filter to items with minLevel <= reqLevel <= maxLevel (both inclusive). */
        public EquipQuery levelBetween(int minLevel, int maxLevel) {
            List<EquipEntry> filtered = new ArrayList<>();
            for (EquipEntry e : source) {
                if (e.reqLevel >= minLevel && e.reqLevel <= maxLevel) filtered.add(e);
            }
            source = filtered;
            return this;
        }

        /**
         * Filter to items whose reqJob equals the given value exactly.
         * reqJob 0 means common/beginner gear, so unlike {@link #forJob(int)}
         * those items only match when 0 is requested.
         */
        public EquipQuery forJobExact(int reqJob) {
            List<EquipEntry> filtered = new ArrayList<>();
            for (EquipEntry e : source) {
                if (e.reqJob == reqJob) filtered.add(e);
            }
            source = filtered;
            return this;
        }

        /** Filter to items whose reqJob bitmask matches (0 = any job). */
        public EquipQuery forJob(int reqJobBitmask) {
            List<EquipEntry> filtered = new ArrayList<>();
            for (EquipEntry e : source) {
                if (e.reqJob == 0 || (e.reqJob & reqJobBitmask) != 0) filtered.add(e);
            }
            source = filtered;
            return this;
        }

        public List<EquipEntry> asList() {
            return source;
        }

        public int count() {
            return source.size();
        }

        /** Pick one at random, or null if empty. */
        public EquipEntry random() {
            if (source.isEmpty()) return null;
            return source.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(source.size()));
        }
    }
}
