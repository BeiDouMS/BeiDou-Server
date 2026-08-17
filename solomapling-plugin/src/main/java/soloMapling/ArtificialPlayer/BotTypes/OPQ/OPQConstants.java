package soloMapling.ArtificialPlayer.BotTypes.OPQ;

import java.awt.*;
import java.util.List;

/**
 * Map / item / platform constants for the Orbis Party Quest rush bot system.
 *
 * NOTE: Many of these IDs are placeholders taken from the OPQ spec document and
 * the common GMS v83 data set. They MUST be verified against this server's WZ
 * export before relying on them in live runs.
 * that are most likely to drift.
 */
public final class OPQConstants {

    private OPQConstants() {}

    // ---- Map IDs ------------------------------------------------------------
    public static final int OPQ_LOBBY      = 200080101; // recruitment lobby (Orbis Ticketing Booth area)
    public static final int OPQ_STAGE_1    = 920010000; // Stage 1 Cloud Collection Entrance - GOOD
    public static final int OPQ_TOWER      = 920010100; // intermediate tower hub
    public static final int OPQ_STAGE_2    = 920010400; // music record stage
    public static final int OPQ_EXIT_LOBBY = 920011200; // Exit Lobby On the Way Out - GOOD

    // Convenience grouping
    public static final List<Integer> OPQ_INSIDE_MAPS = List.of(
            OPQ_STAGE_1, OPQ_TOWER, OPQ_STAGE_2, OPQ_EXIT_LOBBY
    );

    // ---- NPC IDs -----------------------------------------------------------
    public static final int CHAMBERLAIN_EAK = 2013001; // Stage 1 Cloud NPC - Click on him to teleport to tower

    // ---- Item IDs -----------------------------------------------------------
    public static final int CLOUD_PIECE    = 4001063; // Stage 1 Cloud Pieces - GOOD

    // Stage 2: 7 distinct LP record items dropped by boxes
    public static final int RECORD_LP_FIRST = 4001056;
    public static final int RECORD_LP_LAST  = 4001062;
    public static final List<Integer> STAGE_2_ITEMS = List.of(
            4001056, 4001057, 4001058, 4001059, 4001060, 4001061, 4001062
    );

    public static final List<Integer> STAGE_1_ITEMS = List.of(CLOUD_PIECE);

    // ---- Stage 1 reactor data ids -------------------------------------------
    // Reactor data (template) ids on map 920010000, sourced from
    // wz/Map.wz/Map/Map9/920010000.img.xml:
    //   2006000 -> "eak" altar reactor where cloud pieces are dropped (skip).
    //   2002001 -> the 20 breakable cloud reactors (cloud1..cloud20).
    // Note: the 1000000003 / 1000000004-23 values seen in the !reactor list
    // command are runtime object ids (oid), which are reassigned per map
    // instance — never use those for filtering. Filter by data id instead.
    public static final int STAGE_1_ALTAR_REACTOR_ID = 2006000;
    public static final int STAGE_1_CLOUD_REACTOR_ID = 2002001;

    // ---- Stage 2 reactor data ids -------------------------------------------
    // 7 breakable box reactors on map 920010400 (one per platform m3–m9).
    // Data IDs confirmed from runtime dump: 2002004 through 2002010.
    public static final int STAGE_2_BOX_REACTOR_FIRST = 2002004;
    public static final int STAGE_2_BOX_REACTOR_LAST  = 2002010;
    // Central music box reactor (dataId=2008006, pos ~x=-1706,y=-240).
    public static final int STAGE_2_MUSIC_BOX_REACTOR_ID = 2008006;

    // Ordinal labels for chat: index 0 = reactor 1000000005, index 6 = reactor 1000000011
    public static final String[] STAGE_2_BOX_ORDINALS = {
            "1st", "2nd", "3rd", "4th", "5th", "6th", "7th"
    };

    // ---- Stage 1 top-platform (post-handin) ---------------------------------
    // After the leader hands all cloud pieces to the NPC, they get teleported
    // to a small platform near the top of the map. Bots watch the leader's
    // position and treat stage 1 as complete the moment they observe the
    // leader inside the tolerance box around this anchor.
    //
    // MapleStory uses screen-style coords (down is +y) so the top of a map
    // lives at very negative y values — the sign on Y here is correct.
    public static final Point STAGE_1_ENTRY_TP = new Point(266, 143);
    public static final Point STAGE_1_COMPLETE_TP = new Point(165, -1270);
    public static final int STAGE_1_TOP_PLATFORM_X        =   165;
    public static final int STAGE_1_TOP_PLATFORM_Y        = -1270;
    public static final int STAGE_1_TOP_PLATFORM_TOL_X_PX =   300;
    public static final int STAGE_1_TOP_PLATFORM_TOL_Y_PX =   150;

    // ---- Stage 1 loot scan tuning -------------------------------------------
    // After breaking a cloud reactor, the dropped piece can land at the
    // reactor's anchor or fall through several footholds below it. The bot
    // first probes its own feet (where the just-broken reactor was) and, if
    // nothing's there, walks straight down the reactor's x in fixed steps.
    public static final double STAGE_1_LOOT_SCAN_RANGE_PX    = 8000;
    public static final int    STAGE_1_LOOT_FALLBACK_STEPS   =  20;
    public static final int    STAGE_1_LOOT_FALLBACK_STEP_PX = 2500;

    // ---- Platform / coordinate targets --------------------------------------
    // Stage 1 is reactor-driven, not platform-driven: bots scan the map for
    // cloud reactors and walk to the reactor's coordinate directly. The
    // orchestrator assigns each bot a unique reactor oid (closest-unclaimed).

    // Stage 1 drop / return zone — where the leader stands near the NPC.

    // Stage 2: boxes are on platforms m3–m9 (m1/m2 are base floor and entry).
    public static final List<String> STAGE_2_BOX_PLATFORMS = List.of(
            "m3", "m4", "m5", "m6", "m7", "m8", "m9"
    );

    // Stage 2 drop zone — bots navigate to the music box reactor position.
    public static final String STAGE_2_DROP_PLATFORM = "m1";

    // ---- Tuning -------------------------------------------------------------
    public static final long STAGE_WAIT_TIMEOUT_MS        = 120_000; // 2 min per stage wait
    public static final long RECRUIT_MESSAGE_INTERVAL_MS  =  12_000; // chat every ~12s in lobby
    public static final long NAVIGATE_SETTLE_MS           =   1_500; // pause after arriving at platform
    public static final long ASSIGNMENT_POLL_MS           =     500; // how often the bot re-checks a null assignment
    public static final int  MAX_REACTOR_HITS             =       4; // OPQ cloud reactors break after 4 hits
    public static final long SWING_INTERVAL_MS            =     700; // delay between bot swings (~typical 1H attack speed)
    public static final int  REACTOR_HIT_RANGE_PX         =      200; // bot must be within this many px of reactor.x to hit
}
