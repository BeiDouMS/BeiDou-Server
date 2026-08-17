package soloMapling.ArtificialPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.gms.client.Character;
import com.esotericsoftware.yamlbeans.YamlReader;
import soloMapling.ArtificialPlayer.BotTypes.DiceBot;

import java.util.Map;
import java.io.FileReader;
import java.io.IOException;

import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotDialogue;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.BotEmote;
import static soloMapling.server.SoloMaplingUtilities.random;

public class BotDialogueHandler {

    private Character chr;

    public static class DialogueConstructor {
        private List<String> dialogue;
        private final List<Integer> emotes;             // node-level emote palette (random pick)
        private final List<List<Integer>> lineEmotes;   // per-line override, aligned to dialogue; entry null = use palette
        private final long duration;

        public DialogueConstructor(List<String> dialogue, List<Integer> emotes, long duration) {
            this(dialogue, emotes, null, duration);
        }

        public DialogueConstructor(List<String> dialogue, List<Integer> emotes, List<List<Integer>> lineEmotes, long duration) {
            this.dialogue = dialogue;
            this.emotes = emotes;
            this.lineEmotes = lineEmotes;
            this.duration = duration;
        }

        private void setDialogue(List<String> newDialogue) {
            this.dialogue = newDialogue;
        }

        public List<String> getDialogue() {
            return this.dialogue;
        }

        public String getDialogue(int i) {
            return getDialogue().get(i);
        }

        // Random emote from the node-level palette. Independent of which line was chosen.
        public Integer getEmote() {
            if (emotes == null || emotes.isEmpty()) {
                return 0;
            }
            return emotes.get(random.nextInt(emotes.size()));
        }

        // Emote tied to a specific line: its tagged override (random pick if a list),
        // falling back to the node-level palette when the line has no override.
        public Integer getEmoteForIndex(int i) {
            if (lineEmotes != null && i >= 0 && i < lineEmotes.size()) {
                List<Integer> override = lineEmotes.get(i);
                if (override != null && !override.isEmpty()) {
                    return override.get(random.nextInt(override.size()));
                }
            }
            return getEmote();
        }

        public List<Integer> getEmotes() {
            return emotes;
        }

        private long getDuration() {
            return this.duration;
        }
    }

    public BotDialogueHandler(Character chr) {
        this.chr = chr;
    }

    public void executeBotFlavorDialogue(String DialogueNodeName, BotSM botSM) {
        runBotFlavorDialogue(botSM.getChr(), getDialogueCon(botSM.dialoguePath, botSM.botType, DialogueNodeName));
    }

    // Like flavor dialogue, but lines may contain {TOKEN} placeholders resolved from the bot's live
    // game-state (see DialogueContextResolver). A line whose tokens can't resolve is skipped, falling
    // back to a plain (token-free) line, so a raw token never reaches chat.
    public void executeBotContextDialogue(String DialogueNodeName, BotSM botSM) {
        runBotContextFlavorDialogue(botSM.getChr(), getDialogueCon(botSM.dialoguePath, botSM.botType, DialogueNodeName));
    }

    // As above, but lines may also reference a player via {PLAYER_*} tokens (level, class, fame,
    // gear, pet, guild, ...). Pass the player the bot is reacting to.
    public void executeBotContextDialogue(String DialogueNodeName, BotSM botSM, Character player) {
        runBotContextFlavorDialogue(botSM.getChr(), getDialogueCon(botSM.dialoguePath, botSM.botType, DialogueNodeName), player);
    }

    // As above, but biases line selection: context ({TOKEN}-carrying) lines are only drawn
    // contextChance of the time, plain lines the rest. Keeps the flavor without leaning on the
    // {PLAYER_*}/{MAP}/... templated lines for the bulk of chatter.
    public void executeBotContextDialogue(String DialogueNodeName, BotSM botSM, Character player, double contextChance) {
        runBotContextFlavorDialogue(botSM.getChr(), getDialogueCon(botSM.dialoguePath, botSM.botType, DialogueNodeName), player, contextChance);
    }

    public void executeBotDialogueWithReplacementStrings(String DialogueNodeName, Map<String, String> replacements, BotSM botSM) {
        runBotDialogue(botSM.getChr(), getDialogueConWithReplacedStrings(botSM.dialoguePath, botSM.botType, DialogueNodeName, replacements));
    }

    public void executeBotDialogue(String DialogueNodeName, BotSM botSM) {
        runBotDialogue(botSM.getChr(), getDialogueCon(botSM.dialoguePath, botSM.botType, DialogueNodeName));
    }

    public void listOptions(Character player, BotSM botSM) {
        // Check if the instance is of type DiceBot
        if (botSM instanceof DiceBot) {
            System.out.println("Dice bot instance");
            DiceBot diceBot = (DiceBot) botSM; // Downcast to DiceBot
            diceBot.displayCommands(player);   // Now you can call DiceBot methods
        } else {
            System.out.println("Not a DiceBot instance");
            botSM.displayCommands(player);
        }

    }

    public static Map<String, Object> readDialogueYaml(String dialoguePack, String dialogueType, String dialogueNode) {
        String dialoguePackBase = "src/main/java/soloMapling/ArtificialPlayer/BotDialoguePack/";
        String filePath = String.format("%s%s", dialoguePackBase, dialoguePack);

        Map<String, Object> dialogueConstructorNode = null;
        try {
            YamlReader reader = new YamlReader(new FileReader(filePath));

            // Read the root node
            Map<String, Object> root = (Map<String, Object>) reader.read();
            Map<String, Object> BotTypeNode = (Map<String, Object>) root.get(dialogueType);
            dialogueConstructorNode = (Map<String, Object>) BotTypeNode.get(dialogueNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dialogueConstructorNode;
    }

    public static DialogueConstructor getDialogueCon(String BotTypeDialoguePath, String BotType, String DialogueNodeName) {
        Map<String, Object> dialogMap = readDialogueYaml(BotTypeDialoguePath, BotType, DialogueNodeName);
        if (dialogMap == null) {
            return null;
        }
        List<String> textList = new ArrayList<>();
        List<List<Integer>> lineEmotes = new ArrayList<>();
        boolean anyLineEmote = parseTextEntries(dialogMap.get("text"), textList, lineEmotes);
        List<Integer> emotes = parseEmotes(dialogMap.get("emote"));
        long duration = (convertToInt(dialogMap.get("wait")) * 1000); // milliseconds
        return new DialogueConstructor(textList, emotes, anyLineEmote ? lineEmotes : null, duration);
    }

    // A node's "text" entry is either a plain string (uses the node-level emote palette)
    // or a map {line/text, emote} carrying its own targeted emote(s). Fills textOut and
    // emotesOut aligned by index (a null emote entry means "no override, use the palette").
    // Returns true if any entry supplied an override, so plain-string nodes skip the per-line path.
    private static boolean parseTextEntries(Object raw, List<String> textOut, List<List<Integer>> emotesOut) {
        boolean any = false;
        if (raw instanceof List) {
            for (Object entry : (List<?>) raw) {
                if (entry instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) entry;
                    Object lineVal = m.containsKey("line") ? m.get("line") : m.get("text");
                    textOut.add(lineVal == null ? "" : lineVal.toString());
                    if (m.get("emote") != null) {
                        emotesOut.add(parseEmotes(m.get("emote")));
                        any = true;
                    } else {
                        emotesOut.add(null);
                    }
                } else {
                    textOut.add(entry == null ? "" : entry.toString());
                    emotesOut.add(null);
                }
            }
        }
        return any;
    }

    public static DialogueConstructor getDialogueConWithReplacedStrings(String BotTypeDialoguePath, String BotType, String DialogueNodeName, Map<String, String> replacements) {
        DialogueConstructor og = getDialogueCon(BotTypeDialoguePath, BotType, DialogueNodeName);
        og.setDialogue(replaceStrings(og.getDialogue(), replacements));
        return og;
    }

    public static List<String> replaceStrings(List<String> inputList, Map<String, String> replacements) {
        if (inputList == null || replacements == null) {
            throw new IllegalArgumentException("Input list and replacements map cannot be null");
        }

        List<String> resultList = new ArrayList<>();
        for (String str : inputList) {
            String modifiedStr = str;
            // Replace all keys in the map with their corresponding values
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                modifiedStr = modifiedStr.replace(entry.getKey(), entry.getValue());
            }
            resultList.add(modifiedStr);
        }
        return resultList;
    }

    public static String getRandomDialogueLine(BotSM botSM, String DialogueNodeName) {
        DialogueConstructor dialog = getDialogueCon(botSM.dialoguePath, botSM.botType, DialogueNodeName);
        return dialog.getDialogue().get(random.nextInt(dialog.getDialogue().size()));
    }

    // Picks a random line from a node and resolves any {TOKEN}s against the speaking bot (self) and
    // an optional player. Re-rolls lines whose tokens can't resolve (bounded), then falls back to a
    // token-free line — same policy as runBotContextFlavorDialogue — so a raw {TOKEN} never reaches
    // chat. Returns null when the node is missing/empty or nothing resolvable remains (caller stays
    // silent). Use this instead of getRandomDialogueLine for any node that may carry context tokens.
    public static String getRandomResolvedLine(BotSM botSM, String node) {
        return getRandomResolvedLine(botSM, node, null);
    }

    public static String getRandomResolvedLine(BotSM botSM, String node, Character player) {
        return getRandomResolvedLine(botSM.dialoguePath, botSM.botType, node, botSM.getChr(), player);
    }

    public static String getRandomResolvedLine(String dialoguePath, String botType, String node, Character speaker, Character player) {
        DialogueConstructor dialog = getDialogueCon(dialoguePath, botType, node);
        if (dialog == null || dialog.getDialogue().isEmpty()) {
            return null;
        }
        List<String> lines = dialog.getDialogue();
        int n = lines.size();
        int tries = Math.min(CONTEXT_REROLLS, n);
        for (int attempt = 0; attempt < tries; attempt++) {
            String raw = lines.get(random.nextInt(n));
            if (!DialogueContextResolver.hasTokens(raw)) {
                return raw;
            }
            Optional<String> filled = DialogueContextResolver.fill(raw, speaker, player);
            if (filled.isPresent()) {
                return filled.get();
            }
        }
        for (String line : lines) {
            if (!DialogueContextResolver.hasTokens(line)) {
                return line;
            }
        }
        return null;
    }

    public static void runBotDialogue(Character character, DialogueConstructor dialog) {
        if (dialog == null) {
            return;
        }
        runDialogue(character, dialog, dialog.getDialogue(), dialog.getEmote());
    }

    public static void runBotFlavorDialogue(Character character, DialogueConstructor dialog) {
        if (dialog == null || dialog.getDialogue().isEmpty()) {
            return;
        }
        int idx = random.nextInt(dialog.getDialogue().size());
        String randomText = dialog.getDialogue().get(idx);
        runDialogue(character, dialog, Collections.singletonList(randomText), dialog.getEmoteForIndex(idx));
    }

    private static final int CONTEXT_REROLLS = 6;

    public static void runBotContextFlavorDialogue(Character character, DialogueConstructor dialog) {
        runBotContextFlavorDialogue(character, dialog, null);
    }

    // Rolls a line; if it carries {TOKEN}s, resolves them from the bot's context (and the given
    // player, if any). Lines that can't resolve are re-rolled (bounded), then we fall back to a
    // plain token-free line so a raw token never leaks. If nothing resolves and there is no plain
    // line, the bot simply stays silent.
    public static void runBotContextFlavorDialogue(Character character, DialogueConstructor dialog, Character player) {
        if (dialog == null || dialog.getDialogue().isEmpty()) {
            return;
        }
        List<String> lines = dialog.getDialogue();
        int n = lines.size();
        int tries = Math.min(CONTEXT_REROLLS, n);
        for (int attempt = 0; attempt < tries; attempt++) {
            int idx = random.nextInt(n);
            String raw = lines.get(idx);
            if (!DialogueContextResolver.hasTokens(raw)) {
                runDialogue(character, dialog, Collections.singletonList(raw), dialog.getEmoteForIndex(idx));
                return;
            }
            Optional<String> filled = DialogueContextResolver.fill(raw, character, player);
            if (filled.isPresent()) {
                runDialogue(character, dialog, Collections.singletonList(filled.get()), dialog.getEmoteForIndex(idx));
                return;
            }
        }
        for (int i = 0; i < n; i++) {
            if (!DialogueContextResolver.hasTokens(lines.get(i))) {
                runDialogue(character, dialog, Collections.singletonList(lines.get(i)), dialog.getEmoteForIndex(i));
                return;
            }
        }
    }

    // Default share of chatter drawn from context ({TOKEN}) lines vs plain lines (the rest).
    public static final double CONTEXT_LINE_CHANCE = 0.20;

    // Weighted variant of runBotContextFlavorDialogue: picks from the context ({TOKEN}-carrying)
    // lines only contextChance of the time and plain lines otherwise, then falls back to the other
    // pool if the preferred one yields nothing speakable. Same token-resolution + no-leak policy.
    public static void runBotContextFlavorDialogue(Character character, DialogueConstructor dialog, Character player, double contextChance) {
        if (dialog == null || dialog.getDialogue().isEmpty()) {
            return;
        }
        List<String> lines = dialog.getDialogue();
        List<Integer> contextLines = new ArrayList<>();
        List<Integer> plainLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            (DialogueContextResolver.hasTokens(lines.get(i)) ? contextLines : plainLines).add(i);
        }
        boolean preferContext = random.nextDouble() < contextChance;
        List<Integer> first = preferContext ? contextLines : plainLines;
        List<Integer> second = preferContext ? plainLines : contextLines;
        if (emitOneFrom(character, dialog, lines, first, player)) {
            return;
        }
        emitOneFrom(character, dialog, lines, second, player);
    }

    // Tries (bounded re-rolls) to speak one line from the given index pool, resolving any tokens.
    // Returns true once a line is spoken, false if the pool is empty or nothing resolved.
    private static boolean emitOneFrom(Character character, DialogueConstructor dialog,
                                       List<String> lines, List<Integer> pool, Character player) {
        if (pool.isEmpty()) {
            return false;
        }
        int tries = Math.min(CONTEXT_REROLLS, pool.size());
        for (int attempt = 0; attempt < tries; attempt++) {
            int idx = pool.get(random.nextInt(pool.size()));
            String raw = lines.get(idx);
            if (!DialogueContextResolver.hasTokens(raw)) {
                runDialogue(character, dialog, Collections.singletonList(raw), dialog.getEmoteForIndex(idx));
                return true;
            }
            Optional<String> filled = DialogueContextResolver.fill(raw, character, player);
            if (filled.isPresent()) {
                runDialogue(character, dialog, Collections.singletonList(filled.get()), dialog.getEmoteForIndex(idx));
                return true;
            }
        }
        return false;
    }

    // Deliberate synchronous choreography (the canonical case): blocks for the
    // YAML-configured duration so executeBotDialogue* callers stay sequential.
    private static void runDialogue(Character character, DialogueConstructor dialog, List<String> textToShow, int emote) {
        if (dialog == null) {
            return;
        }
        BotDialogue(character, textToShow);
        BotEmote(character, emote);
        BotHelpers.blockingSleep(dialog.getDuration());
    }

    private static List<Integer> parseEmotes(Object obj) {
        if (obj instanceof List) {
            List<?> rawList = (List<?>) obj;
            List<Integer> emotes = new ArrayList<>();
            for (Object item : rawList) {
                emotes.add(convertToInt(item));
            }
            return emotes;
        }
        return Collections.singletonList(convertToInt(obj));
    }

    private static Integer convertToInt(Object obj) {
        if (obj instanceof Integer) {
            return (Integer) obj;  // Directly cast if it's already an Integer
        } else if (obj instanceof Long) {
            return ((Long) obj).intValue();  // Convert Long to int
        } else if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);  // Convert String to int
            } catch (NumberFormatException e) {
                System.err.println("Error converting String to int: " + obj);
            }
        }
        return 0;  // Default value if unable to convert
    }
    
}
