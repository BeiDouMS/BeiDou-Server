package net.packet;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Packet {
    Charset STRING_CHARSET = StandardCharsets.US_ASCII;

    byte[] getBytes();
}
