package net.packet;

import java.awt.*;

public interface InPacket extends Packet {
    byte readByte();
    short readShort();
    int readInt();
    long readLong();
    Point readPoint();
    String readString();
    byte[] read(int numberOfBytes);
    void skip(int numberOfBytes);
    int available();
    void seek(int byteOffset);
    int getPosition();
}
