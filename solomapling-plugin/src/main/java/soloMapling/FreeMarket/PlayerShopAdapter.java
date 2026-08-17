package soloMapling.FreeMarket;

import org.gms.client.Character;
import org.gms.server.maps.PlayerShop;
import org.gms.server.maps.PlayerShopItem;
import org.gms.util.PacketCreator;

import java.awt.*;

// Adapter for PlayerShop
public class PlayerShopAdapter implements ShopKeeper {
    private final PlayerShop shop;

    public PlayerShopAdapter(PlayerShop shop) {
        this.shop = shop;
    }

    @Override
    public String getOwner() {
        return shop.getOwner().getName();
    }

    @Override
    public Point getPosition() {
        return shop.getPosition();
    }

    @Override
    public void visitShop(Character fakechar) {
        getShop().visitShop(fakechar);
    }

    @Override
    public void chat(Character fakechar, String msg) {
        getShop().chat(fakechar, msg);
    }

    @Override
    public void removeVisitor(Character fakechar) {
        getShop().removeVisitor(fakechar);
    }

    @Override
    public void botBuyItem(Character fakechar, PlayerShopItem pItem, short quantity) {
        // Nothing due to HiredMerchant only
    }

    @Override
    public void botBuyItemPlayerShop(Character fakechar, PlayerShopItem pItem, int itemPosition, short quantity) {
        getShop().botBuy(fakechar, pItem, itemPosition, quantity);
        getShop().broadcast(PacketCreator.getPlayerShopItemUpdate(getShop()));
    }

    // Helper method to get the original shop if needed
    public PlayerShop getShop() {
        return shop;
    }
}
