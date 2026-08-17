package soloMapling.FreeMarket;

import org.gms.client.Character;
import org.gms.server.maps.PlayerShopItem;

import java.awt.*;

public interface ShopKeeper {
    String getOwner();
    Point getPosition();
    void visitShop(Character fakechar);
    void chat(Character fakechar, String msg);
    void botBuyItem(Character fakechar, PlayerShopItem pItem, short quantity);
    void botBuyItemPlayerShop(Character fakechar, PlayerShopItem pItem, int itemPosition, short quantity);
    void removeVisitor(Character fakechar);

}

