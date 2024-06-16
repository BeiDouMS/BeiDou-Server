package testutil;

import io.netty.buffer.Unpooled;
import net.packet.ByteBufInPacket;
import net.packet.ByteBufOutPacket;
import net.packet.InPacket;
import net.packet.OutPacket;

import java.util.function.Consumer;

public class Packets {

    public static InPacket buildInPacket(Consumer<OutPacket> contentProvider) {
        OutPacket builderInput = new ByteBufOutPacket();
        contentProvider.accept(builderInput);
        return new ByteBufInPacket(Unpooled.wrappedBuffer(builderInput.getBytes()));
    }
}
