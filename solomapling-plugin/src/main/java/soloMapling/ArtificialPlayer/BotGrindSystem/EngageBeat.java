package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;
import org.gms.server.life.Monster;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotAttackDriver;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;

// The shared in-range combat beat every grind style uses: turn-around micro-beat, ranged kite-back,
// AoE step-into-the-pack, the arced jump-attack engage (with the occasional mini-kite hop), the swing
// itself, and the blink/dash approach move. Extracted verbatim from the pre-split GrindBrain's
// doFight/doRoam duplication; strategy-specific bounds come in through GrindBrain.effectiveLeash /
// strategyGroundAt so camp, roam, and later styles all share one beat. Ours (SoloMapling).
final class EngageBeat {

    // ── Movement style (advanced classes blink/dash toward mobs; PLANTED default keeps low-levels organic) ──
    private static final int SKILL_MOVE_MIN_PX = 150;        // only blink/dash toward a target at least this far (else walk the last stretch)
    private static final int SKILL_MOVE_MIN_DY_PX = 100;     // a stacked target justifies a vertical blink even when dx is small (TELEPORT only)
    private static final long SKILL_MOVE_COOLDOWN_MS = 500;  // cadence floor between blinks/dashes
    private static final double JUMP_ATTACK_CHANCE = 0.68;   // thieves hop (arced) most engages — their signature vs planted warriors/archers
    private static final double KITE_HOP_CHANCE = 0.35;      // within a jump-attack, a thief's chance to hop AWAY (mini-kite) instead of toward
    private static final int JUMP_HOP_REACH_PX = 60;         // approx horizontal reach of an engage hop; keeps a kite hop on-leash and on-ledge

    // ── AoE reposition ──
    private static final int AOE_CLUSTER_PX = 200;           // radius around the bot that counts as one pack for the reposition
    private static final int AOE_REPOSITION_DEADZONE_PX = 60; // don't re-center for a pack this close to underfoot
    private static final long AOE_REPOSITION_COOLDOWN_MS = 700; // cadence floor so it steps in, not jitters

    // ── Ranged kiting (bowmen back off when a mob closes inside the comfort band) ──
    private static final int KITE_MIN_PX = 150;             // a mob nearer than this (|dx|) triggers a step back
    private static final int KITE_STEP_PX = 90;             // how far the bot retreats per kite step
    private static final long KITE_COOLDOWN_MS = 400;       // cadence floor between kite steps

    // ── Attack tempo (real classes can't walk mid-attack; only jumps/teleports move while attacking) ──
    static final long ATTACK_WALK_LOCK_MS = 500;            // after a swing, hold position this long before walking
    private static final int TURN_DEADZONE_PX = 15;         // mob within this |dx| = no meaningful facing to change
    private static final int TURN_STEP_MIN_PX = 5;          // a turn carries the bot a tap's worth into the new facing...
    private static final int TURN_STEP_MAX_PX = 20;         // ...up to a longer press
    private static final long TURN_BEAT_THROTTLE_MS = 500;  // anti-thrash: don't spend a turn-beat more often than this

    private final GrindBrain b;

    // Cadence gates (per bot, reset each episode).
    private long nextSkillMoveMs = 0L;
    private long nextAoeRepositionMs = 0L;
    private long nextKiteMs = 0L;
    private long nextTurnBeatOkMs = 0L;

    EngageBeat(GrindBrain brain) {
        this.b = brain;
    }

    void reset() {
        nextSkillMoveMs = 0L;
        nextAoeRepositionMs = 0L;
        nextKiteMs = 0L;
        nextTurnBeatOkMs = 0L;
    }

    // Pre-swing adjustments, in priority order: turn to face, ranged kite-back, AoE re-center.
    // Returns true when the beat was spent moving/turning — the caller swings next tick instead.
    boolean preSwingAdjust(Character chr, Monster t) {
        if (needsTurnBeat(chr, t)) {
            doTurnBeat(chr, t); // mob is on our other side -> turn + small step this beat, swing next beat
            return true;
        }
        if (b.style == MovementStyle.RANGED && kiteIfTooClose(chr, t)) {
            return true; // a mob closed the gap -> step back to keep spacing, then fire next beat from range
        }
        return aoeRepositionIfWorthwhile(chr); // stepped/blinked into the pack -> swing next beat centred
    }

    // Plant (or hop) and swing. Handles the engage flourish once per engagement, fires the attack,
    // and does the shared bookkeeping (heartbeat, walk lock, kill target-drop). Returns the attack
    // result so a strategy can add its own post-kill behavior (roam clears `engaged`, camp doesn't).
    BotAttackDriver.AttackResult engageAndSwing(Character chr, Monster t) {
        if (!b.engaged) {
            if (jumpsToAttack() && b.rng.nextDouble() < JUMP_ATTACK_CHANCE) {
                int dir = jumpAttackDir(chr, t);
                if (dir != 0) {
                    // Arced hop-and-swing: hop toward the mob (stay mobile, not pogo-ing) or, for an
                    // occasional thief kite, away from it. botAttack fires mid-arc and faceTarget re-faces
                    // the mob, so a kite hop throws stars backward at the pursuer as the bot leaps clear.
                    GCMovement.jumpToward(chr, dir);
                } else {
                    GCMovement.jumpInPlace(chr); // mob underfoot -> plain vertical hop
                }
            } else {
                GCMovement.stop(chr); // plant and swing
            }
            b.engaged = true;
            b.lastMoveTargetX = Integer.MIN_VALUE;
        }
        BotAttackDriver.AttackResult r = BotAttackDriver.botAttack(chr);
        if (r != null && (r.hit() || r.killed())) {
            b.markProgress(); // landed hit/kill -> not stuck
            b.attackWalkLockUntil = now() + ATTACK_WALK_LOCK_MS; // hold position through the swing before walking
        }
        if (r != null && r.killed()) {
            b.targetOid = -1; // re-acquire the next mob
            b.lastKillMs = now();
        }
        return r;
    }

    // Advanced-class approach: a mage blinks and a hermit dashes toward a far target instead of walking, which
    // reads as authentic 3rd-job repositioning (and sweeps the bot over drops for the at-feet vacuum). Returns
    // true if a skill-move fired (movement is issued); false = the caller walks. Gated to TELEPORT/FLASH_JUMP
    // styles, a min distance, and a cadence floor so it doesn't machine-gun.
    boolean skillMoveToward(Character chr, int tx, int ty) {
        if (b.style != MovementStyle.TELEPORT && b.style != MovementStyle.FLASH_JUMP) {
            return false;
        }
        if (now() < nextSkillMoveMs) {
            return false;
        }
        Point pos = chr.getPosition();
        if (pos == null) {
            return false;
        }
        // Axis-aware distance gate: horizontal blinks/dashes need real dx, but a mage may blink to a
        // stacked target nearly overhead (small dx, big dy) — flash jump stays horizontal-only (its
        // arc is a dash, not a climb).
        boolean farX = Math.abs(tx - pos.x) >= SKILL_MOVE_MIN_PX;
        boolean farY = b.style == MovementStyle.TELEPORT && Math.abs(ty - pos.y) >= SKILL_MOVE_MIN_DY_PX;
        if (!farX && !farY) {
            return false; // close enough to just walk the last stretch
        }
        boolean fired = (b.style == MovementStyle.TELEPORT)
                ? teleportOnLedge(chr, tx, ty, farX)
                : flashJumpOnLedge(chr, tx, ty);
        if (fired) {
            nextSkillMoveMs = now() + SKILL_MOVE_COOLDOWN_MS;
            b.lastMoveTargetX = Integer.MIN_VALUE; // re-issue a walk target next tick if still out of range
        }
        return fired;
    }

    // A flash jump that would land on a DIFFERENT ledge than the bot currently stands on is refused: the
    // dash is a horizontal arc, not a warp, and the nav graph's launch-run fit only bounds the run on the
    // CURRENT ledge — it doesn't see a drop mid-arc onto a mini-platform below (and an unbaked region gets
    // no fit guard at all). Hermits/Night Lords would otherwise fling themselves off the anchor ledge and
    // loop trying to dash back up. Returning false hands the caller its walk/pathfind fallback, which
    // climbs the rope back to the ledge instead. onDifferentLedge is peek-only (safe on the combat tick)
    // and answers "can't tell" as false, so an unbaked map falls through to the old dash behaviour — the
    // positional watchdog (planned) is the backstop there. TELEPORT has its own scoped gate below.
    private boolean flashJumpOnLedge(Character chr, int tx, int ty) {
        Point pos = chr.getPosition();
        if (pos == null) {
            return false;
        }
        if (GCMovement.onDifferentLedge(chr.getMap(), pos.x, pos.y, tx, ty)) {
            return false; // would leave our ledge -> walk/pathfind instead
        }
        return GCMovement.flashJump(chr, tx, b.fjCap);
    }

    // F1a: a TELEPORT blink onto a DIFFERENT ledge is refused for the HORIZONTAL case only. On mini-platform
    // maps (Orbis Cloud Park rocks) an ungated blink carries the mage onto a neighbouring rock the CAMP/PATROL
    // anchor watchdog then yanks it back from — the visible "backwards teleport" oscillation. Gating on farX
    // (a real horizontal chase, |dx| >= SKILL_MOVE_MIN_PX) kills that chase while leaving legit STACK vertical
    // fighting intact: a stack's member ledges overlap in X (SpotFinder requires >= 80px overlap), so an up/down
    // blink to a stacked target is small-dx (farY, !farX) and never trips this gate. A blanket onDifferentLedge
    // refusal would break STACK — stacked footholds are distinct nav regions so onDifferentLedge is ALWAYS true
    // between floors, and mages traverse a stack only by blinking. onDifferentLedge is peek-only (safe on the
    // combat tick) and answers "can't tell" as false, so an unbaked map falls through to the old blink; returning
    // false hands the caller its walk/pathfind fallback (or the WAIT->RELOCATE escalation).
    private boolean teleportOnLedge(Character chr, int tx, int ty, boolean farX) {
        Point pos = chr.getPosition();
        if (pos == null) {
            return false;
        }
        if (farX && GCMovement.onDifferentLedge(chr.getMap(), pos.x, pos.y, tx, ty)) {
            return false; // horizontal cross-ledge chase -> walk/pathfind (or WAIT/RELOCATE) instead
        }
        return GCMovement.teleport(chr, tx, ty);
    }

    private boolean jumpsToAttack() {
        return b.style == MovementStyle.JUMP_ATTACK || b.style == MovementStyle.FLASH_JUMP;
    }

    // The horizontal direction for an engage jump-attack hop: normally TOWARD the target so the bot
    // closes in an arc instead of pogo-ing, but occasionally AWAY (the mini-kite — leap back and let the
    // attack's faceTarget throw stars at the pursuer). An away hop is only taken when there's leashed,
    // same-level ground to land on. Returns 0 when the mob is underfoot (no clear side) so the caller
    // falls back to a vertical hop.
    private int jumpAttackDir(Character chr, Monster t) {
        Point pos = chr.getPosition();
        Point mp = (t != null) ? t.getPosition() : null;
        if (pos == null || mp == null) {
            return 0;
        }
        int dx = mp.x - pos.x;
        if (Math.abs(dx) <= TURN_DEADZONE_PX) {
            return 0; // mob underfoot -> a directional hop has no clear target side
        }
        int toward = dx > 0 ? 1 : -1;
        if (b.rng.nextDouble() < KITE_HOP_CHANCE && awayHopLands(chr, -toward)) {
            return -toward; // mini-kite
        }
        return toward;
    }

    // True if hopping one hop in `dir` keeps the bot inside its leash AND over same-level ground, so a
    // kite hop never leaps the bot off its platform or out of its segment. Ground comes from the active
    // strategy (camp: the spot's own ledge; roam: whatever is under the landing X), so roam thieves
    // mini-kite too. Only the AWAY hop needs this — a toward hop lands near an in-leash target.
    private boolean awayHopLands(Character chr, int dir) {
        Point pos = chr.getPosition();
        if (pos == null) {
            return false;
        }
        int landingX = pos.x + dir * JUMP_HOP_REACH_PX;
        int[] leash = b.effectiveLeash(chr);
        if (landingX < leash[0] || landingX > leash[1]) {
            return false; // would cross the leash
        }
        Point gp = b.strategyGroundAt(chr, landingX);
        return gp != null && Math.abs(gp.y - pos.y) <= GrindBrain.APPROACH_Y; // same-level ground to land on
    }

    // AoE step-into-the-pack flourish: an AoE-capable class (not RANGED — those want distance) that's in
    // range but standing off-centre of a live mob pack steps/blinks onto the pack's centroid so the swing
    // lands the whole cluster, then fires next beat. For a mage this reads as "blink into the pack and nuke".
    // Cadence + deadzone gated so it's an occasional adjust, not jitter; clamped to the bot's leash so it
    // never leaves its segment.
    private boolean aoeRepositionIfWorthwhile(Character chr) {
        if (b.style == MovementStyle.RANGED || now() < nextAoeRepositionMs || !BotAttackDriver.hasAoeAttack(chr)) {
            return false;
        }
        Point pos = chr.getPosition();
        if (pos == null) {
            return false;
        }
        Point c = SpotFinder.hostileClusterCentroid(chr.getMap(), pos, AOE_CLUSTER_PX, 2);
        if (c == null || Math.abs(c.x - pos.x) <= AOE_REPOSITION_DEADZONE_PX) {
            return false; // no pack, or already centred on it
        }
        int[] leash = b.effectiveLeash(chr);
        int tx = GrindBrain.clamp(c.x, leash[0], leash[1]);
        if (Math.abs(tx - pos.x) <= AOE_REPOSITION_DEADZONE_PX) {
            return false; // the leash pins us basically in place -> just swing from here
        }
        Point gp = GCMovement.groundPointBelow(chr.getMap(), tx, pos.y);
        int ty = (gp != null) ? gp.y : pos.y;
        nextAoeRepositionMs = now() + AOE_REPOSITION_COOLDOWN_MS;
        boolean blinked = (b.style == MovementStyle.TELEPORT) ? GCMovement.teleport(chr, tx, ty)
                : (b.style == MovementStyle.FLASH_JUMP) ? flashJumpOnLedge(chr, tx, ty)
                : false;
        if (!blinked) {
            GCMovement.move(chr, tx, ty);
            b.lastMoveTargetX = tx;
        }
        b.engaged = false; // we moved -> re-plant next beat before swinging
        return true;
    }

    // Ranged kiting: when a mob closes inside the comfort band, step back along the leash to re-open the
    // gap, then fire next beat from range. Conservative (cadence-gated, one step at a time). If the leash
    // pins the bot against its segment edge it just fires in place instead of moon-walking into a wall.
    private boolean kiteIfTooClose(Character chr, Monster mob) {
        if (now() < nextKiteMs) {
            return false;
        }
        Point pos = chr.getPosition();
        Point mp = (mob != null) ? mob.getPosition() : null;
        if (pos == null || mp == null || Math.abs(pos.x - mp.x) >= KITE_MIN_PX) {
            return false; // comfortable spacing already
        }
        int dir = (pos.x >= mp.x) ? 1 : -1; // step further from the mob
        int[] leash = b.effectiveLeash(chr);
        int tx = GrindBrain.clamp(pos.x + dir * KITE_STEP_PX, leash[0], leash[1]);
        if (Math.abs(tx - pos.x) < TURN_STEP_MIN_PX) {
            return false; // leash edge -> can't back up, just fire
        }
        Point gp = GCMovement.groundPointBelow(chr.getMap(), tx, pos.y);
        GCMovement.move(chr, tx, (gp != null) ? gp.y : pos.y);
        b.lastMoveTargetX = tx;
        b.engaged = false;
        nextKiteMs = now() + KITE_COOLDOWN_MS;
        return true;
    }

    // A mob on the bot's OTHER side needs a turn before the swing. Real MapleStory carries you a few px into
    // the new facing when you turn, so we spend one beat turning (face + a small step) and swing the next
    // beat, instead of an instant same-pixel flip-and-swing. Applies to every class (melee + ranged).
    private boolean needsTurnBeat(Character chr, Monster t) {
        if (now() < nextTurnBeatOkMs) {
            return false; // anti-thrash: don't burn a turn-beat every tick on a mob hovering near our x
        }
        Point bp = chr.getPosition();
        Point mp = (t != null) ? t.getPosition() : null;
        if (bp == null || mp == null || Math.abs(mp.x - bp.x) < TURN_DEADZONE_PX) {
            return false; // basically on top of us -> no meaningful facing to change
        }
        Boolean facingLeft = GCMovement.isFacingLeft(chr);
        if (facingLeft == null) {
            return false;
        }
        boolean wantLeft = mp.x < bp.x;
        return wantLeft != facingLeft; // only when we're currently facing the wrong way
    }

    private void doTurnBeat(Character chr, Monster t) {
        Point bp = chr.getPosition();
        boolean wantLeft = t.getPosition().x < bp.x;
        GCMovement.face(chr, wantLeft);
        int dir = wantLeft ? -1 : 1;
        int step = TURN_STEP_MIN_PX + b.rng.nextInt(TURN_STEP_MAX_PX - TURN_STEP_MIN_PX + 1); // 5..20px "tap"
        int[] leash = b.effectiveLeash(chr);
        int tx = GrindBrain.clamp(bp.x + dir * step, leash[0], leash[1]);
        Point gp = GCMovement.groundPointBelow(chr.getMap(), tx, bp.y);
        GCMovement.move(chr, tx, (gp != null) ? gp.y : bp.y);
        b.engaged = false;                // we stepped; re-plant next tick before swinging
        b.lastMoveTargetX = tx;
        nextTurnBeatOkMs = now() + TURN_BEAT_THROTTLE_MS;
        b.markProgress();                 // turning to engage is productive, not stuck
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
