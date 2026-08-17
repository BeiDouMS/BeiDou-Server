package soloMapling.ArtificialPlayer.BotAttackSystem;

import org.gms.client.Character;
import soloMapling.server.MethodScheduler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Bot buff maintenance. For each of the bot's class buffs whose recast timer has elapsed,
 * broadcasts the buff visual via BotBuffEffects - no stats, MP, or registered effect, just
 * the packets that make the buff look like it happened. Recast cadence is 90% of the buff's
 * WZ duration; timer-gated, so most ticks cast nothing. Call clearBot when a bot despawns.
 */
// Bot-buff maintenance loop; cosmetic reimplementation inspired by GreenCatMS. Credit: NutNNut for the idea.
public final class BotBuffDriver {

    // botId -> (skillId -> absolute epoch-ms at which this buff may be re-shown)
    private static final Map<Integer, Map<Integer, Long>> nextCastByBot = new ConcurrentHashMap<>();

    // Recast cadence for a buff whose WZ duration is missing / non-positive.
    private static final long FALLBACK_RECAST_MS = 60_000L;

    // Gap between consecutive buffs so a multi-buff bot casts them one at a time
    // (in order) rather than all at once.
    private static final long STAGGER_MS = 750L;

    private BotBuffDriver() {}

    /*
     * Maintain the bot's class buffs (the botBuff() entry point). Timer-gated
     * and cheap; safe to call every tick for every bot.
     */
    public static void botBuff(Character bot) {
        castDueBuffs(bot, false);
    }

    /*
     * Force every configured class buff to show immediately, ignoring timers.
     * Backs the !bot buff <id> GM test command. Returns the number of
     * buffs broadcast (0 means this bot's job has no configured buffs).
     */
    public static int forceBuff(Character bot) {
        return castDueBuffs(bot, true);
    }

    /* Release a despawned bot's recast timers so the map doesn't grow unbounded. */
    public static void clearBot(int botId) {
        nextCastByBot.remove(botId);
    }

    /*
     * Show one specific buff skill on the bot now, bypassing the per-job config.
     * Backs !bot castbuff <id> <skillId> - fire e.g. 4101004
     * (Assassin Haste) on any bot to confirm the visual pipe independent of the
     * job lookup. Returns true if the visual was broadcast.
     */
    public static boolean castSkill(Character bot, int skillId) {
        if (bot == null || bot.getMap() == null) return false;
        BotBuffEffects.showBuff(bot, skillId);
        return true;
    }

    private static int castDueBuffs(Character bot, boolean force) {
        if (bot == null) return 0;

        List<Integer> buffIds = BotBuffConfig.buffsForJob(bot.getJob());
        if (buffIds.isEmpty()) return 0;

        long now = System.currentTimeMillis();
        Map<Integer, Long> timers = nextCastByBot.computeIfAbsent(bot.getId(), k -> new ConcurrentHashMap<>());

        int cast = 0;
        for (int skillId : buffIds) {
            if (!force && now < timers.getOrDefault(skillId, 0L)) {
                continue; // not due yet
            }
            final int sid = skillId;
            // Stagger: each due buff fires ~STAGGER_MS after the previous, in order,
            // off-thread (never blocks the caller). The recast timer is set now so the
            // loop won't re-trigger before the scheduled cast runs. castBuff shows the
            // bot's animation and, for party buffs, spreads to nearby party members.
            MethodScheduler.runAfterDelay(() -> BotBuffEffects.castBuff(bot, sid), cast * STAGGER_MS);

            int durationMs = BotBuffEffects.durationOf(sid);
            long recastAt = durationMs > 0 ? now + (long) (durationMs * 0.9) : now + FALLBACK_RECAST_MS;
            timers.put(sid, recastAt);
            cast++;
        }
        return cast;
    }
}
