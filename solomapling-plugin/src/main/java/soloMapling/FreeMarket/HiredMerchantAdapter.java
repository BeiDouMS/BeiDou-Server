package soloMapling.FreeMarket;

import org.gms.client.Character;
import org.gms.server.maps.HiredMerchant;
import org.gms.server.maps.PlayerShopItem;
import org.gms.util.PacketCreator;

import java.awt.*;

// Adapter for HiredMerchant
public class HiredMerchantAdapter implements ShopKeeper {
    private final HiredMerchant merchant;

    public HiredMerchantAdapter(HiredMerchant merchant) {
        this.merchant = merchant;
    }

    @Override
    public String getOwner() {
        return merchant.getOwner();
    }

    @Override
    public Point getPosition() {
        return merchant.getPosition();
    }

    @Override
    public void visitShop(Character fakechar) {
        getMerchant().visitShop(fakechar);
    }

    @Override
    public void chat(Character fakechar, String msg) {
        getMerchant().sendMessage(fakechar, msg);
    }

    @Override
    public void removeVisitor(Character fakechar) {
        getMerchant().removeVisitor(fakechar);
    }

    @Override
    public void botBuyItem(Character fakechar, PlayerShopItem pItem, short quantity) {
        getMerchant().botBuy(fakechar, pItem, quantity);
        getMerchant().broadcastToVisitorsThreadsafe(PacketCreator.updateHiredMerchant(getMerchant(), fakechar));
    }

    @Override
    public void botBuyItemPlayerShop(Character fakechar, PlayerShopItem pItem, int itemPosition, short quantity) {
        // Nothing due to PlayerShop only
    }

    // Helper method to get the original merchant if needed
    public HiredMerchant getMerchant() {
        return merchant;
    }
}
