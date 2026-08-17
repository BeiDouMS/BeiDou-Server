package soloMapling.Casino;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Patches server-side WZ XML files at startup to override item properties
 * (price, tradeBlock, etc.) without permanently modifying original WZ data.
 *
 * The patcher reads the XML, applies changes in-memory, and writes back.
 * It is idempotent: running it multiple times produces the same result.
 *
 * WHAT THIS DOES (server-side):
 *   - Changes the item's "info/price" value so that Shop.sell() returns the correct meso amount.
 *   - Removes "tradeBlock" / "quest" / "only" flags so the server treats the item as tradeable.
 *   - These changes affect ItemInformationProvider caches since they read from these XML files.
 *
 * WHAT THIS DOES NOT DO (client-side):
 *   - The client has its own copy of WZ data. The sell-price displayed in the shop UI
 *     is rendered client-side from the client's WZ, which this patcher does NOT touch.
 *   - So the client may still display the original low sell value in the shop inventory panel.
 *   - To fix client display, the client's .wz binary files would need a separate patch
 *     (e.g. via a MapleLib/C# tool or by distributing a patched Item.wz to players).
 *   - Similarly, client-side "untradeable" visual indicators won't change without a client patch.
 */
public class WzXmlPatcher {

    private static final Logger log = LoggerFactory.getLogger(WzXmlPatcher.class);
    private static final String WZ_BASE_PATH = "wz";

    /**
     * Apply all casino chip patches. Call this once during server startup
     * BEFORE ItemInformationProvider caches item data.
     */
    public static void applyAllPatches() {
        log.info("[WzXmlPatcher] Applying casino chip patches...");
        patchCasinoChipItems();
        log.info("[WzXmlPatcher] All patches applied.");
    }

    private static void patchCasinoChipItems() {
        // Casino chip items 4002000-4002003 live in Item.wz/Etc/0400.img.xml
        Path xmlPath = Paths.get(WZ_BASE_PATH, "Item.wz", "Etc", "0400.img.xml");
        if (!Files.exists(xmlPath)) {
            log.warn("[WzXmlPatcher] File not found: {}. Skipping casino chip patches.", xmlPath);
            return;
        }

        try {
            String content = Files.readString(xmlPath, StandardCharsets.UTF_8);
            String original = content;

            for (Map.Entry<Integer, Integer> chip : CasinoChipConfig.getChips().entrySet()) {
                int itemId = chip.getKey();
                int price = chip.getValue();
                String paddedId = String.format("0%d", itemId); // e.g. "04002000"

                content = patchItemPrice(content, paddedId, price);
                content = removeItemFlag(content, paddedId, "tradeBlock");
                content = removeItemFlag(content, paddedId, "quest");
                content = removeItemFlag(content, paddedId, "only");
            }

            if (!content.equals(original)) {
                Files.writeString(xmlPath, content, StandardCharsets.UTF_8);
                log.info("[WzXmlPatcher] Patched: {}", xmlPath);
            } else {
                log.info("[WzXmlPatcher] No changes needed for: {}", xmlPath);
            }

        } catch (IOException e) {
            log.error("[WzXmlPatcher] Failed to patch {}", xmlPath, e);
        }
    }

    /**
     * Sets the price value for an item inside its info block.
     * If a price line exists, its value is replaced. If not, one is inserted after iconRaw.
     */
    private static String patchItemPrice(String content, String paddedItemId, int newPrice) {
        // Find the item's imgdir block
        String itemStart = "<imgdir name=\"" + paddedItemId + "\">";
        int startIdx = content.indexOf(itemStart);
        if (startIdx == -1) {
            log.warn("[WzXmlPatcher] Item {} not found in XML.", paddedItemId);
            return content;
        }

        // Find the closing </imgdir> for this item (it's the second one — inner info + outer item)
        int infoStart = content.indexOf("<imgdir name=\"info\">", startIdx);
        int infoEnd = content.indexOf("</imgdir>", infoStart);
        if (infoStart == -1 || infoEnd == -1) {
            log.warn("[WzXmlPatcher] Could not find info block for item {}.", paddedItemId);
            return content;
        }

        String infoBlock = content.substring(infoStart, infoEnd);

        // Check if price line already exists
        Pattern pricePattern = Pattern.compile("<int name=\"price\" value=\"(\\d+)\"/>");
        Matcher matcher = pricePattern.matcher(infoBlock);

        String newInfoBlock;
        if (matcher.find()) {
            int currentPrice = Integer.parseInt(matcher.group(1));
            if (currentPrice == newPrice) {
                return content; // Already correct
            }
            newInfoBlock = infoBlock.replaceFirst(
                    "<int name=\"price\" value=\"\\d+\"/>",
                    "<int name=\"price\" value=\"" + newPrice + "\"/>"
            );
        } else {
            // No price line exists, insert one before </imgdir>
            newInfoBlock = infoBlock + "      <int name=\"price\" value=\"" + newPrice + "\"/>\n";
        }

        log.info("[WzXmlPatcher] Item {}: price set to {}", paddedItemId, newPrice);
        return content.substring(0, infoStart) + newInfoBlock + content.substring(infoEnd);
    }

    /**
     * Removes a flag line (e.g. tradeBlock, quest, only) from an item's info block if present.
     */
    private static String removeItemFlag(String content, String paddedItemId, String flagName) {
        String itemStart = "<imgdir name=\"" + paddedItemId + "\">";
        int startIdx = content.indexOf(itemStart);
        if (startIdx == -1) {
            return content;
        }

        // Scope the search to this item's block only
        int itemEnd = content.indexOf("</imgdir>", content.indexOf("</imgdir>", startIdx) + 1);
        if (itemEnd == -1) {
            itemEnd = content.length();
        }

        String itemBlock = content.substring(startIdx, itemEnd);
        String flagLine = "<int name=\"" + flagName + "\" value=\"1\"/>";

        if (itemBlock.contains(flagLine)) {
            // Remove the entire line including surrounding whitespace
            String patternStr = "\\s*<int name=\"" + flagName + "\" value=\"1\"/>";
            String newItemBlock = itemBlock.replaceFirst(patternStr, "");
            log.info("[WzXmlPatcher] Item {}: removed {} flag", paddedItemId, flagName);
            return content.substring(0, startIdx) + newItemBlock + content.substring(itemEnd);
        }

        return content;
    }
}
