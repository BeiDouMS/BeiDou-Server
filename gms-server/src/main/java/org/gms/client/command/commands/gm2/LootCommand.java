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
   @Author: Resinate
*/
package org.gms.client.command.commands.gm2;

import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.util.I18nUtil;

import java.util.Arrays;
import java.util.List;

public class LootCommand extends Command {

    {
        setDescription(I18nUtil.getMessage("LootCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        List<MapObject> items = c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapObjectType.ITEM));
        for (MapObject item : items) {
            MapItem mapItem = (MapItem) item;
            if (mapItem.getOwnerId() == c.getPlayer().getId() || mapItem.getOwnerId() == c.getPlayer().getPartyId()) {
                c.getPlayer().pickupItem(mapItem);
            }
        }

    }
}
