package org.gms.util;

/**
 * @author Shavit
 */
public class NumberTool {

    // Converts 8 bytes to a long.
    public static long BytesToLong(byte[] aToConvert) {
        if (aToConvert.length != Long.BYTES) {
            throw new IllegalArgumentException(String.format("Size of input should be %d", (Long.SIZE / 8)));
        }

        long nResult = 0;

        for (int i = 0; i < Long.BYTES; i++) {
            nResult <<= Byte.SIZE;
            nResult |= (aToConvert[i] & 0xFF);
        }

        return nResult;
    }

    // Converts a long to 8 bytes.
    public static byte[] LongToBytes(long nToConvert) {
        byte[] aBytes = new byte[Long.BYTES];

        for (int i = aBytes.length - 1; i >= 0; i--) {
            aBytes[i] = (byte) (nToConvert & 0xFF);
            nToConvert >>= Byte.SIZE;
        }

        return aBytes;
    }

    public static int floatToInt(float f) {
        return doubleToInt(f);
    }

    public static int doubleToInt(double d) {
        if (d > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) d;
    }
}
