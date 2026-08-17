package soloMapling.ArtificialPlayer.BotTradeSystem;

import org.gms.client.Character;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.Item;
import org.gms.server.Trade;
import org.gms.util.PacketCreator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static soloMapling.ArtificialPlayer.BotLogic.generateCleanItemEquip;
import static soloMapling.ArtificialPlayer.BotLogic.generateCleanItemWithQty;
import static soloMapling.DebugUtilities.debugprint;

public class BotTradeCommands {

    private static boolean isValidTrade(Character fakechar) {
        return fakechar.getTrade() != null;
    }

    public static void startTradeRequest(Character fakechar) {
        Trade.startTrade(fakechar);
    }

    public static void sendTradeRequestToPlayer(Character fakechar, Character invitee) {
        startTradeRequest(fakechar);

        invitee.setTrade(new Trade((byte) 1, invitee));
        invitee.getTrade().setPartner(fakechar.getTrade());
        fakechar.getTrade().setPartner(invitee.getTrade());
        invitee.sendPacket(PacketCreator.tradeInvite(fakechar));
    }

    public static void acceptTradeInvite(Character fakechar) {
        if (isValidTrade(fakechar)) {
            Trade.visitTrade(fakechar, getTradePartnerCharacter(fakechar));
        }
    }

    public static void declineTradeInvite(Character fakechar) {
        try {
            Trade.declineTrade(fakechar);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void readTradeChat() {
        // Read text in trade window
//        todo
    }

    public static void writeTradeChat(Character fakechar, String message) {
        if (isValidTrade(fakechar)) {
            fakechar.getTrade().chat(message);
        }
    }

    public static void setMeso(Character fakechar, int amount) {
        // Bot sets, not add (for easy modification)
        if (amount < 0 || amount > 2147483647) {
            throw new IllegalArgumentException("setMeso must be positive amount or less than max meso");
        }
        if (isValidTrade(fakechar)) {
            fakechar.getTrade().setMesoBot(amount);
        }
    }

    public static int readMeso(Character fakechar) {
        if (isValidTrade(fakechar)) {
            int botMeso = fakechar.getTrade().getMeso();
            debugprint(botMeso);
            return botMeso;
        }
        return 0;
    }

    public static int readPartnerMeso(Character fakechar) {
        if (isValidTrade(fakechar)) {
            int partnerMeso = getTradePartner(fakechar).getMeso();
            debugprint(partnerMeso);
            return partnerMeso;
        }
        return 0;
    }

    public static List<Integer> getOccupiedTradeSlots(Character fakechar) {
        if (isValidTrade(fakechar)) {
            List<Integer> occupiedPositions = new ArrayList<>();
            List<Item> botItems = fakechar.getTrade().getItems();
            for (Item item : botItems) {
                occupiedPositions.add((int) item.getPosition());
            }
            debugprint(occupiedPositions);
            return occupiedPositions;
        }
        return List.of();
    }

    public static List<Integer> getEmptyTradeSlots(Character fakechar) {
        if (isValidTrade(fakechar)) {
            List<Integer> emptySlots = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));
            List<Integer> occupiedSlots = getOccupiedTradeSlots(fakechar);
            if (occupiedSlots == null) {
                return emptySlots;
            }
            emptySlots.removeAll(occupiedSlots);
            debugprint(emptySlots);
            return emptySlots;
        }
        return List.of();
    }

    public static Integer getRandomEmptySlot(Character fakechar) {
        List<Integer> emptySlots = getEmptyTradeSlots(fakechar);

        if (emptySlots == null || emptySlots.isEmpty()) {
            return null; // No empty slots available
        }

        int randomIndex = (int) (Math.random() * emptySlots.size());
        return emptySlots.get(randomIndex);
    }

    public static boolean addEquipToTrade(Character fakechar, Equip equip, int slot) {
        if (validateTradeParams(fakechar, slot) && equip != null) {
            return addItemToTradeWindow(fakechar, equip, slot);
        }
        return false;
    }

    public static boolean addCleanEquipToTrade(Character fakechar, int equipId, int slot) {
        if (validateTradeParams(fakechar, slot)) {
            Equip tradeItem = (Equip) generateCleanItemEquip(equipId);
            return addEquipToTrade(fakechar, tradeItem, slot);
        }
        return false;
    }

    public static boolean addItemToTrade(Character fakechar, int itemId, int qty, int slot) {
        if (validateTradeParams(fakechar, slot)) {
            Item tradeItem = generateCleanItemWithQty(itemId, qty);
            return addItemToTradeWindow(fakechar, tradeItem, slot);
        }
        return false;
    }

    public static boolean swapScamEquipToTrade(Character fakechar, int equipId, int slot) {
        if (isConfirmed(fakechar)) {
            return false;
        }
        if (validateTradeParams(fakechar, slot)) {
            Equip tradeItem = (Equip) generateCleanItemEquip(equipId);
            return swapItemToTradeWindow(fakechar, tradeItem, slot);
        }
        return false;
    }

    public static boolean swapScamItemToTrade(Character fakechar, int itemId, int qty, int slot) {
        if (isConfirmed(fakechar)) {
            return false;
        }
        if (validateTradeParams(fakechar, slot)) {
            Item tradeItem = generateCleanItemWithQty(itemId, qty);
            return swapItemToTradeWindow(fakechar, tradeItem, slot);
        }
        return false;
    }

    private static boolean validateTradeParams(Character fakechar, int slot) {
        if (fakechar.getTrade() == null) {
            return false;
        }
        if (slot < 1 || slot > 9) {
            throw new IllegalArgumentException("slot must be 1-9.");
        }
        return true;
    }

    private static boolean addItemToTradeWindow(Character fakechar, Item tradeItem, int slot) {
        tradeItem.setPosition((short) slot);
        boolean added = fakechar.getTrade().addItem(tradeItem);
        if (added) {
            fakechar.sendPacket(PacketCreator.getTradeItemAdd((byte) 0, tradeItem));
            if (getTradePartner(fakechar) != null) {
                getTradePartnerCharacter(fakechar).sendPacket(PacketCreator.getTradeItemAdd((byte) 1, tradeItem));
            }
        }
        return added;
    }

    private static boolean swapItemToTradeWindow(Character fakechar, Item tradeItem, int slot) {
        tradeItem.setPosition((short) slot);
        boolean added = fakechar.getTrade().swapItem(tradeItem);
        if (added) {
            fakechar.sendPacket(PacketCreator.getTradeItemAdd((byte) 0, tradeItem));
            if (getTradePartner(fakechar) != null) {
                getTradePartnerCharacter(fakechar).sendPacket(PacketCreator.getTradeItemAdd((byte) 1, tradeItem));
            }
        }
        return added;
    }

    public static Item readItemInLocalSlot(Character fakechar, int slot) {
        List<Item> localItems = getLocalItems(fakechar);
        if (localItems == null) {
            throw new IllegalStateException("Local's items list is null.");
        }
        for (Item item : localItems) {
            if (item.getPosition() == slot) {
                return item;
            }
        }
        return null;
    }

    public static List<Item> getLocalItems(Character fakechar) {
        if (isValidTrade(fakechar)) {
            return fakechar.getTrade().getItems();
        }
        return null;
    }

    public static List<Item> getPartnersItems(Character fakechar) {
        if (isValidTrade(fakechar)) {
            return getTradePartner(fakechar).getItems();
        }
        return null;
    }

    public static Item readItemInPartnerSlot(Character fakechar, int slot) {
        List<Item> partnerItems = getPartnersItems(fakechar);
        if (partnerItems == null) {
            throw new IllegalStateException("Partner's items list is null.");
        }
        for (Item item : partnerItems) {
            if (item.getPosition() == slot) {
                return item;
            }
        }
        return null;
    }

    public static boolean isPartnerLocked(Character fakechar) {
        if (isValidTrade(fakechar)) {
            boolean partnerLocked = getTradePartner(fakechar).isLocked();
            debugprint(partnerLocked);
            return partnerLocked;
        }
        return false;
    }

    public static void confirmTrade(Character fakechar) {
        if (isValidTrade(fakechar)) {
            Trade.completeTrade(fakechar);
        }
    }

    public static boolean isConfirmed(Character fakechar) {
        if (isValidTrade(fakechar)) {
            return fakechar.getTrade().isLocked();
        }
        return false;
    }

    public static void cancelTrade(Character fakechar) {
        Trade.declineTrade(fakechar);
    }

    public static Trade getTradePartner(Character fakechar) {
        return fakechar.getTrade().getPartner();
    }

    public static Character getTradePartnerCharacter(Character fakechar) {
        return getTradePartner(fakechar).getChr();
    }

    // Flavor methods

    public static int readPartnerFame(Character fakechar) {
        if (isValidTrade(fakechar)) {
            int fame = getTradePartnerCharacter(fakechar).getFame();
            debugprint(fame);
            return fame;
        }
        return 0;
    }

    public static int readPartnerLevel(Character fakechar) {
        if (isValidTrade(fakechar)) {
            int level = getTradePartnerCharacter(fakechar).getLevel();
            debugprint(level);
            return level;
        }
        return 0;
    }

}
