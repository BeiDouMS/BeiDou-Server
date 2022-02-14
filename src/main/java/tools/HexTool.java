/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package tools;

import constants.string.CharsetConstants;
import io.netty.buffer.ByteBufUtil;

import java.util.HexFormat;

// TODO: use HexFormat from Java 17
public class HexTool {

    /**
     * Convert a byte array to its hex string representation.
     * Each byte value is equivalent to two hex characters delimited by a space.
     *
     * @param bytes Byte array to convert to a hex string.
     *              Example: {1, 16, 127, -1} is converted to "01 F0 7F FF"
     * @return The hex string
     */
    public static String toString(byte[] bytes) {
        return HexFormat.ofDelimiter(" ").withUpperCase().formatHex(bytes);
    }

    /**
     * Convert a hex string to its byte array representation. Two consecutive hex characters are equivalent to one byte.
     *
     * @param hexString Hex string to convert to bytes. Hex character pairs may be delimited by a space or not (compact)
     *                  Example: "01 10 7F FF" is converted to {1, 16, 127, -1}.
     * @return The byte array
     */
    public static byte[] toBytes(String hexString) {
        return HexFormat.of().parseHex(removeAllSpaces(hexString));
    }

    private static String removeAllSpaces(String input) {
        return input.replaceAll("\\s", "");
    }

    public static String toStringFromAscii(final byte[] bytes) {
        byte[] ret = new byte[bytes.length];
        for (int x = 0; x < bytes.length; x++) {
            if (bytes[x] < 32 && bytes[x] >= 0) {
                ret[x] = '.';
            } else {
                int chr = ((short) bytes[x]) & 0xFF;
                ret[x] = (byte) chr;
            }
        }

        return new String(ret, CharsetConstants.CHARSET);
    }

    /**
     * Get upper case hex dump
     */
    public static String bytesToHex(byte[] bytes) {
        return ByteBufUtil.hexDump(bytes).toUpperCase();
    }

    public static byte[] hexToBytes(String hex) {
        return ByteBufUtil.decodeHexDump(hex);
    }
}
