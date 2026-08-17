package soloMapling.Environment;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import soloMapling.ArtificialPlayer.BotTownSystem.TownPresenceConfig;

/**
 * Loads {@code EnvironmentPopulation.yaml}: startup wave toggles, FM/Henesys/merchant counts,
 * TrainingBot spawn cohorts, and {@code waves.town_presence.towns} (ambient town bots).
 *
 * <p>Resolution order for the file:
 * <ol>
 *   <li>explicit path from {@link #setConfigPath(String)} / {@code solomapling.population-config}</li>
 *   <li>cwd-relative {@value #DEFAULT_FS_PATH} (gms-server working directory)</li>
 *   <li>classpath resource {@value #CLASSPATH_RESOURCE}</li>
 * </ol>
 */
public final class EnvironmentPopulationConfig {

    private EnvironmentPopulationConfig() {
    }

    public static final String DEFAULT_FS_PATH =
            "src/main/java/soloMapling/Environment/EnvironmentPopulation.yaml";
    public static final String CLASSPATH_RESOURCE =
            "soloMapling/Environment/EnvironmentPopulation.yaml";

    public record PlatformBatch(int m1, int m2, int m5) {
    }

    public record HenesysBatch(int main, int market, int park, int social) {
    }

    public record MerchantBatch(String platform, int selling, int buying, int nx) {
    }

    public record TrainingCohort(int mapId, int count, int levelLo, int levelHi) {
    }

    public record WarmNav(int mapId, int hops) {
    }

    public record WaveEssentials(boolean enabled, boolean casinoNpcs, boolean tutorial,
                                 HenesysBatch henesys, String fmRegion, PlatformBatch fmEntrance) {
    }

    public record WaveFmBuildout(boolean enabled, String fmRegion, PlatformBatch fmEntrance,
                                 List<MerchantBatch> merchants, boolean gacha) {
    }

    public record WaveHenesysPopulation(boolean enabled, boolean jqPetPark, HenesysBatch henesys,
                                        boolean fillersHenesys, boolean startSocialSystems) {
    }

    public record WaveExpandFm(boolean enabled, String fmRegion, PlatformBatch fmEntrance,
                               List<MerchantBatch> merchants, boolean fillersHenesysMarket) {
    }

    public record WaveHenesysSubAreas(boolean enabled, String fmRegion, HenesysBatch henesys,
                                      boolean fillersHenesysPark, boolean fillersPotionShop,
                                      boolean fillersGameZone, boolean gameZoneHosts) {
    }

    public record WaveSpecialty(boolean enabled, boolean blackjack, boolean dropGame,
                                boolean dropGameSpectators, boolean socialPetPark,
                                boolean convertScrollBots) {
    }

    public record WaveLateArrivals(boolean enabled, boolean opqLobby, List<MerchantBatch> merchants) {
    }

    public record WaveTraining(boolean enabled, WarmNav warmNav, List<TrainingCohort> cohorts) {
    }

    public record WaveTownPresence(boolean enabled) {
    }

    public record PopulationPlan(
            int version,
            double scale,
            String loadedFrom,
            WaveEssentials essentials,
            WaveFmBuildout fmBuildout,
            WaveHenesysPopulation henesysPopulation,
            WaveExpandFm expandFmMarket,
            WaveHenesysSubAreas henesysSubAreas,
            WaveSpecialty specialty,
            WaveLateArrivals lateArrivals,
            WaveTraining training,
            WaveTownPresence townPresence
    ) {
        public int scaled(int n) {
            if (n <= 0) {
                return 0;
            }
            return Math.max(0, (int) Math.round(n * scale));
        }

        public int trainingCohortTotal() {
            int sum = 0;
            for (TrainingCohort c : training.cohorts()) {
                sum += scaled(c.count());
            }
            return sum;
        }
    }

    private static volatile String overridePath;
    private static volatile PopulationPlan cached;
    private static volatile Map<String, Object> cachedRoot;

    public static void setConfigPath(String path) {
        overridePath = (path == null || path.isBlank()) ? null : path.trim();
        cached = null;
        cachedRoot = null;
    }

    public static PopulationPlan plan() {
        PopulationPlan local = cached;
        if (local == null) {
            local = load();
            cached = local;
        }
        return local;
    }

    public static PopulationPlan reload() {
        cached = load();
        // TownPresenceConfig reads the same YAML; drop its cache so the next towns() re-parses.
        TownPresenceConfig.invalidate();
        return cached;
    }

    public static Optional<String> lastLoadedFrom() {
        PopulationPlan p = plan();
        return Optional.ofNullable(p.loadedFrom());
    }

    /**
     * Raw {@code waves.town_presence.towns} list from the last successful load (empty if absent).
     * Used by {@link TownPresenceConfig}.
     */
    @SuppressWarnings("unchecked")
    public static List<?> rawTownsList() {
        plan();
        Map<String, Object> root = cachedRoot;
        if (root == null) {
            return List.of();
        }
        Map<String, Object> waves = asMap(root.get("waves"));
        Map<String, Object> tp = asMap(waves.get("town_presence"));
        Object towns = tp.get("towns");
        if (towns instanceof List<?> list) {
            return list;
        }
        // Legacy: top-level towns: (old TownPresence.yaml shape if someone pasted it)
        Object top = root.get("towns");
        if (top instanceof List<?> list) {
            return list;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static PopulationPlan load() {
        try (Reader reader = openReader()) {
            if (reader == null) {
                System.out.println("[EnvironmentPopulationConfig] no config found; using built-in defaults");
                cachedRoot = null;
                return defaults("(built-in)");
            }
            YamlReader yaml = new YamlReader(reader);
            Map<String, Object> root = (Map<String, Object>) yaml.read();
            if (root == null) {
                cachedRoot = null;
                return defaults("(empty yaml)");
            }
            cachedRoot = root;
            return parse(root, currentSourceLabel);
        } catch (Exception e) {
            System.out.println("[EnvironmentPopulationConfig] failed to load: " + e.getMessage()
                    + " — falling back to built-in defaults");
            cachedRoot = null;
            return defaults("(fallback after error)");
        }
    }

    private static String currentSourceLabel = "(unknown)";

    private static Reader openReader() throws Exception {
        if (overridePath != null) {
            File f = new File(overridePath);
            if (f.isFile()) {
                currentSourceLabel = f.getAbsolutePath();
                return new FileReader(f, StandardCharsets.UTF_8);
            }
            System.out.println("[EnvironmentPopulationConfig] override path missing: " + overridePath);
        }
        File cwd = new File(DEFAULT_FS_PATH);
        if (cwd.isFile()) {
            currentSourceLabel = cwd.getAbsolutePath();
            return new FileReader(cwd, StandardCharsets.UTF_8);
        }
        InputStream in = EnvironmentPopulationConfig.class.getClassLoader()
                .getResourceAsStream(CLASSPATH_RESOURCE);
        if (in != null) {
            currentSourceLabel = "classpath:" + CLASSPATH_RESOURCE;
            return new InputStreamReader(in, StandardCharsets.UTF_8);
        }
        currentSourceLabel = null;
        return null;
    }

    @SuppressWarnings("unchecked")
    private static PopulationPlan parse(Map<String, Object> root, String source) {
        int version = toInt(root.get("version"), 1);
        double scale = toDouble(root.get("scale"), 1.0);
        Map<String, Object> waves = asMap(root.get("waves"));

        return new PopulationPlan(
                version,
                scale,
                source,
                parseEssentials(asMap(waves.get("essentials"))),
                parseFmBuildout(asMap(waves.get("fm_buildout"))),
                parseHenesysPopulation(asMap(waves.get("henesys_population"))),
                parseExpandFm(asMap(waves.get("expand_fm_market"))),
                parseHenesysSubAreas(asMap(waves.get("henesys_sub_areas"))),
                parseSpecialty(asMap(waves.get("specialty"))),
                parseLateArrivals(asMap(waves.get("late_arrivals"))),
                parseTraining(asMap(waves.get("training"))),
                new WaveTownPresence(toBool(asMap(waves.get("town_presence")).get("enabled"), true))
        );
    }

    private static WaveEssentials parseEssentials(Map<String, Object> m) {
        return new WaveEssentials(
                toBool(m.get("enabled"), true),
                toBool(m.get("casino_npcs"), true),
                toBool(m.get("tutorial"), true),
                parseHenesys(asMap(m.get("henesys")), 10, 0, 0, 0),
                str(m.get("fm_region"), "henesys"),
                parsePlatform(asMap(m.get("fm_entrance")), 5, 5, 5)
        );
    }

    private static WaveFmBuildout parseFmBuildout(Map<String, Object> m) {
        return new WaveFmBuildout(
                toBool(m.get("enabled"), true),
                str(m.get("fm_region"), "ludi"),
                parsePlatform(asMap(m.get("fm_entrance")), 5, 5, 5),
                parseMerchants(m.get("merchants")),
                toBool(m.get("gacha"), true)
        );
    }

    private static WaveHenesysPopulation parseHenesysPopulation(Map<String, Object> m) {
        return new WaveHenesysPopulation(
                toBool(m.get("enabled"), true),
                toBool(m.get("jq_pet_park"), true),
                parseHenesys(asMap(m.get("henesys")), 10, 10, 0, 5),
                toBool(m.get("fillers_henesys"), true),
                toBool(m.get("start_social_systems"), true)
        );
    }

    private static WaveExpandFm parseExpandFm(Map<String, Object> m) {
        return new WaveExpandFm(
                toBool(m.get("enabled"), true),
                str(m.get("fm_region"), "perion"),
                parsePlatform(asMap(m.get("fm_entrance")), 5, 5, 5),
                parseMerchants(m.get("merchants")),
                toBool(m.get("fillers_henesys_market"), true)
        );
    }

    private static WaveHenesysSubAreas parseHenesysSubAreas(Map<String, Object> m) {
        return new WaveHenesysSubAreas(
                toBool(m.get("enabled"), true),
                str(m.get("fm_region"), "elnath"),
                parseHenesys(asMap(m.get("henesys")), 10, 10, 10, 4),
                toBool(m.get("fillers_henesys_park"), true),
                toBool(m.get("fillers_potion_shop"), true),
                toBool(m.get("fillers_game_zone"), true),
                toBool(m.get("game_zone_hosts"), true)
        );
    }

    private static WaveSpecialty parseSpecialty(Map<String, Object> m) {
        return new WaveSpecialty(
                toBool(m.get("enabled"), true),
                toBool(m.get("blackjack"), true),
                toBool(m.get("drop_game"), true),
                toBool(m.get("drop_game_spectators"), true),
                toBool(m.get("social_pet_park"), true),
                toBool(m.get("convert_scroll_bots"), true)
        );
    }

    private static WaveLateArrivals parseLateArrivals(Map<String, Object> m) {
        return new WaveLateArrivals(
                toBool(m.get("enabled"), true),
                toBool(m.get("opq_lobby"), true),
                parseMerchants(m.get("merchants"))
        );
    }

    private static WaveTraining parseTraining(Map<String, Object> m) {
        Map<String, Object> warm = asMap(m.get("warm_nav"));
        WarmNav warmNav = new WarmNav(toInt(warm.get("map"), 100000000), toInt(warm.get("hops"), 1));
        List<TrainingCohort> cohorts = new ArrayList<>();
        Object node = m.get("cohorts");
        if (node instanceof List<?> list) {
            for (Object o : list) {
                if (!(o instanceof Map<?, ?> raw)) {
                    continue;
                }
                Map<String, Object> cm = (Map<String, Object>) raw;
                int mapId = toInt(cm.get("map"), -1);
                int count = toInt(cm.get("count"), 0);
                int lo = toInt(cm.get("level_lo"), 1);
                int hi = toInt(cm.get("level_hi"), lo);
                if (mapId > 0 && count > 0) {
                    cohorts.add(new TrainingCohort(mapId, count, lo, hi));
                }
            }
        }
        return new WaveTraining(toBool(m.get("enabled"), true), warmNav, List.copyOf(cohorts));
    }

    private static HenesysBatch parseHenesys(Map<String, Object> m, int dMain, int dMarket, int dPark, int dSocial) {
        return new HenesysBatch(
                toInt(m.get("main"), dMain),
                toInt(m.get("market"), dMarket),
                toInt(m.get("park"), dPark),
                toInt(m.get("social"), dSocial)
        );
    }

    private static PlatformBatch parsePlatform(Map<String, Object> m, int d1, int d2, int d5) {
        return new PlatformBatch(
                toInt(m.get("m1"), d1),
                toInt(m.get("m2"), d2),
                toInt(m.get("m5"), d5)
        );
    }

    @SuppressWarnings("unchecked")
    private static List<MerchantBatch> parseMerchants(Object node) {
        if (!(node instanceof List<?> list)) {
            return List.of();
        }
        List<MerchantBatch> out = new ArrayList<>();
        for (Object o : list) {
            if (!(o instanceof Map<?, ?> raw)) {
                continue;
            }
            Map<String, Object> m = (Map<String, Object>) raw;
            String platform = str(m.get("platform"), "m1");
            out.add(new MerchantBatch(
                    platform,
                    toInt(m.get("selling"), 0),
                    toInt(m.get("buying"), 0),
                    toInt(m.get("nx"), 0)
            ));
        }
        return List.copyOf(out);
    }

    /** Built-in defaults mirror the BeiDou-tuned hardcoded Wave 8 / town startup. */
    public static PopulationPlan defaults(String source) {
        List<TrainingCohort> cohorts = List.of(
                new TrainingCohort(104000000, 5, 1, 15),
                new TrainingCohort(100000000, 25, 10, 95),
                new TrainingCohort(103000000, 25, 10, 65),
                new TrainingCohort(102000000, 25, 10, 65),
                new TrainingCohort(101000000, 25, 10, 65),
                new TrainingCohort(105040300, 25, 25, 95),
                new TrainingCohort(105070001, 20, 40, 95),
                new TrainingCohort(200000000, 25, 30, 86),
                new TrainingCohort(220000000, 20, 25, 95),
                new TrainingCohort(220050300, 15, 70, 95),
                new TrainingCohort(211000000, 20, 50, 80),
                new TrainingCohort(211040300, 20, 60, 90),
                new TrainingCohort(100000000, 5, 1, 9),
                new TrainingCohort(103000000, 5, 1, 9),
                new TrainingCohort(102000000, 5, 1, 9),
                new TrainingCohort(101000000, 5, 1, 9)
        );
        return new PopulationPlan(
                1,
                1.0,
                source,
                new WaveEssentials(true, true, true, new HenesysBatch(10, 0, 0, 0), "henesys",
                        new PlatformBatch(5, 5, 5)),
                new WaveFmBuildout(true, "ludi", new PlatformBatch(5, 5, 5), List.of(
                        new MerchantBatch("m1", 2, 2, 1),
                        new MerchantBatch("m2", 2, 2, 0),
                        new MerchantBatch("m5", 2, 2, 0)
                ), true),
                new WaveHenesysPopulation(true, true, new HenesysBatch(10, 10, 0, 5), true, true),
                new WaveExpandFm(true, "perion", new PlatformBatch(5, 5, 5), List.of(
                        new MerchantBatch("m1", 3, 3, 0),
                        new MerchantBatch("m2", 2, 2, 1),
                        new MerchantBatch("m5", 3, 3, 1)
                ), true),
                new WaveHenesysSubAreas(true, "elnath", new HenesysBatch(10, 10, 10, 4),
                        true, true, true, true),
                new WaveSpecialty(true, true, true, true, true, true),
                new WaveLateArrivals(true, true, List.of(
                        new MerchantBatch("m1", 2, 2, 0),
                        new MerchantBatch("m2", 2, 2, 1),
                        new MerchantBatch("m5", 2, 2, 1)
                )),
                new WaveTraining(true, new WarmNav(100000000, 1), cohorts),
                new WaveTownPresence(true)
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        if (o instanceof Map<?, ?> m) {
            return (Map<String, Object>) m;
        }
        return Collections.emptyMap();
    }

    private static String str(Object o, String def) {
        if (o == null) {
            return def;
        }
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? def : s;
    }

    private static boolean toBool(Object o, boolean def) {
        if (o == null) {
            return def;
        }
        if (o instanceof Boolean b) {
            return b;
        }
        String s = String.valueOf(o).trim().toLowerCase();
        if (s.equals("true") || s.equals("yes") || s.equals("1")) {
            return true;
        }
        if (s.equals("false") || s.equals("no") || s.equals("0")) {
            return false;
        }
        return def;
    }

    private static int toInt(Object o, int def) {
        if (o == null) {
            return def;
        }
        if (o instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static double toDouble(Object o, double def) {
        if (o == null) {
            return def;
        }
        if (o instanceof Number n) {
            return n.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
