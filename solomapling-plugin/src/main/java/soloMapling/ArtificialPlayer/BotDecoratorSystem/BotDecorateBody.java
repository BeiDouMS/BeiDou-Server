package soloMapling.ArtificialPlayer.BotDecoratorSystem;

import org.gms.client.Character;
import org.gms.client.SkinColor;

public class BotDecorateBody {

    public static void decorateBotBody(Character fakechar) {
        fakechar.setSkinColor(SkinColor.getById((getRandomSkin())));
        fakechar.setFace(BotCosmeticPool.selectEyes((byte) fakechar.getGender(), fakechar.getTier()));
        fakechar.setHair(BotCosmeticPool.selectHair((byte) fakechar.getGender(), fakechar.getTier()));
    }

    private static int getRandomSkin() {
        return Math.random() < 0.8 ? (byte) 0 : (byte) 1; // 80% white = 0, tan = 1
    }
}
