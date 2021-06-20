package net.packet;

public interface Packet {
    short getHeader();
    byte[] getBytes();
}
