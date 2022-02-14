package tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HexToolTest {

    @Test
    void bytesToHexString() {
        byte[] bytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 127, -1};
        String expectedHexString = "01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 7F FF";
        assertEquals(expectedHexString, HexTool.toString(bytes));
    }

    @Test
    void hexStringWithSpacesToBytes() {
        String hexString = "01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10 11 7F FF";
        byte[] expectedBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 127, -1};
        assertArrayEquals(expectedBytes, HexTool.toBytes(hexString));
    }

    @Test
    void compactHexStringToBytes() {
        String hexString = "0102030405060708090A0B0C0D0E0F10117FFF";
        byte[] expectedBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 127, -1};
        assertArrayEquals(expectedBytes, HexTool.toBytes(hexString));
    }

    @Test
    void upperCaseHexToBytesAndBack() {
        String hex = "A1B2C3";
        byte[] bytes = HexTool.hexToBytes(hex);
        assertEquals(hex, HexTool.bytesToHex(bytes));
    }

    @Test
    void mixedCaseHexToBytesAndBack() {
        String hex = "aB5DaA";
        byte[] bytes = HexTool.hexToBytes(hex);
        assertEquals(hex.toUpperCase(), HexTool.bytesToHex(bytes));
    }
}