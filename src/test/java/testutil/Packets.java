package testutil;

import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.packet.InPacket;

public class Packets {

    public static InPacket emptyInPacket() {
        return new ByteBufInPacket(Unpooled.buffer());
    }
}
