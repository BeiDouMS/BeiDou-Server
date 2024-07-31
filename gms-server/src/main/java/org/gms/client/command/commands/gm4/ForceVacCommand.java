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
package org.gms.client.command.commands.gm4;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.client.inventory.Pet;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.constants.id.ItemId;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.util.I18nUtil;
import org.gms.util.PacketCreator;

import java.util.Arrays;
import java.util.List;

public class ForceVacCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("ForceVacCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        List<MapObject> items = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, List.of(MapObjectType.ITEM));
        for (MapObject item : items) {
            MapItem mapItem = (MapItem) item;

            mapItem.lockItem();
            try {
                if (mapItem.isPickedUp()) {
                    continue;
                }

                if (mapItem.getMeso() > 0) {
                    player.gainMeso(mapItem.getMeso(), true);
                } else if (player.applyConsumeOnPickup(mapItem.getItemId())) {    // thanks Vcoc for pointing out consumables on pickup not being processed here
                } else if (ItemId.isNxCard(mapItem.getItemId())) {
                    // Add NX to account, show effect and make item disappear
                    player.getCashShop().gainCash(1, mapItem.getItemId() == ItemId.NX_CARD_100 ? 100 : 250);
                } else if (mapItem.getItem().getItemId() >= 5000000 && mapItem.getItem().getItemId() <= 5000100) {
                    int petId = Pet.createPet(mapItem.getItem().getItemId());
                    if (petId == -1) {
                        continue;
                    }
                    InventoryManipulator.addById(c, mapItem.getItem().getItemId(), mapItem.getItem().getQuantity(), null, petId);
                } else if (InventoryManipulator.addFromDrop(c, mapItem.getItem(), true)) {
                    if (mapItem.getItemId() == ItemId.NX_CARD_100) {
                        player.updateAriantScore();
                    }
                }

                player.getMap().pickItemDrop(PacketCreator.removeItemFromMap(mapItem.getObjectId(), 2, player.getId()), mapItem);
            } finally {
                mapItem.unlockItem();
            }
        }
    }
}
