package soloMapling.ArtificialPlayer.BotTownSystem;

import java.awt.Point;
import java.util.List;

// Per-map curation overrides that compose with the anchor-weighted algorithm (TownPresenceSampler):
//   - pins:  exact points that are always placed first (hand-polish exactly where taste matters);
//   - ban:   rectangles where no bot is ever placed (kill a bad spot - a wall corner, the crane top);
//   - boost: rectangles whose ledges get their weight multiplied (pull more of the crowd to a plaza).
// A zone's Y bounds are optional (Integer.MIN/MAX when unset) so an X-only band bans/boosts a whole
// vertical slice. Everything here is data the owner authors in TownPresence.yaml (plus pins appended by
// the mark-this-spot command); the algorithm is the floor, these are the polish on top.
public final class TownOverrides {

    // A rectangle with an optional weight multiplier (used only by boost zones; ban zones ignore it).
    public record Zone(int x1, int y1, int x2, int y2, double mult) {
        public boolean contains(int x, int y) {
            return x >= Math.min(x1, x2) && x <= Math.max(x1, x2)
                    && y >= Math.min(y1, y2) && y <= Math.max(y1, y2);
        }
    }

    public static final TownOverrides EMPTY = new TownOverrides(List.of(), List.of(), List.of());

    private final List<Zone> ban;
    private final List<Zone> boost;
    private final List<Point> pins;

    public TownOverrides(List<Zone> ban, List<Zone> boost, List<Point> pins) {
        this.ban = ban;
        this.boost = boost;
        this.pins = pins;
    }

    public List<Point> pins() {
        return pins;
    }

    public boolean isEmpty() {
        return ban.isEmpty() && boost.isEmpty() && pins.isEmpty();
    }

    // True if (x,y) falls inside any ban zone.
    public boolean isBanned(int x, int y) {
        for (Zone z : ban) {
            if (z.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    // Product of every boost zone containing (x,y) (1.0 if none) - the weight multiplier for that point.
    public double boostMultiplier(int x, int y) {
        double m = 1.0;
        for (Zone z : boost) {
            if (z.contains(x, y)) {
                m *= z.mult();
            }
        }
        return m;
    }
}
