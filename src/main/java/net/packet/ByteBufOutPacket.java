package net.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.jcip.annotations.NotThreadSafe;
import net.opcodes.SendOpcode;

import java.awt.*;

@NotThreadSafe
public class ByteBufOutPacket implements OutPacket {
    private final ByteBuf byteBuf;

    public ByteBufOutPacket(SendOpcode op) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeShortLE((short) op.getValue());
        this.byteBuf = byteBuf;
    }

    public ByteBufOutPacket(SendOpcode op, int initialCapacity) {
        ByteBuf byteBuf = Unpooled.buffer(initialCapacity);
        byteBuf.writeShortLE((short) op.getValue());
        this.byteBuf = byteBuf;
    }

    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        int readerIndex = byteBuf.readerIndex();
        byteBuf.getBytes(readerIndex, bytes);
        return bytes;
    }

    @Override
    public void writeByte(byte value) {
        byteBuf.writeByte(value);
    }

    @Override
    public void writeByte(int value) {
        writeByte((byte) value);
    }

    @Override
    public void writeBytes(byte[] value) {
        byteBuf.writeBytes(value);
    }

    @Override
    public void writeShort(int value) {
        byteBuf.writeShortLE(value);
    }

    @Override
    public void writeInt(int value) {
        byteBuf.writeIntLE(value);
    }

    @Override
    public void writeLong(long value) {
        byteBuf.writeLongLE(value);
    }

    @Override
    public void writeBoolean(boolean value) {
        byteBuf.writeByte(value ? 1 : 0);
    }

    @Override
    public void writeString(String value) {
        writeShort((short) value.length());
        writeBytes(value.getBytes(STRING_CHARSET));
    }

    @Override
    public void writePoint(Point value) {
        writeShort((short) value.getX());
        writeShort((short) value.getY());
    }

    @Override
    public void skip(int numberOfBytes) {
        writeBytes(new byte[numberOfBytes]);
    }
}
