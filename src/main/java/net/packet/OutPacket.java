package net.packet;

import net.opcodes.SendOpcode;

import java.awt.*;

public interface OutPacket extends Packet {
    void writeByte(byte value);
    void writeByte(int value);
    void writeBytes(byte[] value);
    void writeShort(int value);
    void writeInt(int value);
    void writeLong(long value);
    void writeBool(boolean value);
    void writeString(String value);
    void writePoint(Point value);
    void skip(int numberOfBytes);

    static OutPacket create(SendOpcode opcode) {
        return new ByteBufOutPacket(opcode);
    }
}
