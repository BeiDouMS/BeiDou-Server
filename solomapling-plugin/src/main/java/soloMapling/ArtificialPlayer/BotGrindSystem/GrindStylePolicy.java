package soloMapling.ArtificialPlayer.BotGrindSystem;

// Resolves the natural (map-derived) grind archetype for an episode, class-aware: the map proposes,
// the bot's MovementStyle disposes. Kept separate from the strategies so the classification rules are
// tuned in one place; GrindBrain applies the GM forced override (!bot grindstyle) on top. Ours.
final class GrindStylePolicy {

    // Stack admission: the best traversable stack must carry this multiple of the best single ledge's
    // feed (and a floor) before STACK beats CAMP — a stack always contains its members, so dominance
    // means "several meaningful floors", not "one good ledge plus crumbs". Calibrate against
    // !env grindprofile dumps of the slime tree / golem temple / Kerning subway vs HHG.
    private static final double STACK_DOMINANCE = 1.5;
    private static final int STACK_MIN_FEED = 8;
    // A SPREAD map with at least this many spots patrols a ring of them instead of camping one.
    private static final int PATROL_MIN_SPOTS = 3;

    private GrindStylePolicy() {
    }

    // Natural style: a feed-dominant vertical stack (for a class that can traverse it) runs STACK; an
    // un-campable map (no ledge holds a harvestable pack) roams; a SPREAD map with several real spots
    // patrols a ring of them; everything else camps. A PLANTED or RANGED bot on a stack map simply
    // camps the best single ledge — same map, different style per class, which is the point (a mage
    // thrives on the tree; a bowman holds a floor).
    static GrindStyle natural(MapGrindProfile p, MovementStyle ms) {
        if (p == null) {
            return GrindStyle.CAMP;
        }
        if (bestTraversableStack(p, ms) != null) {
            return GrindStyle.STACK;
        }
        if (p.roam()) {
            return GrindStyle.ROAM;
        }
        if (p.regime() == MapGrindProfile.Regime.SPREAD && p.spots().size() >= PATROL_MIN_SPOTS) {
            return GrindStyle.PATROL;
        }
        return GrindStyle.CAMP;
    }

    // The feed-dominant stack this movement style can work, or null when CAMP/ROAM should handle the map.
    static SpotStack bestTraversableStack(MapGrindProfile p, MovementStyle ms) {
        int bestSameLedge = 0;
        for (Spot sp : p.spots()) {
            bestSameLedge = Math.max(bestSameLedge, sp.sameLedgeSpawnCount());
        }
        SpotStack best = null;
        for (SpotStack st : p.stacks()) {
            if (!canTraverse(ms, st)) {
                continue;
            }
            if (st.totalFeed() < STACK_MIN_FEED || st.totalFeed() < bestSameLedge * STACK_DOMINANCE) {
                continue;
            }
            if (best == null || st.totalFeed() > best.totalFeed()) {
                best = st;
            }
        }
        return best;
    }

    // TELEPORT bridges any stack gap up to the blink range; jump-capable thieves work hop-traversable
    // stacks (plain jumps / ropes between floors); PLANTED/RANGED walkers stay off stacks entirely.
    static boolean canTraverse(MovementStyle ms, SpotStack st) {
        if (ms == MovementStyle.TELEPORT) {
            return true;
        }
        return (ms == MovementStyle.FLASH_JUMP || ms == MovementStyle.JUMP_ATTACK) && st.hopTraversable();
    }
}
