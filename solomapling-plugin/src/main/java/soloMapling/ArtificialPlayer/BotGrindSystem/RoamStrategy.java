package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapObject;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;

// ROAM: anchor-free grind for un-campable maps (no ledge holds a harvestable pack — tiny platforms,
// jumpy mobs). Each beat: loot underfoot, seek the nearest live mob across ledges and close on it —
// walking / arced-jumping / blinking — swing when in range, repeat. Reuses the same shared engage
// beats as CAMP; the only difference is there is no spot/claim pinning the bot. Moved verbatim from
// the pre-split GrindBrain, plus two additions sanctioned by Audit III: the WAIT-style loot sweep now
// runs during dry beats (roam ranged bots finally clear their piles), and a persistent dry seek range
// reports the map saturated so the macro crowd-bail relieves contested roam maps. Ours (SoloMapling).
final class RoamStrategy implements GrindStrategy {

    private static final int ROAM_RANGE_X = 900;            // seek a live mob within this |dx| (cross-ledge)
    // Keep roam HORIZONTAL-first: only target mobs within ~a jump/short-drop of the bot's own Y, so it never
    // commits to a steep off-map descent on tall vertical maps (El Nath cliffs) where our live faller has no
    // floor clamp. Safe multi-ledge vertical descent (stack-adjacent ledges) is the STACK/Phase-3 work.
    private static final int ROAM_RANGE_Y = 140;
    // "Phase-3-lite" vertical unlock: a mob beyond ROAM_RANGE_Y is still admitted (up to this dy) when
    // it stands on a ledge in the SAME detected stack as the bot — a validated blink/hop pair (slime
    // tree levels), never an open descent (El Nath cliffs stay protected by the tight range above).
    private static final int ROAM_STACK_RANGE_Y = 400;
    static final int ROAM_LEASH_PX = 700;                   // roam has no anchor: turn/kite steps clamp to a wide box around the bot
    private static final int ROAM_LOOT_SEARCH_PX = 600;     // dry-beat loot sweep radius (roam has no spot radius to reuse)
    // Seek range dry this long while grinding -> the map is contested (or exhausted) for this bot; either
    // way the right move is elsewhere. Surfaced via saturated() so TrainingBot's crowd-bail relocates it
    // (Audit III 2.3 — roam maps previously had no crowding signal at all).
    private static final long ROAM_STARVED_MS = 12_000;

    private final GrindBrain b;
    private long starvedSinceMs = 0L;       // first dry-beat moment (0 = seek range is feeding)
    private volatile boolean saturated = false;

    RoamStrategy(GrindBrain brain) {
        this.b = brain;
    }

    @Override
    public GrindStyle style() {
        return GrindStyle.ROAM;
    }

    @Override
    public void start(Character chr) {
        // No spot to pre-claim; the first observed tick starts seeking from wherever the bot stands.
    }

    @Override
    public void tick(Character chr) {
        b.loot.grabLootAtFeet(chr);
        Monster t = roamTarget(chr);
        if (t == null) {
            // Dry beat: tidy the drop pile first (a real player loots between packs), then hold and re-scan.
            int[] leash = leash(chr);
            if (!b.loot.tryWalkAndLoot(chr, leash[0], leash[1], ROAM_LOOT_SEARCH_PX)) {
                GCMovement.stop(chr); // no mob in seek range this beat — hold; re-scan next tick
                b.engaged = false;
            }
            if (starvedSinceMs == 0L) {
                starvedSinceMs = now();
            } else if (now() - starvedSinceMs >= ROAM_STARVED_MS) {
                saturated = true; // persistently dry -> contested/exhausted; macro crowd-bail moves us on
            }
            return;
        }
        starvedSinceMs = 0L;
        saturated = false;
        if (b.inAttackRange(chr, t)) {
            if (b.engage.preSwingAdjust(chr, t)) {
                return;
            }
            var r = b.engage.engageAndSwing(chr, t);
            if (r != null && r.killed()) {
                b.engaged = false; // roam re-plants per kill (the next pack is usually a hop away)
            }
            return;
        }
        b.engaged = false;
        approachRoam(chr, t);
        if (b.madeApproachProgress(chr)) {
            b.markProgress();
        } else if (now() > b.retargetDeadline) {
            b.targetOid = -1; // can't reach it (e.g. an isolated platform) -> drop and seek another next tick
        }
    }

    @Override
    public void releaseUnderLock(Character chr) {
        starvedSinceMs = 0L;
        saturated = false;
    }

    @Override
    public void resetEpisodeUnderLock() {
        starvedSinceMs = 0L;
        saturated = false;
    }

    @Override
    public void resetAfterTeleport(Character chr) {
        starvedSinceMs = 0L; // fresh position, fresh seek window
    }

    @Override
    public boolean saturated() {
        return saturated;
    }

    @Override
    public String label() {
        return "[roam]";
    }

    // Roam has no anchor: turn/kite/hop steps clamp to a wide box around the bot instead.
    @Override
    public int[] leash(Character chr) {
        Point p = (chr != null) ? chr.getPosition() : null;
        int cx = (p != null) ? p.x : 0;
        return new int[]{cx - ROAM_LEASH_PX, cx + ROAM_LEASH_PX};
    }

    // Roam hops land on whatever ground is below the landing X (cross-ledge is the point here).
    @Override
    public Point groundAt(Character chr, int x) {
        Point p = (chr != null) ? chr.getPosition() : null;
        if (p == null || chr.getMap() == null) {
            return null;
        }
        return GCMovement.groundPointBelow(chr.getMap(), x, p.y);
    }

    // The current roam target if still alive/hostile/in seek range, else the nearest live cross-ledge mob.
    // Both the sticky check and the fresh seek use the same acceptance: the tight horizontal-first box,
    // extended vertically only along validated stack pairs.
    private Monster roamTarget(Character chr) {
        Point pos = chr.getPosition();
        if (pos == null) {
            return null;
        }
        MapGrindProfile p = SpotFinder.profileIfBuilt(chr.getMapId());
        if (b.targetOid >= 0) {
            MapObject mo = chr.getMap().getMapObject(b.targetOid);
            if (mo instanceof Monster m && SpotFinder.isHostile(m)) {
                Point mp = m.getPosition();
                if (mp != null && Math.abs(mp.x - pos.x) <= ROAM_RANGE_X && withinSeekY(chr, p, pos, mp)) {
                    return m; // keep chasing the same mob
                }
            }
        }
        Monster t = SpotFinder.nearestHostileCrossLedge(chr.getMap(), pos, ROAM_RANGE_X, ROAM_RANGE_Y,
                p, ROAM_STACK_RANGE_Y);
        b.targetOid = (t != null) ? t.getObjectId() : -1;
        b.resetApproachProgress(chr);
        return t;
    }

    private boolean withinSeekY(Character chr, MapGrindProfile p, Point pos, Point mp) {
        int ady = Math.abs(mp.y - pos.y);
        return ady <= ROAM_RANGE_Y
                || (ady <= ROAM_STACK_RANGE_Y && SpotFinder.sameStack(p, chr.getMap(), pos, mp));
    }

    // Approach a roam target across ledges (no leash): mages blink / hermits dash toward it, else walk to the
    // foothold under it (the nav layer jumps/ropes up as needed).
    private void approachRoam(Character chr, Monster mob) {
        Point mp = mob.getPosition();
        if (mp == null) {
            return;
        }
        Point gp = GCMovement.groundPointBelow(chr.getMap(), mp.x, mp.y);
        int tx = mp.x;
        int ty = (gp != null) ? gp.y : mp.y;
        if (b.engage.skillMoveToward(chr, tx, ty)) {
            return; // blink/dash toward it
        }
        if (now() < b.attackWalkLockUntil) {
            return; // just swung — hold, then move
        }
        if (Math.abs(tx - b.lastMoveTargetX) < GrindBrain.ROAM_RETARGET_EPS) {
            return; // already heading there
        }
        GCMovement.move(chr, tx, ty);
        b.lastMoveTargetX = tx;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
