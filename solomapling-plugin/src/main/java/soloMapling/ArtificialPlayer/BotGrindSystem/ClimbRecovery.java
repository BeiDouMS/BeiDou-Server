package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;
import org.gms.server.life.Monster;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;

// Rope-stall recovery shared by every grind style: while the bot is on a rope, let a deliberate nav
// climb finish, detect a hung climb (no vertical progress), and dismount toward the current target.
// Extracted verbatim from the pre-split GrindBrain; the only change is that the dismount direction
// now reads the brain's shared target (any live target, not the camp-validated sticky one — for a
// dismount kick the direction is all that matters). Ours (SoloMapling).
final class ClimbRecovery {

    private static final long CLIMB_STALL_MS = 1_200;
    private static final int CLIMB_PROGRESS_EPS = 6;
    private static final long DISMOUNT_GRACE_MS = 3_000;

    private final GrindBrain b;
    private long climbStallSinceMs = 0L;
    private long lastDismountMs = 0L;
    private int lastClimbY = 0;

    ClimbRecovery(GrindBrain brain) {
        this.b = brain;
    }

    void reset() {
        climbStallSinceMs = 0L;
        lastDismountMs = 0L;
        lastClimbY = 0;
    }

    // Back on a foothold — clear the stall tracker (called each grounded tick).
    void onGrounded() {
        climbStallSinceMs = 0L;
    }

    // True while the bot is mid-rope or just dismounted — the watchdog defers escalation during recovery.
    boolean isRecovering(Character chr) {
        return GCMovement.isClimbing(chr) || (now() - lastDismountMs) < DISMOUNT_GRACE_MS;
    }

    void handleClimb(Character chr) {
        Point pos = chr.getPosition();
        int y = (pos != null) ? pos.y : 0;
        if (GCMovement.isNavigatingClimb(chr)) {
            climbStallSinceMs = 0L; // deliberate traversal — let the driver finish it
            lastClimbY = y;
            return;
        }
        if (climbStallSinceMs == 0L || Math.abs(y - lastClimbY) >= CLIMB_PROGRESS_EPS) {
            climbStallSinceMs = now(); // making progress (or first sample) — let the climb continue
            lastClimbY = y;
            return;
        }
        if (now() - climbStallSinceMs >= CLIMB_STALL_MS) {
            dismountTowardMob(chr, b.currentTargetMonster(chr)); // hung rope -> jump off toward the current target
        }
    }

    private void dismountTowardMob(Character chr, Monster mob) {
        int dx = 0;
        Point pos = chr.getPosition();
        if (mob != null && mob.getPosition() != null && pos != null) {
            dx = Integer.compare(mob.getPosition().x, pos.x);
        }
        GCMovement.dismountRope(chr, dx);
        lastDismountMs = now();
        climbStallSinceMs = 0L;
        b.engaged = false;
        b.lastMoveTargetX = Integer.MIN_VALUE;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
