package soloMapling.ArtificialPlayer.BotCommandsPack;

import org.gms.client.Character;
import org.gms.client.inventory.Equip;
import org.gms.util.PacketCreator;

import static soloMapling.ArtificialPlayer.BotMovementSystem.MovementPacketConstructor.createIdleStandlingPacket;

public class VFXCommands {

    public static void botScroll(Character fakechar, Equip.ScrollResult result) {
        fakechar.getMap().broadcastMessage(PacketCreator.getScrollEffect(fakechar.getId(), result, false, false));
    }

    public static void botScrollSuccess(Character fakechar) {
        botScroll(fakechar, Equip.ScrollResult.SUCCESS);
    }

    public static void botScrollFail(Character fakechar) {
        botScroll(fakechar, Equip.ScrollResult.CURSE);
    }

}
