package net.packet;

import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface OutPacket extends Packet {
    Charset STRING_CHARSET = StandardCharsets.US_ASCII;

    void writeByte(byte value);
    void writeByte(int value);
    void writeBytes(byte[] value);
    void writeShort(short value);
    void writeInt(int value);
    void writeLong(long value);
    void writeBoolean(boolean value);
    void writeString(String value);
    void writePoint(Point value);
    void skip(int numberOfBytes);
}
