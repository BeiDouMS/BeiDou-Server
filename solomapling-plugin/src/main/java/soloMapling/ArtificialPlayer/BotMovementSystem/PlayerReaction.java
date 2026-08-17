package soloMapling.ArtificialPlayer.BotMovementSystem;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotDialogueHandler;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.server.ExecutorServiceManager;

import java.util.List;
import java.util.Random;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotFullChat;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botFaceTowardsPoint;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.injectArtificialStopPacket;

/**
 * Handles a bot's reaction when it detects a real player nearby during aware-pathfinding.
 * Three possible outcomes: ignore, stop and react, or react while walking.
 */
public class PlayerReaction {

    private static final Random random = new Random();
    private static final String DIALOGUE_PATH = "HenesysBotDialogue.yaml";
    private static final String BOT_TYPE = "HenesysBot";
    private static final String DIALOGUE_NODE = "PlayerReaction";
    private static List<Integer> ambientEmotes;

    private static void loadDialogue() {
        if (ambientEmotes != null) return;
        BotDialogueHandler.DialogueConstructor dialog =
                BotDialogueHandler.getDialogueCon(DIALOGUE_PATH, BOT_TYPE, DIALOGUE_NODE);
        if (dialog != null) {
            ambientEmotes = dialog.getEmotes();
        }
    }

    public enum ReactionType {
        IGNORE,
        STOP_REACT,
        WALK_REACT
    }

    /**
     * Rolls a three-way weighted random to decide how the bot reacts.
     *
     * @param ignoreWeight   weight for doing nothing (e.g. 40)
     * @param stopWeight     weight for stopping and reacting to the player (e.g. 30)
     * @param walkWeight     weight for reacting while continuing to walk (e.g. 30)
     * @return the chosen ReactionType
     */
    public static ReactionType rollReaction(int ignoreWeight, int stopWeight, int walkWeight) {
        int total = ignoreWeight + stopWeight + walkWeight;
        int roll = random.nextInt(total);

        if (roll < ignoreWeight) {
            return ReactionType.IGNORE;
        } else if (roll < ignoreWeight + stopWeight) {
            return ReactionType.STOP_REACT;
        } else {
            return ReactionType.WALK_REACT;
        }
    }

    /**
     * Full stop reaction: interrupts the bot's movement, faces the player,
     * performs an emote or chat, pauses briefly, then returns so the caller
     * can resume pathfinding.
     *
     * Deliberate synchronous choreography: runs inside the movement replay's
     * MidMovementCheck callback on the replay thread — the replay is paused
     * exactly as long as this blocks, which is the intended visual.
     *
     * @return the snapshot of the player the bot reacted to
     */
    public static PlayerSnapshot executeStopReaction(Character bot, Character player) {
        PlayerSnapshot snapshot = new PlayerSnapshot(player);

        // Visually stop the bot — don't use interruptBotMovement() here because
        // this runs inside the MidMovementCheck callback on the same thread as
        // the packet loop. Setting the interrupt flag would poison the next
        // BotMoveStreamHelper call (e.g. botFaceTowardsPoint). The callback
        // returning true already stops the packet loop for us.
        injectArtificialStopPacket(bot);
        BotHelpers.blockingSleep(700);

        // Face towards the player
        botFaceTowardsPoint(bot, player.getPosition());
        BotHelpers.blockingSleep(400);

        // Pick a reaction: emote, chat, or both. Chat is token-resolved against the bot and the player
        // so {PLAYER_*}/{MAP}/... never reach chat raw; if nothing resolves we emote instead.
        String line = getResolvedReactionLine(bot, player);
        int pick = random.nextInt(3);
        if (pick == 0) {
            BotEmote(bot, getRandomAmbientEmote());
        } else if (pick == 1) {
            if (line != null) {
                BotFullChat(bot, line);
            } else {
                BotEmote(bot, getRandomAmbientEmote());
            }
        } else {
            BotEmote(bot, getRandomAmbientEmote());
            BotHelpers.blockingSleep(500);
            if (line != null) {
                BotFullChat(bot, line);
            }
        }

        // Brief pause before resuming movement (500ms - 2s)
        int pauseMs = 500 + random.nextInt(2000);
        BotHelpers.blockingSleep(pauseMs);

        return snapshot;
    }

    /**
     * Walk reaction: fires an emote or ambient chat line asynchronously
     * without interrupting movement. The bot is NOT reacting to the player
     * specifically — just doing something flavorful while walking.
     */
    public static void executeWalkReaction(Character bot) {
        ExecutorServiceManager.getExecutorService().execute(() -> {
            int pick = random.nextInt(2);
            if (pick == 0) {
                BotEmote(bot, getRandomAmbientEmote());
                return;
            }
            // No specific player here, so {PLAYER_*} lines drop and we fall back to a token-free line.
            String line = getResolvedReactionLine(bot, null);
            if (line != null) {
                BotFullChat(bot, line);
            } else {
                BotEmote(bot, getRandomAmbientEmote());
            }
        });
    }

    // Resolve a PlayerReaction line's {TOKEN}s against the bot (self) and player (nullable). Re-rolls
    // unresolvable lines and falls back to a token-free one, so a raw {TOKEN} never reaches chat;
    // returns null when nothing speakable remains (caller emotes instead).
    private static String getResolvedReactionLine(Character bot, Character player) {
        return BotDialogueHandler.getRandomResolvedLine(DIALOGUE_PATH, BOT_TYPE, DIALOGUE_NODE, bot, player);
    }

    private static int getRandomAmbientEmote() {
        loadDialogue();
        if (ambientEmotes == null || ambientEmotes.isEmpty()) return 2;
        return ambientEmotes.get(random.nextInt(ambientEmotes.size()));
    }
}
