package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.BotTradeSystem.BotTradeSM;
import soloMapling.FreeMarket.FMItem;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static soloMapling.ArtificialPlayer.BotTypeManager.BotType.NX_MERCHANT_BOT;
import static soloMapling.ArtificialPlayer.BotTypeManager.convertBotType;
import static soloMapling.BotLogger.log;
import static soloMapling.Environment.PlatformPlacement.botMoveToPlatformAnyUnoccupiedSpotDynamic;
import static soloMapling.Environment.PlatformPlacement.getCurrentPlatform;
import static soloMapling.Environment.PlatformPlacement.getMainPlatformIds;
import static soloMapling.FreeMarket.ArtificialShopGenerator.generateDarkScrollsList;
import static soloMapling.FreeMarket.ArtificialShopGenerator.generateScrollsList;
import static soloMapling.FreeMarket.FMEconomyManager.formatPriceToShorthand;
import static soloMapling.FreeMarket.FMEconomyManager.priceAdjustmentRules;
import static soloMapling.itemPool.ItemInformationProviderUtilities.getItemName;
import static soloMapling.server.SoloMaplingUtilities.getRandomElement;
import static soloMapling.server.SoloMaplingUtilities.random;
import static soloMapling.server.SoloMaplingUtilities.rollChanceInverse;

public class BuyingMerchantBot extends BotSM {
    private BuyingState buyingState = BuyingState.RESET;
    private List<String> hint = Collections.singletonList(getChr().getName());
    private List<FMItem> itemsToBuy;
    private int itemIndex = 0;
    private boolean movedDuringAdvertise = false;

    private static final double BUY_DISCOUNT_MIN = 0.80;
    private static final double BUY_DISCOUNT_MAX = 0.90;

    private enum BuyingState {
        RESET,
        SELECT_ITEM,
        ADVERTISE,
        CHECK_TRADES,
        IDLE_ACTIONS
    }

    private static final List<String> FLAVOR_NODES = List.of("ScamMessages", "BeggingMessages", "RWTMessages", "FunnyMessages");

    public BuyingMerchantBot(Character character) {
        super(character);
        dialoguePath = "MerchantBotDialogue.yaml";
        botType = "MerchantBot";
    }

    private void resetState() {
        itemIndex = 0;
        loadItemList();
        buyingState = BuyingState.RESET;
    }

    private void loadItemList() {
        Supplier<List<FMItem>>[] generators = new Supplier[]{
                () -> generateScrollsList("A"),
                () -> generateDarkScrollsList("A")
        };
        itemsToBuy = generators[random.nextInt(generators.length)].get();
    }

    private FMItem getCurrentItem() {
        if (itemsToBuy == null || itemIndex >= itemsToBuy.size()) {
            return null;
        }
        return itemsToBuy.get(itemIndex);
    }

    private void selectNextItem() {
        if (itemsToBuy == null || itemsToBuy.isEmpty()) {
            loadItemList();
        }

        itemIndex++;
        if (itemIndex >= itemsToBuy.size()) {
            itemIndex = 0;
            loadItemList();
        }

        FMItem currItem = getCurrentItem();
        if (currItem == null) {
            return;
        }

        // Set up buying mode: we want the item, we offer mesos at a discount
        setTradeMode(BotTradeSM.TradeMode.BUYING);
        getTradeWants().resetTradeWants();
        getTradeWants().addItemWanted(currItem.getItemId(), 1);

        int marketPrice = currItem.getPrice();
        double discount = BUY_DISCOUNT_MIN + random.nextDouble() * (BUY_DISCOUNT_MAX - BUY_DISCOUNT_MIN);
        int buyPrice = priceAdjustmentRules((int) (marketPrice * discount));
        getTradeWants().setMesoOffering(buyPrice);
        getTradeWants().setMesoWanted(0);

        resetLastTradeResult();
        resetLastTradedCharacter();
    }

    private void advertise() {
        FMItem itm = getCurrentItem();
        if (itm == null) {
            return;
        }
        String itemName = getItemName(itm.getItemId());
        if (itemName != null) {
            String msg = buildBuyingMessage(itemName, getTradeWants().getMesoOffering());
            SocialCommands.BotSpeak(getChr(), msg);
        }
    }

    static String buildBuyingMessage(String itemName, int offerPrice) {
        List<String> prefixes = List.of("Buying", "B>", "B>>", "BUY>", "Buying>");
        List<String> suffixes = List.of("Trade Me", "PM me", "just trade me!", "hmu", "whisp me",
                "no lowball", "no noobs", "no scammers", "Pros only", "hotties only", "no nx h0es",
                "baddies only", "no weebs", "English Only", "No Spanish",
                "serious offers only", "dont waste my time", "legit only", "fair price only");

        String msg = getRandomElement(prefixes) + " " + itemName + " " + formatPriceToShorthand(offerPrice) + " " + getRandomElement(suffixes);

        int fillerCount = random.nextInt(3);
        for (int i = 0; i < fillerCount; i++) {
            msg += " @@@@@@@@";
        }

        msg = msg.replace("[", "").replace("]", "");

        if (random.nextDouble() < 0.15) {
            msg = msg.toUpperCase();
        }
        return msg;
    }

    // Dynamic movement lands on the exact picked pixel, so the old nudgeAwayFromOverlap
    // band-aid (recorded paths piling bots onto fixed endpoints) is no longer needed here.
    private boolean tryPlatformShuffleWhileAdvertising() {
        if (rollChanceInverse(10)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getCurrentPlatform(getChr()));
            return true;
        } else if (rollChanceInverse(20)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(List.of("m1", "m5")));
            return true;
        } else if (rollChanceInverse(30)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(List.of("m1", "m2")));
            return true;
        } else if (rollChanceInverse(70)) {
            int currentMap = getChr().getMapId();
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(getMainPlatformIds(currentMap)));
            return true;
        }
        return false;
    }

    private void handleIdleActions() {
        if (movedDuringAdvertise) {
            movedDuringAdvertise = false;
            return;
        }
        if (rollChanceInverse(10)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getCurrentPlatform(getChr()));
        } else if (rollChanceInverse(20)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(List.of("m1", "m5")));
        } else if (rollChanceInverse(30)) {
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(List.of("m1", "m2")));
        } else if (rollChanceInverse(70)) {
            int currentMap = getChr().getMapId();
            botMoveToPlatformAnyUnoccupiedSpotDynamic(getChr(), getRandomElement(getMainPlatformIds(currentMap)));
        }
    }

    private boolean tryConvertToNXMerchant() {
        if (rollChanceInverse(100)) {
            convertBotType(getChr(), NX_MERCHANT_BOT);
            return true;
        }
        return false;
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        if (getState() == BotState.TRADING) {
            return;
        }
        getDebugger().debugLoggingFull(
                String.format("%s BuyingMerchantBot: %s", getChr().getName(), buyingState),
                String.format("%s", buyingState));

        switch (buyingState) {
            case RESET:
                resetState();
                buyingState = BuyingState.SELECT_ITEM;
                break;
            case SELECT_ITEM:
                selectNextItem();
                buyingState = BuyingState.ADVERTISE;
                break;
            case ADVERTISE:
                if (rollChanceInverse(25)) {
                    getDialogueHandler().executeBotFlavorDialogue(getRandomElement(FLAVOR_NODES), this);
                } else {
                    advertise();
                }
                movedDuringAdvertise = tryPlatformShuffleWhileAdvertising();
                buyingState = BuyingState.CHECK_TRADES;
                break;
            case CHECK_TRADES:
                checkForTrades();
                buyingState = BuyingState.IDLE_ACTIONS;
                break;
            case IDLE_ACTIONS:
                handleIdleActions();
                if (tryConvertToNXMerchant()) {
                    return;
                }
                buyingState = BuyingState.SELECT_ITEM;
                break;
            default:
                log("Unexpected state: " + buyingState);
                state = BotState.FINISHED;
                throw new IllegalStateException("Unexpected state: " + buyingState);
        }
    }

    @Override
    public void displayCommands(Character chr) {
        SocialCommands.displayPlayerChatCommands(chr, hint);
    }

    @Override
    public void processMessages() {
        try {
            ChatMessage message = MessageQueue.getInstance().getMessageWithTimeout("secondary", 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
