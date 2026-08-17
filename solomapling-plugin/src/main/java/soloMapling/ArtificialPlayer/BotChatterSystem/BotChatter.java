package soloMapling.ArtificialPlayer.BotChatterSystem;

import org.gms.client.Character;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.DialogueContextResolver;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotTypes.SocialBot;
import soloMapling.ArtificialPlayer.BotTypes.TownWandererBot;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.server.BotTiming;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botFaceTowardsPoint;

// Ambient bot-to-bot conversation: two nearby idle town bots occasionally play out a short, natural
// back-and-forth (2-4 turns) so a town reads as populated before a player engages, and livelier when one
// watches. Entry point is maybeStartChatter(bot), called from a participant's existing idle tick - no new
// thread, no new ticker (BotFlavor's model). Everything here is pure chat/emote packets, hard-gated on a
// real player being able to see it.
//
// This is distinct from SocialHotPotatoManager (single-line ambient barks on the Henesys maps): this drives
// genuine paired, alternating turn-taking across all towns, off the macro ticks. A chatting pair reports
// isAvailableForAmbientActions()==false via isEngaged, so it is auto-excluded from hot-potato barks,
// LevelUpCongrats, BotFlavor, and player pickup - player interaction always wins.
//
// Our own creation (not a GreenCat extraction).
public final class BotChatter {

    private BotChatter() {
    }

    // ---- tuning knobs (public volatile for a future !env chatter readout) ----
    // Per eligible observed tick, the chance to try starting a chat (a quiet tick consumes no cooldown).
    public static volatile double CHATTER_CHANCE = 0.10;
    // Only bots this close are a plausible partner (visibly adjacent, not shouting across the map).
    public static volatile int CHATTER_RADIUS = 180;
    // Self-expiring engagement deadline: a pair that dies mid-chain (release beat dropped by the gate)
    // frees itself within this window even if forget() is missed. Must exceed the max chain duration
    // (opening + turns * TURN_PAUSE_MAX) so a normally-completing chat always releases via its terminal
    // beat before the deadline fires.
    public static volatile long MAX_CHATTER_MS = 22_000;
    // Min spacing between one bot's chats (jittered), so a plaza yields occasional pairs, not a wall of bubbles.
    public static volatile long CHATTER_COOLDOWN_MIN_MS = 30_000;
    public static volatile long CHATTER_COOLDOWN_MAX_MS = 90_000;
    // Reading pace between turns.
    public static volatile long TURN_PAUSE_MIN_MS = 2_200;
    public static volatile long TURN_PAUSE_MAX_MS = 3_800;
    // Chance a spoken turn is punctuated by a friendly emote.
    public static volatile double CHATTER_EMOTE_CHANCE = 0.25;

    private static final int[] FRIENDLY_EMOTES = {1, 2, 5, 6};

    // Fallback exchange used when TownChatterDialogue.yaml has no lines loaded (so chatter never goes
    // fully silent on a bad/empty edit). Ordered alternating turns A,B,A...
    private static final List<String> FALLBACK_EXCHANGE = List.of(
            "hey", "hey, how's it going?", "not bad, just hanging around");

    // charId -> engagement deadline (now + MAX_CHATTER_MS). An entry past its deadline is stale.
    private static final Map<Integer, Long> ENGAGED = new ConcurrentHashMap<>();
    // charId -> earliest time this bot may chat again.
    private static final Map<Integer, Long> COOLDOWN = new ConcurrentHashMap<>();

    // Called from an idle/available participant tick. Rolls the gates cheap-first and, if they pass, engages
    // a nearby partner and starts a short alternating exchange. No-op when unobserved (chatter is packets).
    public static void maybeStartChatter(BotSM initiator) {
        tryStart(initiator, false);
    }

    // Force a chat immediately, skipping the cooldown + chance rolls (still requires available + observed +
    // a nearby free partner). Test/tuning entry for !env chatter fire. Returns true if a chat started.
    public static boolean forceChatter(BotSM initiator) {
        return tryStart(initiator, true);
    }

    private static boolean tryStart(BotSM initiator, boolean force) {
        if (initiator == null) {
            return false;
        }
        Character me = initiator.getChr();
        if (me == null || me.getMap() == null) {
            return false;
        }
        if (!initiator.isAvailableForAmbientActions()) {
            return false; // engaged / traveling / mid-conversation
        }
        if (!GCMovement.isMapObserved(me.getMapId())) {
            return false; // no observer - chatter is packets, gate packets never simulation
        }
        if (!force) {
            if (onCooldown(me)) {
                return false;
            }
            if (ThreadLocalRandom.current().nextDouble() >= CHATTER_CHANCE) {
                return false; // quiet tick; no cooldown consumed
            }
        }

        BotSM partnerBot = findPartner(initiator, me);
        if (partnerBot == null) {
            return false;
        }
        Character partner = partnerBot.getChr();
        if (partner == null) {
            return false;
        }

        List<String> exchange = pickExchange();
        if (exchange == null || exchange.size() < 2) {
            return false;
        }
        if (!engagePair(me, partner)) {
            return false; // lost the race for one of the two
        }
        // Arm cooldowns at engage time so anti-spam holds even if the chain's tail is dropped by the gate.
        armCooldown(me);
        armCooldown(partner);
        startExchange(me, partner, exchange);
        return true;
    }

    // Count of bots currently mid-chat (non-stale engagements). Diagnostics only (!env chatter).
    public static int engagedCount() {
        long now = System.currentTimeMillis();
        int n = 0;
        for (Long deadline : ENGAGED.values()) {
            if (deadline != null && deadline > now) {
                n++;
            }
        }
        return n;
    }

    // A random ordered exchange from TownChatterDialogue.yaml, falling back to the hardcoded exchange when
    // the file is empty/unparseable so chatter never goes fully silent.
    private static List<String> pickExchange() {
        List<String> ex = TownChatterLines.randomExchange();
        return (ex != null && ex.size() >= 2) ? ex : FALLBACK_EXCHANGE;
    }

    // Nearest eligible bot on the initiator's map within CHATTER_RADIUS: a chatter participant that is
    // available and not already engaged. Bounded to the same map's character list.
    private static BotSM findPartner(BotSM initiator, Character me) {
        MapleMap map = me.getMap();
        Point mp = me.getPosition();
        double bestSq = (double) CHATTER_RADIUS * CHATTER_RADIUS;
        BotSM best = null;
        for (Character chr : map.getAllPlayers()) {
            if (chr == null || chr.getId() == me.getId() || !isBot(chr)) {
                continue;
            }
            double dsq = chr.getPosition().distanceSq(mp);
            if (dsq > bestSq) {
                continue;
            }
            BotSM bot = CharacterStorage.getBotById(chr.getId());
            if (bot == null || bot == initiator || !isParticipant(bot)) {
                continue;
            }
            if (isEngaged(chr) || !bot.isAvailableForAmbientActions()) {
                continue;
            }
            best = bot;
            bestSq = dsq; // narrow to the nearest eligible partner
        }
        return best;
    }

    // Only these two types chatter (HenesysBot stays the frozen set piece; it can opt in later by adding
    // the one idle-tick call). Engaging a non-participant as a listener would not self-exclude it from other
    // ambient actions, so restrict partners to types whose availability reflects engagement.
    private static boolean isParticipant(BotSM bot) {
        return bot instanceof SocialBot || bot instanceof TownWandererBot;
    }

    // Build and launch the alternating exchange on one BotTiming chain. The chain only sends packets and
    // toggles the engaged/cooldown maps - it never mutates either bot's FSM state fields.
    private static void startExchange(Character a, Character b, List<String> exchange) {
        BotTiming.Chain chain = BotTiming.chain()
                .stopUnless(() -> chatterAlive(a, b));
        // Opening: face each other (engine-safe; cosmetic).
        chain.run(() -> {
            faceToward(a, b);
            faceToward(b, a);
        }).pauseRandom(500, 1100);
        for (int i = 0; i < exchange.size(); i++) {
            final Character speaker = (i % 2 == 0) ? a : b;
            final Character listener = (i % 2 == 0) ? b : a;
            final String raw = exchange.get(i);
            chain.run(() -> speakResolved(speaker, listener, raw))
                    .pauseRandom(TURN_PAUSE_MIN_MS, TURN_PAUSE_MAX_MS);
        }
        chain.run(() -> {
            release(a);
            release(b);
        });
        chain.start();
    }

    // Speak one turn, resolving any {TOKEN}s against the speaker (and listener as the "player" subject).
    // A line whose tokens can't resolve is dropped rather than spoken raw (same discipline as
    // getRandomResolvedLine). An occasional friendly emote punctuates the turn.
    private static void speakResolved(Character speaker, Character listener, String raw) {
        if (speaker == null || raw == null) {
            return;
        }
        String line = raw;
        if (DialogueContextResolver.hasTokens(raw)) {
            Optional<String> filled = DialogueContextResolver.fill(raw, speaker, listener);
            if (filled.isEmpty()) {
                return; // unresolvable - stay silent this turn
            }
            line = filled.get();
        }
        BotSpeak(speaker, line);
        if (ThreadLocalRandom.current().nextDouble() < CHATTER_EMOTE_CHANCE) {
            BotEmote(speaker, FRIENDLY_EMOTES[ThreadLocalRandom.current().nextInt(FRIENDLY_EMOTES.length)]);
        }
    }

    // Re-checked before every following beat: both bots present, same map, still engaged, still observed.
    // A walk-off / player-interrupt / teardown drops the rest of the chain cleanly.
    private static boolean chatterAlive(Character a, Character b) {
        if (a == null || b == null || a.getMap() == null || b.getMap() == null) {
            return false;
        }
        if (a.getMap() != b.getMap()) {
            return false;
        }
        if (!isEngaged(a) || !isEngaged(b)) {
            return false;
        }
        return GCMovement.isMapObserved(a.getMapId());
    }

    // Face a target, using the engine the actor is actually driven by so we never fight the GC driver on a
    // GC-driven bot (or leave an old-engine bot un-turned). GCMovement.face is a no-op on non-GC bots.
    private static void faceToward(Character actor, Character target) {
        if (actor == null || target == null) {
            return;
        }
        boolean left = target.getPosition().getX() < actor.getPosition().getX();
        if (GCMovement.isEnabled(actor)) {
            GCMovement.face(actor, left);
        } else {
            botFaceTowardsPoint(actor, target.getPosition());
        }
    }

    // ---- engaged registry (concurrency-critical) ----

    // True while chr is mid-chat. Treats an entry past its deadline as stale (drops it, returns false), so a
    // pair whose release beat never ran self-heals within MAX_CHATTER_MS.
    public static boolean isEngaged(Character chr) {
        if (chr == null) {
            return false;
        }
        Long deadline = ENGAGED.get(chr.getId());
        if (deadline == null) {
            return false;
        }
        if (System.currentTimeMillis() >= deadline) {
            ENGAGED.remove(chr.getId(), deadline); // stale - drop only if unchanged
            return false;
        }
        return true;
    }

    // Engage both or neither. Each per-key compute is atomic; a stale entry is treated as free so a leaked
    // engagement never wedges a bot forever.
    private static boolean engagePair(Character a, Character b) {
        long deadline = System.currentTimeMillis() + MAX_CHATTER_MS;
        if (!tryEngage(a.getId(), deadline)) {
            return false;
        }
        if (!tryEngage(b.getId(), deadline)) {
            release(a); // free the one we took
            return false;
        }
        return true;
    }

    private static boolean tryEngage(int id, long deadline) {
        boolean[] took = {false};
        ENGAGED.compute(id, (k, existing) -> {
            if (existing != null && existing > System.currentTimeMillis()) {
                return existing; // still engaged - do not take
            }
            took[0] = true;
            return deadline; // free or stale - take it
        });
        return took[0];
    }

    // Free an engagement (terminal chain beat). Idempotent.
    public static void release(Character chr) {
        if (chr != null) {
            ENGAGED.remove(chr.getId());
        }
    }

    // Best-effort teardown/convert cleanup: drop both engagement and cooldown so the maps don't leak.
    public static void forget(Character chr) {
        if (chr != null) {
            ENGAGED.remove(chr.getId());
            COOLDOWN.remove(chr.getId());
        }
    }

    // ---- per-bot cooldown ----

    private static boolean onCooldown(Character chr) {
        Long until = COOLDOWN.get(chr.getId());
        return until != null && System.currentTimeMillis() < until;
    }

    private static void armCooldown(Character chr) {
        long span = Math.max(1, CHATTER_COOLDOWN_MAX_MS - CHATTER_COOLDOWN_MIN_MS);
        long cd = CHATTER_COOLDOWN_MIN_MS + (long) (ThreadLocalRandom.current().nextDouble() * span);
        COOLDOWN.put(chr.getId(), System.currentTimeMillis() + cd);
    }
}
