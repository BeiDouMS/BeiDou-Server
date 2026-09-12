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
package org.gms.net.server.channel.handlers;

import org.gms.client.Client;
import org.gms.client.autoban.AutobanFactory;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.net.server.Server;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;

/**
 * @author Matze
 */
public final class ChangeChannelHandler extends AbstractPacketHandler {

    @Override
    public final void handlePacket(InPacket p, Client c) {
        int channel = p.readByte() + 1;
        p.readInt();
        // 关服进行中：换频道是「转场」路径（异步存档 + 从频道摘出），拒绝，等关服流程统一断线存档
        if (Server.getInstance().isShuttingDown()) {
            c.sendPacket(PacketCreator.serverNotice(1, I18nUtil.getMessage("Server.shuttingDown.message1")));
            c.sendPacket(PacketCreator.enableActions());
            return;
        }
        c.getPlayer().getAutoBanManager().setTimestamp(6, Server.getInstance().getCurrentTimestamp(), 3);
        if (c.getChannel() == channel) {
            AutobanFactory.GENERAL.alert(c.getPlayer(), "CCing to same channel.");
            c.disconnect(false, false);
            return;
        } else if (c.getPlayer().getCashShop().isOpened() || c.getPlayer().getMiniGame() != null || c.getPlayer().getPlayerShop() != null || c.getPlayer().getTrade() != null) {
            c.sendPacket(PacketCreator.enableActions());
            return;
        }

        c.changeChannel(channel);
    }
}