package soloMapling.FreeMarket.ShopOfferSystem;

import org.gms.server.ItemInformationProvider;
import org.gms.server.maps.PlayerShopItem;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfferParser {

    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+\\.?\\d*)\\s*(k|m|b)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDINAL_PATTERN = Pattern.compile("(1st|2nd|3rd|([4-9]|\\d{2,})th)", Pattern.CASE_INSENSITIVE);

    public static class ParsedOffer {
        private final String itemName;
        private final int itemIndex;
        private final long offerPrice;
        private final PlayerShopItem shopItem;

        public ParsedOffer(String itemName, int itemIndex, long offerPrice, PlayerShopItem shopItem) {
            this.itemName = itemName;
            this.itemIndex = itemIndex;
            this.offerPrice = offerPrice;
            this.shopItem = shopItem;
        }

        public String getItemName() { return itemName; }
        public int getItemIndex() { return itemIndex; }
        public long getOfferPrice() { return offerPrice; }
        public PlayerShopItem getShopItem() { return shopItem; }
    }

    public static ParsedOffer parse(String message, List<PlayerShopItem> shopItems) {
        long price = extractPrice(message);
        if (price <= 0) return null;

        String lower = message.toLowerCase();
        int requestedOrdinal = extractOrdinal(lower);
        ItemInformationProvider ii = ItemInformationProvider.getInstance();

        String matchedName = null;
        List<Integer> matchingIndices = new ArrayList<>();

        for (int i = 0; i < shopItems.size(); i++) {
            PlayerShopItem item = shopItems.get(i);
            if (!item.isExist() || item.getBundles() <= 0) continue;

            String itemName = ii.getName(item.getItem().getItemId());
            if (itemName == null || itemName.isEmpty()) continue;

            if (lower.contains(itemName.toLowerCase())) {
                if (matchedName == null) {
                    matchedName = itemName;
                }
                if (itemName.equalsIgnoreCase(matchedName)) {
                    matchingIndices.add(i);
                }
            }
        }

        if (matchedName == null || matchingIndices.isEmpty()) return null;

        int selectedIdx;
        if (requestedOrdinal > 0 && requestedOrdinal <= matchingIndices.size()) {
            selectedIdx = matchingIndices.get(requestedOrdinal - 1);
        } else if (matchingIndices.size() == 1) {
            selectedIdx = matchingIndices.get(0);
        } else {
            selectedIdx = findClosestPriceMatch(matchingIndices, shopItems, price);
        }

        PlayerShopItem matched = shopItems.get(selectedIdx);
        return new ParsedOffer(matchedName, selectedIdx, price, matched);
    }

    private static int findClosestPriceMatch(List<Integer> indices, List<PlayerShopItem> items, long offerPrice) {
        int bestIdx = indices.get(0);
        long bestDiff = Long.MAX_VALUE;

        for (int idx : indices) {
            long diff = Math.abs(items.get(idx).getPrice() - offerPrice);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIdx = idx;
            }
        }
        return bestIdx;
    }

    static int extractOrdinal(String lower) {
        Matcher m = ORDINAL_PATTERN.matcher(lower);
        if (!m.find()) return -1;

        String match = m.group().toLowerCase();
        if (match.equals("1st")) return 1;
        if (match.equals("2nd")) return 2;
        if (match.equals("3rd")) return 3;
        String digits = match.replaceAll("\\D", "");
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static long extractPrice(String message) {
        Matcher matcher = PRICE_PATTERN.matcher(message);
        long bestPrice = -1;

        while (matcher.find()) {
            try {
                double value = Double.parseDouble(matcher.group(1));
                String suffix = matcher.group(2);
                if (suffix != null) {
                    switch (suffix.toLowerCase()) {
                        case "k": value *= 1_000; break;
                        case "m": value *= 1_000_000; break;
                        case "b": value *= 1_000_000_000; break;
                    }
                }
                long parsed = (long) value;
                if (parsed > bestPrice) {
                    bestPrice = parsed;
                }
            } catch (NumberFormatException e) {
                // skip malformed
            }
        }

        return bestPrice;
    }
}
