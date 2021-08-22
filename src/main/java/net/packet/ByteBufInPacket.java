package net.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.awt.*;

public class ByteBufInPacket implements InPacket {
    private final ByteBuf byteBuf;

    public ByteBufInPacket(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public byte[] getBytes() {
        return ByteBufUtil.getBytes(byteBuf);
    }

    @Override
    public byte readByte() {
        return byteBuf.readByte();
    }

    @Override
    public short readShort() {
        return byteBuf.readShortLE();
    }

    @Override
    public int readInt() {
        return byteBuf.readIntLE();
    }

    @Override
    public long readLong() {
        return byteBuf.readLongLE();
    }

    @Override
    public Point readPos() {
        final short x = byteBuf.readShortLE();
        final short y = byteBuf.readShortLE();
        return new Point(x, y);
    }

    @Override
    public String readString() {
        short length = readShort();
        byte[] stringBytes = new byte[length];
        byteBuf.readBytes(stringBytes);
        return new String(stringBytes, STRING_CHARSET);
    }

    @Override
    public byte[] readBytes(int numberOfBytes) {
        byte[] bytes = new byte[numberOfBytes];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public void skip(int numberOfBytes) {
        byteBuf.skipBytes(numberOfBytes);
    }

    @Override
    public int available() {
        return byteBuf.readableBytes();
    }

    @Override
    public void seek(int byteOffset) {
        byteBuf.readerIndex(byteOffset);
    }

    @Override
    public int getPosition() {
        return byteBuf.readerIndex();
    }
}
