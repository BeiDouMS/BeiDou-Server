package net.packet;

import io.netty.buffer.ByteBuf;

import java.awt.*;

public class ByteBufInPacket implements InPacket {
    private final ByteBuf byteBuf;

    public ByteBufInPacket(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public short getHeader() {
        return byteBuf.getShortLE(0);
    }

    @Override
    public byte[] getBytes() {
        // TODO implement
        throw new UnsupportedOperationException();
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
    public Point readPoint() {
        final short x = byteBuf.readShortLE();
        final short y = byteBuf.readShortLE();
        return new Point(x, y);
    }

    @Override
    public String readString() {
        // TODO
        return null;
    }

    @Override
    public byte[] readBytes(int bytesToRead) {
        byte[] bytes = new byte[bytesToRead];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    @Override
    public void skip(int bytesToSkip) {
        byteBuf.skipBytes(bytesToSkip);
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
