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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package org.gms.client.command.commands.gm3;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.constants.game.GameConstants;
import org.gms.constants.id.NpcId;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;

public class MusicCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("MusicCommand.message1"));
    }

    private static String getSongList() {
        StringBuilder songList = new StringBuilder(I18nUtil.getMessage("MusicCommand.message2")).append("\r\n");
        for (String s : GameConstants.GAME_SONGS) {
            songList.append("  ").append(s).append("\r\n");
        }

        return songList.toString();
    }

    @Override
    public void execute(Client c, String[] params) {

        Character player = c.getPlayer();
        if (params.length < 1) {
            String sendMsg = "";

            sendMsg += I18nUtil.getMessage("MusicCommand.message3") + "\r\n\r\n";
            sendMsg += getSongList();

            c.sendPacket(PacketCreator.getNPCTalk(NpcId.BILLY, (byte) 0, sendMsg, "00 00", (byte) 0));
            return;
        }

        String song = player.getLastCommandMessage();
        for (String s : GameConstants.GAME_SONGS) {
            if (s.equalsIgnoreCase(song)) {    // thanks Masterrulax for finding an issue here
                player.getMap().broadcastMessage(PacketCreator.musicChange(s));
                player.yellowMessage(I18nUtil.getMessage("MusicCommand.message4", s));
                return;
            }
        }

        String sendMsg = "";
        sendMsg += I18nUtil.getMessage("MusicCommand.message5") + "\r\n\r\n";
        sendMsg += getSongList();

        c.sendPacket(PacketCreator.getNPCTalk(NpcId.BILLY, (byte) 0, sendMsg, "00 00", (byte) 0));
    }
}
