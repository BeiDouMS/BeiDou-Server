package tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HexToolTest {

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