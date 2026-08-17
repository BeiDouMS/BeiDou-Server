package soloMapling.ArtificialPlayer.BotBuffRequestSystem;

import org.gms.client.Character;
import org.gms.constants.skills.Assassin;
import org.gms.constants.skills.Bandit;
import org.gms.constants.skills.Bishop;
import org.gms.constants.skills.Bowmaster;
import org.gms.constants.skills.Cleric;
import org.gms.constants.skills.DarkKnight;
import org.gms.constants.skills.FPArchMage;
import org.gms.constants.skills.FPWizard;
import org.gms.constants.skills.Fighter;
import org.gms.constants.skills.Hero;
import org.gms.constants.skills.ILArchMage;
import org.gms.constants.skills.ILWizard;
import org.gms.constants.skills.Marksman;
import org.gms.constants.skills.NightLord;
import org.gms.constants.skills.Paladin;
import org.gms.constants.skills.Priest;
import org.gms.constants.skills.Shadower;
import org.gms.constants.skills.Spearman;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotBuffConfig;
import soloMapling.ArtificialPlayer.BotAttackSystem.BotBuffEffects;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.server.MethodScheduler;

import java.awt.Point;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Chat-triggered bot buffs. When a real player says e.g. "hs pls", this finds the nearest
 * available bot whose job actually has that buff and, after a short delay, makes it react
 * (face the player, emote, speak) and hand over a 10-minute version of the buff - server-
 * side, party or not. The bot is a puppet here: it never reads chat, this handler drives it,
 * so it just looks like the bot reacting to you.
 *
 * Only party/support buffs that make sense to give another player are eligible, and only a
 * job that genuinely has the buff can grant it (Priest/Bishop -> Holy Symbol, 4th-jobbers ->
 * Maple Warrior, ...). A bot that grants one goes on a per-bot cooldown for everything.
 */
public final class BotBuffRequestHandler {

    private BotBuffRequestHandler() {}

    // ---- tunables ----
    private static final int PROXIMITY_X = 200;          // "very close" horizontal reach (px)
    private static final int PROXIMITY_Y = 120;          // vertical reach (px)
    private static final long COOLDOWN_MS = 180_000;     // 3 min per bot, all buffs/players
    private static final long MIN_DELAY_MS = 3_000;      // reaction delay window (realness)
    private static final long MAX_DELAY_MS = 5_000;
    private static final int REACT_EMOTE = 2;            // facial expression played on grant

    // A request must include one of these words, so normal conversation doesn't trigger it.
    private static final Set<String> REQUEST_WORDS = Set.of("please", "pls", "plz");

    // botId -> epoch ms the bot may buff again. Per-bot; covers all buffs and all players.
    private static final Map<Integer, Long> cooldownUntil = new ConcurrentHashMap<>();

    /** A requestable buff and the per-branch skill ids that count as "this buff". */
    private record BuffConcept(String name, int[] skillIds) {}

    // alias (lowercase, may be multi-word) -> concept. Party/support buffs only - never the
    // self-only buffs a kit also contains (Dragon Blood, Shadow Partner, Berserk, ...).
    private static final Map<String, BuffConcept> ALIASES = new LinkedHashMap<>();

    static {
        BuffConcept holySymbol = new BuffConcept("Holy Symbol", new int[]{Priest.HOLY_SYMBOL});
        BuffConcept hyperBody = new BuffConcept("Hyper Body", new int[]{Spearman.HYPER_BODY});
        BuffConcept ironWill = new BuffConcept("Iron Will", new int[]{Spearman.IRON_WILL});
        BuffConcept rage = new BuffConcept("Rage", new int[]{Fighter.RAGE});
        BuffConcept bless = new BuffConcept("Bless", new int[]{Cleric.BLESS});
        BuffConcept haste = new BuffConcept("Haste", new int[]{Assassin.HASTE, Bandit.HASTE});
        BuffConcept meditation = new BuffConcept("Meditation", new int[]{FPWizard.MEDITATION, ILWizard.MEDITATION});
        BuffConcept sharpEyes = new BuffConcept("Sharp Eyes", new int[]{Bowmaster.SHARP_EYES, Marksman.SHARP_EYES});
        BuffConcept mapleWarrior = new BuffConcept("Maple Warrior", new int[]{
                Hero.MAPLE_WARRIOR, Paladin.MAPLE_WARRIOR, DarkKnight.MAPLE_WARRIOR,
                FPArchMage.MAPLE_WARRIOR, ILArchMage.MAPLE_WARRIOR, Bishop.MAPLE_WARRIOR,
                Bowmaster.MAPLE_WARRIOR, Marksman.MAPLE_WARRIOR, NightLord.MAPLE_WARRIOR, Shadower.MAPLE_WARRIOR});

        register(holySymbol, "hs", "holy symbol", "symbol");
        register(hyperBody, "hb", "hyper body", "hyper");
        register(rage, "rage");
        register(bless, "bless");
        register(haste, "haste");
        register(meditation, "medi", "meditation");
        register(sharpEyes, "se", "sharp eyes");
        register(mapleWarrior, "mw", "maple warrior");
    }

    private static void register(BuffConcept concept, String... aliases) {
        for (String alias : aliases) {
            ALIASES.put(alias, concept);
        }
    }

    /**
     * Inspect a real player's chat line; if it's a buff request, schedule a nearby eligible
     * bot to grant it. No-op for anything that isn't a request. Cheap and safe to call on
     * every general-chat message.
     */
    public static void tryHandle(Character player, String message) {
        if (player == null || BotHelpers.isBot(player)) {
            return; // real players only - bot chat must never feed this
        }
        if (message == null || message.isBlank()) {
            return;
        }
        String[] tokens = message.trim().toLowerCase().split("\\s+");
        if (!hasRequestWord(tokens)) {
            return;
        }
        BuffConcept concept = matchConcept(tokens);
        if (concept == null) {
            return;
        }

        // Snapshot where the player asked from - the bot search is anchored here.
        MapleMap map = player.getMap();
        Point pos = player.getPosition();
        if (map == null || pos == null) {
            return;
        }

        Character bot = findNearestEligibleBot(map, pos, concept);
        if (bot == null) {
            return; // nobody close enough can help right now - stay silent (organic)
        }

        long delay = ThreadLocalRandom.current().nextLong(MIN_DELAY_MS, MAX_DELAY_MS + 1);
        MethodScheduler.runAfterDelay(() -> grant(bot, player, concept), delay);
    }

    private static boolean hasRequestWord(String[] tokens) {
        for (String token : tokens) {
            if (REQUEST_WORDS.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /** First concept whose alias appears as a contiguous run of whole tokens (no substrings). */
    private static BuffConcept matchConcept(String[] tokens) {
        for (Map.Entry<String, BuffConcept> entry : ALIASES.entrySet()) {
            if (containsPhrase(tokens, entry.getKey().split("\\s+"))) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static boolean containsPhrase(String[] tokens, String[] phrase) {
        if (phrase.length == 0 || tokens.length < phrase.length) {
            return false;
        }
        for (int i = 0; i <= tokens.length - phrase.length; i++) {
            boolean match = true;
            for (int j = 0; j < phrase.length; j++) {
                if (!tokens[i + j].equals(phrase[j])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        return false;
    }

    private static Character findNearestEligibleBot(MapleMap map, Point pos, BuffConcept concept) {
        long now = System.currentTimeMillis();
        Character best = null;
        double bestSq = Double.MAX_VALUE;

        // Scan everyone on the asker's map, not the active-bot registry: inert "!bot spawn"
        // bots live on the map but were never registered (no bot type assigned), so they'd
        // otherwise be invisible here. isBot is id-based, so it catches registered + inert alike.
        for (Character chr : map.getCharacters()) {
            if (chr == null || !BotHelpers.isBot(chr)) {
                continue;
            }
            // Only registered bots have a state; an inert spawn is never "busy".
            BotSM bot = CharacterStorage.getBotById(chr.getId());
            if (bot != null && bot.getState() == BotSM.BotState.TRADING) {
                continue; // trading is the only "busy" - attacking / OPQ / idle are all fine
            }
            Long until = cooldownUntil.get(chr.getId());
            if (until != null && now < until) {
                continue; // recently buffed someone
            }
            if (resolveSkill(concept, chr) == 0) {
                continue; // job doesn't actually have this buff
            }
            Point bp = chr.getPosition();
            if (bp == null || Math.abs(bp.x - pos.x) > PROXIMITY_X || Math.abs(bp.y - pos.y) > PROXIMITY_Y) {
                continue;
            }
            double dsq = pos.distanceSq(bp);
            if (dsq < bestSq) {
                bestSq = dsq;
                best = chr;
            }
        }
        return best;
    }

    /** The actual skill id this bot would cast for the concept, or 0 if its job lacks it. */
    private static int resolveSkill(BuffConcept concept, Character chr) {
        List<Integer> kit = BotBuffConfig.buffsForJob(chr.getJob());
        for (int id : concept.skillIds()) {
            if (kit.contains(id)) {
                return id;
            }
        }
        return 0;
    }

    private static void grant(Character chr, Character player, BuffConcept concept) {
        if (chr == null || chr.getMap() == null || player == null || player.getMap() == null) {
            return;
        }
        int skillId = resolveSkill(concept, chr);
        if (skillId == 0) {
            return;
        }

        // Claim the cooldown atomically so two near-simultaneous requests can't double-grant.
        long now = System.currentTimeMillis();
        synchronized (cooldownUntil) {
            Long until = cooldownUntil.get(chr.getId());
            if (until != null && now < until) {
                return;
            }
            cooldownUntil.put(chr.getId(), now + COOLDOWN_MS);
        }

        // Organic reaction: face the asker (if still co-located), emote, say the buff, grant it.
        if (chr.getMap().getId() == player.getMap().getId() && player.getPosition() != null) {
            MovementCommands.botFaceTowardsPoint(chr, player.getPosition());
        }
        SocialCommands.BotEmote(chr, REACT_EMOTE);
        SocialCommands.BotChatbubble(chr, reactLine(concept, player));
        BotBuffEffects.giveExtendedBuff(chr, skillId, List.of(player), BotBuffEffects.EXTENDED_DURATION_MS);
    }

    private static String reactLine(BuffConcept concept, Character player) {
        String[] lines = {
                concept.name() + " for you!",
                "Here's your " + concept.name() + "~",
                concept.name() + "! Enjoy, " + player.getName() + "!"
        };
        return lines[ThreadLocalRandom.current().nextInt(lines.length)];
    }

    /** Drop a despawned bot's cooldown entry so the map doesn't grow unbounded. */
    public static void clearBot(int botId) {
        cooldownUntil.remove(botId);
    }
}
