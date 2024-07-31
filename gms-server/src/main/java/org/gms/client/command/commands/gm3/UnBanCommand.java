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
import org.gms.util.DatabaseConnection;
import org.gms.util.I18nUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UnBanCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("UnBanCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("UnBanCommand.message2"));
            return;
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            int aid = Character.getAccountIdByName(params[0]);

            try (PreparedStatement p = con.prepareStatement("UPDATE accounts SET banned = -1 WHERE id = " + aid)) {
                p.executeUpdate();
            }

            try (PreparedStatement p = con.prepareStatement("DELETE FROM ipbans WHERE aid = " + aid)) {
                p.executeUpdate();
            }

            try (PreparedStatement p = con.prepareStatement("DELETE FROM macbans WHERE aid = " + aid)) {
                p.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.message(I18nUtil.getMessage("UnBanCommand.message3", params[0]));
            return;
        }
        player.message(I18nUtil.getMessage("UnBanCommand.message4", params[0]));
    }
}
