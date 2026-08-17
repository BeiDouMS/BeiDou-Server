package soloMapling.ArtificialPlayer.BotGrindSystem;

import java.util.List;

// A detected vertical stack of grind spots: 2+ spots whose X extents overlap and whose anchor ledges
// sit within blink reach of a vertical neighbour (slime tree, golem temple, Kerning subway car roofs).
// The STACK archetype claims a whole stack as one composite spot and assigns claim slots to member
// ledges, so bots layer visibly across the levels. Detected once per map in SpotFinder.build from the
// same static spawn+foothold data as the spots themselves. Ours (SoloMapling).
public record SpotStack(
        List<Integer> spotIndices,  // members as indices into MapGrindProfile.spots(), sorted top-down (ascending y)
        int x0, int x1,             // union of member X extents (anchor ± radius) — the tether's horizontal bounds
        int topY, int bottomY,      // anchor y of the top / bottom member ledges
        int totalFeed,              // sum of member sameLedgeSpawnCount — the stack's harvestable feed
        boolean hopTraversable) {   // every vertical neighbour gap within plain-jump reach (non-teleport classes can work it)
}
