/*
    This file is part of the HeavenMS MapleStory Server
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
package org.gms.client.command.commands.gm4;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;

/**
 * @author Ronan
 */
public class BossDropRateCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("BossDropRateCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("BossDropRateCommand.message2"));
            return;
        }

        float bossDrop = Math.max(Float.parseFloat(params[0]), 1F);
        c.getWorldServer().setBossDropRate(bossDrop);
        c.getWorldServer().broadcastPacket(PacketCreator.serverNotice(6, I18nUtil.getMessage("BossDropRateCommand.message3", bossDrop)));
    }
}
