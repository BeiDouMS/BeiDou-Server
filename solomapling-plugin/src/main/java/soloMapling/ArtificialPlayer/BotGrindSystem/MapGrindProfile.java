package soloMapling.ArtificialPlayer.BotGrindSystem;

import java.util.List;

// Cheap per-map grind measurement, computed once at the first SELECT_SPOT and cached in SpotFinder.
// The spawn layout is static WZ data, so this is effectively build-once. It is the SINGLE map
// measurement the grind sub-FSM reads: SELECT_SPOT scores `spots` for a bot (the §4 score + a per-bot
// reachability filter), and WAIT's relocate patience is regime-adjusted (GrindBrain.waitPatienceMs /
// unproductiveMs — COMPACT camps through lulls, SPREAD/SPARSE leave a dry spot sooner). The regime label
// only nudges knob DEFAULTS (patience / relocate-eagerness) and debug narration — it NEVER branches the
// FSM. walkable* is the bbox over the walkable ledges (the grind-relevant "size", not the VR rectangle).
//
// steady-state mob count == spawnPointCount (MapleMap.respawn refills toward monsterSpawn.size()), so
// spawnDensity = spawnPointCount / walkableSpanX is a true "how camp-able" proxy. Our own creation.
public record MapGrindProfile(
        int mapId,
        int walkableMinX,
        int walkableMaxX,
        int walkableSpanX,
        int spawnPointCount,
        double spawnDensity,
        List<Spot> spots,
        int clusterCount,
        int meanInterSpotGapX,
        Regime regime,
        boolean roam,
        List<SpotStack> stacks,
        long builtAtMs) {

    // Coarse map classification. Tunes knob defaults + narration only; the FSM is identical for all three.
    public enum Regime { COMPACT, SPREAD, SPARSE }

    // roam = no spot holds a harvestable pack (best sameLedgeSpawnCount below SpotFinder's campable floor):
    // tiny-platform / jumpy-mob maps (Ludibrium dice rooms, Terrace Hall) where anchoring starves. The grind
    // brain routes these bots to the anchor-free roam path (seek nearest live mob across ledges) instead of
    // CAMP. Orthogonal to Regime (which still tunes CAMP patience). Map-archetype grinding, Phase 2.

    // stacks = detected vertical spot stacks (SpotFinder.detectStacks): overlapping-X spots whose ledges
    // sit within blink reach of a vertical neighbour. GrindStylePolicy sends stack-capable classes to the
    // STACK archetype when a stack dominates the map's feed; RoamStrategy uses stack membership to
    // validate the vertical hops it may take (roam "Phase-3-lite").
}
