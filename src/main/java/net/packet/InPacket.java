package net.packet;

import java.awt.*;

public interface InPacket extends Packet {
    byte readByte();
    short readShort();
    int readInt();
    long readLong();
    Point readPoint();
    String readString();
    byte[] readBytes(int bytesToRead);
    void skip(int numberOfBytes);
    int available();
    void seek(int byteOffset);
    int getPosition();
}
