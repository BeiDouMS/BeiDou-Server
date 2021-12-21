/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.packet.logging;

import client.Character;
import client.Client;
import net.opcodes.RecvOpcode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.HexTool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Logs packets from monitored characters to a file.
 *
 * @author Alan (SharpAceX)
 */

public class MonitoredChrLogger {
    private static final Logger log = LoggerFactory.getLogger(MonitoredChrLogger.class);
    public static final Set<Integer> monitored = new HashSet<>();
    public static final Set<Integer> ignored = new HashSet<>();

    public static void logPacketIfMonitored(Client c, short packetId, byte[] packetContent) {
        Character chr = c.getPlayer();
        if (chr == null) {
            return;
        }
        if (!monitored.contains(chr.getId())) {
            return;
        }
        RecvOpcode op = getOpcodeFromValue(packetId);
        if (isRecvBlocked(op)) {
            return;
        }

        String packet = packetContent.length > 0 ? HexTool.toString(packetContent) : "<empty>";
        log.info("{}-{} {}-{}", c.getAccountName(), chr.getName(), packetId, packet);
    }

    private static boolean isRecvBlocked(RecvOpcode op) {
        return switch (op) {
            case MOVE_PLAYER, GENERAL_CHAT, TAKE_DAMAGE, MOVE_PET, MOVE_LIFE, NPC_ACTION, FACE_EXPRESSION -> true;
            default -> false;
        };
    }

    private static RecvOpcode getOpcodeFromValue(int value) {
        return Arrays.stream(RecvOpcode.values())
                .filter(opcode -> value == opcode.getValue())
                .findAny()
                .orElse(null);
    }
}
