package soloMapling.Environment;

import org.gms.client.Character;
import org.gms.client.Job;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotGeneration;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands;
import soloMapling.ArtificialPlayer.BotDecoratorSystem.BotDecorate;
import soloMapling.ArtificialPlayer.BotDecoratorSystem.BotDecorationQueue;
import soloMapling.ArtificialPlayer.BotDecoratorSystem.BotEquipChecker;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotMapEntryResponder;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTypeManager;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.ArtificialPlayer.BotGrindSystem.BotSpotPicker;
import soloMapling.ArtificialPlayer.BotTownSystem.TownPresenceConfig;
import soloMapling.ArtificialPlayer.BotTownSystem.TownPresenceSampler;
import soloMapling.ArtificialPlayer.BotTypes.Blackjack.BlackjackDealerBot;
import soloMapling.ArtificialPlayer.ConversationManager;
import soloMapling.ArtificialPlayer.SocialHotPotatoManager;
import soloMapling.Casino.CasinoChipConfig;
import soloMapling.server.ExecutorServiceManager;
import soloMapling.server.NpcSpawner;
import org.gms.constants.id.MapId;
import org.gms.constants.id.NpcId;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static soloMapling.ArtificialPlayer.BotCustomization.EquipBot;
import static soloMapling.ArtificialPlayer.BotCustomization.getRandomChairId;
import static soloMapling.ArtificialPlayer.BotGeneration.createBotPollReadiness;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage.checkIfRespondant;
import static soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage.getBotById;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botFaceTowardsPoint;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botSitChair;
import static soloMapling.ArtificialPlayer.BotTypeManager.setAndStartBots;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.DebugUtilities.fmt;
import static soloMapling.Environment.PlatformPlacement.createBotWithRetry;
import static soloMapling.Environment.PlatformPlacement.getAllCharsOnMap;
import static soloMapling.Environment.PlatformPlacement.getMainPlatformIds;
import static soloMapling.Environment.PlatformPlacement.spawnBotsOnMapOnPlatform;
import static soloMapling.Environment.PlatformPlacement.spawnBotsOnMapOnPlatformInRadius;
import static soloMapling.Environment.PlatformPlacement.spawnFillerBots;
import static soloMapling.Environment.PlatformPlacement.spawnFillerBotsLockedY;
import static soloMapling.Environment.PlatformSpawner.findUnoccupiedPoint;
import static soloMapling.Environment.PlatformSpawner.findUnoccupiedPoints;
import static soloMapling.FreeMarket.ArtificialFreeMarket.populateFreeMarketRegion;
import static soloMapling.server.SoloMaplingUtilities.getMapleMapById;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EnvironmentManager {

    private static final String BASE_PATH = "src/main/java/soloMapling/ArtificialPlayer/BotMovementSystem/movementDataPackets";

    /**
     * Tolerance for Y-coordinate matching when determining if a character is on a platform.
     * Characters within this vertical distance of the platform are considered "on" it.
     */
    private static final int Y_TOLERANCE = 10;

    /**
     * Tolerance for X-coordinate proximity when checking if a character is near platform bounds.
     * Allows some buffer beyond the recorded min/max X values.
     */
    private static final int X_TOLERANCE = 20;


    private static final Random random = new Random();
    private static final int FM_ENTRANCE = 910000000;
    private static final int HENESYS = 100000000;
    private static final int HENESYS_MARKET = 100000100;
    private static final int HENESYS_PARK = 100000200;
    private static final int HENESYS_POTION_SHOP = 100000102;
    private static final int HENESYS_GAME_ZONE = 100000203;
    private static final int HENESYS_PET_PARK = 100000202;
    private static final int MAPLE_ISLAND_TUTORIAL = 10000;
    private static final int OPQ_LOBBY = 200080101;

    /** Warm channel-1 MapManager for hubs bots stampede first (avoids cold-load + DB under lock). */
    private static void preloadStartupMaps() {
        int[] hubs = {
                FM_ENTRANCE, HENESYS, HENESYS_MARKET, HENESYS_PARK,
                HENESYS_POTION_SHOP, HENESYS_GAME_ZONE, HENESYS_PET_PARK,
                MAPLE_ISLAND_TUTORIAL, OPQ_LOBBY,
                // FM rooms commonly used by populateFreeMarketRegion
                910000001, 910000002, 910000003, 910000004, 910000005,
                910000006, 910000007, 910000008, 910000009, 910000010,
                910000011, 910000012, 910000013, 910000014, 910000015,
                910000016, 910000017, 910000018, 910000019, 910000020,
                910000021, 910000022
        };
        long t0 = System.currentTimeMillis();
        int ok = 0;
        for (int mapId : hubs) {
            try {
                if (getMapleMapById(mapId) != null) {
                    ok++;
                }
            } catch (Throwable t) {
                debugprint(fmt("preload map {} failed: {}", mapId, t.toString()));
            }
        }
        System.out.println(String.format(
                "[EnvironmentManager] Preloaded %d/%d hub maps in %.1fs",
                ok, hubs.length, (System.currentTimeMillis() - t0) / 1000.0));
    }

    public static void environmentLoadStartup() {
        // EquipMetadataCache + DesirableEquipList are server data, loaded during
        // Server.init() alongside the other WZ-derived data - guaranteed ready
        // before any player can trigger this.
        long startupStart = System.currentTimeMillis();

        // Wake bots instantly (movement + macro brain) whenever a real player shares their map, in both
        // directions - a player entering a populated map, or a bot returning to the player's map.
        BotMapEntryResponder.register();

        // Warm MapManager for hub maps before parallel waves stampede getMap+DB.
        preloadStartupMaps();

        // Counts / toggles from EnvironmentPopulation.yaml (see EnvironmentPopulationConfig).
        EnvironmentPopulationConfig.PopulationPlan pop = EnvironmentPopulationConfig.plan();
        System.out.println(String.format(
                "[EnvironmentManager] Population plan source=%s scale=%.2f trainingCohorts=%d (scaled total=%d)",
                pop.loadedFrom(), pop.scale(), pop.training().cohorts().size(), pop.trainingCohortTotal()));

        var w1 = pop.essentials();
        if (w1.enabled()) {
            List<Runnable> tasks = new ArrayList<>();
            if (w1.casinoNpcs()) {
                tasks.add(() -> spawnCasinoNpcs());
            }
            if (w1.tutorial()) {
                tasks.add(() -> spawnTutorialBot());
            }
            var h = w1.henesys();
            tasks.add(() -> spawnHenesysBotsBatch(pop.scaled(h.main()), pop.scaled(h.market()),
                    pop.scaled(h.park()), pop.scaled(h.social())));
            if (w1.fmRegion() != null && !w1.fmRegion().isBlank()) {
                tasks.add(() -> populateFreeMarketRegion(w1.fmRegion()));
            }
            var fm = w1.fmEntrance();
            tasks.add(() -> spawnFMEntranceBotsBatch(pop.scaled(fm.m1()), pop.scaled(fm.m2()), pop.scaled(fm.m5())));
            runWave(1, "Essentials", tasks);
        }

        var w2 = pop.fmBuildout();
        if (w2.enabled()) {
            List<Runnable> tasks = new ArrayList<>();
            if (w2.fmRegion() != null && !w2.fmRegion().isBlank()) {
                tasks.add(() -> populateFreeMarketRegion(w2.fmRegion()));
            }
            var fm = w2.fmEntrance();
            tasks.add(() -> spawnFMEntranceBotsBatch(pop.scaled(fm.m1()), pop.scaled(fm.m2()), pop.scaled(fm.m5())));
            for (var m : w2.merchants()) {
                tasks.add(() -> spawnMerchBotsBatch(m.platform(),
                        pop.scaled(m.selling()), pop.scaled(m.buying()), pop.scaled(m.nx())));
            }
            if (w2.gacha()) {
                tasks.add(() -> spawnGachaBotsHenesys());
            }
            runWave(2, "FM buildout", tasks);
        }

        var w3 = pop.henesysPopulation();
        if (w3.enabled()) {
            List<Runnable> tasks = new ArrayList<>();
            if (w3.jqPetPark()) {
                tasks.add(() -> spawnJQBotsPetPark());
            }
            var h = w3.henesys();
            tasks.add(() -> spawnHenesysBotsBatch(pop.scaled(h.main()), pop.scaled(h.market()),
                    pop.scaled(h.park()), pop.scaled(h.social())));
            if (w3.fillersHenesys()) {
                tasks.add(() -> spawnFillerBotsHenesys());
            }
            runWave(3, "Henesys population", tasks);
        }
        if (w3.startSocialSystems()) {
            SocialHotPotatoManager.getInstance().start();
            ConversationManager.getInstance().start();
        }

        var w4 = pop.expandFmMarket();
        if (w4.enabled()) {
            List<Runnable> tasks = new ArrayList<>();
            if (w4.fmRegion() != null && !w4.fmRegion().isBlank()) {
                tasks.add(() -> populateFreeMarketRegion(w4.fmRegion()));
            }
            var fm = w4.fmEntrance();
            tasks.add(() -> spawnFMEntranceBotsBatch(pop.scaled(fm.m1()), pop.scaled(fm.m2()), pop.scaled(fm.m5())));
            for (var m : w4.merchants()) {
                tasks.add(() -> spawnMerchBotsBatch(m.platform(),
                        pop.scaled(m.selling()), pop.scaled(m.buying()), pop.scaled(m.nx())));
            }
            if (w4.fillersHenesysMarket()) {
                tasks.add(() -> spawnFillerBotsHenesysMarket());
            }
            runWave(4, "Expand FM + Henesys Market", tasks);
        }

        var w5 = pop.henesysSubAreas();
        if (w5.enabled()) {
            List<Runnable> tasks = new ArrayList<>();
            if (w5.fmRegion() != null && !w5.fmRegion().isBlank()) {
                tasks.add(() -> populateFreeMarketRegion(w5.fmRegion()));
            }
            var h = w5.henesys();
            tasks.add(() -> spawnHenesysBotsBatch(pop.scaled(h.main()), pop.scaled(h.market()),
                    pop.scaled(h.park()), pop.scaled(h.social())));
            if (w5.fillersHenesysPark()) {
                tasks.add(() -> spawnFillerBotsHenesysPark());
            }
            if (w5.fillersPotionShop()) {
                tasks.add(() -> spawnFillerBotsPotionShop());
            }
            if (w5.fillersGameZone()) {
                tasks.add(() -> spawnFillerBotsGameZone());
            }
            if (w5.gameZoneHosts()) {
                tasks.add(() -> spawnGameZoneHostBots());
            }
            runWave(5, "Henesys sub-areas", tasks);
        }

        var w6 = pop.specialty();
        if (w6.enabled()) {
            List<Runnable> tasks = new ArrayList<>();
            if (w6.blackjack()) {
                tasks.add(() -> spawnBlackjackTables());
            }
            if (w6.dropGame()) {
                tasks.add(() -> spawnDropGameBotPotionShop());
            }
            if (w6.dropGameSpectators()) {
                tasks.add(() -> spawnDropGameSpectatorsPotionShop());
            }
            if (w6.socialPetPark()) {
                tasks.add(() -> spawnSocialBotsPetPark());
            }
            if (w6.convertScrollBots()) {
                tasks.add(() -> convertRandomFillersToScrollBots());
            }
            runWave(6, "Specialty areas", tasks);
        }

        var w7 = pop.lateArrivals();
        if (w7.enabled()) {
            List<Runnable> tasks = new ArrayList<>();
            if (w7.opqLobby()) {
                tasks.add(() -> spawnOPQBotsInLobby());
            }
            for (var m : w7.merchants()) {
                tasks.add(() -> spawnMerchBotsBatch(m.platform(),
                        pop.scaled(m.selling()), pop.scaled(m.buying()), pop.scaled(m.nx())));
            }
            runWave(7, "Late arrivals", tasks);
        }

        // Training grinders: cohorts from YAML. Each cohort spawns at a hub map; bots discover nearby
        // level-appropriate field maps via TrainingMapFinder and fan out. Deep hubs (Ant Tunnel Park,
        // Path of Time, Sharp Cliff) populate far dungeons the town hop-radius cannot reach.
        var w8 = pop.training();
        if (w8.enabled()) {
            List<Runnable> tasks = new ArrayList<>();
            var warm = w8.warmNav();
            if (warm != null && warm.hops() > 0 && warm.mapId() > 0) {
                tasks.add(() -> GCMovement.mapsWithinHops(warm.mapId(), warm.hops()));
            }
            for (var cohort : w8.cohorts()) {
                int n = pop.scaled(cohort.count());
                if (n <= 0) {
                    continue;
                }
                int mapId = cohort.mapId();
                int lo = cohort.levelLo();
                int hi = cohort.levelHi();
                tasks.add(() -> spawnTrainingBotsAt(mapId, n, lo, hi));
            }
            runWave(8, "Training bots", tasks);
        }

        // Ambient town population from EnvironmentPopulation.yaml waves.town_presence.towns.
        var w9 = pop.townPresence();
        if (w9.enabled()) {
            List<Runnable> townTasks = new ArrayList<>();
            for (TownPresenceConfig.TownEntry town : TownPresenceConfig.towns()) {
                townTasks.add(() -> spawnTown(town));
            }
            runWave(9, "Town presence", townTasks);
        }

        BotDecorationQueue.start();
        BotEquipChecker.start();

        double totalSeconds = (System.currentTimeMillis() - startupStart) / 1000.0;
        System.out.println(String.format(
                "[EnvironmentManager] === All bots initialized: %d bots in %.1fs ===",
                BotGeneration.getBotsCreatedCount(), totalSeconds));
    }

    // Spawn one town's training-bot cohort: n job-coherent roaming grinders at the town's spawn
    // portal, each a random non-pirate explorer class (1..4) with a coherent level (lo..hi) + job +
    // gear set together by the decorator. They self-discover nearby level-appropriate field maps and
    // fan out. A sub-level-10 band spawns Beginners (job 0): the decorator gives them a sword and
    // they fight with a basic skill-0 swing, so low bands are fine (class 1..4 is moot - all sword).
    private static int spawnTrainingBotsAt(int townMapId, int n, int loLevel, int hiLevel) {
        MapleMap map = getMapleMapById(townMapId);
        if (map == null || map.getPortal(0) == null) {
            debugprint(fmt("TrainingBots: no map / spawn portal for {}", townMapId));
            return 0;
        }
        Point sp = map.getPortal(0).getPosition();
        int spawned = spawnScatteredTrainingBots(map, sp, n, loLevel, hiLevel).size();
        debugprint(fmt("TrainingBots: {} spawned on map {} (lv {}..{})", spawned, townMapId, loLevel, hiLevel));
        return spawned;
    }

    // Spawn n training bots scattered across the map's reachable platforms - an organic ground spot per
    // bot picked by BotSpotPicker (every walkable ledge in the X band, vertical stacking included),
    // instead of stacking everyone on one point. `anchor` seeds the reachability filter (pass the spawn
    // portal, or a GM's position for a dev dry-run) and is the per-bot fallback when the nav graph yields
    // no eligible ledge (empty/unbaked). Returns the created ids, already typed + started as TRAINING_BOTs.
    public static List<Integer> spawnScatteredTrainingBots(MapleMap map, Point anchor, int n, int loLevel, int hiLevel) {
        List<Point> spots = BotSpotPicker.pickGroundSpots(map, anchor.x, anchor.y, n);
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Point spawnAt = i < spots.size() ? spots.get(i) : anchor;
            int baseClass = BotDecorate.rollBaseClass(); // weighted 1..4 (Thief-heavy, Bowman-rare; Pirate excluded)
            try {
                int botId = BotGeneration.createBot(spawnAt, map, baseClass, loLevel, hiLevel);
                if (botId > 0) {
                    ids.add(botId);
                }
            } catch (Exception e) {
                debugprint(fmt("TrainingBots: create failed on {} ({})", map.getId(), e.getMessage()));
            }
        }
        setAndStartBots(ids, BotTypeManager.BotType.TRAINING_BOT);
        return ids;
    }

    // Ambient town population: for each town in EnvironmentPopulation.yaml (town_presence.towns), scatter
    // its per-map stationed SocialBot headcounts plus its town-level roaming TownWandererBot count.
    public static void spawnTownPresence() {
        for (TownPresenceConfig.TownEntry town : TownPresenceConfig.towns()) {
            spawnTown(town);
        }
    }

    // Spawn one town's ambient population: its per-map stationed SocialBots plus its roaming wanderers.
    public static void spawnTown(TownPresenceConfig.TownEntry town) {
        for (TownPresenceConfig.MapShare share : town.maps()) {
            int n = spawnSocialCohort(share.mapId(), share.count(), town.levelLo(), town.levelHi());
            debugprint(fmt("TownPresence: {} social bots on map {} ({}, lv {}..{})",
                    n, share.mapId(), town.name(), town.levelLo(), town.levelHi()));
        }
        if (town.wanderers() > 0) {
            int w = spawnTownWanderers(town.mainMapId(), town.wanderers(), town.levelLo(), town.levelHi());
            debugprint(fmt("TownPresence: {} wanderers on map {} ({})", w, town.mainMapId(), town.name()));
        }
    }

    // Spawn n stationed ambient SocialBots on a map at anchor-weighted ground spots (near NPCs/shops, on
    // the main ground band, with a thin straggler tail - see TownPresenceSampler). Returns how many created.
    public static int spawnSocialCohort(int mapId, int n, int loLevel, int hiLevel) {
        return spawnTownCohort(mapId, n, loLevel, hiLevel, BotTypeManager.BotType.SOCIAL_BOT);
    }

    // Spawn n roaming TownWandererBots seeded at anchor-weighted spots on a town's main map; they fan out
    // and drift its map family on their own. The generic (non-Henesys) counterpart to HenesysBot.
    public static int spawnTownWanderers(int mapId, int n, int loLevel, int hiLevel) {
        return spawnTownCohort(mapId, n, loLevel, hiLevel, BotTypeManager.BotType.TOWN_WANDERER_BOT);
    }

    // Shared town-cohort spawn: place n bots at anchor-weighted ground spots on the map (mirrors
    // spawnScatteredTrainingBots but with the weighted sampler), typed as `type`.
    private static int spawnTownCohort(int mapId, int n, int loLevel, int hiLevel, BotTypeManager.BotType type) {
        MapleMap map = getMapleMapById(mapId);
        if (map == null || map.getPortal(0) == null) {
            debugprint(fmt("TownPresence: no map / spawn portal for {}", mapId));
            return 0;
        }
        Point anchor = map.getPortal(0).getPosition();
        List<Point> spots = TownPresenceSampler.sample(map, anchor, n, TownPresenceConfig.overridesFor(mapId));
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Point spawnAt = i < spots.size() ? spots.get(i) : anchor;
            int baseClass = BotDecorate.rollBaseClass(); // weighted 1..4 (Pirate excluded), gear/job set together
            try {
                int botId = BotGeneration.createBot(spawnAt, map, baseClass, loLevel, hiLevel);
                if (botId > 0) {
                    ids.add(botId);
                }
            } catch (Exception e) {
                debugprint(fmt("TownPresence: create failed on {} ({})", mapId, e.getMessage()));
            }
        }
        setAndStartBots(ids, type);
        return ids.size();
    }

    /**
     * Run one startup wave: all tasks in parallel, blocking until the wave
     * completes. Logs start/end with elapsed time and bots spawned. FM room
     * population is fire-and-forget internally, so its bots may be attributed
     * to a later wave's count.
     */
    private static void runWave(int number, String name, List<Runnable> tasks) {
        System.out.println(String.format(
                "[EnvironmentManager] === Wave %d (%s) starting ===", number, name));
        long start = System.currentTimeMillis();
        int botsBefore = BotGeneration.getBotsCreatedCount();

        runPhase(tasks);

        double seconds = (System.currentTimeMillis() - start) / 1000.0;
        int botsSpawned = BotGeneration.getBotsCreatedCount() - botsBefore;
        System.out.println(String.format(
                "[EnvironmentManager] === Wave %d (%s) complete - %d bots spawned in %.1fs ===",
                number, name, botsSpawned, seconds));
    }

    private static void spawnFMEntranceBotsBatch(int m1Count, int m2Count, int m5Count) {
        if (m1Count > 0) {
            List<Integer> bots = spawnBotsOnMapOnPlatform(m1Count, FM_ENTRANCE, "m1");
            setAndStartBots(bots, BotTypeManager.BotType.FM_BOT);
        }
        if (m2Count > 0) {
            List<Integer> bots = spawnBotsOnMapOnPlatform(m2Count, FM_ENTRANCE, "m2");
            setAndStartBots(bots, BotTypeManager.BotType.FM_BOT);
        }
        if (m5Count > 0) {
            List<Integer> bots = spawnBotsOnMapOnPlatform(m5Count, FM_ENTRANCE, "m5");
            setAndStartBots(bots, BotTypeManager.BotType.FM_BOT);
        }
    }

    private static void spawnMerchBotsBatch(String platform, int selling, int buying, int nx) {
        if (selling > 0) {
            List<Integer> bots = spawnBotsOnMapOnPlatform(selling, FM_ENTRANCE, platform);
            setAndStartBots(bots, BotTypeManager.BotType.SELLING_MERCHANT_BOT);
        }
        if (buying > 0) {
            List<Integer> bots = spawnBotsOnMapOnPlatform(buying, FM_ENTRANCE, platform);
            setAndStartBots(bots, BotTypeManager.BotType.BUYING_MERCHANT_BOT);
        }
        if (nx > 0) {
            List<Integer> bots = spawnBotsOnMapOnPlatform(nx, FM_ENTRANCE, platform);
            setAndStartBots(bots, BotTypeManager.BotType.NX_MERCHANT_BOT);
        }
    }

    private static void spawnHenesysBotsBatch(int mainCount, int marketCount, int parkCount, int socialCount) {
        if (mainCount > 0) {
            List<Integer> bots = spawnBotsOnMapOnPlatform(mainCount, HENESYS, "m1");
            setAndStartBots(bots, BotTypeManager.BotType.HENESYS_BOT);
        }
        if (marketCount > 0) {
            List<Integer> bots = spawnBotsOnMapOnPlatform(marketCount, HENESYS_MARKET, "m1");
            setAndStartBots(bots, BotTypeManager.BotType.HENESYS_BOT);
        }
        if (parkCount > 0) {
            List<Integer> bots = spawnBotsOnMapOnPlatform(parkCount, HENESYS_PARK, "m1");
            setAndStartBots(bots, BotTypeManager.BotType.HENESYS_BOT);
        }
        if (socialCount > 0) {
            int perSpot = Math.max(1, socialCount / 3);
            List<Integer> s1 = spawnBotsOnMapOnPlatform(perSpot, HENESYS, "m4_social");
            List<Integer> s2 = spawnBotsOnMapOnPlatform(perSpot, HENESYS, "m5_social");
            List<Integer> s3 = spawnBotsOnMapOnPlatform(perSpot, HENESYS, "m6_social");
        }
    }

    public static void spawnCasinoNpcs() {
        int casinoMap = 100000203;
        NpcSpawner.spawnNpc(CasinoChipConfig.CASINO_NPC_ID, casinoMap, 1321, 214);
        NpcSpawner.spawnNpc(NpcId.RPS_ADMIN, casinoMap, 899, 275);
    }

    private static void runPhase(List<Runnable> tasks) {
        // Virtual threads: wave tasks spend most of their time blocked (spawn
        // choreography sleeps, readiness latches), so they shouldn't occupy
        // the fixed thread pool.
        CompletableFuture<?>[] futures = tasks.stream()
                .map(task -> CompletableFuture.runAsync(task, ExecutorServiceManager.getVirtualThreadExecutorService()))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
    }

    /*
    spawn 50 bots in fm
     */
    public static void spawnBotsInFMEntrance() {
        int fm_entrance = 910000000;
        List<Integer> bots1 = spawnBotsOnMapOnPlatform(15, fm_entrance, "m1"); // Bottom row
        debugprint("bots1", bots1);
        setAndStartBots(bots1, BotTypeManager.BotType.FM_BOT);
        List<Integer> bots3 = spawnBotsOnMapOnPlatform(15, fm_entrance, "m5"); // Left side
        debugprint("bots3", bots3);
        setAndStartBots(bots3, BotTypeManager.BotType.FM_BOT);
        List<Integer> bots2 = spawnBotsOnMapOnPlatform(15, fm_entrance, "m2"); // Second row
        debugprint("bots2", bots2);
        setAndStartBots(bots2, BotTypeManager.BotType.FM_BOT);
    }

    public static void spawnMerchBotsInFMEntrance() {
        int fm_entrance = 910000000;

        // m1 - Bottom row: 7 selling, 7 buying, 1 nx
        List<Integer> m1Selling = spawnBotsOnMapOnPlatform(7, fm_entrance, "m1");
        setAndStartBots(m1Selling, BotTypeManager.BotType.SELLING_MERCHANT_BOT);
        List<Integer> m1Buying = spawnBotsOnMapOnPlatform(7, fm_entrance, "m1");
        setAndStartBots(m1Buying, BotTypeManager.BotType.BUYING_MERCHANT_BOT);
        List<Integer> m1NX = spawnBotsOnMapOnPlatform(1, fm_entrance, "m1");
        setAndStartBots(m1NX, BotTypeManager.BotType.NX_MERCHANT_BOT);

        // m5 - Left side: 7 selling, 7 buying, 2 nx
        List<Integer> m5Selling = spawnBotsOnMapOnPlatform(7, fm_entrance, "m5");
        setAndStartBots(m5Selling, BotTypeManager.BotType.SELLING_MERCHANT_BOT);
        List<Integer> m5Buying = spawnBotsOnMapOnPlatform(7, fm_entrance, "m5");
        setAndStartBots(m5Buying, BotTypeManager.BotType.BUYING_MERCHANT_BOT);
        List<Integer> m5NX = spawnBotsOnMapOnPlatform(2, fm_entrance, "m5");
        setAndStartBots(m5NX, BotTypeManager.BotType.NX_MERCHANT_BOT);

        // m2 - Second row: 6 selling, 6 buying, 2 nx
        List<Integer> m2Selling = spawnBotsOnMapOnPlatform(6, fm_entrance, "m2");
        setAndStartBots(m2Selling, BotTypeManager.BotType.SELLING_MERCHANT_BOT);
        List<Integer> m2Buying = spawnBotsOnMapOnPlatform(6, fm_entrance, "m2");
        setAndStartBots(m2Buying, BotTypeManager.BotType.BUYING_MERCHANT_BOT);
        List<Integer> m2NX = spawnBotsOnMapOnPlatform(2, fm_entrance, "m2");
        setAndStartBots(m2NX, BotTypeManager.BotType.NX_MERCHANT_BOT);
    }

    public static void spawnHenesysBots() {
        int henesys_map = 100000000;
        List<Integer> bots1 = spawnBotsOnMapOnPlatform(30, henesys_map, "m1");
        setAndStartBots(bots1, BotTypeManager.BotType.HENESYS_BOT);

        List<Integer> bots2 = spawnBotsOnMapOnPlatform(10, 100000100, "m1");
        List<Integer> bots3 = spawnBotsOnMapOnPlatform(10, 100000100, "m2");
        setAndStartBots(bots2, BotTypeManager.BotType.HENESYS_BOT); // hene market
        setAndStartBots(bots3, BotTypeManager.BotType.HENESYS_BOT);

        List<Integer> bots4 = spawnBotsOnMapOnPlatform(10, 100000200, "m1");
        setAndStartBots(bots4, BotTypeManager.BotType.HENESYS_BOT); // hene park

        List<Integer> bots5 = spawnBotsOnMapOnPlatform(3, henesys_map, "m4_social"); // nana fairy area
        List<Integer> bots6 = spawnBotsOnMapOnPlatform(3, henesys_map, "m5_social");
        List<Integer> bots7 = spawnBotsOnMapOnPlatform(3, henesys_map, "m6_social");
    }

    public static void spawnGachaBotsHenesys() {
        List<Integer> bots2 = spawnBotsOnMapOnPlatformInRadius(3, 100000100, "m1",  new Point(366,154), 250);
        setAndStartBots(bots2, BotTypeManager.BotType.GACHA_BOT); // hene market
    }

    private static int randomizeCount(int base) {
        return Math.max(1, base + random.nextInt(3) - 1); // -1, 0, or +1
    }

    public static void spawnFillerBotsHenesys() {
        int map = HENESYS;
        debugprint("Spawning filler bots in Henesys...");

        List<Integer> allIds = new ArrayList<>();
        allIds.addAll(spawnFillerBots(randomizeCount(5), map, new Point(-696, 274), new Point(-10, 274)));       // left side
        allIds.addAll(spawnFillerBots(randomizeCount(2), map, new Point(-144, 218), new Point(36, 218)));         // left side taxi barrels
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(-286, 101), new Point(7, 94)));           // left side top plat
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(248, 274), new Point(573, 274)));         // left tree under
        allIds.addAll(spawnFillerBots(randomizeCount(6), map, new Point(2596, 334), new Point(3347, 334)));       // near market portal
        allIds.addAll(spawnFillerBots(randomizeCount(6), map, new Point(3393, 124), new Point(4247, 124)));       // near park portal
        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(3831, 454), new Point(4382, 454)));       // under park portal
        allIds.addAll(spawnFillerBots(randomizeCount(8), map, new Point(4832, 454), new Point(5762, 454)));       // near maya house
        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(5547, -176), new Point(6232, -176)));     // near sleepy portal
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(4732, -116), new Point(5424, -116)));     // near sleepy portal 2

        setAndStartBots(allIds, BotTypeManager.BotType.SOCIAL_BOT);
        debugprint("Henesys filler bots complete.");
    }

    public static void spawnFillerBotsHenesysMarket() {
        int map = HENESYS_MARKET;
        debugprint("Spawning filler bots in Henesys Market...");

        List<Integer> allIds = new ArrayList<>();
        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(-548, 154), new Point(568, 154)));        // left side
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(592, 154), new Point(1148, 154)));        // left side 2
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(-105, 154), new Point(568, 154)));        // left side 3
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(1340, 214), new Point(2442, 214)));       // near weapon store
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(1369, -56), new Point(2546, -56)));       // above weapon store
        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(2689, -116), new Point(3636, -116)));     // near potion store
        allIds.addAll(spawnFillerBots(randomizeCount(5), map, new Point(2744, 94), new Point(3494, 94)));         // below potion store
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(3760, 94), new Point(5100, 94)));         // right side
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(3852, -176), new Point(4427, -176)));     // top right side

        setAndStartBots(allIds, BotTypeManager.BotType.SOCIAL_BOT);
        debugprint("Henesys Market filler bots complete.");
    }

    public static void spawnFillerBotsHenesysPark() {
        int map = HENESYS_PARK;
        debugprint("Spawning filler bots in Henesys Park...");

        List<Integer> allIds = new ArrayList<>();
        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(-53, 454), new Point(597, 454)));         // left side
        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(982, 424), new Point(1288, 424)));        // near storage keeper
        allIds.addAll(spawnFillerBots(randomizeCount(5), map, new Point(984, 574), new Point(1606, 574)));        // HPQ bottom
        allIds.addAll(spawnFillerBots(randomizeCount(2), map, new Point(1563, 304), new Point(1769, 304)));       // JQ platform
        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(1915, 574), new Point(2909, 574)));       // fountain bottom
        allIds.addAll(spawnFillerBots(randomizeCount(1), map, new Point(2019, 364), new Point(2118, 364)));       // fountain left tomb
        allIds.addAll(spawnFillerBots(randomizeCount(2), map, new Point(2198, 424), new Point(2471, 424)));       // fountain top
        allIds.addAll(spawnFillerBots(randomizeCount(1), map, new Point(2549, 364), new Point(2663, 364)));       // right tomb
        allIds.addAll(spawnFillerBots(randomizeCount(2), map, new Point(3233, 334), new Point(3607, 334)));       // right statue TP
        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(3585, 694), new Point(4390, 694)));       // outside bowman portal

        setAndStartBots(allIds, BotTypeManager.BotType.SOCIAL_BOT);
        debugprint("Henesys Park filler bots complete.");
    }

    public static void spawnFillerBotsGameZone() {
        int map = HENESYS_GAME_ZONE;
        debugprint("Spawning filler bots in Henesys Game Zone...");

        List<Integer> allIds = new ArrayList<>();
//        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(-830, 274), new Point(-62, 274)));        // left side // interferes with table 1
        allIds.addAll(spawnFillerBots(randomizeCount(4), map, new Point(1027, 394), new Point(1483, 394)));       // right side lower
        allIds.addAll(spawnFillerBots(randomizeCount(6), map, new Point(-83, 274), new Point(340, 274)));         // center
        allIds.addAll(spawnFillerBots(randomizeCount(3), map, new Point(263, 64), new Point(929, 64)));           // top platform

        setAndStartBots(allIds, BotTypeManager.BotType.SOCIAL_BOT);
        debugprint("Henesys Game Zone filler bots complete.");
    }

    public static void spawnFillerBotsPotionShop() {
        int map = HENESYS_POTION_SHOP;
        debugprint("Spawning filler bots in Henesys Potion Shop...");

        List<Integer> allIds = new ArrayList<>();
        allIds.addAll(spawnFillerBotsLockedY(randomizeCount(3), map, new Point(-370, 182), new Point(175, 182)));   // bottom left
        allIds.addAll(spawnFillerBotsLockedY(randomizeCount(2), map, new Point(193, 182), new Point(370, 182)));    // bottom right
        allIds.addAll(spawnFillerBotsLockedY(randomizeCount(3), map, new Point(-112, -127), new Point(245, -127))); // top bar

        setAndStartBots(allIds, BotTypeManager.BotType.SOCIAL_BOT);
        debugprint("Henesys Potion Shop filler bots complete.");
    }

    public static void spawnDropGameBotPotionShop() {
        debugprint("Spawning Drop Game Bot in Henesys Potion Shop...");
        Point spawn = new Point(45, 182);
        ExecutorServiceManager.runAsync(() -> {
            Character fakechar = createBotWithRetry(spawn, HENESYS_POTION_SHOP, 5);
            if (fakechar != null) {
                ExecutorServiceManager.getScheduledExecutorService().schedule(() -> {
                    setAndStartBots(List.of(fakechar.getId()), BotTypeManager.BotType.DROP_GAME_BOT);
                    debugprint("Drop Game Bot started in Henesys Potion Shop.");
                }, 5, TimeUnit.SECONDS);
            } else {
                debugprint("Failed to spawn Drop Game Bot in Henesys Potion Shop.");
            }
        });
    }

    private static final int HHG1 = 104040000; // Henesys Hunting Ground 1

    // Weapon ids by type; any level works since the bot equip bypasses requirements. The
    // weapon TYPE is what matters - it drives the attack route, projectile, and (for
    // warriors) the sword/spear skill form.
    private static final int W_SWORD = 1302012, W_SPEAR = 1432007, W_WAND = 1372015,
            W_BOW = 1452009, W_CROSSBOW = 1462009, W_CLAW = 1472026, W_DAGGER = 1332018;

    // One fixed spot per class line, with the job to use at tier 1/2/3/4. Forcing the job +
    // weapon lets the attack resolver pick the tier-appropriate skill for in-game testing.
    private record TierSlot(Point pos, int weaponId, int t1, int t2, int t3, int t4) {
        int jobForTier(int tier) {
            return switch (tier) { case 2 -> t2; case 3 -> t3; case 4 -> t4; default -> t1; };
        }
    }

    private static final List<TierSlot> ATTACK_TEST_SLOTS = List.of(
            new TierSlot(new Point(240, 215),  W_SWORD,    100, 110, 111, 112), // Warrior: Fighter->Crusader->Hero
            new TierSlot(new Point(1022, 215), W_SPEAR,    100, 130, 131, 132), // Warrior: Spearman->DrK->DarkKnight (spear forms)
            new TierSlot(new Point(340, -85),  W_WAND,     200, 210, 211, 212), // Mage: F/P Wizard->Mage->ArchMage
            new TierSlot(new Point(912, -85),  W_WAND,     200, 220, 221, 222), // Mage: I/L Wizard->Mage->ArchMage
            new TierSlot(new Point(631, 215),  W_WAND,     200, 230, 231, 232), // Mage: Cleric->Priest->Bishop (holy); between the two y=215 warriors
            new TierSlot(new Point(900, -325), W_BOW,      300, 310, 311, 312), // Archer: Hunter->Ranger->Bowmaster
            new TierSlot(new Point(404, -325), W_CROSSBOW, 300, 320, 321, 322), // Archer: Crossbowman->Sniper->Marksman
            new TierSlot(new Point(391, -565), W_CLAW,     400, 410, 411, 412), // Thief: Assassin->Hermit->Night Lord
            new TierSlot(new Point(866, -565), W_DAGGER,   400, 420, 421, 422)  // Thief: Bandit->Chief Bandit->Shadower
    );

    /**
     * Spawns one attack test bot per class line on Henesys Hunting Ground 1, all forced to
     * the given job tier (1-4) and a matching level + weapon, so each tier's attacks can be
     * eyeballed in isolation. Forcing the weapon also guarantees the crossbowman gets a
     * crossbow (sidestepping the decoration pool's bow bias).
     */
    public static void spawnAttackTestBots(int tierArg) {
        final int tier = (tierArg < 1 || tierArg > 4) ? 1 : tierArg;
        final int level = switch (tier) { case 2 -> 50; case 3 -> 100; case 4 -> 130; default -> 25; };

        debugprint(fmt("Spawning tier-{} attack test bots on Henesys Hunting Ground 1...", tier));
        for (TierSlot slot : ATTACK_TEST_SLOTS) {
            ExecutorServiceManager.runAsync(() -> {
                Character bot = createBotWithRetry(slot.pos(), HHG1, 5);
                if (bot == null) {
                    debugprint(fmt("Failed to spawn attack test bot at {}", slot.pos()));
                    return;
                }
                bot.setLevel(level);
                bot.setJob(Job.getById(slot.jobForTier(tier)));
                EquipBot(bot, slot.weaponId());
                setAndStartBots(List.of(bot.getId()), BotTypeManager.BotType.TEST_ATTACK_BOT);
            });
        }
    }

    public static void spawnDropGameSpectatorsPotionShop() {
        debugprint("Spawning Drop Game Spectator bots in Henesys Potion Shop...");
        Point[] spots = {
                new Point(145, 145), new Point(76, 22), new Point(297, -28),
                new Point(-87, -25), new Point(-269, -27),
                new Point(-142, 31), new Point(-28, 103), new Point(151, -43),
                new Point(-142, 141)
        };

        List<Point> available = new ArrayList<>(List.of(spots));
        Collections.shuffle(available);
        int count = 3 + new Random().nextInt(4);

        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < count && i < available.size(); i++) {
            Character bot = createBotWithRetry(available.get(i), HENESYS_POTION_SHOP, 3);
            if (bot != null) {
                ids.add(bot.getId());
            }
        }

        if (!ids.isEmpty()) {
            setAndStartBots(ids, BotTypeManager.BotType.SOCIAL_BOT);
            debugprint("Spawned " + ids.size() + " Drop Game Spectator bots in Potion Shop.");
        }
    }

    public static void spawnTutorialBot() {
        debugprint("Spawning Tutorial Bot on Maple Island...");
        Point spawn = new Point(158, 485);
        Character fakechar = createBotWithRetry(spawn, MAPLE_ISLAND_TUTORIAL, 5);
        if (fakechar != null) {
            setAndStartBots(List.of(fakechar.getId()), BotTypeManager.BotType.TUTORIAL_BOT);
            debugprint("Tutorial Bot started on Maple Island.");
        } else {
            debugprint("Failed to spawn Tutorial Bot on Maple Island.");
        }
    }

    public static void spawnJQBotsPetPark() {
        debugprint("Spawning JQ Bots in Henesys Pet Park...");
        List<Integer> botIds = spawnBotsOnMapOnPlatform(15, HENESYS_PET_PARK, "m1");
        setAndStartBots(botIds, BotTypeManager.BotType.HENESYS_JQ_BOT);
        debugprint(fmt("Pet Park JQ bots spawned: {}", botIds.size()));
    }

    public static void spawnSocialBotsPetPark() {
        int map = HENESYS_PET_PARK;
        debugprint("Spawning social bots in Henesys Pet Park...");

        List<Integer> allIds = new ArrayList<>();
        allIds.addAll(spawnFillerBots(1, map, new Point(-194, 34), new Point(184, 34)));
        allIds.addAll(spawnFillerBots(2, map, new Point(-449, 154), new Point(369, 154)));
        allIds.addAll(spawnFillerBots(3, map, new Point(618, 154), new Point(1375, 154)));
        allIds.addAll(spawnFillerBots(1, map, new Point(841, -116), new Point(1125, -116)));
        allIds.addAll(spawnFillerBots(1, map, new Point(437, -326), new Point(810, -326)));
        allIds.addAll(spawnFillerBots(1, map, new Point(531, -626), new Point(731, -626)));
        allIds.addAll(spawnFillerBots(1, map, new Point(790, -506), new Point(993, -506)));
        allIds.addAll(spawnFillerBots(1, map, new Point(1072, -446), new Point(1274, -446)));
        allIds.addAll(spawnFillerBots(3, map, new Point(-1808, 274), new Point(-738, 274)));

        setAndStartBots(allIds, BotTypeManager.BotType.SOCIAL_BOT);
        debugprint(fmt("Pet Park social bots spawned: {}", allIds.size()));
    }

    public static void spawnGameZoneHostBots() {
        debugprint("Spawning Game Zone Host Bots...");
        Point[] spawns = { new Point(503, 250), new Point(716, 254) };
        List<Integer> botIds = new ArrayList<>();

        for (Point spawn : spawns) {
            Character fakechar = createBotWithRetry(spawn, HENESYS_GAME_ZONE, 5);
            if (fakechar != null) {
                botIds.add(fakechar.getId());
            } else {
                debugprint(fmt("Failed to spawn Game Zone Host Bot at {}", spawn));
            }
        }

        if (!botIds.isEmpty()) {
            setAndStartBots(botIds, BotTypeManager.BotType.GAME_ZONE_HOST_BOT);
            debugprint(fmt("Game Zone Host Bots started: {}", botIds.size()));
        }
    }

    public static void spawnBlackjackTables() {
        debugprint("Spawning Blackjack Tables in Game Zone...");

        // Table 1
        spawnBlackjackTable(
                new Point(-947, 64), new Point(-169, 64),
                new Point(-920, 274), new Point(-169, 274));
        // Table 2
        spawnBlackjackTable(
                new Point(-939, -296), new Point(-152, -296),
                new Point(-937, -116), new Point(-149, -116));
        // Table 3
        spawnBlackjackTable(
                new Point(226, -296), new Point(937, -296),
                new Point(227, -116), new Point(940, -116));
        // Table 4
        spawnBlackjackTable(
                new Point(-927, -656), new Point(-130, -656),
                new Point(-956, -476), new Point(-151, -476));
        // Table 5
        spawnBlackjackTable(
                new Point(229, -656), new Point(924, -656),
                new Point(220, -476), new Point(943, -476));

        debugprint("All Blackjack Tables spawned.");
    }

    private static Point[] calculateTablePositions(Point topP1, Point topP2, Point botP1, Point botP2) {
        int topMinX = Math.min(topP1.x, topP2.x);
        int topMaxX = Math.max(topP1.x, topP2.x);
        int topY = topP1.y;
        int topThird = (topMaxX - topMinX) / 3;

        int botMinX = Math.min(botP1.x, botP2.x);
        int botMaxX = Math.max(botP1.x, botP2.x);
        int botY = botP1.y;
        int botThird = (botMaxX - botMinX) / 3;

        return new Point[] {
                new Point(topMinX + topThird + topThird / 2, topY),                       // [0] Dealer — top middle (no jitter)
                new Point(topMinX + topThird / 2 + jitter(), topY),                       // [1] Player — top left
                new Point(topMinX + topThird * 2 + topThird / 2 + jitter(), topY),        // [2] Player — top right
                new Point(botMinX + botThird / 2 + jitter(), botY),                       // [3] Player — bottom left
                new Point(botMinX + botThird + botThird / 2 + jitter(), botY),             // [4] Player — bottom middle
                new Point(botMinX + botThird * 2 + botThird / 2 + jitter(), botY),         // [5] Player — bottom right
        };
    }

    private static int jitter() {
        return random.nextInt(125) - 50;
    }

    private static void spawnBlackjackTable(Point topP1, Point topP2, Point botP1, Point botP2) {
        Point[] seats = calculateTablePositions(topP1, topP2, botP1, botP2);
        int playerCount = 2 + random.nextInt(4); // 2-5 players

        // Spawn dealer bot at seat[0]
        Character dealerChar = createBotWithRetry(seats[0], HENESYS_GAME_ZONE, 5);
        if (dealerChar == null) {
            debugprint("Failed to spawn Blackjack dealer bot");
            return;
        }

        setAndStartBots(List.of(dealerChar.getId()), BotTypeManager.BotType.BLACKJACK_DEALER);

        // Spawn player bots at random seats from seats[1]-[5]
        List<Integer> playerSeatIndices = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Collections.shuffle(playerSeatIndices);
        List<Character> playerChars = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            int seatIdx = playerSeatIndices.get(i);
            Character playerChar = createBotWithRetry(seats[seatIdx], HENESYS_GAME_ZONE, 5);
            if (playerChar != null) {
                playerChars.add(playerChar);
            }
        }

        // Register players with the dealer's table immediately (game logic),
        // but face them towards the dealer only after the spawn drop-down/
        // turn-around choreography finishes, so it can't override the facing.
        BotSM dealerBot = getBotById(dealerChar.getId());
        if (dealerBot instanceof BlackjackDealerBot bjDealer) {
            for (Character playerChar : playerChars) {
                bjDealer.getTable().addPlayer(playerChar);
                bjDealer.getInteractors().setRespondant(playerChar);
            }
            ExecutorServiceManager.getScheduledExecutorService().schedule(() -> {
                for (Character playerChar : playerChars) {
                    botFaceTowardsPoint(playerChar, seats[0]);
                }
            }, BotGeneration.SPAWN_CHOREOGRAPHY_MAX_MS + 500, TimeUnit.MILLISECONDS);
            debugprint(fmt("Blackjack table spawned: dealer={}, players={}", dealerChar.getId(), playerChars.size()));
        } else {
            debugprint("Failed to retrieve BlackjackDealerBot from CharacterStorage");
        }
    }

    public static void convertRandomFillersToScrollBots() {
        debugprint("Converting random filler bots to Scroll Bots across Henesys maps...");

        int[] maps = { HENESYS, HENESYS_MARKET, HENESYS_PARK, HENESYS_POTION_SHOP, HENESYS_GAME_ZONE };
        for (int mapId : maps) {
            int count = 1 + random.nextInt(3); // 1-3
            convertRandomIdleBotsToScrollBots(mapId, count);
        }

        debugprint("Scroll Bot conversion complete.");
    }

    public static void convertRandomIdleBotsToScrollBots(int mapId, int count) {
        List<Character> allChars = getAllCharsOnMap(mapId);

        List<Integer> idleBotIds = allChars.stream()
                .filter(chr -> {
                    if (!isBot(chr)) return false;
                    BotSM bot = getBotById(chr.getId());
                    return bot != null && bot.isAvailableForAmbientActions();
                })
                .map(Character::getId)
                .collect(Collectors.toList());

        if (idleBotIds.isEmpty()) {
            debugprint(fmt("No idle bots found on map {} to convert", mapId));
            return;
        }

        Collections.shuffle(idleBotIds);
        int toConvert = Math.min(count, idleBotIds.size());
        List<Integer> selected = idleBotIds.subList(0, toConvert);

        debugprint(fmt("Converting {} idle bots to Scroll Bots on map {}: {}", toConvert, mapId, selected));
        setAndStartBots(selected, BotTypeManager.BotType.SCROLL_BOT);
    }

    public static void spawnOPQBotsInLobby() {
        int totalBots = 10 + random.nextInt(6); // 10-15
        List<String> platforms = getMainPlatformIds(OPQ_LOBBY);

        if (platforms.isEmpty()) {
            debugprint("No platforms found for OPQ lobby map");
            return;
        }

        debugprint(fmt("Spawning {} OPQ bots across {} platforms in lobby...", totalBots, platforms.size()));

        List<Integer> allBotIds = new ArrayList<>();
        int perPlatform = totalBots / platforms.size();
        int remainder = totalBots % platforms.size();

        for (int i = 0; i < platforms.size(); i++) {
            int count = perPlatform + (i < remainder ? 1 : 0);
            if (count <= 0) continue;
            List<Integer> ids = spawnBotsOnMapOnPlatform(count, OPQ_LOBBY, platforms.get(i));
            allBotIds.addAll(ids);
        }

        if (!allBotIds.isEmpty()) {
            setBotsLevelRange(allBotIds, 50, 70);
            setAndStartBots(allBotIds, BotTypeManager.BotType.OPQ_BOT);
            debugprint(fmt("OPQ lobby bots spawned and started: {}", allBotIds.size()));
        }
    }

    public static void setBotsLevelRange(List<Integer> botIds, int minLevel, int maxLevel) {
        for (int botId : botIds) {
            Character bot = BotHelpers.getCharFromChannelStorage(botId);
            if (bot != null) {
                bot.setLevel(minLevel + random.nextInt(maxLevel - minLevel + 1));
            }
        }
    }

}