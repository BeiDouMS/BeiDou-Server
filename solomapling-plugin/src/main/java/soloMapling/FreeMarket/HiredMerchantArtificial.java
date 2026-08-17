package soloMapling.FreeMarket;

import org.gms.client.Character;
import org.gms.server.maps.HiredMerchant;

import java.awt.*;

public class HiredMerchantArtificial extends HiredMerchant {

    public enum shopTypes {
        Common,
        Warrior,
        Mage,
        Bowman,
        Thief,
        Pirate,
        Potion,
        Scroll,
        DarkScroll,
        Mastery,
        Chair,
        ETC,
        Stars
    }

    private String tier = "";
    private int mapId = 0;
    private shopTypes primary = null;
    private shopTypes secondary = null;
    private shopTypes tertiary = null;

    public HiredMerchantArtificial(Character owner, String desc, int itemId, int id, String name) {
        super(owner, desc, itemId);

        setOwnerId(id);
        setOwnerName(name);
    }

    public Point getPos() {
        return getPosition();
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getTier() {
        return this.tier;
    }

    public void setMapId(int newMapId) {
        this.mapId = newMapId;
    }

    public int getMapIdArtificial() {
        return this.mapId;
    }

    public int getRoomNumber() {
        return this.mapId % 100;
    }

    public shopTypes getPrimary() {
        return this.primary;
    }

    public void setPrimary(shopTypes shopType) {
        this.primary = shopType;
    }

    public shopTypes getSecondary() {
        return this.secondary;
    }

    public void setSecondary(shopTypes shopType) {
        this.secondary = shopType;
    }

    public shopTypes getTertiary() {
        return this.tertiary;
    }

    public void setTertiary(shopTypes shopType) {
        this.tertiary = shopType;
    }


}
