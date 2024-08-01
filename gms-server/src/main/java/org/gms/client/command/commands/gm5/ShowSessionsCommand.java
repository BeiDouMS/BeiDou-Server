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
package org.gms.client.command.commands.gm5;

import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.net.server.coordinator.session.SessionCoordinator;
import org.gms.util.I18nUtil;

/**
 * @author Ronan
 */
public class ShowSessionsCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("ShowSessionsCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        SessionCoordinator.getInstance().printSessionTrace(c);
    }
}
