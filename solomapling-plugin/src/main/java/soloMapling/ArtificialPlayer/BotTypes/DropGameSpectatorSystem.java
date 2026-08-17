package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.server.ItemInformationProvider;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotDialogueHandler;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.ConversationManager;
import soloMapling.server.MethodScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotSpeak;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.botFaceTowardsPoint;

public class DropGameSpectatorSystem {

    private static final String DIALOGUE_PATH = "DropGameSpectatorDialogue.yaml";
    private static final String BOT_TYPE = "DropGameSpectator";
    private static final Random random = new Random();

    private static final String[] CATEGORIES = {"Excitement", "Envy", "Hype", "Question", "Cheer"};
    private static final int[] CATEGORY_WEIGHTS = {30, 20, 20, 15, 15};
    private static final int WEIGHT_TOTAL = 100;

    private static final int COOLDOWN_MIN_DROPS = 7;
    private static final int COOLDOWN_MAX_DROPS = 12;

    private static int dropCounter = 0;
    private static int nextReactionAt = COOLDOWN_MIN_DROPS + new Random().nextInt(COOLDOWN_MAX_DROPS - COOLDOWN_MIN_DROPS + 1);
    private static int lastReactorId = -1;

    public static void onItemDropped(Character dropGameBot, Character player, int itemId) {
        dropCounter++;
        if (dropCounter < nextReactionAt) return;

        dropCounter = 0;
        nextReactionAt = COOLDOWN_MIN_DROPS + random.nextInt(COOLDOWN_MAX_DROPS - COOLDOWN_MIN_DROPS + 1);

        String itemName = ItemInformationProvider.getInstance().getName(itemId);
        if (itemName == null || itemName.isEmpty()) return;

        MapleMap map = dropGameBot.getMap();
        if (map == null) return;

        List<Character> spectators = findAvailableSpectators(map, dropGameBot);
        if (spectators.isEmpty()) return;

        Collections.shuffle(spectators, random);
        Character bot = pickDifferentBot(spectators);
        if (bot == null) return;

        lastReactorId = bot.getId();
        int delay = 1500 + random.nextInt(3500);

        MethodScheduler.runAfterDelay(() -> {
            botFaceTowardsPoint(bot, player.getPosition());
            reactWithDialogue(bot, itemName);
        }, delay);
    }

    private static Character pickDifferentBot(List<Character> spectators) {
        if (spectators.size() == 1) return spectators.get(0);
        for (Character c : spectators) {
            if (c.getId() != lastReactorId) return c;
        }
        return spectators.get(0);
    }

    private static List<Character> findAvailableSpectators(MapleMap map, Character exclude) {
        List<Character> result = new ArrayList<>();
        for (Character chr : map.getAllPlayers()) {
            if (chr.getId() == exclude.getId()) continue;
            if (!isBot(chr)) continue;
            BotSM bot = CharacterStorage.getBotById(chr.getId());
            if (bot == null || !bot.isAvailableForAmbientActions()) continue;
            if (ConversationManager.getInstance().isInConversation(chr.getId())) continue;
            result.add(chr);
        }
        return result;
    }

    private static void reactWithDialogue(Character bot, String itemName) {
        String category = selectWeightedCategory();

        BotDialogueHandler.DialogueConstructor dialog =
                BotDialogueHandler.getDialogueCon(DIALOGUE_PATH, BOT_TYPE, category);
        if (dialog == null || dialog.getDialogue().isEmpty()) return;

        List<String> lines = dialog.getDialogue();
        String line = lines.get(random.nextInt(lines.size()));
        line = line.replace("{item}", itemName);

        BotSpeak(bot, line);

        List<Integer> emotes = dialog.getEmotes();
        if (emotes != null && !emotes.isEmpty()) {
            int emoteId = emotes.get(0);
            if (emoteId > 0 && random.nextInt(100) < 50) {
                BotEmote(bot, emoteId);
            }
        }
    }

    private static String selectWeightedCategory() {
        int roll = random.nextInt(WEIGHT_TOTAL);
        int cumulative = 0;
        for (int i = 0; i < CATEGORIES.length; i++) {
            cumulative += CATEGORY_WEIGHTS[i];
            if (roll < cumulative) {
                return CATEGORIES[i];
            }
        }
        return CATEGORIES[CATEGORIES.length - 1];
    }
}
