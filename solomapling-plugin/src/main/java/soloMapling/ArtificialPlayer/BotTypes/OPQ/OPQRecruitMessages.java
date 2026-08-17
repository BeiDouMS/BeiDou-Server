package soloMapling.ArtificialPlayer.BotTypes.OPQ;

import org.gms.client.Character;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Randomized OPQ lobby recruit-chat generator.
 *
 * Mirrors the structure of MerchantBot.MerchantBotMessageCreator:
 *   [prefix] [pq name] [optional level/class tag] [optional filler]
 *
 * The goal is visual noise variety, not believability — these lines appear in
 * the Orbis PQ lobby crowd, interleaved with real-player chat.
 */
public final class OPQRecruitMessages {

    private OPQRecruitMessages() {}

    private static final List<String> PREFIXES = new ArrayList<>(Arrays.asList(
            "J>", "Joining>", "J>>", "Join>", "LFP>", "lf>"
    ));

    private static final List<String> PQ_NAMES = new ArrayList<>(Arrays.asList(
            "OPQ", "Orbis PQ", "Orbis Party Quest", "pq", "Orbis", "OPQ please"
    ));

    private static final List<String> FILLERS = new ArrayList<>(Arrays.asList(
            "@@@@@@@@", "!!!", "plz", "asap"
    ));

    public static String generateRecruitMessage(Character chr) {
        Random random = new Random();

        String prefix = PREFIXES.get(random.nextInt(PREFIXES.size()));
        String pqName = PQ_NAMES.get(random.nextInt(PQ_NAMES.size()));

        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(' ');

        // 35% chance to self-tag with level+job ("Lvl 55 Priest J> OPQ" style).
        if (chr != null && random.nextDouble() < 0.35) {
            try {
                sb.insert(0, "Lvl " + chr.getLevel() + " " + chr.getJob().name() + " ");
            } catch (Exception ignored) {
                // Character APIs missing something; skip the self-tag.
            }
        }

        sb.append(pqName);

        // 0–2 filler appends
        int fillerCount = random.nextInt(3);
        for (int i = 0; i < fillerCount; i++) {
            sb.append(' ').append(FILLERS.get(random.nextInt(FILLERS.size())));
        }

        String out = sb.toString().replaceAll("\\[", "").replaceAll("]", "");

        // 15% chance shout-cap the whole line, same as MerchantBotMessageCreator.
        if (Math.random() < 0.15) {
            out = out.toUpperCase();
        }
        return out;
    }
}
