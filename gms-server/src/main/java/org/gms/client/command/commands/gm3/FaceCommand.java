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
import org.gms.client.Stat;
import org.gms.client.command.Command;
import org.gms.constants.inventory.ItemConstants;
import org.gms.server.ItemInformationProvider;
import org.gms.util.I18nUtil;
import org.gms.util.StringUtil;

public class FaceCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("FaceCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("FaceCommand.message2"));
            return;
        }

        try {
            if (params.length == 1) {
                int itemId = Integer.parseInt(params[0]);
                if (!ItemConstants.isFace(itemId) || ItemInformationProvider.getInstance().getName(itemId) == null) {
                    player.yellowMessage(I18nUtil.getMessage("FaceCommand.message3", params[0]));
                    return;
                }

                player.setFace(itemId);
                player.updateSingleStat(Stat.FACE, itemId);
                player.equipChanged();
            } else {
                int itemId = Integer.parseInt(params[1]);
                if (!ItemConstants.isFace(itemId) || ItemInformationProvider.getInstance().getName(itemId) == null) {
                    player.yellowMessage(I18nUtil.getMessage("FaceCommand.message3", params[1]));
                }

                Character victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
                if (victim == null && StringUtil.isNumeric(params[0])) {
                    victim = c.getWorldServer().getPlayerStorage().getCharacterById(Integer.parseInt(params[0]));
                }
                if (victim != null) {
                    victim.setFace(itemId);
                    victim.updateSingleStat(Stat.FACE, itemId);
                    victim.equipChanged();
                } else {
                    player.message(I18nUtil.getMessage("BombCommand.message3", params[0]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
