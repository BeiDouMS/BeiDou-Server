package net.packet;

import io.netty.buffer.ByteBuf;

public class ByteBufOutPacket implements OutPacket {
    private final ByteBuf byteBuf;

    public ByteBufOutPacket(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }
}
