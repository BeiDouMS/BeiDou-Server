package net.packet.logging;

import io.netty.buffer.Unpooled;

import java.util.Set;

public class LoggingUtil {
    private static final Set<Short> ignoredDebugRecvPackets = Set.of((short) 167, (short) 197, (short) 89, (short) 91, (short) 41, (short) 188, (short) 107);

    public static short readFirstShort(byte[] bytes) {
        return Unpooled.wrappedBuffer(bytes).readShortLE();
    }

    public static boolean isIgnoredRecvPacket(short opcode) {
        return ignoredDebugRecvPackets.contains(opcode);
    }
}
