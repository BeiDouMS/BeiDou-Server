package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.server.maps.Rope;
import soloMapling.ArtificialPlayer.BotGrindSystem.GrindBrain;
import soloMapling.ArtificialPlayer.BotGrindSystem.RestSpotFinder;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;
import java.util.Random;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.botClearChalkboard;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.botSetChalkboard;
import static soloMapling.ArtificialPlayer.BotCustomization.getRandomChairId;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botCancelChair;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botSitChair;

// A TrainingBot's grind break (rest flavor): the once-per-session roll, the walk to a safe rest spot,
// the sit / stand / rope-hang / "brb" chalkboard / sleepy mutter, and the resume bookkeeping — extracted
// from TrainingBot. The bot's macro FSM keeps its BREAK_TRAVEL / BREAK_REST phases and just delegates each
// tick here; this class owns all break state and never touches the FSM directly (phase changes come
// from the boolean/enum results). Ours (SoloMapling).
final class GrindBreakRoutine {

    // Rolled ONCE per grind session at GRIND entry: with BREAK_CHANCE_PER_SESSION the session gets
    // one break at a uniformly random moment (never in its first/last BREAK_EDGE_MS). Sessions are
    // already 10-20 min randomized and staggered per bot, so breaks never synchronize across the
    // population. Mean interval ~ avg session / chance: at 30% roughly one break per ~50 min per
    // bot, ~2-3% of grinders resting at any instant. The BREAK phases aren't GRIND, so the shared
    // combat ticker stops swinging automatically; the saved session remainder resumes afterward.
    private static final double BREAK_CHANCE_PER_SESSION = 0.30;
    private static final long BREAK_MIN_MS = 60_000;         // rest for 60-100s
    private static final long BREAK_MAX_MS = 100_000;
    private static final long BREAK_EDGE_MS = 60_000;        // no break in a session's first/last minute
    private static final long BREAK_TRAVEL_STALL_MS = 9_000; // no forward progress this long -> retarget/give up
    private static final long BREAK_TRAVEL_MAX_MS = 60_000;  // absolute ceiling on the walk/climb to the rest spot
    private static final double BREAK_SIT_CHANCE = 0.75;     // sit in a chair vs stand around (ground rest only)
    private static final double BREAK_SIGN_CHANCE = 0.15;    // put up a "brb" chalkboard while resting (ground only)
    private static final double ROPE_HANG_CHANCE = 0.35;     // if a safe rope band exists, hang there instead of a chair
    private static final int PROGRESS_EPS_PX = 24;           // min position change between ticks that counts as "moving"
    private static final int ROPE_ARRIVE_X = 14;             // "on the rope" horizontal tolerance
    private static final int ROPE_ARRIVE_Y = 44;             // "in the hang band" vertical tolerance around the target

    private final TrainingBot bot;
    private final Random rng = new Random();

    private long breakAtMs = 0;              // this session's scheduled break moment (0 = none)
    private long breakUntilMs = 0;
    private long breakMutterAtMs = 0;        // mid-rest Fatigue line moment
    private long resumeRemainingMs = 0;      // session time left when the break started
    private Point restSpot = null;
    private RestSpotFinder.Kind restKind = RestSpotFinder.Kind.GROUND;
    private Rope restRope = null;            // the rope being hung on (ROPE kind only)
    private boolean sitting = false;
    private boolean signSet = false;
    private boolean muttered = false;
    private boolean restStarted = false;     // tickRest's one-time setup latch (armed by begin)
    private long travelStartMs = 0;          // start of the current walk/climb to the rest spot (absolute-ceiling anchor)
    private long travelDeadlineMs = 0;       // stall deadline, pushed out while the trek makes progress
    private Point travelLastPos = null;      // last position that counted as progress (no-progress stall watchdog)
    private boolean retargeted = false;      // has a stalled trek already fallen back to a local safe spot?

    GrindBreakRoutine(TrainingBot bot) {
        this.bot = bot;
    }

    // Roll this session's break. Resumed sessions (a post-break remainder) never roll a second one.
    void schedule(long grindUntilMs, boolean resumingFromBreak) {
        breakAtMs = 0;
        if (resumingFromBreak || rng.nextDouble() >= BREAK_CHANCE_PER_SESSION) {
            return;
        }
        long sessionLen = grindUntilMs - now();
        if (sessionLen <= BREAK_EDGE_MS * 2) {
            return; // shortened (mid-session) first grind — not worth a break
        }
        breakAtMs = now() + BREAK_EDGE_MS + (long) (rng.nextDouble() * (sessionLen - BREAK_EDGE_MS * 2));
    }

    boolean due() {
        return breakAtMs > 0 && now() >= breakAtMs;
    }

    // Dev hook (!bot breaknow): pull the break to the next grind tick.
    void forceNow() {
        breakAtMs = now();
    }

    boolean resuming() {
        return resumeRemainingMs > 0;
    }

    // The saved session remainder for the post-break re-entry (one-shot).
    long consumeResumeRemaining() {
        long r = resumeRemainingMs;
        resumeRemainingMs = 0;
        return r;
    }

    // Start the break: resolve the rest spot BEFORE any side effect — unobserved with no nearby
    // 100%-safe ledge returns null, meaning "skip this break" (better to keep grinding than trek across
    // the map unseen or rest near mobs; observed always yields a safe spot). A safe rope band, when one
    // exists, is used with a chance gate (else a chair). On true the caller enters BREAK_TRAVEL; the
    // due-flag is consumed either way, so a skipped session simply gets no break.
    boolean begin(Character chr, GrindBrain grind, long savedRemainingMs) {
        breakAtMs = 0;
        GCMovement.setRestHold(chr, false); // clear any stale rope hold from a prior break
        RestSpotFinder.RestSpot ground = RestSpotFinder.find(chr);
        if (ground == null) {
            bot.debugChat("break skipped: no nearby safe rest spot (unobserved)");
            return false;
        }
        RestSpotFinder.RestSpot rope = RestSpotFinder.findRope(chr);
        RestSpotFinder.RestSpot chosen = (rope != null && rng.nextDouble() < ROPE_HANG_CHANCE) ? rope : ground;
        restSpot = chosen.point();
        restKind = chosen.kind();
        restRope = chosen.rope();
        resumeRemainingMs = savedRemainingMs;
        grind.release(chr);   // free the spot for others; the map occupancy slot stays reserved
        GCMovement.stop(chr);
        sitting = restKind == RestSpotFinder.Kind.GROUND && rng.nextDouble() < BREAK_SIT_CHANCE;
        muttered = false;
        restStarted = false;
        bot.sayContext("BreakStart", chr, null);
        bot.debugChat("break -> " + (restKind == RestSpotFinder.Kind.ROPE ? "rope hang"
                : (sitting ? "sit" : "stand")) + " at x" + restSpot.x + " y" + restSpot.y);
        // Travel setup happens here (not on the phase's first tick): the destination is known and the
        // movement layer was just stopped, so issue the walk immediately.
        travelStartMs = now();
        travelLastPos = chr.getPosition();
        retargeted = false;
        travelDeadlineMs = now() + BREAK_TRAVEL_STALL_MS;
        GCMovement.move(chr, restSpot.x, restSpot.y);
        return true;
    }

    // One BREAK_TRAVEL tick: walk (or climb) to the rest spot on the same map. A no-progress watchdog —
    // not a flat cap — lets a legit multi-rope trek to a safe upper ledge finish as long as it keeps
    // advancing, but catches a genuine stall in seconds. On a stall we retarget ONCE to a GROUND spot
    // reachable from here without a climb, so a stuck bot never sits down among the mobs. True = start
    // resting.
    boolean tickTravel(Character chr) {
        Point pos = chr.getPosition();
        if (restKind == RestSpotFinder.Kind.ROPE) {
            // Arrival = actually on the rope, within the hang band. The driver climbs to the band and
            // holds (dy~0); we engage the explicit rest hold so nothing dislodges it, then start resting.
            if (pos != null && restRope != null
                    && GCMovement.isClimbing(chr)
                    && Math.abs(pos.x - restRope.x()) <= ROPE_ARRIVE_X
                    && Math.abs(pos.y - restSpot.y) <= ROPE_ARRIVE_Y) {
                GCMovement.setRestHold(chr, true);
                GCMovement.stop(chr); // drop the move/nav intent; the rest hold keeps it hanging
                return true;
            }
        } else if (pos != null && Math.abs(pos.x - restSpot.x) <= 60 && Math.abs(pos.y - restSpot.y) <= 120) {
            GCMovement.stop(chr);
            return true;
        }
        // A committed climb toward the rope (isNavigatingClimb) counts as progress, so the final rope trek
        // isn't mistaken for a stall while the bot is mid-rope.
        boolean progressed = pos != null && (travelLastPos == null
                || Math.abs(pos.x - travelLastPos.x) + Math.abs(pos.y - travelLastPos.y) > PROGRESS_EPS_PX);
        if (progressed || GCMovement.isNavigatingClimb(chr)) {
            if (pos != null) {
                travelLastPos = pos;
            }
            travelDeadlineMs = now() + BREAK_TRAVEL_STALL_MS;
        }
        boolean stalled = now() > travelDeadlineMs;
        boolean tooLong = now() - travelStartMs > BREAK_TRAVEL_MAX_MS;
        if (stalled || tooLong) {
            if (!retargeted) {
                retargeted = true;
                RestSpotFinder.RestSpot local = RestSpotFinder.findLocal(chr); // safe GROUND spot, walk-only
                restSpot = local.point();
                restKind = RestSpotFinder.Kind.GROUND;
                restRope = null;
                GCMovement.setRestHold(chr, false); // in case a partial rope hold slipped in
                travelStartMs = now();
                travelDeadlineMs = now() + BREAK_TRAVEL_STALL_MS;
                travelLastPos = chr.getPosition();
                GCMovement.move(chr, restSpot.x, restSpot.y);
                bot.debugChat("break travel stalled -> local rest at x" + restSpot.x);
                return false;
            }
            GCMovement.stop(chr); // already fell back to a local safe spot once — rest here
            return true;
        }
        return false; // still travelling
    }

    // One BREAK_REST tick: sit / stand / hang, maybe put up a "brb" chalkboard (ground only), one sleepy
    // mutter around the middle (the Fatigue pool's consumer), then finish. Menus/invites keep working
    // through the break (recruitingNow defers the stand-up). True = break over; the caller re-enters GRIND
    // with the saved remainder.
    boolean tickRest(Character chr) {
        if (!restStarted) {
            restStarted = true;
            long restMs = BREAK_MIN_MS + (long) (rng.nextDouble() * (BREAK_MAX_MS - BREAK_MIN_MS));
            breakUntilMs = now() + restMs;
            breakMutterAtMs = now() + restMs / 2;
            if (restKind == RestSpotFinder.Kind.GROUND) {
                // Last-moment veto: a mob may have wandered onto the chosen spot while we walked over.
                // Don't plant a chair on it — stand instead (observed maps only; unobserved is unseen).
                if (sitting && RestSpotFinder.isSpotMobOccupied(chr, restSpot)) {
                    sitting = false;
                    bot.debugChat("break: mob on rest spot -> stand instead of sit");
                }
                if (sitting) {
                    botSitChair(chr, getRandomChairId());
                }
                if (rng.nextDouble() < BREAK_SIGN_CHANCE) {
                    botSetChalkboard(chr, bot.breakSignText());
                    signSet = true;
                }
            }
            // ROPE: no chair or chalkboard (both are client-illegal while climbing); the rest hold is
            // already engaged (tickTravel), and the mid-rest Fatigue mutter below is fine.
            return false;
        }
        if (!muttered && now() >= breakMutterAtMs) {
            muttered = true;
            bot.sayContext("Fatigue", chr, null);
        }
        if (now() >= breakUntilMs && !bot.recruitingNow()) { // finish the conversation before standing up
            if (signSet) {
                botClearChalkboard(chr);
                signSet = false;
            }
            if (restKind == RestSpotFinder.Kind.ROPE) {
                GCMovement.setRestHold(chr, false);
                GCMovement.dismountRope(chr, 0); // drop straight off onto the ground below; GRIND re-picks a spot
            } else if (chr.getChair() > 0) {
                botCancelChair(chr);
            }
            bot.sayContext("BreakOver", chr, null);
            return true;
        }
        return false;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
