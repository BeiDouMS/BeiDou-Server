package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.constants.game.ExpTable;
import org.gms.constants.id.MapId;
import org.gms.net.server.Server;
import org.gms.net.server.world.Party;
import org.gms.net.server.world.PartyCharacter;
import org.gms.net.server.world.World;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackDriver;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotBuffDriver;
import soloMapling.ArtificialPlayer.BotOptionMenu;
import soloMapling.ArtificialPlayer.BotPartySystem.BotPartyQueue;
import soloMapling.ArtificialPlayer.BotPartySystem.BotRecruitManager;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTypeManager;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import soloMapling.ArtificialPlayer.BotDialogueHandler;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotGrindSystem.DeepHub;
import soloMapling.ArtificialPlayer.BotGrindSystem.GrindBrain;
import soloMapling.ArtificialPlayer.BotGrindSystem.MapMobIndex;
import soloMapling.ArtificialPlayer.BotGrindSystem.TrainingMapChooser;
import soloMapling.ArtificialPlayer.BotGrindSystem.SpotFinder;
import soloMapling.ArtificialPlayer.BotGrindSystem.TrainingMap;
import soloMapling.ArtificialPlayer.BotGrindSystem.TrainingMapFinder;
import soloMapling.ArtificialPlayer.BotTownSystem.TownLoiter;
import soloMapling.ArtificialPlayer.BotWanderSystem.BotWanderSystem;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.server.EventMessageSystem.EventBus;
import soloMapling.server.EventMessageSystem.EventFactory;
import soloMapling.server.ExecutorServiceManager;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotFullChat;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botCancelChair;
import static soloMapling.BotLogger.log;

// A roaming grinder. Spawns in a base town, travels out to a level-appropriate map with mobs, grinds
// for a session, returns to a town hub, and repeats — organically, forever. First production consumer
// of GCMovement (LOD-governed movement) and BotAttackDriver (combat).
//
// Two state machines, kept separate (the design's core split):
//  - Macro brain: updateState() on the slow BotSM tick (2-6s): INIT -> IN_TOWN -> DECIDE -> GO_TRAIN
//    -> GRIND -> GO_TOWN -> IN_TOWN ... Tier-agnostic; only issues movement intents and accrues abstract EXP.
//  - Combat tier: one shared 0.5s ticker (ensureCombatTicker) swings every grinding bot whose map is
//    observed (GCMovement.isMapObserved). The single shared task drives all bots' combat — no thread per bot.
//
// EXP: when unobserved (the common case) the bot levels by cheap arithmetic (kills/min x per-kill-exp),
// applied silently (no packets) via setExp/setLevel. When a real player is on its map, real kills (the
// combat ticker) feed EXP through the normal engine path. Bots are ephemeral decoration — no economy
// impact, wiped on restart.
//
// Map discovery is deterministic from WZ: the bot finds level-appropriate field maps near its spawn
// town by BFS over the portal graph (TrainingMapFinder + MapMobIndex), reading mob levels straight from
// Map.wz/Mob.wz. No hand-authored region table; town-locality emerges from hop distance.
public class TrainingBot extends BotSM {

    // ── Tunables (decoration, not balance — rough is fine) ───────────────────
    // REAL-tier swing/decision cadence (shared ticker). Only OBSERVED grinders do real work here — combatTick
    // early-returns for non-GRIND and grind.tick early-returns on an unobserved map — so raising the rate
    // spends LOD bandwidth on the handful of on-screen bots (snappier reacquire, tighter jump-attack/blink
    // timing, more responsive loot vacuum) while unobserved grinders only pay a doubled cheap early-return.
    // UNTESTED: validate the combat sweep cost with !env perf before/after; revert to 500 if it climbs.
    private static final long COMBAT_TICK_MS = 250;          // was 500; observation-tiered by the early-returns above
    private static final double KILLS_PER_MIN = 30.0;        // abstract grind speed
    private static final long GRIND_MIN_MS = 600_000;        // a grind session lasts 10–20 min
    private static final long GRIND_MAX_MS = 1_200_000;
    // A bot that DIDN'T migrate to its first grind (warped in, or already on the map at the first decision) is
    // meant to look like it's been grinding a while, not just arrived — so its FIRST session is partially
    // elapsed (caught at a random point in a normal session), floored so it doesn't instantly turn around.
    private static final long MID_SESSION_FLOOR_MS = 90_000; // min remaining on a partially-elapsed first session
    // Travel watchdog: abandon a trip only after this long with NO progress at all — no hop completed AND no
    // movement on the current map. The deadline is pushed out every time the bot advances a map or moves, so
    // big maps and long multi-hop routes are never cut off for merely taking a while; only a genuinely wedged
    // trip (stuck in one spot, not changing maps) trips it, and onFail re-plans from here (never teleports).
    private static final long TRAVEL_TIMEOUT_MS = 120_000;
    private static final int TRAVEL_PROGRESS_EPS_PX = 24;    // min position change between ticks that counts as "moving"
    private static final long TOWN_DWELL_MIN_MS = 4_000;     // linger in town (settle) 4–10 s before deciding
    private static final long TOWN_DWELL_MAX_MS = 10_000;
    private static final double SHOP_VISIT_CHANCE = 0.4;     // chance a town visit includes a shop trip (timing variety)
    private static final long SHOP_DWELL_MIN_MS = 30_000;    // browse a store 30–90 s
    private static final long SHOP_DWELL_MAX_MS = 90_000;
    // Town map id -> {potion shop map, weapon/armor shop map}. A bot in a listed town may detour to one or
    // both stores (in either order) before heading out, for organic startup/return timing. GMS v83 store maps.
    private static final Map<Integer, int[]> TOWN_SHOPS = Map.of(
            100000000, new int[]{100000102, 100000101}, // Henesys
            101000000, new int[]{101000002, 101000001}, // Ellinia
            102000000, new int[]{102000002, 102000001}, // Perion
            103000000, new int[]{103000002, 103000001}, // Kerning City
            104000000, new int[]{104000002, 104000001}, // Lith Harbor
            200000000, new int[]{200000002, 200000001}, // Orbis
            220000000, new int[]{220000002, 220000001}, // Ludibrium
            211000000, new int[]{211000102, 211000101}  // El Nath (Department Store/potion, Weapon Store/equip — reached via El Nath Market hub)
    );
    // Sleepywood has no potion/equip stores; its town "errand" is a flavor trip to the hotel sauna instead.
    // The sauna entry is a non-portal NPC teleport (smoke-and-mirrors warp); the exit is a real one-way portal
    // back into the hotel. Only bots whose home town is Sleepywood ever run this (see doInTown / startSaunaTrip).
    private static final int SLEEPYWOOD_HOTEL = 105040400;   // hotel hub, reached by a portal from Sleepywood town
    private static final int SAUNA_REGULAR = 105040401;
    private static final int SAUNA_VIP = 105040402;
    private static final int SAUNA_DOOR_X = -19;             // the in-hotel "sauna door" spot (not a portal): walk here
    private static final int SAUNA_DOOR_Y = -208;            //   for the visual, then warp into the chosen sauna
    private static final double SAUNA_VIP_CHANCE = 0.40;     // 60% regular sauna / 40% VIP sauna
    private static final long DOOR_WALK_MAX_MS = 20_000;     // cap the door walk so a missed arrival can't stall the trip
    private static final long SAUNA_TRIP_TIMEOUT_MS = 300_000; // whole-trip watchdog → force-recover to town if it hangs
    private static final int LEVEL_CAP = 195;                // guard (no real cap intended — see spec)
    // Map-selection policy (bands, chill visits, wander radius, fit/distance/capacity weighting, the
    // occupancy registry) lives in BotGrindSystem/TrainingMapChooser — doDecide only choreographs.
    // First-trip warp: on the VERY FIRST decision to go train (mainly world startup), most bots teleport
    // straight to the picked grind map instead of walking there, so the world looks already-populated with
    // grinders rather than a town-emptying migration. One-time only (firstTrip) — every later trip travels
    // naturally. Gated on the town being unobserved so a watching player never sees a bot blink out.
    private static final double FIRST_TRIP_TELEPORT_CHANCE = 0.70;

    // ── Grind breaks (rest flavor) — all state, knobs, and behavior live in GrindBreakRoutine ──

    // ── Self-repair watchdog (macro tick) ────────────────────────────────────
    // If the bot is observed but lands no hit for this long it's genuinely stuck (wedged / boxed in / dead
    // map): teleport to a portal and re-grind; if that still doesn't help, leave the map. A landed hit (the
    // grind engine's combat heartbeat) resets the whole ladder. The roam/loot/climb tunables now live in GrindBrain.
    private static final long STUCK_TELEPORT_MS = 30_000;     // no landed hit this long (observed) → teleport to a portal, re-grind
    private static final long STUCK_BAIL_MS = 60_000;         // still no hit after a teleport → bail the map (re-DECIDE)
    private static final long REPAIR_COOLDOWN_MS = 12_000;    // min gap between repair actions, so each one gets a window to prove out

    // ── Map crowding balance (spread the cohort across maps, not just spots) ──────
    // A map's bot carrying capacity = its claimable-spot count (SpotFinder.mapBotCapacity), so "map at
    // capacity" IS "every spot claimed" by construction. Selection (weightedPick) hard-caps at this; the
    // crowd-bail relieves a map that fills up after bots commit by sending the surplus a hop deeper.
    private static final long MAP_SATURATED_DWELL_MS = 8_000; // saturation must persist this long before a crowd-bail (ride out arrival races)
    private static final long MAP_EXCLUDE_MS = 45_000;        // don't re-pick a map we just left for crowding
    private static final int MAX_MAP_HOPS_PER_EPISODE = 2;    // crowd-driven map changes before the bot settles & shares
    // Crowd-bail runs unobserved too: an unobserved changeMap is invisible and near-free, and it levels
    // crowding BEFORE a player arrives — with the old observed-only gate, a stacked map showed its full
    // stack at the exact moment someone first walked in. Unobserved bots tick at 60-120s while grinding,
    // so unobserved relief paces itself in minutes, not seconds. Flip to false to restore the old gate.
    private static final boolean CROWD_BAIL_UNOBSERVED = true;

    // ── Ambient dialogue (context-token flavor; spoken only when observed, throttled) ──
    // Idle self-muttering only - reacting to nearby players is the movement driver's job
    // (BotPlayerReaction), so this stays deliberately sparse to avoid talkative spam.
    private static final long AMBIENT_MIN_MS = 120_000;      // min gap between idle grind lines (2 min)
    private static final long AMBIENT_MAX_MS = 240_000;      // max gap (4 min)
    // Self-buff cadence while grinding: re-show the bot's class buffs every 90-120s so a watched grinder
    // looks like it's keeping itself buffed (cosmetic - BotBuffDriver shows visuals only, no real stats).
    private static final long BUFF_MIN_MS = 90_000;
    private static final long BUFF_MAX_MS = 120_000;

    // ── Shared combat ticker (one task for ALL training bots) ────────────────
    private static final Set<TrainingBot> ACTIVE_GRINDERS = ConcurrentHashMap.newKeySet();
    private static volatile boolean combatTickerStarted = false;

    private static synchronized void ensureCombatTicker() {
        if (combatTickerStarted) {
            return;
        }
        combatTickerStarted = true;
        ExecutorServiceManager.getScheduledExecutorService().scheduleAtFixedRate(
                TrainingBot::combatTickAll, COMBAT_TICK_MS, COMBAT_TICK_MS, TimeUnit.MILLISECONDS);
    }

    // Swing every grinding bot whose map a real player can see. One exception per bot never stops the ticker.
    private static void combatTickAll() {
        long sweepStart = System.currentTimeMillis();
        for (TrainingBot bot : ACTIVE_GRINDERS) {
            try {
                bot.combatTick();
            } catch (Exception e) {
                // a single bot's combat error must never kill the shared ticker
            }
        }
        soloMapling.server.BotPerfStats.recordCombatSweep(
                System.currentTimeMillis() - sweepStart, ACTIVE_GRINDERS.size());
    }

    // How many bots the shared combat ticker currently visits (for !env perf).
    public static int activeGrinderCount() {
        return ACTIVE_GRINDERS.size();
    }

    // Fable Phase 3: an unobserved GRINDING bot is the deep-background case - abstract
    // EXP accrues by elapsed time (doGrind) and the watchdog skips unobserved bots, so
    // a 60-120s heartbeat loses nothing and cuts the idle macro load ~10x. All other
    // phases (travel/decide/town/shop) keep the normal slow tick so the bot's life
    // keeps moving between grind sessions. A player entering the map still wakes the
    // bot instantly via the map-entry nudge.
    @Override
    protected long lowPriorityDelayMs() {
        if (phase == Phase.GRIND) {
            return 60_000 + rng.nextInt(60_000);
        }
        return super.lowPriorityDelayMs();
    }

    private void combatTick() {
        Character chr = getChr();
        if (chr == null || !getRunning() || phase != Phase.GRIND) {
            return; // gated; removal happens in leaveGrind()/stopScheduledTask()
        }
        grind.tick(chr); // observed → spot grind (FIGHT⇄WAIT); unobserved → no-op (the macro tick accrues abstract EXP)
    }

    // ── Macro brain state ────────────────────────────────────────────────────
    private enum Phase { INIT, IN_TOWN, SHOP_TRAVEL, SHOP_DWELL, SHOP_RETURN, DECIDE, GO_TRAIN, GRIND, GO_TOWN,
        BREAK_TRAVEL, BREAK_REST }

    private volatile Phase phase = Phase.INIT;
    private boolean phaseEntered = false;     // has this phase run its one-time setup?
    private long phaseDeadlineMs = 0;         // travel timeout / town dwell

    // Async travel coordination — the callback fires on the GCMovement driver thread.
    private volatile boolean moveDone = false;
    private volatile boolean moveOk = false;
    // Travel progress watchdog: the trip's deadline resets whenever the bot advances a hop or moves, so the
    // 2-min budget means "no progress for 2 min", not "the whole trip within 2 min".
    private int travelLastMapId = -1;
    private Point travelLastPos = null;

    private int homeMapId = -1;           // the spawn town; GO_TOWN returns here
    private boolean firstTrip = true;     // first decision to go train: maybe warp straight there (see FIRST_TRIP_TELEPORT_CHANCE)
    private boolean midSessionGrind = false; // next grind starts partially elapsed (warped/in-place first trip — see MID_SESSION_FLOOR_MS)
    private final List<Integer> shopQueue = new ArrayList<>(); // store maps still to visit this town stop
    private int shopTargetMapId = -1;     // the store currently being travelled to
    private int currentTrainMapId = -1;   // the discovered map currently being trained on
    private int currentMobLevel = 0;      // its representative mob level (drives abstract EXP)
    private long grindUntilMs = 0;
    private long lastExpAccrualMs = 0;
    private long nextChatterMs = 0;        // throttle gate for ambient grind chatter
    private long nextBuffMs = 0;           // re-buff gate while grinding (only advances after an actual buff)
    private int lastKnownLevel = -1;       // tracks level to detect a level-up worth announcing

    // Sleepywood sauna flavor trip — an async errand that runs off the macro FSM; the IN_TOWN tick parks on it.
    private volatile boolean saunaTripActive = false; // a sauna trip's async chain is in flight
    private volatile boolean saunaTripDone = false;   // chain finished → macro tick advances to DECIDE
    private volatile long saunaTripDeadlineMs = 0;    // whole-trip watchdog deadline

    // ── Grind engine + macro watchdog state ──
    // The per-bot localized grind sub-FSM (SELECT_SPOT→TRAVEL→FIGHT⇄WAIT→RELOCATE). TrainingBot keeps the
    // macro brain and delegates each observed combat tick to it; it owns the combat heartbeat the watchdog reads.
    private final GrindBrain grind = new GrindBrain(this::debugChat);
    private boolean teleportedThisEpisode = false; // macro watchdog: a portal-teleport has already been tried this stuck episode
    private long lastRepairMs = 0L;             // macro watchdog: last self-repair action (cooldown gate)

    // ── Map crowding state (macro tick only) ──
    // Maps this bot recently left because they were saturated (mapId -> cooldown expiry), so DECIDE steers
    // away from them for a beat; the per-outing crowd-hop counter so a fully-packed region settles into
    // sharing instead of migrating forever; and the persist timer for the saturation dwell.
    private final Map<Integer, Long> mapCrowdCooldown = new HashMap<>();
    private int crowdHopsThisEpisode = 0;
    private long mapSaturatedSinceMs = 0L;       // when the current grind first read saturated (0 = not saturated)

    // ── Grind breaks (macro tick only; state + behavior in GrindBreakRoutine) ──
    private final GrindBreakRoutine breaks = new GrindBreakRoutine(this);

    private final Random rng = new Random();

    // Interactive menu (a player chats the bot's name; the Dispatcher routes it here via
    // displayCommands). Rides the shared inquirer/tertiary path - see BotOptionMenu. Two variants:
    // "Follow me!" only appears once the bot is actually in a party (asking a stranger to be
    // followed is meaningless), and "Wanna party up?" disappears once it is.
    // Keyword lists are matched by substring IN OPTION ORDER - keep the flavor-chat option's
    // keywords narrow so a typed sentence like "how about joining my party" can't hijack the
    // party roll (greedy "how"/"train" did exactly that in live test round 2).
    private final BotOptionMenu soloMenu = new BotOptionMenu(this,
            List.of("How's the training?", "Wanna party up?", "Goodbye"),
            List.of(List.of("hows", "how is", "how goes"),
                    List.of("party", "team", "join"),
                    List.of("bye", "goodbye", "cya", "later")),
            this::onSoloMenuSelect);
    private final BotOptionMenu partyMenu = new BotOptionMenu(this,
            List.of("How's the training?", "Follow me!", "Goodbye"),
            List.of(List.of("hows", "how is", "how goes"),
                    List.of("follow", "come", "lead"),
                    List.of("bye", "goodbye", "cya", "later")),
            this::onPartyMenuSelect);

    public TrainingBot(Character character) {
        super(character);
        botType = "TrainingBot";
        dialoguePath = "TrainingBotDialogue.yaml";
    }

    @Override
    public void displayCommands(Character chr) {
        if (getChr().getParty() != null) {
            soloMenu.deactivate();
            partyMenu.show(chr);
        } else {
            partyMenu.deactivate();
            soloMenu.show(chr);
        }
    }

    private boolean menuActive() {
        return soloMenu.isActive() || partyMenu.isActive();
    }

    // Mid-recruit: a conversation is open or the bot said "invite me" and the window is live.
    // Grind self-repair (teleport/bail), crowd-bail, and the session-end walk-off all hold during
    // this - a bot that says yes and then blinks across the map breaks the exchange.
    boolean recruitingNow() { // package-visible: GrindBreakRoutine defers its stand-up on a live recruit
        return menuActive() || BotRecruitManager.isArmed(getChr().getId());
    }

    // Conversing players expect snappy replies; and once the bot has said "invite me" it must keep
    // ticking fast through the whole armed window so pollRecruitInvite drains the incoming invite
    // promptly (not just while the menu is open). Otherwise the base observed/unobserved policy holds
    // (including the 60-120s deep-grind override below).
    @Override
    public void checkPrioritySpeed() {
        if (recruitingNow()) {
            setPriorityHigh();
            return;
        }
        super.checkPrioritySpeed();
    }

    private void enterPhase(Phase next) {
        if (phase == Phase.SHOP_DWELL) {
            BotWanderSystem.stop(getChr()); // leaving a shop: end the flavor wander before travelling out
        }
        if (phase == Phase.IN_TOWN) {
            TownLoiter.stop(getChr()); // leaving town: end the loiter (stop fidget, free the ledge claim)
        }
        phase = next;
        phaseEntered = false;
        moveDone = false;
        moveOk = false;
        debugChat("phase -> " + next);
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        Character chr = getChr();
        if (chr == null || chr.getMap() == null) {
            return;
        }
        getDebugger().debugLoggingFull(
                String.format("%s TrainingBot phase: %s", chr.getName(), phase), String.format("%s", phase));

        switch (phase) {
            case INIT -> doInit();
            case IN_TOWN -> doInTown();
            case SHOP_TRAVEL -> doTravel(shopTargetMapId, Phase.SHOP_DWELL, Phase.SHOP_RETURN);
            case SHOP_DWELL -> doShopDwell();
            case SHOP_RETURN -> doTravel(homeMapId >= 0 ? homeMapId : MapId.HENESYS,
                    Phase.DECIDE, Phase.DECIDE);
            case DECIDE -> doDecide();
            case GO_TRAIN -> doTravel(currentTrainMapId, Phase.GRIND, Phase.DECIDE);
            case GRIND -> doGrind();
            case GO_TOWN -> doTravel(homeMapId >= 0 ? homeMapId : MapId.HENESYS,
                    Phase.IN_TOWN, Phase.IN_TOWN);
            case BREAK_TRAVEL -> doBreakTravel();
            case BREAK_REST -> doBreakRest();
        }

        pollRecruitInvite(); // gated party-invite drain (the "wanna party up?" flow)
        soloMenu.poll();
        partyMenu.poll();    // kept last: a "Follow me!" selection converts this bot away
    }

    // ── Phases ───────────────────────────────────────────────────────────────

    private void doInit() {
        ensureCombatTicker();
        homeMapId = getChr().getMapId();
        // No mobs here → it's a town: do the town beat first. Has mobs → a field: decide immediately.
        enterPhase(MapMobIndex.level(homeMapId) < 0 ? Phase.IN_TOWN : Phase.DECIDE);
    }

    // In a town hub: settle a beat, maybe plan a shop trip, then either head to the first store or decide
    // what to train on. Reached at spawn and after every grind, so shop trips add organic timing variety.
    private void doInTown() {
        // A Sleepywood sauna trip runs its own async chain off the FSM — park here while it's in flight (and
        // force-recover if it overruns). Gated by homeMapId below, so no other town ever enters this branch.
        if (saunaTripActive) {
            if (now() > saunaTripDeadlineMs) {
                abortSaunaTrip();
            }
            return;
        }
        if (saunaTripDone) {
            saunaTripDone = false;
            enterPhase(Phase.DECIDE); // trip finished — resume the grind loop
            return;
        }
        if (!phaseEntered) {
            phaseEntered = true;
            crowdHopsThisEpisode = 0; // back home -> fresh outing, reset the crowd-migration budget
            buildShopPlan();
            // A deep hub's restock is an ON-MAP food-cart / grocer (no separate store map), so walk to it in
            // place and linger like a shop browse instead of idling on the entry portal. Extends the dwell.
            boolean errand = maybeStartHubErrand();
            if (!errand) {
                // Normal town return: walk off the arrival portal to a scattered spot and fidget there, so a
                // town of returning grinders reads as milling players instead of a statue clump on the portal.
                // A hub errand already walks the bot to its vendor, so it doesn't need this.
                TownLoiter.settle(getChr());
            }
            phaseDeadlineMs = now() + (errand ? shopDwellMs() : dwellMs());
            if (rng.nextInt(3) == 0) {
                BotEmote(getChr(), 1 + rng.nextInt(7)); // a little life in town
            }
            return;
        }
        if (now() < phaseDeadlineMs) {
            return;
        }
        if (!shopQueue.isEmpty()) {
            shopTargetMapId = shopQueue.remove(0);
            enterPhase(Phase.SHOP_TRAVEL);
        } else if (homeMapId == MapId.SLEEPYWOOD && rng.nextDouble() < SHOP_VISIT_CHANCE) {
            startSaunaTrip(); // Sleepywood has no stores — its errand is a trip to the hotel sauna
        } else {
            enterPhase(Phase.DECIDE);
        }
    }

    // Decide this town stop's store itinerary: nothing, just potions, just equips, or both in either order.
    private void buildShopPlan() {
        shopQueue.clear();
        int[] shops = TOWN_SHOPS.get(homeMapId);
        if (shops == null || rng.nextDouble() > SHOP_VISIT_CHANCE) {
            return; // unknown town, or skipping the shops this visit
        }
        int potion = shops[0];
        int equip = shops[1];
        switch (rng.nextInt(4)) {
            case 0 -> shopQueue.add(potion);
            case 1 -> shopQueue.add(equip);
            case 2 -> { shopQueue.add(potion); shopQueue.add(equip); }
            default -> { shopQueue.add(equip); shopQueue.add(potion); }
        }
    }

    // Deep-hub town errand: a hub sits inside a dungeon, so its "shop" is an on-map food-cart / grocer NPC
    // rather than a separate store map (TOWN_SHOPS). With the usual shop chance, walk to that vendor and
    // linger — reads as restocking pots — instead of idling on the entry portal. Returns true when started
    // (so IN_TOWN uses the longer shop-browse dwell). Only fires while actually standing on the home hub.
    private boolean maybeStartHubErrand() {
        Character chr = getChr();
        DeepHub.Info hub = DeepHub.of(homeMapId);
        if (hub == null || hub.potionNpc() == null || chr.getMapId() != homeMapId) {
            return false; // not a deep hub, no on-map vendor, or not currently at home
        }
        if (!shopQueue.isEmpty() || rng.nextDouble() >= SHOP_VISIT_CHANCE) {
            return false; // a store itinerary already covers this stop, or skipping the errand this visit
        }
        GCMovement.move(chr, hub.potionNpc().x, hub.potionNpc().y);
        sayContext("ShopRestock", chr, null);
        return true;
    }

    // Browse a store for a randomized beat, then move to the next store or head back out to train.
    private void doShopDwell() {
        if (!phaseEntered) {
            phaseEntered = true;
            phaseDeadlineMs = now() + shopDwellMs();
            if (rng.nextInt(2) == 0) {
                BotEmote(getChr(), 1 + rng.nextInt(7));
            }
            sayContext("ShopRestock", getChr(), null);
            BotWanderSystem.start(getChr()); // browse the shop map (whole-map) instead of piling on the portal
            return;
        }
        if (now() < phaseDeadlineMs) {
            return;
        }
        if (!shopQueue.isEmpty()) {
            shopTargetMapId = shopQueue.remove(0);
            enterPhase(Phase.SHOP_TRAVEL);
        } else {
            enterPhase(Phase.SHOP_RETURN);
        }
    }

    // ── Sleepywood flavor: hotel sauna trip (Sleepywood-home bots only) ────────
    // A self-contained async errand standing in for the shop trip Sleepywood can't offer (no potion/equip
    // stores). It runs entirely off the macro FSM via chained callbacks — no new Phase states: the IN_TOWN
    // tick parks on saunaTripActive while it runs and resumes at DECIDE when it finishes. Gated by
    // homeMapId == SLEEPYWOOD, so it can never leak into any other town's behavior.
    //   town -> hotel -> walk to the (non-portal) sauna door -> warp into a sauna -> wander -> exit -> town.
    private void startSaunaTrip() {
        Character chr = getChr();
        if (chr == null) {
            enterPhase(Phase.DECIDE);
            return;
        }
        TownLoiter.stop(chr); // this async errand leaves town without enterPhase - end the loiter first
        saunaTripActive = true;
        saunaTripDone = false;
        saunaTripDeadlineMs = now() + SAUNA_TRIP_TIMEOUT_MS;
        debugChat("Sleepywood errand -> heading to the sauna");
        if (rng.nextInt(2) == 0) {
            BotEmote(chr, 1 + rng.nextInt(7)); // a beat of life before heading off
        }
        GCMovement.travel(chr, SLEEPYWOOD_HOTEL, ok -> enterSaunaFromHotel(chr));
    }

    // In the hotel: walk to the sauna door for the visual, then warp into a sauna (the NPC-script teleport is
    // pure smoke-and-mirrors server-side). The warp fires on arrival, or after a cap so a missed/abandoned
    // door-walk can't stall the trip — whichever comes first, exactly once.
    private void enterSaunaFromHotel(Character chr) {
        if (!saunaTripActive) {
            return; // aborted while travelling to the hotel
        }
        AtomicBoolean entered = new AtomicBoolean(false);
        Runnable enter = () -> {
            if (!saunaTripActive || !entered.compareAndSet(false, true)) {
                return; // already entered (door-arrival vs cap race), or the trip was aborted
            }
            int sauna = rng.nextDouble() < SAUNA_VIP_CHANCE ? SAUNA_VIP : SAUNA_REGULAR;
            chr.changeMap(sauna);
            BotWanderSystem.start(chr); // relax in the sauna for a beat
            ExecutorServiceManager.getScheduledExecutorService().schedule(
                    () -> leaveSauna(chr), shopDwellMs(), TimeUnit.MILLISECONDS);
        };
        GCMovement.move(chr, SAUNA_DOOR_X, SAUNA_DOOR_Y, enter);
        ExecutorServiceManager.getScheduledExecutorService().schedule(enter, DOOR_WALK_MAX_MS, TimeUnit.MILLISECONDS);
    }

    // Done relaxing: the sauna's one-way exit portal drops the bot back into the hotel; from there take the
    // hotel's portal back down to Sleepywood town. Then hand control back to the macro tick (-> DECIDE).
    private void leaveSauna(Character chr) {
        if (!saunaTripActive) {
            return;
        }
        BotWanderSystem.stop(chr);
        GCMovement.travel(chr, SLEEPYWOOD_HOTEL, okHotel ->
                GCMovement.travel(chr, homeMapId, okTown -> finishSaunaTrip()));
    }

    private void finishSaunaTrip() {
        if (!saunaTripActive) {
            return; // already aborted/finished — don't re-arm the handoff
        }
        saunaTripActive = false;
        saunaTripDone = true; // doInTown advances to DECIDE on the macro thread
    }

    // Trip watchdog (macro thread): the chained callbacks can silently drop on an unreachable hop, so if the
    // whole errand overruns, cut it and hard-recover to town rather than leaving the bot parked forever.
    private void abortSaunaTrip() {
        Character chr = getChr();
        debugChat("sauna trip overran -> force-recover to town");
        saunaTripActive = false; // disarm any in-flight callbacks first
        if (chr != null) {
            BotWanderSystem.stop(chr);
            GCMovement.cancelTravel(chr);
            GCMovement.stop(chr);
            if (homeMapId >= 0 && chr.getMapId() != homeMapId) {
                chr.changeMap(homeMapId);
            }
        }
        saunaTripDone = true;
    }

    // Discover a field map near the current town (WZ-driven): level-scaled wander radius, weighted
    // toward level-appropriate + less-crowded maps, with a chance to chill at an easier map.
    private void doDecide() {
        Character chr = getChr();
        // Station-here handoff (FollowerBot's "Train here with me!"): grind the current map, skip
        // discovery entirely - the map BFS excludes its origin, so without this the bot always migrates.
        if (BotRecruitManager.consumeStationHere(chr.getId()) && MapMobIndex.level(chr.getMapId()) >= 0) {
            debugChat("DECIDE: station-here -> grind current map " + chr.getMapId());
            setTrainTarget(chr.getMapId(), Math.max(1, MapMobIndex.level(chr.getMapId())));
            firstTrip = false;
            enterPhase(Phase.GRIND);
            return;
        }
        // Party-aware: partied with a real player -> train on their map (or hold the current map
        // while they're parked somewhere mobless). Deliberately skips the capacity/crowd weighting -
        // the player chose this map, and party EXP needs same-map anyway.
        Integer partyMap = partyTargetMapId();
        if (partyMap != null) {
            debugChat("DECIDE: party target -> map " + partyMap);
            setTrainTarget(partyMap, Math.max(1, MapMobIndex.level(partyMap)));
            firstTrip = false;
            enterPhase(chr.getMapId() == partyMap ? Phase.GRIND : Phase.GO_TRAIN);
            return;
        }
        Set<Integer> excluded = crowdExcludedMaps(); // maps on crowd-cooldown (pruned) — steer away from them
        clearTrainTarget(); // release any stale reservation before choosing (choose() reserves the pick's slot)
        TrainingMap pick = TrainingMapChooser.choose(chr, homeMapId, excluded, this::debugChat);
        if (pick == null) {
            // Nothing reachable with mobs (or all on cooldown) — idle in town and try again later.
            debugChat("DECIDE: nothing reachable (lv " + chr.getLevel() + ") -> idle in town");
            enterPhase(Phase.IN_TOWN);
            return;
        }
        // choose() already holds the pick's occupancy slot — record it without re-reserving.
        currentTrainMapId = pick.mapId();
        currentMobLevel = Math.max(1, pick.mobLevel());
        debugChat("DECIDE: chose map " + pick.mapId() + " (mob lv " + pick.mobLevel() + ")");
        // First trip only (mainly startup): most bots warp straight to the grind map instead of walking, so
        // the world reads as already-populated. Skipped when the town is observed (don't blink out in front of
        // a player) or already on the picked map. doGrind's entry then runs the normal grind setup. Consumed
        // either way, so every subsequent trip travels naturally.
        boolean wasFirstTrip = firstTrip;
        firstTrip = false;
        boolean migrates = chr.getMapId() != pick.mapId();
        if (wasFirstTrip && migrates
                && !GCMovement.isMapObserved(chr.getMapId())
                && rng.nextDouble() < FIRST_TRIP_TELEPORT_CHANCE) {
            debugChat("DECIDE: first-trip warp -> map " + pick.mapId());
            midSessionGrind = true; // warped in -> reads as already-grinding -> shortened first session
            GCMovement.stop(chr); // clear any stale town move intent before the warp
            chr.changeMap(pick.mapId());
            enterPhase(Phase.GRIND);
            return;
        }
        if (wasFirstTrip && !migrates) {
            midSessionGrind = true; // already on the grind map (no visible travel) -> also a mid-session start
        }
        // First trip with a real cross-map walk (the ~30% that don't warp): we watch it travel, so it reads as
        // freshly heading out -> a full session is correct. midSessionGrind stays false.
        enterPhase(Phase.GO_TRAIN);
    }

    // Issue a one-shot travel to destMapId and advance on the async result. Already on the map -> straight
    // to onArrive. Timed out / failed -> onFail. Non-blocking: re-polls each macro tick while the trip
    // is in flight.
    private void doTravel(int destMapId, Phase onArrive, Phase onFail) {
        Character chr = getChr();
        if (destMapId < 0) {
            enterPhase(onFail);
            return;
        }
        if (chr.getMapId() == destMapId) {
            enterPhase(onArrive);
            return;
        }
        if (!phaseEntered) {
            phaseEntered = true;
            phaseDeadlineMs = now() + TRAVEL_TIMEOUT_MS;
            travelLastMapId = chr.getMapId();
            travelLastPos = chr.getPosition();
            debugChat("travel -> map " + destMapId);
            GCMovement.travel(chr, destMapId, ok -> {
                moveOk = (ok != null && ok);
                moveDone = true;
            });
            return;
        }
        if (moveDone) {
            if (!moveOk) {
                debugChat("travel reported FAIL -> " + onFail);
            }
            enterPhase(moveOk ? onArrive : onFail);
            return;
        }
        // Progress-aware watchdog: while the bot is advancing maps OR moving across the current one, it's
        // travelling fine (a big map / long route is expected) — keep pushing the deadline out. Only a trip
        // that makes no progress at all for the whole window is wedged; abandon it and re-plan (no teleport).
        if (madeTravelProgress(chr)) {
            phaseDeadlineMs = now() + TRAVEL_TIMEOUT_MS;
        } else if (now() > phaseDeadlineMs) {
            debugChat("travel STUCK: no progress for " + (TRAVEL_TIMEOUT_MS / 1000) + "s -> " + onFail);
            GCMovement.cancelTravel(chr);
            enterPhase(onFail);
        }
        // else still travelling — wait for the next tick
    }

    // True if the bot advanced a hop (its map changed) or moved on its current map since the last check. The
    // position anchor only advances when movement clears the epsilon, so slow steady travel still accumulates
    // into progress across ticks instead of reading as stuck.
    private boolean madeTravelProgress(Character chr) {
        int curMap = chr.getMapId();
        if (curMap != travelLastMapId) {
            travelLastMapId = curMap;
            travelLastPos = chr.getPosition(); // hop completed — unambiguous progress; re-anchor on the new map
            return true;
        }
        Point pos = chr.getPosition();
        if (pos != null && (travelLastPos == null
                || Math.abs(pos.x - travelLastPos.x) + Math.abs(pos.y - travelLastPos.y) > TRAVEL_PROGRESS_EPS_PX)) {
            travelLastPos = pos;
            return true;
        }
        return false;
    }

    // At a training map: anchor on a platform, swing (when observed) via the shared ticker, accrue EXP.
    private void doGrind() {
        Character chr = getChr();
        if (!phaseEntered) {
            phaseEntered = true;
            teleportedThisEpisode = false;
            GCMovement.setGrinding(chr, true); // engage grind nav guards (no idle-hang on ropes)
            grind.start(chr); // fresh heartbeat, select a spot, and move into it (disperses cohorts even unobserved)
            boolean resumingFromBreak = breaks.resuming();
            grindUntilMs = now() + (resumingFromBreak ? breaks.consumeResumeRemaining() : grindSessionMs());
            breaks.schedule(grindUntilMs, resumingFromBreak);
            lastExpAccrualMs = now();
            nextBuffMs = now(); // buff up as soon as a player can see it (looks already-buffed on arrival)
            ACTIVE_GRINDERS.add(this);
            lastKnownLevel = chr.getLevel();
            if (!resumingFromBreak) {
                sayContext("GrindStart", chr, null); // a resumed session already spoke BreakOver
            }
            debugChat("GRIND on map " + chr.getMapId() + " " + grind.spotLabel());
            return;
        }

        // Abstract EXP only when unobserved (observed time is covered by real kills via the ticker).
        long nowMs = now();
        double elapsedSec = (nowMs - lastExpAccrualMs) / 1000.0;
        lastExpAccrualMs = nowMs;
        if (elapsedSec > 0 && !GCMovement.isMapObserved(chr.getMapId()) && currentMobLevel > 0) {
            accrueAbstractExp(currentMobLevel, elapsedSec);
        }

        // Announce a level-up the moment it becomes visible (real kills on an observed map).
        int lvl = chr.getLevel();
        if (lastKnownLevel >= 0 && lvl > lastKnownLevel) {
            sayContext("LevelUp", chr, null);
        }
        lastKnownLevel = lvl;

        maybeGrindChatter(chr);
        maybeSelfBuff(chr);

        if (grindWatchdog(chr)) {
            return; // self-repair left the map (re-DECIDE) — skip the normal grind-timer transition
        }

        if (grindCrowdBail(chr)) {
            return; // map too crowded — left for a deeper one (re-DECIDE); skip the grind-timer transition
        }

        if (breaks.due() && !recruitingNow()) {
            // begin() consumes the due-flag either way; false = no safe rest spot -> session continues.
            if (breaks.begin(chr, grind, Math.max(MID_SESSION_FLOOR_MS, grindUntilMs - nowMs))) {
                enterPhase(Phase.BREAK_TRAVEL);
            }
            return;
        }

        if (nowMs >= grindUntilMs && !recruitingNow()) { // don't walk off mid-conversation/invite window
            sayContext("MapTransition", chr, null); // "this spot's dead, moving on" — said while still on the map
            leaveGrind();
            enterPhase(Phase.GO_TOWN);
        }
    }

    // Macro self-repair watchdog (runs on the slow tick, observed only). The combat ticker keeps the
    // heartbeat (lastCombatProgressMs = last landed hit). If that goes stale the bot isn't engaging anything:
    //   ~30s + a reachable mob nearby → wedged/bad-pathing → teleport to the nearest portal and re-grind.
    //   ~30s + NO reachable mob, or ~60s after a teleport that didn't help → this map's a dead end → re-DECIDE.
    // A landed hit (heartbeat refresh) drops it back below the threshold and resets the ladder. Returns true
    // only when it changed phase (left the map), so doGrind skips its own grind-timer transition that tick.
    private boolean grindWatchdog(Character chr) {
        if (chr == null || !GCMovement.isMapObserved(chr.getMapId())) {
            return false; // unobserved bots don't fight (abstract EXP) — they can't be physically stuck
        }
        if (recruitingNow()) {
            return false; // mid-conversation / live invite window: never teleport or bail from under the player
        }
        long stuck = grind.msSinceProgress();
        if (stuck < STUCK_TELEPORT_MS) {
            teleportedThisEpisode = false; // making (or recently made) progress → reset the ladder
            return false;
        }
        if (now() - lastRepairMs < REPAIR_COOLDOWN_MS) {
            return false; // let the previous repair take effect before escalating
        }
        boolean mobReachable = isReachableHostileNearby(chr);
        boolean recovering = grind.isRecovering(chr);
        if (mobReachable && !teleportedThisEpisode && !recovering) {
            // A mob is reachable but we still can't land a hit (wedged / pathing failure) → reset position.
            debugChat("watchdog: no hit for " + (stuck / 1000) + "s, mob reachable -> teleport to portal & re-grind");
            teleportToNearestPortal(chr);
            grind.resetupAfterTeleport(chr); // fresh kill window + re-select a spot from the new position
            teleportedThisEpisode = true;
            lastRepairMs = now();
            return false;
        }
        if (!mobReachable || stuck >= STUCK_BAIL_MS) {
            // Nothing to fight here, or a teleport already failed to help → leave and pick a new map.
            debugChat("watchdog: stuck " + (stuck / 1000) + "s (" + (mobReachable ? "teleport didn't help" : "no reachable mob") + ") -> bail map, re-decide");
            leaveGrind();
            enterPhase(Phase.DECIDE);
            return true;
        }
        return false;
    }

    // Crowd-balance escalation (macro tick; runs unobserved too — see CROWD_BAIL_UNOBSERVED). The grind
    // sub-FSM reports the map saturated when every reachable spot is claimed and the bot is sharing; if
    // that persists past the dwell, leave for a
    // deeper, less-crowded map — capacity-aware DECIDE spills us outward. Three guards stop thrash: a persist
    // timer (ignore arrival-race blips), a per-map cooldown (no instant return), and a per-outing hop cap (a
    // fully packed region settles into sharing instead of migrating forever). Returns true if it left the map.
    private boolean grindCrowdBail(Character chr) {
        if (chr == null) {
            return false;
        }
        Integer partyMap = partyTargetMapId();
        if (partyMap != null && partyMap == chr.getMapId()) {
            return false; // never crowd-bail off the party's map - staying with the player wins
        }
        if (recruitingNow()) {
            return false; // mid-recruit: hold the map until the exchange resolves
        }
        if (!CROWD_BAIL_UNOBSERVED && !GCMovement.isMapObserved(chr.getMapId())) {
            return false; // old gate: only relieve crowding a player can actually see
        }
        if (!grind.mapSaturated() || crowdHopsThisEpisode >= MAX_MAP_HOPS_PER_EPISODE) {
            mapSaturatedSinceMs = 0L; // not saturated, or out of hops -> reset the persist timer
            return false;
        }
        if (mapSaturatedSinceMs == 0L) {
            mapSaturatedSinceMs = now(); // first saturated tick -> start the dwell
            return false;
        }
        if (now() - mapSaturatedSinceMs < MAP_SATURATED_DWELL_MS) {
            return false; // saturation must persist (ride out a cohort-arrival race before bailing)
        }
        int leaving = chr.getMapId();
        debugChat("crowd: map " + leaving + " saturated -> bail to a deeper map");
        mapCrowdCooldown.put(leaving, now() + MAP_EXCLUDE_MS);
        crowdHopsThisEpisode++;
        mapSaturatedSinceMs = 0L;
        leaveGrind();
        enterPhase(Phase.DECIDE);
        return true;
    }

    // ── Grind breaks (rest flavor — behavior in GrindBreakRoutine; the phases just delegate) ──

    // Dev hook (!bot grindstyle): force this bot's grind archetype for testing (auto = back to the
    // profile-resolved natural style). A live grind swaps its strategy on the next combat tick; an
    // idle bot applies it when it next grinds. Returns a status line, or null on an unknown style.
    public String forceGrindStyle(String styleName) {
        soloMapling.ArtificialPlayer.BotGrindSystem.GrindStyle wanted = null;
        if (!styleName.equalsIgnoreCase("auto")) {
            try {
                wanted = soloMapling.ArtificialPlayer.BotGrindSystem.GrindStyle.valueOf(styleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        grind.forceStyle(wanted);
        nudgeSoon(300); // wake the macro brain so an idle bot re-decides promptly
        return getChr().getName() + ": grind style -> " + (wanted == null ? "AUTO" : wanted)
                + (phase == Phase.GRIND ? " (live swap on next combat tick)" : " (applies at next grind)");
    }

    // Dev hook (!bot breaknow): pull a break to the next grind tick.
    public boolean forceBreakNow() {
        if (phase != Phase.GRIND) {
            return false;
        }
        breaks.forceNow();
        nudgeSoon(300);
        return true;
    }

    private void doBreakTravel() {
        phaseEntered = true; // the routine owns its own setup latches (armed in begin())
        if (breaks.tickTravel(getChr())) {
            enterPhase(Phase.BREAK_REST);
        }
    }

    private void doBreakRest() {
        phaseEntered = true;
        if (breaks.tickRest(getChr())) {
            enterPhase(Phase.GRIND); // re-enters with the saved remainder; grind.start re-picks a spot
        }
    }

    // Break-sign flavor line ("brb" chalkboard). Stays here for the dialogue plumbing (dialoguePath).
    String breakSignText() {
        try {
            String line = BotDialogueHandler.getRandomResolvedLine(dialoguePath, botType, "BreakSign", getChr(), null);
            if (line != null) {
                return line;
            }
        } catch (Exception e) {
            // fall through to the default sign
        }
        return "brb";
    }

    // Is there a hostile mob the bot could actually walk/jump to from where it stands? Reachability-filtered
    // (a mob stranded on a platform the bot can't get to doesn't count) using the same foothold-snap the
    // approach uses, so a jumping mob still resolves to its real platform.
    private boolean isReachableHostileNearby(Character chr) {
        MapleMap map = chr.getMap();
        Point pos = chr.getPosition();
        if (map == null || pos == null) {
            return false;
        }
        Set<Integer> reach = GCMovement.reachableRegions(map, pos.x, pos.y);
        if (reach.isEmpty()) {
            return false;
        }
        for (Monster m : map.getAllMonsters()) {
            if (!SpotFinder.isHostile(m) || m.getPosition() == null) {
                continue;
            }
            Point g = GCMovement.groundPointBelow(map, m.getPosition().x, m.getPosition().y);
            if (g != null && reach.contains(GCMovement.regionIdAt(map, g.x, g.y))) {
                return true;
            }
        }
        return false;
    }

    // Hard-teleport to the nearest portal on the current map — a guaranteed-valid foothold — to break a
    // geometry wedge. The caller re-runs setupGrind from the new spot.
    private void teleportToNearestPortal(Character chr) {
        MapleMap map = chr.getMap();
        Point pos = chr.getPosition();
        if (map == null || pos == null) {
            return;
        }
        Point best = null;
        double bestSq = Double.MAX_VALUE;
        for (Portal p : map.getPortals()) {
            Point pp = p.getPosition();
            if (pp == null) {
                continue;
            }
            double dsq = pos.distanceSq(pp);
            if (dsq < bestSq) {
                bestSq = dsq;
                best = pp;
            }
        }
        if (best != null) {
            GCMovement.stop(chr); // drop the in-flight move so the driver re-acquires cleanly from the new spot
            GCMovement.teleportTo(chr, best.x, best.y);
        }
    }

    private void leaveGrind() {
        ACTIVE_GRINDERS.remove(this);
        grind.release(getChr()); // drop the spot claim + reset combat state
        clearTrainTarget(); // release this map's occupancy slot
        GCMovement.setRestHold(getChr(), false); // belt-and-braces: never leave a rope rest hold set on bail
        GCMovement.setGrinding(getChr(), false); // back to normal nav for travel
        GCMovement.stop(getChr()); // clear the roam target before travelling
    }

    // ── Ambient dialogue (context-aware flavor) ───────────────────────────────

    // Occasionally mutters to itself while grinding (GrindAmbient). Reacting to nearby players is handled
    // by the movement driver's BotPlayerReaction layer, so this is idle-only and rarely fires. Throttled
    // by nextChatterMs (2-4 min); only speaks when a player is observing the map.
    private void maybeGrindChatter(Character chr) {
        long t = now();
        if (t < nextChatterMs) {
            return;
        }
        nextChatterMs = t + AMBIENT_MIN_MS + (long) (rng.nextDouble() * (AMBIENT_MAX_MS - AMBIENT_MIN_MS));
        if (!GCMovement.isMapObserved(chr.getMapId())) {
            return; // gate advanced so we don't re-roll every tick; nothing worth saying unobserved
        }
        sayContext("GrindAmbient", chr, null);
    }

    // Re-show the bot's class buffs every 90-120s while grinding. Only fires when a player can see it and
    // the bot is grounded (no buffing from a rope); the timer advances only on an actual buff, so an
    // unobserved bot buffs the instant it's first observed and then keeps to the cadence. forceBuff
    // ignores BotBuffDriver's own duration timers so the visible re-buff stays on this fixed cadence.
    private void maybeSelfBuff(Character chr) {
        if (now() < nextBuffMs) {
            return;
        }
        if (!GCMovement.isMapObserved(chr.getMapId())) {
            return; // hold the timer until it's worth showing.
        }
        BotBuffDriver.forceBuff(chr);
        nextBuffMs = now() + BUFF_MIN_MS + (long) (rng.nextDouble() * (BUFF_MAX_MS - BUFF_MIN_MS));
    }

    // Fires a context-token dialogue node, but only when a real player can see it (observed map), and
    // off the macro thread so the line's hold never blocks the tick. Biased toward plain lines over
    // {TOKEN} ones (CONTEXT_LINE_CHANCE). player may be null (self-only nodes).
    void sayContext(String node, Character chr, Character player) { // package-visible: GrindBreakRoutine speaks through the bot
        if (chr == null || !GCMovement.isMapObserved(chr.getMapId())) {
            return;
        }
        ExecutorServiceManager.runAsync(() ->
                getDialogueHandler().executeBotContextDialogue(node, this, player, BotDialogueHandler.CONTEXT_LINE_CHANCE));
    }

    // ── Party recruiting & interactive menu ─────────────────────────────────

    // The map a partied bot should train on: the first real (non-bot, online) party member's map if
    // it's grindable; else hold the current map if that's grindable (the player's in a town buying
    // pots - don't wander off); else null (normal weighted pick).
    private Integer partyTargetMapId() {
        Party party = getChr().getParty();
        if (party == null) {
            return null;
        }
        Character member = firstRealPartyMember(party);
        if (member == null) {
            return null;
        }
        int memberMap = member.getMapId();
        if (MapMobIndex.level(memberMap) >= 0) {
            return memberMap;
        }
        if (MapMobIndex.level(getChr().getMapId()) >= 0) {
            return getChr().getMapId();
        }
        return null;
    }

    private Character firstRealPartyMember(Party party) {
        for (PartyCharacter pc : party.getMembers()) {
            Character p = pc == null ? null : pc.getPlayer();
            if (p != null && !isBot(p) && p.getMap() != null) {
                return p;
            }
        }
        return null;
    }

    // Answers any pending party invite every tick: accept only the player the dialogue armed
    // (rollPartyAsk), politely reject everything else so the first-wins queue never rots.
    private void pollRecruitInvite() {
        Character chr = getChr();
        if (!BotPartyQueue.getInstance().hasPendingInvite(chr)) {
            return;
        }
        int recruiterId = BotRecruitManager.armedInviterId(chr.getId()); // read BEFORE poll - JOINED clears it
        if (BotRecruitManager.pollInvites(chr) == BotRecruitManager.InvitePoll.JOINED) {
            Character recruiter = chr.getClient().getChannelServer()
                    .getPlayerStorage().getCharacterById(recruiterId);
            sayRecruit("PartyJoined", recruiter);
            // No re-typing: a partied TrainingBot keeps grinding; DECIDE is now party-aware.
        }
    }

    private void onSoloMenuSelect(int idx, Character player) {
        switch (idx) {
            case 0 -> { // How's the training?
                sayRecruit("TrainingTalk", player);
                soloMenu.show(player);
            }
            case 1 -> optPartyAsk(player);
            default -> { // Goodbye
                sayRecruit("Goodbye", player);
                soloMenu.close(player);
            }
        }
    }

    private void onPartyMenuSelect(int idx, Character player) {
        switch (idx) {
            case 0 -> { // How's the training?
                sayRecruit("TrainingTalk", player);
                partyMenu.show(player);
            }
            case 1 -> optFollowMe(player);
            default -> { // Goodbye
                sayRecruit("Goodbye", player);
                partyMenu.close(player);
            }
        }
    }

    private void optPartyAsk(Character player) {
        Character chr = getChr();
        if (chr.getParty() != null) { // partied since the solo menu was shown
            sayRecruit(inSameParty(player) ? "AlreadyPartied" : "PartyDecline", player);
            soloMenu.close(player);
            return;
        }
        BotRecruitManager.RecruitAnswer ans = BotRecruitManager.rollPartyAsk(
                chr, player, BotRecruitManager.TRAINING_ACCEPT_CHANCE, false);
        if (ans == BotRecruitManager.RecruitAnswer.ACCEPTED) {
            sayRecruit("PartyAccept", player);
            soloMenu.close(player); // pollRecruitInvite now waits for this player's invite
        } else {
            sayRecruit("PartyDecline", player);
            soloMenu.show(player);
        }
    }

    // Follow me! -> become this player's FollowerBot (same-party gated; the shower only picked
    // the party menu, the ASKER may still be a stranger).
    private void optFollowMe(Character player) {
        Character chr = getChr();
        if (!inSameParty(player)) {
            sayRecruit("PartyFirst", player);
            partyMenu.close(player);
            return;
        }
        if (BotRecruitManager.activeFollowerCount() >= BotRecruitManager.FOLLOWER_CAP) {
            sayRecruit("PartyDecline", player);
            partyMenu.close(player);
            return;
        }
        sayRecruit("FollowAccept", player);
        partyMenu.close(player);
        BotRecruitManager.setPendingLeader(chr.getId(), player.getId());
        // stopScheduledTask (via convert) releases the grind: spot claim, occupancy slot,
        // combat/buff drivers, GC movement lock.
        BotTypeManager.convertBotType(chr, BotTypeManager.BotType.FOLLOWER_BOT);
    }

    private boolean inSameParty(Character player) {
        return getChr().getParty() != null && player.getParty() != null
                && player.getParty().getId() == getChr().getParty().getId();
    }

    // Direct menu reply to the asking player (they're on the map by definition - no observation
    // gate, no context-chance bias like sayContext).
    private void sayRecruit(String node, Character player) {
        Character chr = getChr();
        if (chr == null || chr.getMap() == null) {
            return;
        }
        try {
            String line = BotDialogueHandler.getRandomResolvedLine(dialoguePath, botType, node, chr, player);
            if (line != null) {
                BotSpeak(chr, line);
            }
        } catch (Exception e) {
            // a missing dialogue node must never break the tick
        }
    }

    // Debug narration (OFF by default). When enabled, the bot announces what it's doing — every phase change
    // and the notable fail/repair branches — as normal chat (speech bubble + chat box) so you can watch the
    // macro FSM live. The switch is a PLAIN LOCAL below, deliberately NOT static/final, so you can flip it to
    // true and HotSwap just this one method while the server is running (no restart; a static/final would be
    // constant-folded at the call sites or keep its old value across a reload).
    void debugChat(String message) { // package-visible: GrindBreakRoutine narrates through the bot
        boolean debugChatEnabled = false; // <-- flip to true + HotSwap this method to turn narration on
        if (!debugChatEnabled) {
            return;
        }
        Character chr = getChr();
        if (chr == null || chr.getMap() == null) {
            return;
        }
        BotFullChat(chr, message); // general chat: bubble above the head AND a line in the chat box
    }

    // Silent, level-scaled accrual: kills/min x per-kill-exp, where per-kill-exp ~ the map's mob level.
    private void accrueAbstractExp(int mobLevel, double elapsedSec) {
        int perKillExp = Math.max(1, mobLevel);
        int gain = (int) Math.round((KILLS_PER_MIN / 60.0) * perKillExp * elapsedSec);
        if (gain <= 0) {
            return;
        }
        Character chr = getChr();
        long exp = (long) chr.getExp() + gain;
        int level = chr.getLevel();
        int startLevel = level;
        while (level < LEVEL_CAP) {
            int need = ExpTable.getExpNeededForLevel(level);
            if (need <= 0 || exp < need) {
                break;
            }
            exp -= need;
            level++;
        }
        chr.setLevel(level);
        chr.setExp((int) Math.min(exp, Integer.MAX_VALUE));
        if (level > startLevel) {
            // Announce the bot's own level-up so nearby bots can congratulate it (same event path as
            // real players). Once, at the final level. The abstract EXP loop can span several levels
            // when the bot ticks slowly while unobserved - we still fire a single event.
            EventBus.getInstance().publish(EventFactory.createLevelUpEvent(chr));
        }
    }

    // ── Map selection (level-scaled hops + weighted, occupancy-balanced, chill-aware pick) ──

    // Maps recently left because they were saturated, with expired entries pruned. Fed to the chooser
    // so a just-left packed map isn't immediately re-picked (map-scale analogue of the spot exclude cooldown).
    private Set<Integer> crowdExcludedMaps() {
        long t = now();
        mapCrowdCooldown.values().removeIf(until -> until <= t);
        return new HashSet<>(mapCrowdCooldown.keySet());
    }

    // Record a train target + reserve its world-wide occupancy slot (station-here / party paths; the
    // normal DECIDE path gets its slot from TrainingMapChooser.choose and records the fields directly).
    private void setTrainTarget(int mapId, int mobLevel) {
        clearTrainTarget();
        currentTrainMapId = mapId;
        currentMobLevel = mobLevel;
        TrainingMapChooser.reserve(mapId);
    }

    private void clearTrainTarget() {
        if (currentTrainMapId >= 0) {
            TrainingMapChooser.release(currentTrainMapId);
        }
        currentTrainMapId = -1;
        currentMobLevel = 0;
    }

    private long dwellMs() {
        return TOWN_DWELL_MIN_MS + (long) (rng.nextDouble() * (TOWN_DWELL_MAX_MS - TOWN_DWELL_MIN_MS));
    }

    // Length of the grind session about to start. Normally a fresh 10–20 min. But a bot flagged
    // midSessionGrind (warped in, or already on the map at its first decision) is meant to look like it's
    // been grinding a while: caught at a random point inside a normal session, floored so a just-arrived bot
    // doesn't instantly turn around. One-time — the flag is consumed here, so every later session is full.
    private long grindSessionMs() {
        long full = GRIND_MIN_MS + (long) (rng.nextDouble() * (GRIND_MAX_MS - GRIND_MIN_MS));
        if (midSessionGrind) {
            midSessionGrind = false;
            return Math.max(MID_SESSION_FLOOR_MS, (long) (rng.nextDouble() * full));
        }
        return full;
    }

    private long shopDwellMs() {
        return SHOP_DWELL_MIN_MS + (long) (rng.nextDouble() * (SHOP_DWELL_MAX_MS - SHOP_DWELL_MIN_MS));
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    // ── Diagnostics: aggregate LOD/training overview (backs !gcmove lod train) ──

    // A snapshot for a GM: how many active training bots sit in each LOD tier (full/halo/dwell/coarse) and
    // each macro phase, plus the maps currently FULL and HALO with names, so it's quick to verify the right
    // maps are being observed/trained. Aggregate counts only (per-bot listing would be too noisy at scale).
    public static List<String> lodDiagnostic() {
        List<String> out = new ArrayList<>();

        // Name occupied maps without MapFactory plumbing: index every online character's live map once.
        Map<Integer, MapleMap> mapsById = new HashMap<>();
        Server server = Server.getInstance();
        if (server != null) {
            for (World w : server.getWorlds()) {
                if (w == null) {
                    continue;
                }
                for (Character c : w.getPlayerStorage().getAllCharacters()) {
                    if (c != null && c.getMap() != null) {
                        mapsById.putIfAbsent(c.getMapId(), c.getMap());
                    }
                }
            }
        }

        int total = 0, full = 0, halo = 0, dwell = 0, coarse = 0;
        int pInit = 0, pTown = 0, pShop = 0, pDecide = 0, pTrain = 0, pGrind = 0, pGoTown = 0, pBreak = 0;
        Map<Integer, Integer> trainByMap = new TreeMap<>();
        for (BotSM b : CharacterStorage.getAllBots().values()) {
            if (!(b instanceof TrainingBot tb)) {
                continue;
            }
            Character chr = tb.getChr();
            if (chr == null) {
                continue;
            }
            total++;
            int mapId = chr.getMapId();
            trainByMap.merge(mapId, 1, Integer::sum);
            switch (GCMovement.lodTier(mapId)) {
                case "full" -> full++;
                case "halo" -> halo++;
                case "dwell" -> dwell++;
                default -> coarse++;
            }
            switch (tb.phase) {
                case INIT -> pInit++;
                case IN_TOWN -> pTown++;
                case SHOP_TRAVEL, SHOP_DWELL, SHOP_RETURN -> pShop++;
                case DECIDE -> pDecide++;
                case GO_TRAIN -> pTrain++;
                case GRIND -> pGrind++;
                case GO_TOWN -> pGoTown++;
                case BREAK_TRAVEL, BREAK_REST -> pBreak++;
            }
        }

        out.add("=== TrainingBot LOD overview ===");
        out.add(String.format("training bots: %d   tiers: full=%d halo=%d dwell=%d coarse=%d",
                total, full, halo, dwell, coarse));
        out.add(String.format("phases: grind=%d goTrain=%d goTown=%d inTown=%d shop=%d decide=%d init=%d break=%d",
                pGrind, pTrain, pGoTown, pTown, pShop, pDecide, pInit, pBreak));
        out.add(String.format("-- FULL maps (real player present): %d --", GCMovement.observedFullMaps().size()));
        appendMapLines(out, GCMovement.observedFullMaps(), mapsById, trainByMap);
        out.add(String.format("-- HALO maps (portal-adjacent): %d --", GCMovement.observedHaloMaps().size()));
        appendMapLines(out, GCMovement.observedHaloMaps(), mapsById, trainByMap);
        return out;
    }

    private static void appendMapLines(List<String> out, Set<Integer> mapIds,
                                       Map<Integer, MapleMap> mapsById, Map<Integer, Integer> trainByMap) {
        if (mapIds.isEmpty()) {
            out.add("  (none)");
            return;
        }
        for (int mapId : new TreeSet<>(mapIds)) {
            MapleMap m = mapsById.get(mapId);
            String name = (m != null) ? (m.getStreetName() + " - " + m.getMapName()) : "?";
            int bots = trainByMap.getOrDefault(mapId, 0);
            out.add(String.format("  %d  %s  [%d training bot(s)]", mapId, name, bots));
        }
    }

    // Release combat + movement when the bot stops (FINISHED / converted / manually stopped).
    @Override
    public synchronized void stopScheduledTask() {
        ACTIVE_GRINDERS.remove(this);
        Character chr = getChr();
        if (chr != null) {
            if (chr.getChair() > 0) {
                botCancelChair(chr); // mid-break conversion: don't leave the new type glued to a chair
            }
            grind.release(chr);
            clearTrainTarget(); // release this map's occupancy slot
            BotAttackDriver.clearBot(chr.getId());
            BotBuffDriver.clearBot(chr.getId());
            BotWanderSystem.stop(chr); // end any shop-dwell flavor wander
            TownLoiter.stop(chr); // free any town-loiter ledge claim + fidget before releasing movement
            GCMovement.disable(chr); // releases the shared movement lock the recorded engine needs
        }
        super.stopScheduledTask();
        log("[TrainingBot] stopped: " + (chr != null ? chr.getName() : "?"));
    }
}
