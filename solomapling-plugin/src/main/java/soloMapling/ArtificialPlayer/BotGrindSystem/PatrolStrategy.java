package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotSpotClaims;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// PATROL: camp with a route. For SPREAD maps (several medium spots, real gaps between them) the bot
// holds an ordered ring of the map's best 2-4 spots and, when its current spot goes dry, rotates to
// the NEXT ring member instead of re-scoring the whole map — a purposeful sweep back and forth across
// the field instead of either camping one spot through long lulls or teleport-shopping for the global
// argmax. Everything else — claims, bands, patience (already SPREAD-tuned via the regime knobs),
// FIGHT/WAIT, loot — is inherited from CampStrategy unchanged; only pickSpot() differs. Ours.
final class PatrolStrategy extends CampStrategy {

    private static final int RING_MAX = 4;   // ring size cap (top spots by harvestable feed)
    private static final int RING_MIN = 2;   // fewer reachable spots than this -> plain CAMP picking

    private List<Integer> ring = List.of();  // ring members as profile spot indices, x-sorted (natural sweep)
    private int ringPos = -1;                // current ring position (-1 = not started)

    PatrolStrategy(GrindBrain brain) {
        super(brain);
    }

    @Override
    public GrindStyle style() {
        return GrindStyle.PATROL;
    }

    @Override
    public String label() {
        return (ring.isEmpty() ? "[patrol]" : "[patrol " + (ringPos + 1) + "/" + ring.size() + "]")
                + " " + super.label();
    }

    @Override
    public void releaseUnderLock(Character chr) {
        super.releaseUnderLock(chr);
        ring = List.of();
        ringPos = -1;
    }

    @Override
    public void resetEpisodeUnderLock() {
        super.resetEpisodeUnderLock();
        ring = List.of();
        ringPos = -1;
    }

    // Rotate: the next ring member with a free claim slot wins; a fully-claimed ring falls back to its
    // least-crowded member (sharing — the overflow story matches camp's). The just-left spot isn't
    // specially excluded: with >= 2 members the rotation never re-picks it first, and on a busy ring
    // sharing it again is the correct fallback anyway. Each claim-retry attempt advances the ring.
    @Override
    Spot pickSpot(Character chr, MapGrindProfile p) {
        if (ring.isEmpty()) {
            buildRing(chr, p);
        }
        if (ring.size() < RING_MIN) {
            return super.pickSpot(chr, p); // degenerate map for a ring — behave exactly like CAMP
        }
        int n = ring.size();
        int fallbackPos = -1;
        int fallbackHolders = Integer.MAX_VALUE;
        for (int i = 1; i <= n; i++) {
            int pos = (Math.max(ringPos, 0) + i) % n;
            int idx = ring.get(pos);
            Spot s = p.spots().get(idx);
            int holders = BotSpotClaims.holders(chr.getMapId(), idx);
            if (holders < s.shareCap()) {
                ringPos = pos;
                return s;
            }
            if (holders < fallbackHolders) {
                fallbackHolders = holders;
                fallbackPos = pos;
            }
        }
        ringPos = fallbackPos;
        return p.spots().get(ring.get(fallbackPos));
    }

    // Ring = the top RING_MAX reachable spots by harvestable feed, then x-sorted so the rotation
    // sweeps across the map instead of zig-zagging. Built once per episode (the profile is static).
    private void buildRing(Character chr, MapGrindProfile p) {
        Point pos = chr.getPosition();
        Set<Integer> reach = (pos != null)
                ? GCMovement.reachableRegions(chr.getMap(), pos.x, pos.y)
                : Set.of();
        boolean filter = !reach.isEmpty();
        List<Integer> candidates = new ArrayList<>();
        for (int i = 0; i < p.spots().size(); i++) {
            Spot s = p.spots().get(i);
            if (filter && s.regionId() >= 0 && !reach.contains(s.regionId())) {
                continue; // unreachable island ledge
            }
            candidates.add(i);
        }
        candidates.sort((a, z) -> Integer.compare(
                p.spots().get(z).sameLedgeSpawnCount(), p.spots().get(a).sameLedgeSpawnCount()));
        List<Integer> top = new ArrayList<>(candidates.subList(0, Math.min(RING_MAX, candidates.size())));
        top.sort((a, z) -> Integer.compare(p.spots().get(a).anchor().x, p.spots().get(z).anchor().x));
        ring = List.copyOf(top);
        ringPos = -1;
    }
}
