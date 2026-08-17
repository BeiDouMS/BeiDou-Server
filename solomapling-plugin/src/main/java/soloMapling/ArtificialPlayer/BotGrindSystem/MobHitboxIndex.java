package soloMapling.ArtificialPlayer.BotGrindSystem;

import org.gms.provider.Data;
import org.gms.provider.DataProvider;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;
import org.gms.util.StringUtil;

import java.awt.Point;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Per-mob hitbox (half-width / height in px), read straight from WZ Mob data and cached forever. The
// server never loads mob geometry otherwise (only stats), so this fills the gap: the rest-spot safety
// model needs each map's mob size to know how far a mob's body reaches around a spawn.
//
// Each mob img has stand/move animation frames, and every frame canvas carries `lt`/`rb` bounding-box
// vectors given as offsets from the foot origin (lt = top-left: negative x = left reach, negative y =
// height above the feet; rb = bottom-right: positive x = right reach). We take the WIDEST box across all
// stand + move frames (an animated mob swings past its first frame). Missing/malformed data or a mob img
// that only links to another falls back to a conservative default. Ours (SoloMapling).
public final class MobHitboxIndex {

    private MobHitboxIndex() {
    }

    // halfWidth = max side reach from the origin; height = max reach above the feet. reach() collapses the
    // box to the single "how far can this mob's body touch" number the inflation radius uses.
    public record Hitbox(int halfWidth, int height) {
        public int reach() {
            return Math.max(halfWidth, height);
        }
    }

    // Conservative fallback when a mob's geometry can't be read (roughly a small humanoid mob).
    private static final Hitbox DEFAULT = new Hitbox(40, 60);
    private static final int MAX_LINK_DEPTH = 3;

    private static final ThreadLocal<DataProvider> MOB_SOURCE =
            ThreadLocal.withInitial(() -> DataProviderFactory.getDataProvider(WZFiles.MOB));
    private static final Map<Integer, Hitbox> CACHE = new ConcurrentHashMap<>();

    public static Hitbox hitbox(int mobId) {
        return CACHE.computeIfAbsent(mobId, MobHitboxIndex::compute);
    }

    // How far this mob's body reaches from where it stands (px) — the single number the rest-spot
    // inflation radius scales.
    public static int reach(int mobId) {
        return hitbox(mobId).reach();
    }

    private static Hitbox compute(int mobId) {
        try {
            return computeWithDepth(mobId, 0);
        } catch (RuntimeException e) {
            return DEFAULT;
        }
    }

    private static Hitbox computeWithDepth(int mobId, int depth) {
        DataProvider src = MOB_SOURCE.get();
        Data mob = src.getData(StringUtil.getLeftPaddedStr(mobId + ".img", '0', 11));
        if (mob == null) {
            return DEFAULT;
        }
        int halfWidth = 0;
        int height = 0;
        for (String anim : new String[]{"stand", "move"}) {
            Data node = mob.getChildByPath(anim);
            if (node == null) {
                continue;
            }
            for (Data frame : node) {
                Point lt = DataTool.getPoint("lt", frame, null);
                Point rb = DataTool.getPoint("rb", frame, null);
                if (lt == null || rb == null) {
                    continue;
                }
                halfWidth = Math.max(halfWidth, Math.max(Math.abs(lt.x), Math.abs(rb.x)));
                height = Math.max(height, Math.max(Math.abs(lt.y), Math.abs(rb.y)));
            }
        }
        if (halfWidth == 0 && height == 0) {
            // No own frames — a link-only mob img carries its geometry under the linked id (same pattern
            // LifeFactory follows for stats).
            Data info = mob.getChildByPath("info");
            int link = info != null ? DataTool.getIntConvert("link", info, 0) : 0;
            if (link != 0 && link != mobId && depth < MAX_LINK_DEPTH) {
                return computeWithDepth(link, depth + 1);
            }
            return DEFAULT;
        }
        return new Hitbox(Math.max(1, halfWidth), Math.max(1, height));
    }
}
