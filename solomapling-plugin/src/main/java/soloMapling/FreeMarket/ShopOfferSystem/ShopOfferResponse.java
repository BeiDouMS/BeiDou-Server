package soloMapling.FreeMarket.ShopOfferSystem;

import org.gms.client.Character;
import org.gms.server.maps.PlayerShop;
import org.gms.server.maps.PlayerShopItem;
import soloMapling.ArtificialPlayer.BotDialogueHandler;
import soloMapling.server.MethodScheduler;
import org.gms.util.PacketCreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static soloMapling.BotLogger.log;

public class ShopOfferResponse {

    private static final String DIALOGUE_PATH = "ShopOfferDialogue.yaml";
    private static final String BOT_TYPE = "ShopOffer";
    private static final Random random = new Random();

    public static void handlePresentOwner(Character player, PlayerShop shop, OfferParser.ParsedOffer offer, HaggleSession session) {
        Character owner = shop.getOwner();
        session.incrementAttempt();

        if (session.hasExceededAttempts()) {
            String msg = getDialogueLine("KickResponse", offer, null);
            shopOwnerChat(shop, owner, msg);
            MethodScheduler.runAfterDelay(() -> shop.banPlayer(player.getName()), 2000);
            ShopOfferSystem.getInstance().removeSession(player.getId());
            return;
        }

        if (session.hasCounterPending()) {
            if (offer.getOfferPrice() >= session.getCounterPrice()) {
                acceptOffer(shop, owner, player, offer, session.getCounterPrice());
                return;
            }
        }

        OfferEvaluator.Decision decision = OfferEvaluator.evaluate(offer.getOfferPrice(), offer.getShopItem().getPrice());

        switch (decision) {
            case ACCEPT:
                acceptOffer(shop, owner, player, offer, offer.getOfferPrice());
                break;
            case COUNTER:
                long counterPrice = OfferEvaluator.calculateCounterPrice(offer.getOfferPrice(), offer.getShopItem().getPrice());
                session.setCounterPrice(counterPrice);
                Map<String, String> counterReplacements = buildReplacements(offer, player);
                counterReplacements.put("{counter_price}", formatPrice(counterPrice));
                String counterMsg = getDialogueLineWithReplacements("CounterResponse", counterReplacements);
                shopOwnerChat(shop, owner, counterMsg);
                break;
            case DECLINE:
                String declineMsg = getDialogueLine("DeclineResponse", offer, player);
                shopOwnerChat(shop, owner, declineMsg);
                break;
        }
    }

    public static void handleAFKOwner(Character player, PlayerShop shop, OfferParser.ParsedOffer offer, ShopOfferSystem system) {
        OfferEvaluator.Decision decision = OfferEvaluator.evaluate(offer.getOfferPrice(), offer.getShopItem().getPrice());

        if (decision == OfferEvaluator.Decision.DECLINE) {
            return;
        }

        long acceptedPrice = offer.getOfferPrice();
        String itemName = offer.getItemName();
        String ownerName = shop.getOwner().getName();
        int ownerId = shop.getOwner().getId();
        int itemIndex = offer.getItemIndex();
        PlayerShopItem shopItem = offer.getShopItem();
        String roomLabel = getFMRoomLabel(shop.getMapId());
        int delay = (5 + random.nextInt(26)) * 60 * 1000;

        system.lockItem(ownerId, itemIndex);

        log("[ShopOfferSystem] AFK accept scheduled: " + itemName + " @ " + acceptedPrice + " for " + player.getName() + " (delay=" + (delay / 60000) + "min)");

        MethodScheduler.runAfterDelay(() -> {
            shopItem.setPrice((int) acceptedPrice);
            shop.broadcast(PacketCreator.getPlayerShopItemUpdate(shop));

            if (player.isLoggedIn()) {
                player.sendPacket(PacketCreator.getWhisperReceive(
                        ownerName, player.getClient().getChannel(), false,
                        "Hey, I updated the price on " + itemName + " to " + formatPrice(acceptedPrice) + ". Come grab it! " + roomLabel
                ));
            }
        }, delay);
    }

    public static void handleHiredMerchantAFK(Character player, String ownerName, int ownerId, int mapId,
                                               PlayerShopItem shopItem, OfferParser.ParsedOffer offer,
                                               ShopOfferSystem system, Runnable broadcastUpdate) {
        OfferEvaluator.Decision decision = OfferEvaluator.evaluate(offer.getOfferPrice(), shopItem.getPrice());

        if (decision == OfferEvaluator.Decision.DECLINE) {
            return;
        }

        long acceptedPrice = offer.getOfferPrice();
        String itemName = offer.getItemName();
        int itemIndex = offer.getItemIndex();
        String roomLabel = getFMRoomLabel(mapId);
        int delay = (5 + random.nextInt(26)) * 60 * 1000;

        system.lockItem(ownerId, itemIndex);

        log("[ShopOfferSystem] HiredMerchant AFK accept scheduled: " + itemName + " @ " + acceptedPrice + " for " + player.getName() + " (delay=" + (delay / 60000) + "min)");

        MethodScheduler.runAfterDelay(() -> {
            shopItem.setPrice((int) acceptedPrice);
            if (broadcastUpdate != null) broadcastUpdate.run();

            if (player.isLoggedIn()) {
                player.sendPacket(PacketCreator.getWhisperReceive(
                        ownerName, player.getClient().getChannel(), false,
                        "Hey, I updated the price on " + itemName + " to " + formatPrice(acceptedPrice) + ". Come grab it! " + roomLabel
                ));
            }
        }, delay);
    }

    private static void acceptOffer(PlayerShop shop, Character owner, Character player,
                                     OfferParser.ParsedOffer offer, long finalPrice) {
        String msg = getDialogueLine("AcceptResponse", offer, player);
        shopOwnerChat(shop, owner, msg);

        offer.getShopItem().setPrice((int) finalPrice);
        shop.broadcast(PacketCreator.getPlayerShopItemUpdate(shop));

        ShopOfferSystem.getInstance().lockItem(owner.getId(), offer.getItemIndex());
        ShopOfferSystem.getInstance().removeSession(player.getId());

        log("[ShopOfferSystem] Accepted: " + offer.getItemName() + " @ " + finalPrice + " for " + player.getName());
    }

    private static void shopOwnerChat(PlayerShop shop, Character owner, String message) {
        if (message == null || message.isEmpty()) return;
        shop.chat(owner, message);
    }

    private static String getDialogueLine(String node, OfferParser.ParsedOffer offer, Character player) {
        Map<String, String> replacements = buildReplacements(offer, player);
        return getDialogueLineWithReplacements(node, replacements);
    }

    private static String getDialogueLineWithReplacements(String node, Map<String, String> replacements) {
        BotDialogueHandler.DialogueConstructor dialog =
                BotDialogueHandler.getDialogueCon(DIALOGUE_PATH, BOT_TYPE, node);
        if (dialog == null || dialog.getDialogue().isEmpty()) return "";

        List<String> lines = dialog.getDialogue();
        String line = lines.get(random.nextInt(lines.size()));

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            line = line.replace(entry.getKey(), entry.getValue());
        }
        return line;
    }

    private static Map<String, String> buildReplacements(OfferParser.ParsedOffer offer, Character player) {
        Map<String, String> map = new HashMap<>();
        map.put("{item}", offer != null ? offer.getItemName() : "");
        map.put("{price}", offer != null ? formatPrice(offer.getOfferPrice()) : "");
        map.put("{player}", player != null ? player.getName() : "");
        map.put("{listing_price}", offer != null ? formatPrice(offer.getShopItem().getPrice()) : "");
        return map;
    }

    private static String getFMRoomLabel(int mapId) {
        int room = mapId - 910000000;
        if (room >= 1 && room <= 22) {
            return "FM " + room;
        }
        return "";
    }

    public static String formatPrice(long price) {
        if (price >= 1_000_000_000 && price % 1_000_000_000 == 0) {
            return (price / 1_000_000_000) + "b";
        } else if (price >= 1_000_000 && price % 1_000_000 == 0) {
            return (price / 1_000_000) + "m";
        } else if (price >= 1_000 && price % 1_000 == 0) {
            return (price / 1_000) + "k";
        }
        return String.valueOf(price);
    }
}
