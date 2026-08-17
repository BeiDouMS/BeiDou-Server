package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotDialogueHandler;
import soloMapling.ArtificialPlayer.BotLogic;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotMovementSystem.PlayerReaction;
import soloMapling.server.ExecutorServiceManager;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

// Our own player-reaction layer for bots roaming on GCMovement: while a bot shares a map with a real
// player, it occasionally reacts to a nearby one - keep walking, emote/chat on the move, or stop, turn,
// and emote/chat. Reuses PlayerReaction's roll, but the STOP is non-blocking here (the old
// executeStopReaction sleeps for seconds and would stall the shared driver thread): it sets a short pause
// the driver honours by idling, faces the player, and fires the line async. Observed maps only.
//
// The spoken line comes from the bot's OWN dialogue (its "PlayerReaction" node), not a hardcoded file,
// so a TrainingBot speaks adventurer lines and any future GCMovement bot speaks its own. A per-player
// cooldown is shared across ALL bots so a single player isn't greeted on a loop by a passing crowd.
final class BotPlayerReaction {
    private BotPlayerReaction() {
    }

    // The dialogue node every GCMovement bot uses to react to a nearby player.
    private static final String REACT_NODE = "PlayerReaction";

    // Detection box around the bot (centred). Matches the original pathFinderAware default (300x200).
    private static final int DETECT_WIDTH = 300;
    private static final int DETECT_HEIGHT = 200;
    // Reaction mix: ignore / stop-and-turn / react-while-walking. Adventurers mostly keep moving.
    private static final int IGNORE_WEIGHT = 55;
    private static final int STOP_WEIGHT = 15;
    private static final int WALK_WEIGHT = 30;
    // Don't scan every AI tick; a relaxed cadence is plenty and keeps it cheap.
    private static final long SCAN_INTERVAL_MS = 1_500;
    // How long a stop-reaction holds the bot in place (face + line play during this).
    private static final long STOP_PAUSE_MIN_MS = 900;
    private static final long STOP_PAUSE_MAX_MS = 2_500;
    // Once a player has been reacted to, no bot reacts to them again for this long (anti-spam, tied to
    // the player so map-hopping or a crowd of bots can't loop greetings at them).
    private static final long REACT_COOLDOWN_MIN_MS = 120_000; // 2 min
    private static final long REACT_COOLDOWN_MAX_MS = 240_000; // 4 min

    // playerId -> earliest time any bot may react to that player again. Shared across all bots.
    private static final Map<Integer, Long> PLAYER_REACT_UNTIL = new ConcurrentHashMap<>();

    // Throttled scan for a nearby real player; roll a reaction for the first one off cooldown.
    static void maybeReact(BotMovementState entry, Character bot) {
        long nowMs = System.currentTimeMillis();
        if (nowMs < entry.nextPlayerScanMs || entry.reactingUntilMs > nowMs) {
            return;
        }
        entry.nextPlayerScanMs = nowMs + SCAN_INTERVAL_MS;

        List<Character> players = BotLogic.getRealPlayersInRange(bot, DETECT_WIDTH, DETECT_HEIGHT);
        for (Character player : players) {
            if (player == null || onCooldown(player.getId(), nowMs)) {
                continue;
            }
            switch (PlayerReaction.rollReaction(IGNORE_WEIGHT, STOP_WEIGHT, WALK_WEIGHT)) {
                case IGNORE -> {
                    // no reaction, no cooldown: a lingering player can still earn one on a later scan
                }
                case WALK_REACT -> {
                    speak(bot, player);                 // line on the move, no movement change
                    markReacted(player.getId(), nowMs);
                    return;
                }
                case STOP_REACT -> {
                    reactStop(entry, bot, player);
                    markReacted(player.getId(), nowMs);
                    return;
                }
            }
        }
    }

    // Non-blocking stop-and-turn: face the player, broadcast the turn, hold the bot for a short beat (the
    // driver idles it while reactingUntilMs is in the future), and fire the line asynchronously.
    private static void reactStop(BotMovementState entry, Character bot, Character player) {
        Point pp = player.getPosition();
        Point bp = bot.getPosition();
        if (pp != null && bp != null) {
            entry.facingDir = (pp.x >= bp.x) ? 1 : -1;
            BotMovementManager.broadcastMovement(entry);
        }
        entry.reactingUntilMs = System.currentTimeMillis()
                + ThreadLocalRandom.current().nextLong(STOP_PAUSE_MIN_MS, STOP_PAUSE_MAX_MS + 1);
        speak(bot, player);
    }

    // Speak a reaction line from the bot's own dialogue (context-resolved, 80/20 plain-vs-token), off
    // the driver thread so the line's hold never stalls it. Bots without an FSM fall back to the old
    // generic emote/chat so the layer still works for any non-TrainingBot GCMovement mover.
    private static void speak(Character bot, Character player) {
        BotSM botSM = CharacterStorage.getBotById(bot.getId());
        if (botSM == null) {
            PlayerReaction.executeWalkReaction(bot);
            return;
        }
        ExecutorServiceManager.runAsync(() -> botSM.getDialogueHandler()
                .executeBotContextDialogue(REACT_NODE, botSM, player, BotDialogueHandler.CONTEXT_LINE_CHANCE));
    }

    private static boolean onCooldown(int playerId, long nowMs) {
        Long until = PLAYER_REACT_UNTIL.get(playerId);
        if (until == null) {
            return false;
        }
        if (until <= nowMs) {
            PLAYER_REACT_UNTIL.remove(playerId); // expired, drop it so the map stays small
            return false;
        }
        return true;
    }

    private static void markReacted(int playerId, long nowMs) {
        PLAYER_REACT_UNTIL.put(playerId,
                nowMs + ThreadLocalRandom.current().nextLong(REACT_COOLDOWN_MIN_MS, REACT_COOLDOWN_MAX_MS + 1));
    }
}
