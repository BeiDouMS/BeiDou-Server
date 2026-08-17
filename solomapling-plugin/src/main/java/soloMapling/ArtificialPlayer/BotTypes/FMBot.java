package soloMapling.ArtificialPlayer.BotTypes;

import org.gms.client.Character;
import org.gms.client.inventory.Item;
import org.gms.server.maps.HiredMerchant;
import org.gms.server.maps.PlayerShop;
import org.gms.server.maps.PlayerShopItem;
import soloMapling.ArtificialPlayer.BotCommandsPack.SocialCommands;
import soloMapling.ArtificialPlayer.BotHelpers;
import soloMapling.ArtificialPlayer.BotMessagingSystem.ChatMessage;
import soloMapling.ArtificialPlayer.BotMessagingSystem.MessageQueue;
import soloMapling.ArtificialPlayer.BotSM;
import soloMapling.ArtificialPlayer.GCMoveSystem.GCMovement;
import soloMapling.FreeMarket.HiredMerchantAdapter;
import soloMapling.FreeMarket.PlayerShopAdapter;
import soloMapling.FreeMarket.ShopKeeper;
import soloMapling.itemPool.ItemUtilities;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.Random;


import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.FMRoomWarpPortalId;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botEnterFMRoom;
import static soloMapling.ArtificialPlayer.BotCommandsPack.WarpCommands.botExitFMRoom;
import static soloMapling.ArtificialPlayer.BotDialogueHandler.getRandomResolvedLine;
import static soloMapling.server.SoloMaplingUtilities.generateRandomNumber;
import static soloMapling.ArtificialPlayer.BotHelpers.convertItemIdToName;
import static soloMapling.ArtificialPlayer.BotLogic.isInsideFM;
import static soloMapling.ArtificialPlayer.BotLogic.isInsideFMRooms;
import static soloMapling.ArtificialPlayer.BotLogic.isPointNear;
import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementStructures.MovementEnums.FreeMarketValues.FM_ENTRANCE;
import static soloMapling.ArtificialPlayer.BotMovementSystem.NavigationSystem.FMMovementCommands.getDoorPoint;
import static soloMapling.BotLogger.log;

public class FMBot extends BotSM {
    private FMBotState fmBotState = FMBotState.RESET;
    private List<String> hint = Collections.singletonList(getChr().getName());

    private long startTime;
    private long endTime;

    private int fmRoom = 1;

    private ShopKeeper currentTarget;

    // Visit queue: pre-built order of shops to visit in this room
    private List<ShopKeeper> visitQueue = new ArrayList<>();
    private int visitIndex = 0;
    private Set<Integer> segmentStartIndices = new HashSet<>();

    // Escalating walk chance (pity timer)
    private static final double BASE_WALK_CHANCE = 0.40;
    private static final double WALK_CHANCE_INCREMENT = 0.10;
    private double walkChance = BASE_WALK_CHANCE;

    // GC moves are fire-and-forget, so arrival is polled from the FSM tick. The driver abandons
    // stuck/unreachable moves (isMoving goes false), so bounded counters are enough to guarantee
    // the FSM can never wedge on a walk that won't finish.
    private static final int MAX_DOOR_WALK_ATTEMPTS = 3;
    private static final int MAX_SHOP_ARRIVAL_POLLS = 8;
    private int doorWalkAttempts = 0;
    private int shopArrivalPolls = 0;

    public FMBot(Character character) {
        super(character);
        dialoguePath = "FMBotDialogue.yaml";
        botType = "FMBot";
    }

    private void setFMBotState(FMBotState state) {
        this.fmBotState = state;
    }

    protected enum FMBotState {
        RESET,
        NAV_TO_FM_ROOM,
        PROCESS_ROOM,
        BROWSING_ROOM,
        ACTION_PHASE,
        ENTER_SHOP,
        PROCESS_SHOP,
        EXIT_SHOP,
        EXIT_FM_ROOM
    }

    private void resetFMBotState() {
        setFMBotState(FMBotState.RESET);
        evaluateFMRoom();
    }

    // Self-looping: NAV_TO_FM_ROOM re-enters here every tick until the bot is inside the room.
    private void navToFMRoom() {
        if (isInsideFMRooms(this.getChr())) { // already inside FM room
            doorWalkAttempts = 0;
            return;
        }
        if (!isInsideFM(this.getChr())) {
            botExitFMRoom(getChr(), 1); // warp to the FM entrance; start the door walk next tick
            waitFor(1000);
            return;
        }
        Point doorPt = getDoorPoint(this.fmRoom);
        if (isPointNear(this.getChr().getPosition(), doorPt, 20) || doorWalkAttempts >= MAX_DOOR_WALK_ATTEMPTS) {
            // at the door - or the walk keeps dying, so enter anyway (the old code's timed warp
            // entered regardless of where the walk actually ended too)
            doorWalkAttempts = 0;
            botEnterFMRoom(this.getChr(), this.fmRoom);
            waitFor(1500); // settle beat so PROCESS_ROOM reads the room's shops, not the entrance
            return;
        }
        if (!GCMovement.isMoving(getChr())) { // not started yet, or the last attempt was abandoned
            doorWalkAttempts++;
            GCMovement.move(getChr(), doorPt.x, doorPt.y);
        }
        // still walking - stay in NAV_TO_FM_ROOM and re-check on the next tick
    }

    private void processRoom() {
        List<PlayerShop> shops = this.getChr().getMap().getAllPlayerShops();
        List<HiredMerchant> merchants = this.getChr().getMap().getAllHiredMerchants();
        buildVisitQueue(shops, merchants);
    }

    /**
     * Builds the visit queue by sorting all shops by distance, then performing
     * a deck-cut shuffle to create variety in visit order while preserving
     * spatial locality within each segment.
     */
    private void buildVisitQueue(List<PlayerShop> shops, List<HiredMerchant> merchants) {
        List<ShopKeeper> allShops = new ArrayList<>();
        for (HiredMerchant m : merchants) {
            allShops.add(new HiredMerchantAdapter(m));
        }
        for (PlayerShop s : shops) {
            allShops.add(new PlayerShopAdapter(s));
        }

        if (allShops.isEmpty()) {
            visitQueue = allShops;
            visitIndex = 0;
            segmentStartIndices.clear();
            walkChance = BASE_WALK_CHANCE;
            return;
        }

        // Sort by effective distance from current position (same-row shops prioritized)
        Point currentPos = getChr().getPosition();
        allShops.sort((a, b) -> {
            double distA = effectiveDistance(currentPos, a.getPosition());
            double distB = effectiveDistance(currentPos, b.getPosition());
            return Double.compare(distA, distB);
        });

        // Deck cut: split into 3-4 segments and rearrange for visit order variety
        Random rng = new Random();
        int numSegments = allShops.size() < 4 ? 1 : (3 + rng.nextInt(2));
        visitQueue = deckCut(allShops, numSegments, rng);

        visitIndex = 0;
        walkChance = BASE_WALK_CHANCE;
    }

    /**
     * Splits the sorted shop list into segments and rearranges them randomly,
     * like cutting a deck of cards. Preserves spatial locality within each segment
     * but varies which part of the room the bot starts browsing from.
     */
    private List<ShopKeeper> deckCut(List<ShopKeeper> sorted, int numSegments, Random rng) {
        segmentStartIndices.clear();

        if (sorted.size() <= numSegments) {
            segmentStartIndices.add(0);
            return new ArrayList<>(sorted);
        }

        int segmentSize = sorted.size() / numSegments;
        List<List<ShopKeeper>> segments = new ArrayList<>();

        for (int i = 0; i < numSegments; i++) {
            int start = i * segmentSize;
            int end = (i == numSegments - 1) ? sorted.size() : (i + 1) * segmentSize;
            segments.add(new ArrayList<>(sorted.subList(start, end)));
        }

        Collections.shuffle(segments, rng);

        List<ShopKeeper> result = new ArrayList<>();
        for (List<ShopKeeper> segment : segments) {
            segmentStartIndices.add(result.size());
            result.addAll(segment);
        }

        return result;
    }

    private double effectiveDistance(Point from, Point to) {
        double distance = Math.sqrt(
                Math.pow(from.x - to.x, 2) +
                        Math.pow(from.y - to.y, 2)
        );
        // Same-row shops are heavily prioritized (they're on the same screen row)
        if (Math.abs(from.y - to.y) < 1.0) {
            distance /= 1000.0;
        }
        return distance;
    }

    //

    private void browsingRoom() {
        if (isAllShopsVisited()) {
            return;
        }
        ShopKeeper next = visitQueue.get(visitIndex);
        walkToShop(next);
    }

    private boolean isAllShopsVisited() {
        return visitQueue.isEmpty() || visitIndex >= visitQueue.size();
    }

    //

    /**
     * Decides whether to walk to the next shop using an escalating probability system.
     * - Segment starts (including first shop in room): always walk
     * - Otherwise: roll against walkChance. If walk, reset to base. If not, increment chance.
     * This guarantees bots eventually walk after a streak of stationary clicks,
     * creating natural visual variety.
     */
    private void walkToShop(ShopKeeper shop) {
        currentTarget = shop;
        boolean isSegmentStart = segmentStartIndices.contains(visitIndex);

        try {
            boolean shouldWalk;
            if (isSegmentStart) {
                shouldWalk = true;
            } else {
                shouldWalk = Math.random() < walkChance;
            }

            if (shouldWalk) {
                walkChance = BASE_WALK_CHANCE;
                Point pos = BotHelpers.getRandomizedPointXAxis(currentTarget.getPosition());
                GCMovement.move(this.getChr(), pos.x, pos.y);
                shopArrivalPolls = 0; // ENTER_SHOP holds until this walk lands
            } else {
                walkChance = Math.min(1.0, walkChance + WALK_CHANCE_INCREMENT);
            }
        } catch (Exception ignored) {
            // Issue with path mapping. all good just don't walk then
        }
        visitIndex++;
    }

    //

    private void actionPhase() {
//        SocialCommands.BotChatbubble(getChr(), "Action Phase");
        // handle any desired action (comments, resting, scrolling etc)
        // possibly an action phase 2 after processing shop
        return;
    }

    private void enterShop() {
        // enter shop based on location & cross off from internal list to not repeat
        if (currentTarget == null) {
//            System.out.println("processShop ERROR. NO VALID MERCHANT / SHOP");
            return;
        }
        currentTarget.visitShop(getChr());
    }

    private void processShop() {
        // handle any shop related stuff, comments, buying, offering, window shopping
        if (currentTarget == null) {
//            System.out.println("processShop ERROR. NO VALID MERCHANT / SHOP");
            return;
        }
        purchaseShopItems();
        waitFor(1000); // browse beat before moving on to EXIT_SHOP
    }

    private List<PlayerShopItem> readShopItems() {
        List<PlayerShopItem> items = List.of();
        if (currentTarget instanceof HiredMerchantAdapter) {
            items = ((HiredMerchantAdapter) currentTarget).getMerchant().getItems();
        } else if (currentTarget instanceof PlayerShopAdapter) {
            items = ((PlayerShopAdapter) currentTarget).getShop().getItems();
        }
        return items;
    }

    /*
    Calculates a % chance to purchase 1 item based on probability.
     */
    private void purchaseShopItems() {
        List<PlayerShopItem> items = readShopItems();
        if (items.isEmpty()) {
            return;
        }

        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            PlayerShopItem pItem = items.get(itemIndex);
            if (pItem.getBundles() < 1) {
                continue;
            }
            Item thisItem = pItem.getItem();
            int itemId = thisItem.getItemId();
            String itemName = convertItemIdToName(itemId);
            int itemPrice = pItem.getPrice(); // item's FM price
            Integer itemMarketValue = ItemUtilities.getItemMarketValue(thisItem);
            Boolean purchase = shouldPurchaseItem(itemPrice, itemMarketValue);
            if (purchase) {
                String buymsg = getRandomResolvedLine(FMBot.this, "PurchaseItem");
                if (buymsg != null) {
                    currentTarget.chat(getChr(), buymsg);
                }
                if (currentTarget instanceof HiredMerchantAdapter) {
                    currentTarget.botBuyItem(getChr(), pItem, (short) 1);
                } else if (currentTarget instanceof PlayerShopAdapter) {
                    currentTarget.botBuyItemPlayerShop(getChr(), pItem, itemIndex, (short) 1);
                }
                break;
            }
        }
    }

    private void exitShop() {
        if (currentTarget == null) {
//            System.out.println("ERROR. NO VALID MERCHANT / SHOP");
            return;
        }
        currentTarget.removeVisitor(getChr());
        currentTarget = null;
    }

    // Walks to the room's door portal, then warps out. Returns true once out of the room
    // (or immediately on a degenerate room id) so EXIT_FM_ROOM knows to advance.
    private boolean exitRoom() {
        int roomNumber = getChr().getMapId() - FM_ENTRANCE;
        if (roomNumber < 1 || !FMRoomWarpPortalId.containsKey(roomNumber)) {
            incrementFMRoom();
            return true;
        }
        Point doorPoint = getFMRoomDoorPortalPoint();
        if (!isPointNear(this.getChr().getPosition(), doorPoint, 20) && doorWalkAttempts < MAX_DOOR_WALK_ATTEMPTS) {
            if (!GCMovement.isMoving(getChr())) { // not started yet, or the last attempt was abandoned
                doorWalkAttempts++;
                GCMovement.move(getChr(), doorPoint.x, doorPoint.y);
            }
            return false; // still walking to the door - re-check on the next tick
        }
        doorWalkAttempts = 0;
        botExitFMRoom(getChr(), fmRoom);
        incrementFMRoom();
        return true;
    }

    private void evaluateFMRoom() {
        if (getChr().getMapId() == FM_ENTRANCE) {
            fmRoom = generateRandomNumber(1, 12);
            return;
        }
        fmRoom = getChr().getMapId() - FM_ENTRANCE;
    }

    private void incrementFMRoom() {
        fmRoom++;
        if (fmRoom >= 13) {
            fmRoom = 1;
        }
    }

    private Point getFMRoomDoorPortalPoint() {
        int roomNumber = getChr().getMapId() - FM_ENTRANCE;
        return getChr().getMap().getPortal(FMRoomWarpPortalId.get(roomNumber)).getPosition();
    }

    /**
     * Determines whether to purchase an item based on price-to-market-value ratio
     * Uses probabilistic logic to simulate buying behavior with varying desperation levels
     *
     * @param askingPrice the asking price for the item
     * @param marketValue the estimated fair market value
     * @return true if item should be purchased, false otherwise
     */
    public static boolean shouldPurchaseItem(Integer askingPrice, Integer marketValue) {
        if (marketValue == null) return false;
        if (askingPrice <= 0 || marketValue <= 0) return false;

        double ratio = (double) askingPrice / marketValue;
        if (ratio > 1.2) return false; // Never buy 20%+ overpriced

        double[] thresholds = {0.7, 0.85, 0.95, 1.0, 1.05, 1.1, 1.15, 1.2};
        double[] probabilities = {1.0, 0.95, 0.85, 0.75, 0.55, 0.35, 0.15, 0.05};

        for (int i = 0; i < thresholds.length; i++) {
            if (ratio <= thresholds[i]) {
                return new Random().nextDouble() < probabilities[i];
            }
        }

        return false;
    }

    @Override
    public void updateState() {
        super.updateState();
        if (checkIfNotRunningOrPaused()) {
            return;
        }
        getDebugger().debugLoggingFull(String.format("%s TutorialBotState: %s", this.getChr().getName(), fmBotState), String.format("%s", fmBotState));

        switch (fmBotState) {
            case RESET:
                resetFMBotState();
                setFMBotState(FMBotState.NAV_TO_FM_ROOM);
                break;
            case NAV_TO_FM_ROOM:
                navToFMRoom();
                if (isInsideFMRooms(this.getChr())) {
                    setFMBotState(FMBotState.PROCESS_ROOM);
                }
                break;
            case PROCESS_ROOM:
                processRoom();
                if (isAllShopsVisited()) {
                    setFMBotState(FMBotState.EXIT_FM_ROOM);
                    return;
                }
                setFMBotState(FMBotState.BROWSING_ROOM);
                break;
            case BROWSING_ROOM:
                browsingRoom();
                setFMBotState(FMBotState.ACTION_PHASE);
                break;
            case ACTION_PHASE:
                actionPhase();
                setFMBotState(FMBotState.ENTER_SHOP);
                break;
            case ENTER_SHOP:
                if (GCMovement.isMoving(getChr()) && shopArrivalPolls++ < MAX_SHOP_ARRIVAL_POLLS) {
                    return; // still walking to the shop - re-check on the next tick
                }
                enterShop();
                setFMBotState(FMBotState.PROCESS_SHOP);
                break;
            case PROCESS_SHOP:
                processShop();
                setFMBotState(FMBotState.EXIT_SHOP);
                break;
            case EXIT_SHOP:
                exitShop();
                if (isAllShopsVisited()) {
                    setFMBotState(FMBotState.EXIT_FM_ROOM);
                    return;
                }
                setFMBotState(FMBotState.BROWSING_ROOM);
                break;
            case EXIT_FM_ROOM:
                if (exitRoom()) {
                    setFMBotState(FMBotState.NAV_TO_FM_ROOM);
                }
                break;
            default:
                log("Unexpected state: " + fmBotState);
                state = BotState.FINISHED;
                resetFMBotState();
                throw new IllegalStateException("Unexpected state: " + state);
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
            if (message == null) {
                return;
            }
//            handleBetCommand(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
