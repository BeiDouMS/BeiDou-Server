package soloMapling.ArtificialPlayer.BotTradeSystem;

public class TradeRequest {

    private String traderName;
    private int traderId;

    public TradeRequest(String traderName, int traderId) {
        this.traderName = traderName;
        this.traderId = traderId;
    }

    public String getTraderName() {
        return traderName;
    }

    public int getTraderId() {
        return traderId;
    }

}
