package soloMapling.ArtificialPlayer;

import org.gms.client.Character;
import org.gms.net.server.Server;
import org.gms.server.maps.MapleMap;
import soloMapling.ArtificialPlayer.BotMessagingSystem.CharacterStorage;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands;
import soloMapling.ArtificialPlayer.BotTownSystem.TownPresenceConfig;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.server.ExecutorServiceManager;
import soloMapling.server.MethodScheduler;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotCommandsPack.MegaphoneCommands.BotAvatarMegaphone;
import static soloMapling.ArtificialPlayer.BotCommandsPack.MegaphoneCommands.BotSuperMegaphone;
import static soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands.*;
import static soloMapling.ArtificialPlayer.BotCustomization.getRandomChairId;
import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementCommands.*;
import static soloMapling.BotLogger.log;

public class SocialHotPotatoManager {

    private static SocialHotPotatoManager instance;
    private ScheduledFuture<?> scheduledTask;
    private ScheduledFuture<?> megaScheduledTask;
    private final Random random = new Random();
    private boolean running = false;

    private static final int MIN_INTERVAL_MS = 8_000;
    private static final int MAX_INTERVAL_MS = 20_000;

    private static final int MEGA_MIN_INTERVAL_MS = 30_000;
    private static final int MEGA_MAX_INTERVAL_MS = 90_000;

    private static final int[] HENESYS_MAP_IDS = {
            100000000,  // Henesys
            100000100,  // Henesys Market
            100000200,  // Henesys Park
            100000102   // Henesys Potion Shop
    };

    // Bark map scope: the four Henesys maps plus every town map in TownPresence.yaml. Built once
    // (start / refreshMapScope), rebuilt live on !env townpresence reload. The megaphone ticker is
    // deliberately NOT scoped per town - megaphones are server-wide, so it stays a single ticker drawing
    // from this same pool (never fanned out) to keep the global megaphone rate unchanged.
    private volatile Set<Integer> ambientMapIds = buildAmbientMapIds();

    private static final String SOCIAL_DIALOGUE_PATH = "SocialHotPotatoDialogue.yaml";
    private static final String SOCIAL_BOT_TYPE = "SocialHotPotato";
    private static final String MEGA_DIALOGUE_PATH = "MegaphoneDialogue.yaml";
    private static final String MEGA_BOT_TYPE = "MegaphoneBroadcast";

    private static final String[] SOCIAL_CATEGORIES = {
            "RealLife", "MapleInUniverse", "SocialSim", "AFK",
            "Nostalgia", "RandomOneLiners", "Complaints", "Reactions", "FlexBrag"
    };


    private static final String[] MEGA_CATEGORIES = {
            "BirthdayMessages", "ItemSales", "GuildRecruitment", "PQRecruitment",
            "RWTSpam", "Flex", "RandomAnnouncements", "SocialMessages"
    };

    // Weights for mega category selection (must match MEGA_CATEGORIES order)
    private static final int[] MEGA_WEIGHTS = {
            5,   // BirthdayMessages
            7,   // ItemSales
            7,   // GuildRecruitment
            7,   // PQRecruitment
            19,  // RWTSpam
            19,  // Flex
            18,  // RandomAnnouncements
            18   // SocialMessages
    };
    private static final int MEGA_WEIGHT_TOTAL = 100;

    private SocialHotPotatoManager() {}

    public static synchronized SocialHotPotatoManager getInstance() {
        if (instance == null) {
            instance = new SocialHotPotatoManager();
        }
        return instance;
    }

    public void start() {
        if (running) return;
        running = true;
        refreshMapScope();
        scheduleNextTick();
        scheduleNextMegaTick();
        log("[SocialHotPotato] Started.");
    }

    // Rebuild the bark map scope from config. Called on start and by !env townpresence reload so town
    // curation applies without a restart. Does not touch the (server-wide) megaphone ticker.
    public void refreshMapScope() {
        ambientMapIds = buildAmbientMapIds();
        log("[SocialHotPotato] Bark map scope: " + ambientMapIds.size() + " maps.");
    }

    private static Set<Integer> buildAmbientMapIds() {
        Set<Integer> ids = new LinkedHashSet<>();
        for (int id : HENESYS_MAP_IDS) {
            ids.add(id);
        }
        ids.addAll(TownPresenceConfig.allTownMapIds());
        return ids;
    }

    public void stop() {
        running = false;
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        if (megaScheduledTask != null) {
            megaScheduledTask.cancel(false);
        }
        log("[SocialHotPotato] Stopped.");
    }

    private void scheduleNextTick() {
        if (!running) return;
        int delay = MIN_INTERVAL_MS + random.nextInt(MAX_INTERVAL_MS - MIN_INTERVAL_MS);
        scheduledTask = ExecutorServiceManager.getScheduledExecutorService().schedule(() -> {
            try {
                tick();
            } catch (Exception e) {
                log("[SocialHotPotato] Error during tick: " + e.getMessage());
                e.printStackTrace();
            }
            scheduleNextTick();
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void scheduleNextMegaTick() {
        if (!running) return;
        int delay = MEGA_MIN_INTERVAL_MS + random.nextInt(MEGA_MAX_INTERVAL_MS - MEGA_MIN_INTERVAL_MS);
        megaScheduledTask = ExecutorServiceManager.getScheduledExecutorService().schedule(() -> {
            try {
                megaTick();
            } catch (Exception e) {
                log("[SocialHotPotato] Error during mega tick: " + e.getMessage());
                e.printStackTrace();
            }
            scheduleNextMegaTick();
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void megaTick() {
        // Megaphones are server-wide (heard by every online player regardless of the sender's map), so the
        // pool is unfiltered by observation - draw a sender from any eligible bot in scope.
        Character bot = selectRandomFillerBot(false);
        if (bot == null) return;
        doMegaphone(bot);
    }

    private void tick() {
        // Barks are local chat/emote/chair packets, so gate on observation: only pick from maps a real
        // player is on. Unobserved towns emit no barks (gate packets on observation).
        Character bot = selectRandomFillerBot(true);
        if (bot == null) return;

        if (MovementCommands.nudgeAwayFromOverlap(bot)) return;

        executeRandomAction(bot);
    }

    private Character selectRandomFillerBot(boolean requireObserved) {
        List<Character> fillerBots = new ArrayList<>();

        for (int mapId : ambientMapIds) {
            if (requireObserved && !GCMovement.isMapObserved(mapId)) continue; // skip unwatched maps entirely
            try {
                MapleMap map = Server.getInstance().getChannel(0, 1).getMapFactory().getMap(mapId);
                if (map == null) continue;
                for (Character chr : map.getAllPlayers()) {
                    if (!isBot(chr)) continue;
                    BotSM bot = CharacterStorage.getBotById(chr.getId());
                    if (bot == null || !bot.isAvailableForAmbientActions()) continue;
                    if (ConversationManager.getInstance().isInConversation(chr.getId())) continue;
                    fillerBots.add(chr);
                }
            } catch (Exception e) {
                // Map not loaded yet, skip
            }
        }

        if (fillerBots.isEmpty()) return null;
        return fillerBots.get(random.nextInt(fillerBots.size()));
    }

    private void executeRandomAction(Character bot) {
        int roll = random.nextInt(100);

        if (roll < 45) {
            doChat(bot);
        } else if (roll < 62) {
            doEmote(bot);
        } else if (roll < 77) {
            doPositionChange(bot);
        } else if (roll < 92) {
            doEmoteAndChat(bot);
        } else {
            doChalkboard(bot);
        }
    }

    // --- Action handlers ---

    private void doChat(Character bot) {
        String category = SOCIAL_CATEGORIES[random.nextInt(SOCIAL_CATEGORIES.length)];
        String line = getRandomLine(SOCIAL_DIALOGUE_PATH, SOCIAL_BOT_TYPE, category);
        if (line != null) {
            BotSpeak(bot, line);
        }
    }

    private void doEmote(Character bot) {
        int emoteId = 1 + random.nextInt(22);
        BotEmote(bot, emoteId);
    }

    private void doPositionChange(Character bot) {
        int pick = random.nextInt(4);
        switch (pick) {
            case 0:
                if (bot.getChair() > 0) {
                    botCancelChair(bot);
                } else {
                    botSitChair(bot, getRandomChairId());
                }
                break;
            case 1:
                microTurnAround(bot);
                break;
            case 2:
                BotIdleStandingUpdate(bot);
                break;
            case 3:
                MovementCommands.nudgeSmall(bot);
                break;
        }
    }

    private void doEmoteAndChat(Character bot) {
        int emoteId = 1 + random.nextInt(22);
        BotEmote(bot, emoteId);
        String category = SOCIAL_CATEGORIES[random.nextInt(SOCIAL_CATEGORIES.length)];
        String line = getRandomLine(SOCIAL_DIALOGUE_PATH, SOCIAL_BOT_TYPE, category);
        if (line != null) {
            BotSpeak(bot, line);
        }
    }

    private void doChalkboard(Character bot) {
        String line = getRandomLine(SOCIAL_DIALOGUE_PATH, SOCIAL_BOT_TYPE, "Chalkboard");
        if (line == null) return;
        botSetChalkboard(bot, line);
        MethodScheduler.runAfterDelay(() -> botClearChalkboard(bot), 5 * 60 * 1000);
    }

    private void doMegaphone(Character bot) {
        String category = selectWeightedMegaCategory();
        String line = getRandomLine(MEGA_DIALOGUE_PATH, MEGA_BOT_TYPE, category);
        if (line == null) return;

        if (random.nextInt(100) < 75) {
            BotSuperMegaphone(bot, line);
        } else {
            BotAvatarMegaphone(bot, line);
        }
    }

    private String selectWeightedMegaCategory() {
        int roll = random.nextInt(MEGA_WEIGHT_TOTAL);
        int cumulative = 0;
        for (int i = 0; i < MEGA_CATEGORIES.length; i++) {
            cumulative += MEGA_WEIGHTS[i];
            if (roll < cumulative) {
                return MEGA_CATEGORIES[i];
            }
        }
        return MEGA_CATEGORIES[MEGA_CATEGORIES.length - 1];
    }

    public void testNudge(Character bot) {
        MovementCommands.nudgeSmall(bot);
    }

    public boolean testNudgeOverlap(Character bot) {
        return MovementCommands.nudgeAwayFromOverlap(bot);
    }

    // --- Dialogue loading ---

    private String getRandomLine(String dialoguePath, String botType, String category) {
        try {
            BotDialogueHandler.DialogueConstructor dialog =
                    BotDialogueHandler.getDialogueCon(dialoguePath, botType, category);
            if (dialog == null || dialog.getDialogue().isEmpty()) return null;
            List<String> lines = dialog.getDialogue();
            return lines.get(random.nextInt(lines.size()));
        } catch (Exception e) {
            return null;
        }
    }
}
