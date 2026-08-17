package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import org.gms.constants.game.CharacterStance;
import org.gms.server.maps.Foothold;
import org.gms.server.maps.MapleMap;

import java.awt.Point;

// Adapted from GreenCatMS bot movement-skill packets (NutNNut). Credit: NutNNut.
/*
 * Movement-skill locomotion for grinding bots: mage Teleport (blink) and Hermit/Night-Lord Flash Jump
 * (air dash). Picks a foothold landing, moves the bot there, and broadcasts the v83 MOVE_PLAYER fragments
 * that make observers SEE the blink/dash. This is the "GCMovementAdvanced" layer the audit named: the RE'd
 * constants and the client-verified fragment byte layouts come from GreenCatMS's reverse-engineering (see
 * Documents/Fable "Fable Handoff - 2026-07-06 GreenCatMS Fluid Combat & Movement-Skill Extraction.md").
 *
 * Our bots are ephemeral decoration, so there are NO MP/meso gates and no skill-tree checks — MovementStyle-
 * Policy decides who blinks/dashes by job, and this just does it. Teleport is modelled at max range (150)
 * always. The admired "mage AoEs then teleports to the next pack" look is emergent: the grind brain fires,
 * then routes its next approach leg through here.
 *
 * UNTESTED: the exact fragment byte layout is what renders the blink/dash on observing clients, and a
 * mismatched fragment/animation byte can CRASH observers. Live-test one mage + one hermit on an observed
 * map before scaling.
 *
 * Teleport is an instant blink (correct — a real teleport has no arc): it warps the bot and broadcasts the
 * cmd4/3/0 fragments here. Flash jump is NOT instant — it launches a real directional jump and arms
 * pendingFlashJump; the airborne integrator (BotPhysicsEngine.stepAirborne) injects the (±550,-350) dash
 * impulse at the apex and BotMovementManager.tickAirborne broadcasts the type-6 "fj" frame, so the physics
 * engine carries a genuine arc to a natural landing (faithful GreenCatMS behavior). Ours on top: the dash
 * is TIERED — flashJumpScale = level cap (FlashJumpTiers: young Hermit 0.75, NL/Hermit-100+ full) x
 * platform fit (largest arc whose travel fits the remaining ledge run), so small platforms get a visibly
 * short dash instead of an off-the-edge overshoot.
 */
final class GCMovementSkills {
    private GCMovementSkills() {
    }

    // RE'd client constants (GreenCatMS packet captures).
    static final int TELEPORT_RANGE_PX = 150;    // max blink distance; bots always modelled at max
    static final int TELEPORT_Y_SNAP_PX = 75;    // horizontal-blink vertical snap band
    static final int FLASH_JUMP_Y_SNAP_PX = 90;  // a dash tolerates a bit more height change than a blink

    // Ours (tier/fit layer): dash tiers. FJ_TRAVEL_PX = expected total ground covered per scale (launch
    // drift + dash), derived from the physics constants — calibrate with !gcmove fj before trusting the
    // numbers hard. Per jump we pick the LARGEST tier that (a) is within the bot's level cap
    // (FlashJumpTiers) and (b) fits the remaining platform run, so a Night Lord uses the full dash on a
    // long ledge and automatically downshifts on a small platform instead of dashing off the edge.
    static final float[] FJ_SCALES = {1.0f, 0.75f, 0.55f};
    static final int[] FJ_TRAVEL_PX = {340, 240, 160};
    private static final int FJ_LAND_MARGIN_PX = 40; // keep the landing this far inside the ledge end
    private static final int MOVE_DURATION_MS = 50; // per-fragment duration (matches the driver's broadcast tick)

    // v83 MovePath fragment command bytes.
    private static final byte CMD_ABSOLUTE = 0;
    private static final byte CMD_TELEPORT_DISAPPEAR = 3; // at destination
    private static final byte CMD_TELEPORT_APPEAR = 4;    // at origin

    // ── Teleport (mage blink) ─────────────────────────────────────────────────

    // Blink toward (targetX, targetY): pick a same-region landing up to TELEPORT_RANGE_PX toward the target,
    // warp the bot there, and broadcast the blink. Returns false (caller walks) if nothing valid is in range.
    static boolean execTeleport(BotMovementState st, Character bot, int targetX, int targetY) {
        MapleMap map = bot.getMap();
        if (map == null) {
            return false;
        }
        Point origin = bot.getPosition();
        if (origin == null) {
            return false;
        }
        int dx = targetX - origin.x;
        int dy = targetY - origin.y;
        Point dest;
        if (Math.abs(dx) >= Math.abs(dy)) {
            dest = horizontalLanding(map, origin, dx >= 0 ? 1 : -1, TELEPORT_RANGE_PX, TELEPORT_Y_SNAP_PX);
        } else if (dy > 0) {
            dest = downLanding(map, origin);
        } else {
            dest = upLanding(map, origin);
        }
        if (dest == null || (dest.x == origin.x && dest.y == origin.y)) {
            return false;
        }
        int hdir = dest.x >= origin.x ? 1 : -1;
        boolean downward = dest.y > origin.y + 8;
        int stance = downward ? proneStance(hdir) : standStance(hdir);
        BotPhysicsEngine.teleportTo(st, bot, dest);
        BotMovementManager.resetEntryStateAfterTeleport(st);
        if (ObserverTracker.isActiveMap(bot.getMapId())) {
            broadcastTeleport(bot, origin, dest, stance);
        }
        return true;
    }

    // ── Flash Jump (thief air dash) ───────────────────────────────────────────

    // scaleCap = the bot's level-tier dash cap (FlashJumpTiers via the caller); the platform fit below
    // may downshift under it, never over it.
    static boolean execFlashJump(BotMovementState st, Character bot, int targetX, float scaleCap) {
        MapleMap map = bot.getMap();
        if (map == null) {
            return false;
        }
        Point origin = bot.getPosition();
        if (origin == null || st.inAir || st.climbing) {
            return false; // no position, or already airborne/on a rope — don't stack a dash
        }
        int dir = targetX >= origin.x ? 1 : -1;
        // Pick the largest dash that fits the platform (and the level cap). We do NOT warp to a landing
        // point — the airborne integrator lands the bot naturally; the fit just guarantees the chosen arc
        // has ledge to land on (we intentionally skip GreenCatMS's nav-graph landing sim). Fall-off-map
        // recovery backstops any residual overshoot.
        float scale = fitScale(map, origin, dir, scaleCap);
        if (scale <= 0f) {
            return false; // no arc fits the remaining run — caller walks (or uses the plain engage hop)
        }
        // Launch a real directional jump toward the target, then arm the dash. stepAirborne injects the
        // scaled impulse at the trigger point (apex for scale 1, part-way up for smaller); tickAirborne
        // broadcasts the type-6 "fj" frame; the physics engine carries the arc to a natural landing.
        // Faithful GreenCatMS flash jump — NOT an instant snap.
        BotMovementManager.initiateJump(st, bot, targetX - origin.x);
        st.flashJumpScale = scale;
        st.pendingFlashJump = true;
        return true;
    }

    // Dev/calibration only (the !gcmove fj hook): fire a dash at an exact scale, bypassing the level cap
    // and the platform fit, so real travel distances can be measured against FJ_TRAVEL_PX.
    static boolean execFlashJumpForced(BotMovementState st, Character bot, int dir, float scale) {
        Point origin = bot.getPosition();
        if (bot.getMap() == null || origin == null || st.inAir || st.climbing) {
            return false;
        }
        BotMovementManager.initiateJump(st, bot, dir >= 0 ? 1 : -1);
        st.flashJumpScale = Math.max(0.2f, Math.min(scale, 1f));
        st.pendingFlashJump = true;
        return true;
    }

    // The largest tier scale <= cap whose expected travel fits the platform. Primary gate: the remaining
    // same-ledge run toward dir (the dash lands back on the launch ledge, so run bounds the safe travel).
    // When the nav graph can't place the bot (region unknown), fall back to probing for same-level ground
    // around each tier's landing distance. 0 = nothing fits.
    private static float fitScale(MapleMap map, Point origin, int dir, float cap) {
        int run = sameLedgeRun(map, origin, dir);
        for (int i = 0; i < FJ_SCALES.length; i++) {
            if (FJ_SCALES[i] > cap + 0.001f) {
                continue; // above this bot's level tier
            }
            int travel = FJ_TRAVEL_PX[i];
            if (run >= 0) {
                if (run - FJ_LAND_MARGIN_PX >= travel) {
                    return FJ_SCALES[i];
                }
            } else if (landingProbe(map, origin, dir, travel) != null) {
                return FJ_SCALES[i];
            }
        }
        return 0f;
    }

    // Walkable distance from origin to the end of the bot's own ledge in `dir`, or -1 when the graph
    // has no region for the spot (unbaked / mid-air origin). Grinding bots always run on a baked map
    // (SpotFinder.profile bakes it), so -1 is the rare fallback path, not the norm.
    private static int sameLedgeRun(MapleMap map, Point origin, int dir) {
        int region = GCMovement.regionIdAt(map, origin.x, origin.y);
        if (region < 0) {
            return -1;
        }
        for (GCMovement.Ledge l : GCMovement.walkableLedges(map)) {
            if (l.regionId() == region) {
                return dir > 0 ? l.maxX() - origin.x : origin.x - l.minX();
            }
        }
        return -1;
    }

    // Same-level ground within a small window inward of the expected landing distance (fallback fit
    // check when the ledge run is unknown).
    private static Point landingProbe(MapleMap map, Point origin, int dir, int travel) {
        for (int reach = travel; reach >= travel - 60; reach -= 20) {
            Point g = GCMovement.groundPointBelow(map, origin.x + dir * reach, origin.y - 4);
            if (g != null && Math.abs(g.y - origin.y) <= FLASH_JUMP_Y_SNAP_PX) {
                return g;
            }
        }
        return null;
    }

    // ── Landing math (foothold-only; ported concept from BotPhysicsEngine.teleportLanding) ──

    // Nearest same-level (±ySnap) foothold up to `range` px toward `dir`, stepping the reach inward if the
    // full-range spot is off the ledge (keeps the blink on the bot's own platform instead of into a gap).
    private static Point horizontalLanding(MapleMap map, Point origin, int dir, int range, int ySnap) {
        for (int reach = range; reach >= 40; reach -= 25) {
            int tx = origin.x + dir * reach;
            Point g = GCMovement.groundPointBelow(map, tx, origin.y - 4);
            if (g != null && Math.abs(g.y - origin.y) <= ySnap) {
                return g;
            }
        }
        return null;
    }

    // Furthest platform within range directly above (mirror of downLanding). No foothold-tree "findAbove"
    // exists, so this goes through the bucket-index scan added to BotPhysicsEngine. Null -> caller walks.
    private static Point upLanding(MapleMap map, Point origin) {
        return BotPhysicsEngine.findGroundPointAbove(map, origin, TELEPORT_RANGE_PX);
    }

    // Furthest platform within range directly below (walk platform-by-platform, keep the last in range).
    private static Point downLanding(MapleMap map, Point origin) {
        Point best = null;
        Point probe = GCMovement.groundPointBelow(map, origin.x, origin.y + 1);
        int guard = 0;
        while (probe != null && probe.y > origin.y && probe.y - origin.y <= TELEPORT_RANGE_PX && guard++ < 12) {
            best = probe;
            probe = GCMovement.groundPointBelow(map, origin.x, probe.y + 1);
        }
        return best;
    }

    // ── Broadcast (client-verified fragment layouts; see the Fable handoff doc Appendix A) ──

    // 3 fragments: cmd4 @ origin (origin fh), cmd3 @ dest (fh 0 = arrival airborne), cmd0 settle @ dest.
    private static void broadcastTeleport(Character bot, Point origin, Point dest, int stance) {
        MapleMap map = bot.getMap();
        byte[] data = new byte[1 + 10 + 10 + 14];
        int i = 0;
        data[i++] = 3; // numCommands
        i = putTeleportFrag(data, i, CMD_TELEPORT_APPEAR, origin.x, origin.y, footholdIdAt(map, origin), stance);
        i = putTeleportFrag(data, i, CMD_TELEPORT_DISAPPEAR, dest.x, dest.y, 0, stance);
        putAbsoluteFrag(data, i, dest.x, dest.y, 0, 0, footholdIdAt(map, dest), stance, MOVE_DURATION_MS);
        BotMovementManager.broadcastRawMovement(bot, data);
    }

    // cmd 0 absolute: x,y,velX,velY,fh,stance,duration (same layout as sendMovementPacket's single fragment).
    private static int putAbsoluteFrag(byte[] d, int i, int x, int y, int velX, int velY, int fh, int stance, int durMs) {
        d[i++] = CMD_ABSOLUTE;
        i = putShort(d, i, x);
        i = putShort(d, i, y);
        i = putShort(d, i, velX);
        i = putShort(d, i, velY);
        i = putShort(d, i, fh);
        d[i++] = (byte) stance;
        return putShort(d, i, durMs);
    }

    // cmd 3/4: authored for the v83 CLIENT decode — x,y,fh,stance,elapse(=0 blink) — NOT the server parse
    // layout (x,y,xwobble,ywobble,stance). Wrong layout makes observers read elapse=stance<<8 and the frame
    // lingers ~1s. See the Fable handoff doc / GreenCatMS kb_bot_movement_skills_teleport_flashjump.
    private static int putTeleportFrag(byte[] d, int i, byte cmd, int x, int y, int fh, int stance) {
        d[i++] = cmd;
        i = putShort(d, i, x);
        i = putShort(d, i, y);
        i = putShort(d, i, fh);
        d[i++] = (byte) stance;
        return putShort(d, i, 0); // elapse = 0
    }

    private static int putShort(byte[] d, int i, int v) {
        d[i] = (byte) (v & 0xFF);
        d[i + 1] = (byte) ((v >> 8) & 0xFF);
        return i + 2;
    }

    private static int footholdIdAt(MapleMap map, Point p) {
        Foothold fh = BotPhysicsEngine.findGroundFoothold(map, p);
        return fh != null ? fh.getId() : 0;
    }

    private static int standStance(int dir) {
        return dir < 0 ? CharacterStance.STAND_LEFT_STANCE : CharacterStance.STAND_RIGHT_STANCE;
    }

    private static int proneStance(int dir) {
        return dir < 0 ? CharacterStance.PRONE_LEFT_STANCE : CharacterStance.PRONE_RIGHT_STANCE;
    }
}
