package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import org.gms.client.inventory.InventoryType;
import org.gms.net.server.Server;
import soloMapling.server.ExecutorServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.BotLogger.log;
import static soloMapling.server.ExecutorServiceManager.runAsync;

public class BotEquipChecker {

    private static ScheduledFuture<?> task;
    private static final long INTERVAL_MS = 2 * 60 * 1000;

    private static final short SLOT_TOP = -5;
    private static final short SLOT_PANTS = -6;

    public static void start() {
        if (task != null) return;
        task = ExecutorServiceManager.getScheduledExecutorService().scheduleAtFixedRate(
                BotEquipChecker::check, INTERVAL_MS, INTERVAL_MS, TimeUnit.MILLISECONDS
        );
        System.out.println("[BotEquipChecker] Started — interval=" + (INTERVAL_MS / 60000) + "min");
    }

    private static void check() {
        try {
            long startMs = System.currentTimeMillis();
            List<Character> allChars = new ArrayList<>(
                    Server.getInstance().getChannel(0, 1).getPlayerStorage().getAllCharacters()
            );

            int totalBots = 0;
            List<Character> naked = new ArrayList<>();
            for (Character chr : allChars) {
                if (!isBot(chr)) continue;
                // Mapless bots (e.g. the Console bot) can't be dressed - equipping
                // broadcasts a char-look update to the bot's map.
                if (chr.getMap() == null) continue;
                totalBots++;
                if (isNaked(chr)) naked.add(chr);
            }

            long elapsed = System.currentTimeMillis() - startMs;
            System.out.println("[BotEquipChecker] Scan complete: " + naked.size() + "/" + totalBots
                    + " naked (" + elapsed + "ms)");

            if (!naked.isEmpty()) {
                log("[BotEquipChecker] Found " + naked.size() + " naked bots out of " + totalBots + ", fixing...");
                final int[] fixedCount = {0};
                final int[] failedCount = {0};
                for (Character chr : naked) {
                    runAsync(() -> {
                        try {
                            BotDecorateEquips.equipTopBottom(chr);
                            fixedCount[0]++;
//                            System.out.println("[BotEquipChecker] Equipped " + chr.getName()
//                                    + " (job=" + chr.getJob().name() + " lv=" + chr.getLevel()
//                                    + ") [" + fixedCount[0] + "/" + naked.size() + "]");
                        } catch (Exception e) {
                            failedCount[0]++;
                            System.out.println("[BotEquipChecker] FAILED " + chr.getName()
                                    + " (job=" + chr.getJob().name() + " lv=" + chr.getLevel()
                                    + "): " + e.getMessage());
                        }
                    });
                }
            } else {
                System.out.println("[BotEquipChecker] All " + totalBots + " bots are dressed.");
            }
        } catch (Exception e) {
            System.out.println("[BotEquipChecker] Error during check: " + e.getMessage());
            log("[BotEquipChecker] Error: " + e.getMessage());
        }
    }

    private static boolean isNaked(Character chr) {
        boolean hasTop = chr.getInventory(InventoryType.EQUIPPED).getItem(SLOT_TOP) != null;
        boolean hasPants = chr.getInventory(InventoryType.EQUIPPED).getItem(SLOT_PANTS) != null;
        return !hasTop || !hasPants;
    }
}
