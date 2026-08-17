package soloMapling.command;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import soloMapling.ArtificialPlayer.BotGrindSystem.MapGrindProfile;
import soloMapling.ArtificialPlayer.BotGrindSystem.Spot;
import soloMapling.ArtificialPlayer.BotGrindSystem.SpotFinder;
import soloMapling.ArtificialPlayer.BotChatterSystem.BotChatter;
import soloMapling.ArtificialPlayer.BotChatterSystem.TownChatterLines;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotSpotClaims;
import soloMapling.ArtificialPlayer.BotTownSystem.TownPinsStore;
import soloMapling.ArtificialPlayer.BotTownSystem.TownPresenceConfig;
import soloMapling.ArtificialPlayer.BotTownSystem.TownPresenceSampler;
import soloMapling.Environment.EnvironmentPopulationConfig;
import soloMapling.ArtificialPlayer.BotTypeManager;
import soloMapling.ArtificialPlayer.ConversationManager;
import soloMapling.ArtificialPlayer.SocialHotPotatoManager;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotTypes.TrainingBot;
import soloMapling.ArtificialPlayer.GCMoveSystem.LodCounts;
import soloMapling.Casino.CasinoChipConfig;
import soloMapling.server.BotPerfStats;
import soloMapling.server.ExecutorServiceManager;
import soloMapling.server.NpcSpawner;

import java.awt.Point;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.DebugUtilities.debugprint;
import static soloMapling.Environment.PlatformPlacement.botMoveToPlatformAnyUnoccupiedSpot;
import static soloMapling.Environment.EnvironmentManager.environmentLoadStartup;
import static soloMapling.Environment.PlatformPlacement.getAllCharsOnPlatform;
import static soloMapling.Environment.PlatformPlacement.getAvailablePlatformIds;
import static soloMapling.Environment.PlatformPlacement.getCurrentPlatform;
import static soloMapling.Environment.PlatformPlacement.getMainPlatformIds;
import static soloMapling.Environment.EnvironmentManager.spawnBotsInFMEntrance;
import static soloMapling.Environment.EnvironmentManager.spawnCasinoNpcs;
import static soloMapling.Environment.PlatformPlacement.spawnFillerBots;
import static soloMapling.Environment.EnvironmentManager.spawnScatteredTrainingBots;
import static soloMapling.Environment.EnvironmentManager.spawnTownPresence;
import static soloMapling.Environment.EnvironmentManager.spawnSocialCohort;
import static soloMapling.Environment.EnvironmentManager.spawnTownWanderers;
import static soloMapling.Environment.EnvironmentManager.spawnFillerBotsHenesys;
import static soloMapling.Environment.EnvironmentManager.spawnFillerBotsHenesysMarket;
import static soloMapling.Environment.EnvironmentManager.spawnFillerBotsHenesysPark;
import static soloMapling.Environment.EnvironmentManager.spawnFillerBotsGameZone;
import static soloMapling.Environment.PlatformPlacement.spawnFillerBotsLockedY;
import static soloMapling.Environment.EnvironmentManager.spawnFillerBotsPotionShop;
import static soloMapling.Environment.EnvironmentManager.convertRandomFillersToScrollBots;
import static soloMapling.Environment.EnvironmentManager.spawnOPQBotsInLobby;
import static soloMapling.Environment.EnvironmentManager.spawnGameZoneHostBots;
import static soloMapling.Environment.EnvironmentManager.spawnAttackTestBots;
import static soloMapling.Environment.EnvironmentManager.spawnBlackjackTables;
import static soloMapling.Environment.EnvironmentManager.spawnGachaBotsHenesys;
import static soloMapling.Environment.EnvironmentManager.spawnHenesysBots;
import static soloMapling.Environment.EnvironmentManager.spawnMerchBotsInFMEntrance;
import static soloMapling.server.SoloMaplingUtilities.getRandomElement;
import static soloMapling.server.SoloMaplingUtilities.isInteger;

public class EnvironmentCommand extends Command {
    {
        setDescription("Environment Commands.");
    }

    private static Character player;
    
    @Override
    public void execute(Client c, String[] params) {
        player = c.getPlayer();
        if (params.length == 0) {
            ExecutorServiceManager.getExecutorService().execute(() -> {
                player.yellowMessage("No Command Parameter Found. Try !env help");
            });
            return;
        }
        // townpresence has variable-arity sub-args (reload / spawn / here / weights); handle it before the
        // rigid param-count dispatch below.
        if (params[0].equalsIgnoreCase("townpresence") || params[0].equalsIgnoreCase("tp")) {
            ExecutorServiceManager.getExecutorService().execute(() -> handleTownPresence(params, c));
            return;
        }
        if (params[0].equalsIgnoreCase("population") || params[0].equalsIgnoreCase("pop")) {
            ExecutorServiceManager.getExecutorService().execute(() -> handlePopulation(params, c));
            return;
        }
        if (params[0].equalsIgnoreCase("chatter")) {
            ExecutorServiceManager.getExecutorService().execute(() -> handleChatter(params, c));
            return;
        }
        if (params.length == 1) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                handleSingleInputCommand(params[0], c);
            });
            return;
        }
        if (params.length == 2) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                if (isInteger(params[1])) {
                    int commandNum = Integer.parseInt(params[1]);
                    handleStringIntCommand(params[0], commandNum, c);
                } else if (!isInteger(params[0]) && !isInteger(params[1])) {
                    String commandString1 = (params[0]);
                    String commandString2 = params[1];
                    handleStringStringCommand(commandString1, commandString2, c);
                } else {
                    player.yellowMessage("Second input not an integer");
                }
            });
            return;
        }
        if (params.length == 3) {
            ExecutorServiceManager.getExecutorService().execute(() ->
            {
                if (isInteger(params[1]) && isInteger(params[2])) {
                    int commandNum = Integer.parseInt(params[1]);
                    int commandNum2 = Integer.parseInt(params[2]);
                    handleStringIntIntCommand(params[0], commandNum, commandNum2, c);
                } else if (isInteger(params[1])) {
                    int commandNum = Integer.parseInt(params[1]);
                    String commandString = params[2];
                    handleStringIntStringCommand(params[0], commandNum, commandString);
                } else {
                    player.yellowMessage("Second input not an integer");
                }
            });
            return;
        }
    }

    // !env townpresence (alias tp) — live tuning of the ambient town-presence system (Phase 2 curation
    // loop): reload the YAML, (re-)spawn a town's cohort to eyeball placement, or dump the sampler's
    // ledge weights for the current map. Runs on the command executor already (spawns block on readiness).
    private static void handleTownPresence(String[] params, Client c) {
        Character p = c.getPlayer();
        String action = params.length >= 2 ? params[1].toLowerCase() : "help";
        switch (action) {
            case "reload" -> {
                var towns = TownPresenceConfig.reload();
                // Rebuild the ambient-social managers' map scope so a curated town list applies live.
                ConversationManager.getInstance().refreshMapScope();
                SocialHotPotatoManager.getInstance().refreshMapScope();
                int total = 0;
                for (var t : towns) {
                    for (var m : t.maps()) {
                        total += m.count();
                    }
                }
                p.dropMessage(6, "TownPresence: reloaded " + towns.size() + " towns, " + total
                        + " social bots planned (from EnvironmentPopulation.yaml waves.town_presence.towns).");
            }
            case "spawn" -> {
                if (params.length >= 4) {
                    int mapId = tpInt(params[2], -1);
                    int count = tpInt(params[3], 0);
                    int lo = params.length >= 5 ? tpInt(params[4], 20) : 20;
                    int hi = params.length >= 6 ? tpInt(params[5], lo) : Math.max(lo, 60);
                    int n = spawnSocialCohort(mapId, count, lo, hi);
                    p.dropMessage(6, "TownPresence: spawned " + n + " social bots on map " + mapId
                            + " (lv " + lo + ".." + hi + ").");
                } else if (params.length >= 3 && params[2].equalsIgnoreCase("all")) {
                    // Explicit opt-in: this STACKS a full second population on top of wave 9's startup spawn.
                    p.dropMessage(6, "TownPresence: spawning ALL towns from YAML (stacks on top of any present)...");
                    spawnTownPresence();
                    p.dropMessage(6, "TownPresence: done (per-town counts in console).");
                } else {
                    // Bare 'spawn' would re-run the whole wave-9 spawn and double the world's town population;
                    // require an explicit target so it isn't an accident.
                    p.dropMessage(6, "TownPresence: 'spawn <map> <n> [lo hi]' for one map, or 'spawn all' for every town.");
                    p.dropMessage(6, "Note: spawns STACK (no auto-clear); wave 9 already populated towns at startup.");
                }
            }
            case "here" -> {
                int count = params.length >= 3 ? tpInt(params[2], 10) : 10;
                int n = spawnSocialCohort(p.getMapId(), count, 20, 70);
                p.dropMessage(6, "TownPresence: spawned " + n + " social bots on your map " + p.getMapId() + ".");
            }
            case "wander" -> {
                if (params.length >= 4) {
                    int mapId = tpInt(params[2], -1);
                    int count = tpInt(params[3], 0);
                    int lo = params.length >= 5 ? tpInt(params[4], 20) : 20;
                    int hi = params.length >= 6 ? tpInt(params[5], lo) : Math.max(lo, 70);
                    int n = spawnTownWanderers(mapId, count, lo, hi);
                    p.dropMessage(6, "TownPresence: spawned " + n + " wanderers on map " + mapId + ".");
                } else {
                    int count = params.length >= 3 ? tpInt(params[2], 5) : 5;
                    int n = spawnTownWanderers(p.getMapId(), count, 20, 70);
                    p.dropMessage(6, "TownPresence: spawned " + n + " wanderers on your map " + p.getMapId() + ".");
                }
            }
            case "weights" -> {
                int topN = params.length >= 3 ? tpInt(params[2], 12) : 12;
                MapleMap map = p.getMap();
                Portal sp = map.getPortal(0);
                Point anchor = sp != null ? sp.getPosition() : p.getPosition();
                String dump = TownPresenceSampler.describe(map, anchor, topN,
                        TownPresenceConfig.overridesFor(map.getId()));
                for (String line : dump.split("\\r?\\n")) {
                    if (!line.isBlank()) {
                        p.dropMessage(6, line);
                    }
                }
                System.out.println(dump);
            }
            case "mark" -> {
                int mapId = params.length >= 3 ? tpInt(params[2], p.getMapId()) : p.getMapId();
                Point pos = p.getPosition();
                TownPinsStore.addPin(mapId, pos.x, pos.y);
                TownPresenceConfig.reload(); // so a following spawn/weights picks up the new pin
                p.dropMessage(6, "TownPresence: pinned (" + pos.x + "," + pos.y + ") on map " + mapId
                        + " -> TownPins.txt. Spawn there to see it placed.");
            }
            default -> p.dropMessage(6,
                    "!env townpresence reload | spawn [map n lo hi] | here [n] | wander [map n lo hi] | weights [topN] | mark [mapId]");
        }
    }

    // !env population (alias pop) — inspect / reload EnvironmentPopulation.yaml (wave counts + training cohorts).
    private static void handlePopulation(String[] params, Client c) {
        Character p = c.getPlayer();
        String action = params.length >= 2 ? params[1].toLowerCase() : "show";
        switch (action) {
            case "reload" -> {
                var plan = EnvironmentPopulationConfig.reload();
                var towns = TownPresenceConfig.towns();
                p.dropMessage(6, "Population: reloaded from " + plan.loadedFrom()
                        + " scale=" + plan.scale()
                        + " trainingScaledTotal=" + plan.trainingCohortTotal()
                        + " cohorts=" + plan.training().cohorts().size()
                        + " towns=" + towns.size());
            }
            case "show", "status", "help" -> {
                var plan = EnvironmentPopulationConfig.plan();
                p.dropMessage(6, "Population source=" + plan.loadedFrom() + " scale=" + plan.scale()
                        + " version=" + plan.version());
                p.dropMessage(6, "Waves enabled: essentials=" + plan.essentials().enabled()
                        + " fm=" + plan.fmBuildout().enabled()
                        + " henesys=" + plan.henesysPopulation().enabled()
                        + " training=" + plan.training().enabled()
                        + " townPresence=" + plan.townPresence().enabled());
                p.dropMessage(6, "Training cohorts=" + plan.training().cohorts().size()
                        + " scaledTotal=" + plan.trainingCohortTotal()
                        + " (edit EnvironmentPopulation.yaml; restart or !env load to apply spawn)");
                if ("help".equals(action)) {
                    p.dropMessage(6, "!env population reload | show");
                }
            }
            default -> p.dropMessage(6, "!env population reload | show");
        }
    }

    private static int tpInt(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    // !env chatter — inspect / tune the ambient bot-to-bot conversation system (BotChatterSystem).
    //   (no arg)  readout of the tuning knobs, loaded exchange count, and how many bots are mid-chat now
    //   reload    re-read TownChatterDialogue.yaml without a restart
    //   fire      force every eligible participant on your current map to try starting a chat now
    private static void handleChatter(String[] params, Client c) {
        Character p = c.getPlayer();
        String action = params.length >= 2 ? params[1].toLowerCase() : "readout";
        switch (action) {
            case "reload" -> {
                int n = TownChatterLines.reload().size();
                p.dropMessage(6, "chatter: reloaded TownChatterDialogue.yaml - " + n + " exchanges");
            }
            case "fire" -> {
                MapleMap map = p.getMap();
                int started = 0;
                for (Character chr : map.getAllPlayers()) {
                    if (chr == null || !BotHelpers.isBot(chr)) {
                        continue;
                    }
                    BotSM bot = CharacterStorage.getBotById(chr.getId());
                    if (bot != null && BotChatter.forceChatter(bot)) {
                        started++;
                    }
                }
                p.dropMessage(6, "chatter: forced " + started + " exchange(s) on this map "
                        + "(needs adjacent, available participant pairs within " + BotChatter.CHATTER_RADIUS + "px)");
            }
            default -> {
                p.dropMessage(6, String.format("chatter: chance=%.2f radius=%dpx maxMs=%d cooldown=%d..%dms",
                        BotChatter.CHATTER_CHANCE, BotChatter.CHATTER_RADIUS, BotChatter.MAX_CHATTER_MS,
                        BotChatter.CHATTER_COOLDOWN_MIN_MS, BotChatter.CHATTER_COOLDOWN_MAX_MS));
                p.dropMessage(6, "chatter: " + TownChatterLines.exchanges().size() + " exchanges loaded, "
                        + BotChatter.engagedCount() + " bot(s) mid-chat now");
                p.dropMessage(6, "chatter: reload | fire (force chats on this map)");
            }
        }
    }

    public static void handleSingleInputCommand(String input, Client c) {
        switch (input.toLowerCase()) {
            case "help":
                printHelp();
                break;
            case "loadenv":
                environmentLoadStartup();
                break;
            case "spawnfmbots":
                spawnBotsInFMEntrance();
                break;
            case "spawnmerchantbots":
            case "spawnmerchbots":
                spawnMerchBotsInFMEntrance();
                break;
            case "spawnhenesysbots":
                spawnHenesysBots();
                break;
            case "spawngachabots":
                spawnGachaBotsHenesys();
                break;
            case "getmap":
                debugprint("Current Map:", c.getPlayer().getMapId());
                break;
            case "getportal":
                // From !debug portal
                Portal portal = player.getMap().findClosestPortal(player.getPosition());
                if (portal != null) {
                    player.dropMessage(6, "Closest portal: id: " + portal.getId() + " name: '" + portal.getName() + "' Type: " + portal.getType() + " --> toMap: " + portal.getTargetMapId() + "' state: " + (portal.getPortalState() ? 1 : 0) + ", Pos: " +
                            portal.getPosition());
                } else {
                    player.dropMessage(6, "There is no portal on this map.");
                }
                break;
            case "getplat":
            case "getplatform":
            case "getcurrentplat":
            case "getcurrentplatform":
                System.out.println(getCurrentPlatform(c.getPlayer()));
                break;
            case "getallplatforms":
                System.out.println(getAvailablePlatformIds(c.getPlayer().getMapId()));
                break;
            case "getallmainplatforms":
                System.out.println(getMainPlatformIds((c.getPlayer().getMapId())));
                break;
            case "spawnhenefillers":
                player.yellowMessage("Spawning Henesys filler bots...");
                spawnFillerBotsHenesys();
                player.yellowMessage("Henesys filler bots done.");
                break;
            case "spawnmarketfillers":
                player.yellowMessage("Spawning Henesys Market filler bots...");
                spawnFillerBotsHenesysMarket();
                player.yellowMessage("Henesys Market filler bots done.");
                break;
            case "spawnparkfillers":
                player.yellowMessage("Spawning Henesys Park filler bots...");
                spawnFillerBotsHenesysPark();
                player.yellowMessage("Henesys Park filler bots done.");
                break;
            case "spawngamezonefillers":
                player.yellowMessage("Spawning Game Zone filler bots...");
                spawnFillerBotsGameZone();
                player.yellowMessage("Game Zone filler bots done.");
                break;
            case "spawnpotshopfillers":
                player.yellowMessage("Spawning Potion Shop filler bots...");
                spawnFillerBotsPotionShop();
                player.yellowMessage("Potion Shop filler bots done.");
                break;
            case "spawnallfillers":
                player.yellowMessage("Spawning all filler bots...");
                spawnFillerBotsHenesys();
                spawnFillerBotsHenesysMarket();
                spawnFillerBotsHenesysPark();
                spawnFillerBotsGameZone();
                spawnFillerBotsPotionShop();
                player.yellowMessage("All filler bots done.");
                break;
            case "convertscrollbots":
                player.yellowMessage("Converting random fillers to Scroll Bots...");
                convertRandomFillersToScrollBots();
                player.yellowMessage("Scroll Bot conversion done.");
                break;
            case "spawnopqbots":
                player.yellowMessage("Spawning OPQ bots in lobby...");
                spawnOPQBotsInLobby();
                player.yellowMessage("OPQ lobby bots done.");
                break;
            case "spawngzhbots":
                player.yellowMessage("Spawning Game Zone Host Bots...");
                spawnGameZoneHostBots();
                player.yellowMessage("Game Zone Host Bots done.");
                break;
            case "spawnbjtables":
                player.yellowMessage("Spawning Blackjack Tables...");
                spawnBlackjackTables();
                player.yellowMessage("Blackjack Tables done.");
                break;
            case "attacktest":
                player.yellowMessage("Spawning tier-1 attack test bots on Henesys Hunting Ground 1 (use !env attacktest 2/3/4 for other tiers)...");
                spawnAttackTestBots(1);
                player.yellowMessage("Attack test bots spawning. Stand on the map so mobs move into reach.");
                break;
            case "scattertest":
            case "trainscatter":
                spawnScatterTest(c, 30);
                break;
            case "starthotpotato":
                SocialHotPotatoManager.getInstance().start();
                player.yellowMessage("Social Hot Potato started.");
                break;
            case "stophotpotato":
                SocialHotPotatoManager.getInstance().stop();
                player.yellowMessage("Social Hot Potato stopped.");
                break;
            case "startconvo":
                ConversationManager.getInstance().start();
                player.yellowMessage("Conversation Manager started.");
                break;
            case "stopconvo":
                ConversationManager.getInstance().stop();
                player.yellowMessage("Conversation Manager stopped.");
                break;
            case "spawncasinonpc":
                spawnCasinoNpc(c);
                break;
            case "spawnrpsnpc":
                NpcSpawner.spawnNpcAtPlayer(c.getPlayer(), 9000019);
                break;
            case "spawncasinonpcs":
                spawnCasinoNpcs();
                player.yellowMessage("Casino NPCs spawned on map 100000203.");
                break;
            case "grindprofile":
            case "spotdump":
                dumpGrindProfile(c);
                break;
            case "perf":
                printPerfReport();
                break;
            default:
                player.yellowMessage("Invalid command - Direct Command");
                break;
        }
    }

    public static void handleStringIntIntCommand(String input, int input2, int input3, Client c) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        player.yellowMessage("Command: " + input2 + ", arg: " + input3);
        switch (input.toLowerCase()) {
            default:
                player.yellowMessage("Invalid command - handleStringIntIntCommand");
                break;
        }
    }

    public static void handleStringIntCommand(String input, int input2, Client c) {
        switch (input.toLowerCase()) {
            case "attacktest":
                player.yellowMessage("Spawning tier-" + input2 + " attack test bots on Henesys Hunting Ground 1...");
                spawnAttackTestBots(input2);
                player.yellowMessage("Attack test bots spawning. Stand on the map so mobs move into reach.");
                return;
            case "scattertest":
            case "trainscatter":
                spawnScatterTest(c, input2);
                return;
            case "fillerbot":
                Point p1 = new Point(-548, 154);
                Point p2 = new Point(568, 154);
                int mapId = player.getMapId();
                player.yellowMessage("Spawning " + input2 + " filler bots on map " + mapId);
                java.util.List<Integer> fillerIds = spawnFillerBots(input2, mapId, p1, p2);
                BotTypeManager.setAndStartBots(fillerIds, BotTypeManager.BotType.SOCIAL_BOT);
                player.yellowMessage("Filler bot spawn complete. " + fillerIds.size() + " SocialBots assigned.");
                return;
            case "fillerboty":
                Point p1a = new Point(-112, -127);
                Point p2a = new Point(245, -127);

                int mapIdy = player.getMapId();
                player.yellowMessage("Spawning " + input2 + " filler bots on map " + mapIdy);
                java.util.List<Integer> fillerIdsY = spawnFillerBotsLockedY(input2, mapIdy, p1a, p2a);
                BotTypeManager.setAndStartBots(fillerIdsY, BotTypeManager.BotType.SOCIAL_BOT);
                player.yellowMessage("Filler bot spawn complete. " + fillerIdsY.size() + " SocialBots assigned.");
                break;
            default:
                break;
        }

        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null for handleStringIntCommand");
            return;
        }

        switch (input.toLowerCase()) {
            default:
                player.yellowMessage("Invalid command - handleStringIntCommand");
                break;
        }
    }

    public static void handleStringStringCommand(String input, String input2, Client c) {
        switch (input.toLowerCase()) {
            case "Test":
                break;
            case "getallcharsonplatform":
            case "getcharsonplatform":
                getAllCharsOnPlatform(c.getPlayer().getMapId(), input2);
                break;
            default:
                player.yellowMessage("Invalid command - handleStringIntCommand");
                break;
        }
    }

    public static void handleStringIntStringCommand(String input, int input2, String str) {
        Character fakechar = BotHelpers.getCharFromChannelStorage(input2);
        if (fakechar == null) {
            player.yellowMessage("Bot null");
            return;
        }

        switch (input.toLowerCase()) {
            case "chat":
                BotSpeak(fakechar, str);
                break;
            case "platformshuffle":
                botMoveToPlatformAnyUnoccupiedSpot(fakechar, str);
                break;
            case "platformshufflerandom":
                int currentMap = fakechar.getMapId();
                botMoveToPlatformAnyUnoccupiedSpot(fakechar, getRandomElement(getMainPlatformIds(currentMap)));
                break;
            default:
                player.yellowMessage("Invalid command Two Object");
                break;
        }

    }

    // Fable audit Phase 0: perf snapshot - threads, heap, tick rates (delta since the
    // previous readout), plus bot/tier counts. Run twice a few seconds apart for live rates.
    private static void printPerfReport() {
        for (String line : BotPerfStats.report()) {
            player.yellowMessage(line);
        }
        player.yellowMessage(String.format("bots: %d active   dynamic movement: %d   grinders: %d",
                CharacterStorage.getAllBots().size(), LodCounts.dynamicBots(), TrainingBot.activeGrinderCount()));
        player.yellowMessage(String.format("observed maps: full=%d halo=%d active=%d",
                LodCounts.fullMaps(), LodCounts.haloMaps(), LodCounts.activeMaps()));
    }

    private static void printHelp() {
        player.yellowMessage("---- Environment Commands (!env) ----");
        player.yellowMessage("-- Diagnostics --");
        player.yellowMessage("!env perf                        - bot perf: threads, heap, tick rates, tiers");
        player.yellowMessage("-- World Startup --");
        player.yellowMessage("!env loadenv                     - run full environment startup");
        player.yellowMessage("-- FM Spawning --");
        player.yellowMessage("!env spawnfmbots                 - spawn FM entrance bots");
        player.yellowMessage("!env spawnmerchbots              - spawn merchant bots in FM entrance");
        player.yellowMessage("-- Henesys Spawning --");
        player.yellowMessage("!env spawnhenesysbots            - spawn Henesys wanderer bots");
        player.yellowMessage("!env spawngachabots              - spawn gacha bots in Henesys");
        player.yellowMessage("-- Filler Bots --");
        player.yellowMessage("!env spawnhenefillers            - spawn Henesys filler bots");
        player.yellowMessage("!env spawnmarketfillers          - spawn Henesys Market fillers");
        player.yellowMessage("!env spawnparkfillers            - spawn Henesys Park fillers");
        player.yellowMessage("!env spawngamezonefillers        - spawn Game Zone fillers");
        player.yellowMessage("!env spawnpotshopfillers         - spawn Potion Shop fillers");
        player.yellowMessage("!env spawnallfillers             - spawn all filler bots");
        player.yellowMessage("!env fillerbot <count>           - spawn N fillers at fixed X coords");
        player.yellowMessage("!env fillerboty <count>          - spawn N fillers at fixed Y coords");
        player.yellowMessage("!env convertscrollbots           - convert random fillers to scroll bots");
        player.yellowMessage("-- Special Spawns --");
        player.yellowMessage("!env spawnopqbots                - spawn OPQ lobby bots");
        player.yellowMessage("!env spawngzhbots                - spawn Game Zone Host bots");
        player.yellowMessage("!env spawnbjtables               - spawn Blackjack tables");
        player.yellowMessage("!env attacktest                  - spawn per-class attack test bots on Henesys Hunting Ground 1");
        player.yellowMessage("!env scattertest [count]         - scatter N training bots (def 30) on current map ground spots");
        player.yellowMessage("!env spawncasinonpc              - spawn casino NPC at player");
        player.yellowMessage("!env spawncasinonpcs             - spawn all casino NPCs on map");
        player.yellowMessage("!env spawnrpsnpc                 - spawn RPS NPC");
        player.yellowMessage("-- Social Systems --");
        player.yellowMessage("!env starthotpotato              - start Social Hot Potato manager");
        player.yellowMessage("!env stophotpotato               - stop Social Hot Potato manager");
        player.yellowMessage("!env startconvo                  - start Conversation Manager");
        player.yellowMessage("!env stopconvo                   - stop Conversation Manager");
        player.yellowMessage("-- Map Inspection --");
        player.yellowMessage("!env getmap                      - get current map ID");
        player.yellowMessage("!env getportal                   - get closest portal info");
        player.yellowMessage("!env getplatform                 - get current platform");
        player.yellowMessage("!env getallplatforms             - get all available platforms");
        player.yellowMessage("!env getallmainplatforms         - get all main platforms");
        player.yellowMessage("!env getcharsonplatform <platId> - get chars on a platform");
        player.yellowMessage("!env grindprofile                - dump this map's grind spot profile (calibrate spot tuning)");
        player.yellowMessage("-- Town Presence --");
        player.yellowMessage("!env townpresence reload         - re-read town_presence.towns from EnvironmentPopulation.yaml");
        player.yellowMessage("!env population reload|show      - re-read EnvironmentPopulation.yaml (waves + towns)");
        player.yellowMessage("!env townpresence spawn [map n lo hi] - spawn a town's social cohort (no args = all towns)");
        player.yellowMessage("!env townpresence here [count]   - spawn stationed social bots on your current map (dry-run)");
        player.yellowMessage("!env townpresence wander [map n lo hi] - spawn roaming wanderers (no map = your current map)");
        player.yellowMessage("!env townpresence weights [topN] - dump anchor-weighted ledge weights for this map");
        player.yellowMessage("!env townpresence mark [mapId]   - pin your current spot (stand where you want a bot) -> TownPins.txt");
        player.yellowMessage("-- Bot Chatter --");
        player.yellowMessage("!env chatter                     - readout: knobs, exchanges loaded, bots mid-chat");
        player.yellowMessage("!env chatter reload              - re-read TownChatterDialogue.yaml (no restart)");
        player.yellowMessage("!env chatter fire                - force eligible bot pairs on this map to chat now");
        player.yellowMessage("-- Bot Control --");
        player.yellowMessage("!env chat <cid> <message>        - bot speaks in chat");
        player.yellowMessage("!env platformshuffle <cid> <id>  - move bot to platform");
        player.yellowMessage("!env platformshufflerandom <cid> - move bot to random platform");
    }

    // Dry-run the organic ground-spot scatter (BotSpotPicker): spawn `count` training bots across the
    // current map's reachable platforms, anchored at the GM's position. Lets you eyeball the spread the
    // wave-8 training spawn uses without restarting the world.
    private static void spawnScatterTest(Client c, int count) {
        Character p = c.getPlayer();
        MapleMap map = p.getMap();
        Point from = p.getPosition();
        p.yellowMessage("Scattering " + count + " training bots across map " + p.getMapId()
                + " (anchored at your spot)...");
        java.util.List<Integer> ids = spawnScatteredTrainingBots(map, from, count, 10, 55);
        p.yellowMessage("Scatter spawn complete. " + ids.size() + " training bots placed"
                + (ids.size() < count ? " (some fell back to your spot - nav graph not baked?)" : "") + ".");
    }

    private static void spawnCasinoNpc(Client c) {
        NpcSpawner.spawnNpcAtPlayer(c.getPlayer(), CasinoChipConfig.CASINO_NPC_ID);
    }

    // Dump the grind spot profile for the GM's current map (SpotFinder.profile): spawn count, walkable
    // span, density, regime, and the candidate spot list (anchor / radius / spawnCount + live mobs there).
    // Used to calibrate DENSITY_HI/LO / GAP_LO / SPOT_RADIUS against real maps (§13.10 #1). Building it
    // warms the nav graph for the map if it isn't baked yet.
    private static void dumpGrindProfile(Client c) {
        MapleMap map = c.getPlayer().getMap();
        MapGrindProfile p = SpotFinder.profile(map);
        if (p == null) {
            player.yellowMessage("No grind profile (map null).");
            return;
        }
        player.yellowMessage(String.format("=== grind profile: map %d (%s) ===", p.mapId(), p.regime()));
        player.yellowMessage(String.format("walkable span: %d..%d (%dpx)  spawnPoints: %d  density: %.5f spawns/px (%.2f / screen)",
                p.walkableMinX(), p.walkableMaxX(), p.walkableSpanX(), p.spawnPointCount(),
                p.spawnDensity(), p.spawnDensity() * 1000.0));
        player.yellowMessage(String.format("clusters/spots: %d  meanInterSpotGap: %dpx",
                p.clusterCount(), p.meanInterSpotGapX()));
        int bestSameLedge = 0;
        for (Spot s : p.spots()) {
            bestSameLedge = Math.max(bestSameLedge, s.sameLedgeSpawnCount());
        }
        player.yellowMessage(String.format("mode: %s  (best sameLedge spawns %d — roam when below the campable floor)",
                p.roam() ? "ROAM (no campable ledge)" : "CAMP", bestSameLedge));
        if (!p.stacks().isEmpty()) {
            int bestFeed = 0;
            int hop = 0;
            for (soloMapling.ArtificialPlayer.BotGrindSystem.SpotStack st : p.stacks()) {
                bestFeed = Math.max(bestFeed, st.totalFeed());
                if (st.hopTraversable()) {
                    hop++;
                }
            }
            player.yellowMessage(String.format("stacks: %d (best feed %d, hop-traversable %d) — STACK archetype candidates",
                    p.stacks().size(), bestFeed, hop));
        }
        player.yellowMessage(String.format("occupancy: %d bots targeting this map / capacity %d",
                soloMapling.ArtificialPlayer.BotGrindSystem.TrainingMapChooser.botsTargeting(p.mapId()),
                soloMapling.ArtificialPlayer.BotGrindSystem.TrainingMapChooser.mapCapacity(p.mapId())));
        if (p.spots().isEmpty()) {
            player.yellowMessage("  (no spots — nav graph unbaked or map has no spawn points)");
            return;
        }
        int totalHolders = 0;
        for (int i = 0; i < p.spots().size(); i++) {
            Spot s = p.spots().get(i);
            int live = SpotFinder.liveHostilesWithin(map, s);
            int holders = BotSpotClaims.holders(p.mapId(), i);
            totalHolders += holders;
            player.yellowMessage(String.format("  [%d] anchor (%d,%d) region %d  radius %d  spawnPts %d (sameLedge %d)  ledgeSpan %dpx  liveMobs %d  holders %d/%d",
                    i, s.anchor().x, s.anchor().y, s.regionId(), s.radius(), s.spawnCount(),
                    s.sameLedgeSpawnCount(), s.ledgeSpanPx(), live, holders, s.shareCap()));
        }
        player.yellowMessage(String.format("claim total: %d holders across %d spots", totalHolders, p.spots().size()));
    }

}
