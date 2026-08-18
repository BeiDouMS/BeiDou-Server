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
package org.gms.client;

import io.netty.handler.timeout.IdleStateEvent;
import org.gms.net.packet.Packet;

/**
 * Shared headless Client for SoloMapling bots (no Netty socket).
 * Credits to NutNNut for the headless client pattern (c) 2026.
 */
public class BotClient extends Client {

    public BotClient(int world, int channel) {
        // Type/session/processor unused for bots; world+channel drive routing.
        super(Type.CHANNEL, -1, "bot", null, world, channel);
    }

    @Override
    public void sendPacket(Packet packet) {
        // no socket
    }

    @Override
    public boolean isLoggedIn() {
        return true;
    }

    @Override
    public void updateLoginState(int newState) {
        // headless: no account row, no session registration
    }

    @Override
    public void disconnectSession() {
        // no socket
    }

    @Override
    public void closeSession() {
        // no socket
    }

    @Override
    public void checkIfIdle(final IdleStateEvent event) {
        // never reap the shared bot client
    }

    @Override
    public long getLastPacket() {
        return System.currentTimeMillis();
    }

    /** SoloMapling artificial player id range (see BotHelpers in plugin). */
    public static boolean isBot(Character chr) {
        if (chr == null) {
            return false;
        }
        int id = chr.getId();
        return id > 20_000 || id == 999;
    }
}
