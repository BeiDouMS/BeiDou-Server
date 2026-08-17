package soloMapling.FreeMarket.ShopOfferSystem;

import org.gms.client.Character;
import org.gms.server.maps.HiredMerchant;
import org.gms.server.maps.PlayerShop;
import org.gms.server.maps.PlayerShopItem;
import soloMapling.server.MethodScheduler;
import org.gms.util.PacketCreator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static soloMapling.ArtificialPlayer.BotHelpers.isBot;
import static soloMapling.BotLogger.log;

public class ShopOfferSystem {

    private static ShopOfferSystem instance;
    private final Map<Integer, HaggleSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<Integer, ShopMode> shopModes = new ConcurrentHashMap<>();
    private final Set<String> lockedItems = ConcurrentHashMap.newKeySet();
    private final Random random = new Random();

    private static final double PRESENT_CHANCE = 0.60;

    public enum ShopMode {
        PRESENT,
        AFK
    }

    private ShopOfferSystem() {}

    public static synchronized ShopOfferSystem getInstance() {
        if (instance == null) {
            instance = new ShopOfferSystem();
        }
        return instance;
    }

    public ShopMode getOrAssignMode(int ownerId) {
        return shopModes.computeIfAbsent(
                ownerId,
                id -> random.nextDouble() < PRESENT_CHANCE ? ShopMode.PRESENT : ShopMode.AFK
        );
    }

    public void onPlayerShopChat(Character player, PlayerShop shop, String message) {
        if (player == null || shop == null || message == null) return;
        if (!isBot(shop.getOwner())) return;

        List<PlayerShopItem> items = shop.getItems();
        OfferParser.ParsedOffer offer = OfferParser.parse(message, items);
        if (offer == null) return;

        String lockKey = buildLockKey(shop.getOwner().getId(), offer.getItemIndex());
        if (lockedItems.contains(lockKey)) {
            log("[ShopOfferSystem] Item locked, ignoring offer from " + player.getName());
            return;
        }

        log("[ShopOfferSystem] Offer detected from " + player.getName()
                + ": " + offer.getItemName() + " @ " + offer.getOfferPrice()
                + " (listing: " + offer.getShopItem().getPrice() + ")");

        cleanExpiredSessions();

        ShopMode mode = getOrAssignMode(shop.getOwner().getId());

        int responseDelay = 2000 + random.nextInt(4000);

        if (mode == ShopMode.PRESENT) {
            HaggleSession existingSession = activeSessions.get(player.getId());
            if (existingSession != null) {
                existingSession.touch();
                MethodScheduler.runAfterDelay(
                        () -> ShopOfferResponse.handlePresentOwner(player, shop, offer, existingSession),
                        responseDelay
                );
                return;
            }

            HaggleSession session = new HaggleSession(player.getId(), shop.getOwner().getId());
            activeSessions.put(player.getId(), session);
            MethodScheduler.runAfterDelay(
                    () -> ShopOfferResponse.handlePresentOwner(player, shop, offer, session),
                    responseDelay
            );
        } else {
            ShopOfferResponse.handleAFKOwner(player, shop, offer, this);
        }
    }

    public void onHiredMerchantChat(Character player, HiredMerchant merchant, String message) {
        if (player == null || merchant == null || message == null) return;

        List<PlayerShopItem> items = merchant.getItems();
        OfferParser.ParsedOffer offer = OfferParser.parse(message, items);
        if (offer == null) return;

        String ownerName = merchant.getOwner();
        String lockKey = buildLockKey(merchant.getOwnerId(), offer.getItemIndex());
        if (lockedItems.contains(lockKey)) {
            log("[ShopOfferSystem] HiredMerchant item locked, ignoring offer from " + player.getName());
            return;
        }

        log("[ShopOfferSystem] HiredMerchant offer from " + player.getName()
                + ": " + offer.getItemName() + " @ " + offer.getOfferPrice()
                + " (listing: " + offer.getShopItem().getPrice() + ", owner: " + ownerName + ")");

        ShopOfferResponse.handleHiredMerchantAFK(
                player, ownerName, merchant.getOwnerId(), merchant.getMapId(),
                offer.getShopItem(), offer, this,
                () -> merchant.broadcastToVisitorsThreadsafe(PacketCreator.updateHiredMerchant(merchant, player))
        );
    }

    public void lockItem(int ownerId, int itemIndex) {
        lockedItems.add(buildLockKey(ownerId, itemIndex));
    }

    public void removeSession(int playerId) {
        activeSessions.remove(playerId);
    }

    private String buildLockKey(int ownerId, int itemIndex) {
        return ownerId + "_" + itemIndex;
    }

    private void cleanExpiredSessions() {
        Iterator<Map.Entry<Integer, HaggleSession>> it = activeSessions.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().isExpired()) {
                it.remove();
            }
        }
    }
}
