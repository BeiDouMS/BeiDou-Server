package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;

import java.awt.Point;

// One grind archetype's behavior, driven by GrindBrain (the facade + shared combat context).
// Implementations keep their own placement state (spots, claims, rings, tethers); everything a beat
// shares across styles — sticky target, engage flags, skill-move/turn/kite cadences, loot, rope
// recovery, the watchdog heartbeat — lives on GrindBrain and is accessed directly (same package).
// Ours (SoloMapling).
interface GrindStrategy {

    GrindStyle style();

    // Episode entry (macro thread, OUTSIDE the claim lock): pre-position / pre-claim so a cohort
    // disperses even unobserved. Movement calls are allowed here.
    void start(Character chr);

    // One observed combat tick (shared ticker thread). The brain has already handled the
    // observation gate and rope recovery.
    void tick(Character chr);

    // Drop claims + placement state. Always called UNDER GrindBrain.claimLock (from release() and
    // from a mid-episode strategy swap), so claim mutations stay serialized with in-flight selects.
    void releaseUnderLock(Character chr);

    // Reset placement state under the claim lock at episode start (a leftover claim was already
    // released by release(); a raced one is prevented by claimActive).
    void resetEpisodeUnderLock();

    // The macro watchdog teleported the bot to a portal: re-anchor from the new position. The brain
    // has already dropped the sticky target and approach progress; the heartbeat is deliberately
    // NOT refreshed (the bail timer keeps running if the teleport didn't help).
    void resetAfterTeleport(Character chr);

    // True when this map is full for this style (every spot claimed / seek range contested) — the
    // macro crowd-bail reads it through GrindBrain.mapSaturated().
    boolean saturated();

    // Debug narration tag (TrainingBot debugChat lines).
    String label();

    // The [x0, x1] X range movement may target this tick — the shared engage beats (turn step, kite
    // step, AoE reposition, kite hop) clamp to it so a bot never leaves its segment.
    int[] leash(Character chr);

    // Strategy-appropriate ground under x for hop/step landing checks (camp: the spot's own ledge;
    // roam: whatever ground is below). Null = no safe ground known.
    Point groundAt(Character chr, int x);
}
