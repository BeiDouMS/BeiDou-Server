package soloMapling.ArtificialPlayer.BotTypes.Blackjack;

import org.gms.client.Character;
import org.gms.server.maps.MapObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static soloMapling.DebugUtilities.debugprint;

public class BlackjackPlayer {

    private void dprint(String msg) {
        boolean debugPrintConsole = false;
        if (!debugPrintConsole) return;
        debugprint("[BlackjackPlayer:" + (character == null ? "?" : character.getName()) + "] " + msg);
    }

    public enum PlayerStatus {
        WAITING,
        ACTIVE,
        STAND,
        BUST
    }

    public enum HandResult {
        PENDING,
        WIN,
        LOSE,
        PUSH,
        BLACKJACK_WIN
    }

    private final Character character;
    private final List<String> hand;
    private final List<MapObject> bets;
    private final List<MapObject> cardsOnMap;
    private Point originLocation;
    private PlayerStatus status;
    private HandResult result;
    private String responseStatus;
    private int skippedHands;
    private boolean betsProcessed;

    public BlackjackPlayer(Character character) {
        this.character = character;
        this.hand = new ArrayList<>();
        this.bets = new ArrayList<>();
        this.cardsOnMap = new ArrayList<>();
        this.originLocation = new Point();
        this.status = PlayerStatus.WAITING;
        this.result = HandResult.PENDING;
        this.responseStatus = "WAITING";
        this.skippedHands = 0;
        this.betsProcessed = false;
    }

    // Character

    public Character getCharacter() {
        return character;
    }

    public String getName() {
        return character.getName();
    }

    // Hand

    public List<String> getHand() {
        return hand;
    }

    public void addCardToHand(String card) {
        hand.add(card);
        dprint("addCardToHand card=" + card + " hand=" + hand
                + " value=" + BlackjackRules.calculateHandValue(hand));
    }

    public void clearHand() {
        hand.clear();
    }

    public int getHandValue() {
        return BlackjackRules.calculateHandValue(hand);
    }

    // Bets

    public List<MapObject> getBets() {
        return bets;
    }

    public void setBets(List<MapObject> newBets) {
        bets.clear();
        bets.addAll(newBets);
        dprint("setBets count=" + bets.size());
    }

    public void clearBets() {
        bets.clear();
    }

    public boolean hasBets() {
        return !bets.isEmpty();
    }

    // Cards on map (visual tracking)

    public List<MapObject> getCardsOnMap() {
        return cardsOnMap;
    }

    public void addCardOnMap(MapObject card) {
        cardsOnMap.add(card);
    }

    public void clearCardsOnMap() {
        cardsOnMap.clear();
    }

    // Origin location

    public Point getOriginLocation() {
        return originLocation;
    }

    public void setOriginLocation(Point pos) {
        this.originLocation = new Point(pos);
    }

    // Status

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        if (this.status != status) {
            dprint("status " + this.status + " -> " + status);
        }
        this.status = status;
    }

    // Hand result

    public HandResult getResult() {
        return result;
    }

    public void setResult(HandResult result) {
        if (this.result != result) {
            dprint("result " + this.result + " -> " + result);
        }
        this.result = result;
    }

    // Response tracking (for message queue)

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public boolean hasResponded() {
        return "RESPONDED".equals(responseStatus);
    }

    // Skipped hands (3 skips = removal)

    public boolean skipHand() {
        skippedHands++;
        dprint("skipHand count=" + skippedHands);
        if (skippedHands > 3) {
            System.out.println("[Blackjack] " + getName() + " has skipped 3 hands. Removing from game.");
            return true;
        }
        return false;
    }

    public void resetSkippedHands() {
        skippedHands = 0;
    }

    // Bets processed

    public boolean isBetsProcessed() {
        return betsProcessed;
    }

    public void setBetsProcessed(boolean processed) {
        this.betsProcessed = processed;
    }

    // Reset for new round

    public void resetForNewRound() {
        dprint("resetForNewRound");
        hand.clear();
        bets.clear();
        cardsOnMap.clear();
        status = PlayerStatus.WAITING;
        result = HandResult.PENDING;
        responseStatus = "WAITING";
        betsProcessed = false;
    }
}
