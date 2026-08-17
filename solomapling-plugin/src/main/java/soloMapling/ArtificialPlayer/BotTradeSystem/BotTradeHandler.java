package soloMapling.ArtificialPlayer.BotTradeSystem;

import org.gms.client.Character;

import static soloMapling.DebugUtilities.debugprint;

public class BotTradeHandler {

    Character chr;
    Character tradePartner;

    public BotTradeHandler(Character chr) {
        this.chr = chr;
    }

    public void setTradePartner(org.gms.client.Character player) {
        this.tradePartner = player;
    }

    public void resetTradePartner() {
        setTradePartner(null);
    }

    protected org.gms.client.Character getTradePartnerConfirmed() {
        try {
            return this.tradePartner;
        } catch (Exception e) {
            return null;
        }
    }

    public org.gms.client.Character getTradePartnerRaw() {
        try {
            return chr.getTrade().getPartner().getChr();
        } catch (Exception e) {
            return null;
        }
    }

//    protected boolean checkTradeRequests() {
//        if (getTradePartnerRaw() == null || !verifyPlayerOnSameMap(getTradePartnerRaw())) {
//            resetTradePartner();
//            return false;
//        }
//        setTradePartner(getTradePartnerRaw());
//        return true;
//    }

    public boolean verifyTradePartner() {

        boolean haveTradePartner = getTradePartnerRaw() == null;
        Character tradePartner = getTradePartnerConfirmed();
        boolean haveTradePartnerConfirmed = tradePartner == null;
        boolean tradePartnerOnSameMap;
        if (tradePartner != null) {
            tradePartnerOnSameMap = verifyPlayerOnSameMap(tradePartner);
        } else {
            tradePartnerOnSameMap = false;
        }
//        debugprint("tradePartner, tradePartnerConfirmed, tradePartnerSameMap: ",
//                haveTradePartner, haveTradePartnerConfirmed, tradePartnerOnSameMap);

        if (getTradePartnerRaw() == null ||
                getTradePartnerConfirmed() == null ||
                !verifyPlayerOnSameMap(getTradePartnerConfirmed())) {
//            debugprint("verify trade partner false. ");
            return false;
        }
        return true;
    }

    protected boolean insideAcceptedTrade() {
//        if ()
        return true;
    }

    protected boolean verifyPlayerOnSameMap(Character player) {
        return player.getMapId() == chr.getMapId();
    }

}
