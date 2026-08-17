package soloMapling.ArtificialPlayer.BotGrindSystem;

import java.awt.Point;

// A grind spot: an anchor on a reachable ledge plus a tight radius, and the spawn accounting of the
// cluster it was built from. The bot plants here, kills what falls inside the radius, and waits out
// respawn lulls. radius is cluster-sized (clamped to SPOT_RADIUS_MIN/MAX in SpotFinder), so a compact
// map collapses to one fat spot and a long field tiles into a row of adjacent spots.
//
// spawnCount is the WHOLE cluster's spawn points (capacity/debug); sameLedgeSpawnCount is the subset
// on the anchor's own ledge — the feed the bot can actually harvest, since combat's same-ledge gate
// never bleeds to stacked neighbour ledges. ledgeSpanPx is the anchor ledge's walkable length (0 when
// the nav graph wasn't baked at build time). shareCap is how many bots may claim the spot at once,
// derived from its width (~one per MIN_BAND_PX of span): a long platform legitimately hosts a few
// spaced bots, a short one stays single-occupancy. Our own creation (not a GreenCat extraction).
public record Spot(Point anchor, int regionId, int radius, int spawnCount,
                   int sameLedgeSpawnCount, int ledgeSpanPx, int shareCap) {
}
