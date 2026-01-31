/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

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
package org.gms.constants.net;

import org.gms.net.opcodes.Opcode;
import org.gms.net.opcodes.RecvOpcode;
import org.gms.net.opcodes.SendOpcode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ronan
 */
public class OpcodeConstants {
    public static Map<Integer, String> sendOpcodeNames = new HashMap<>();
    public static Map<Integer, String> recvOpcodeNames = new HashMap<>();

    public static void generateOpcodeNames() {
        switch (ServerConstants.VERSION) {
            case 83  -> init(SendOpcode.values(), RecvOpcode.values());
            default  -> throw new RuntimeException("不支援的版本: " + ServerConstants.VERSION);
        }
    }

    public static void init(Opcode[] sendValues, Opcode[] recvValues) {
        for (Opcode op : sendValues) {
            sendOpcodeNames.put(op.getValue(), op.getName());
        }

        for (Opcode op : recvValues) {
            recvOpcodeNames.put(op.getValue(), op.getName());
        }
    }
}
