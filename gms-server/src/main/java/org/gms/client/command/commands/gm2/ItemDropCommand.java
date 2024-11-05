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
package org.gms.client.command.commands.gm2;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.client.inventory.Pet;
import org.gms.config.GameConfig;
import org.gms.constants.inventory.ItemConstants;
import org.gms.server.ItemInformationProvider;
import org.gms.util.I18nUtil;

import static java.util.concurrent.TimeUnit.DAYS;

public class ItemDropCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("ItemDropCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("ItemDropCommand.message2"));
            return;
        }

        int itemId = Integer.parseInt(params[0]);
        ItemInformationProvider ii = ItemInformationProvider.getInstance();

        if (ii.getName(itemId) == null) {
            player.yellowMessage(I18nUtil.getMessage("ItemCommand.message3", params[0]));
            return;
        }

        short quantity = 1;
        if (params.length >= 2) {
            quantity = Short.parseShort(params[1]);
        }

        if (GameConfig.getServerBoolean("block_generate_cash_item") && ii.isCash(itemId)) {
            player.yellowMessage(I18nUtil.getMessage("ItemCommand.message4"));
            return;
        }

        if (ItemConstants.isPet(itemId)) {
            if (params.length >= 2) {   // thanks to istreety & TacoBell
                quantity = 1;
                long days = Math.max(1, Integer.parseInt(params[1]));
                long expiration = System.currentTimeMillis() + DAYS.toMillis(days);
                int petid = Pet.createPet(itemId);

                Item toDrop = new Item(itemId, (short) 0, quantity, petid);
                toDrop.setExpiration(expiration);

                toDrop.setOwner("");
                if (player.gmLevel() < 3) {
                    short f = toDrop.getFlag();
                    f |= ItemConstants.ACCOUNT_SHARING;
                    f |= ItemConstants.UNTRADEABLE;
                    f |= ItemConstants.SANDBOX;

                    toDrop.setFlag(f);
                    toDrop.setOwner("TRIAL-MODE");
                }

                c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);

                return;
            } else {
                player.yellowMessage(I18nUtil.getMessage("ItemDropCommand.message3"));
                return;
            }
        }

        Item toDrop;
        if (ItemConstants.getInventoryType(itemId) == InventoryType.EQUIP) {
            toDrop = ii.getEquipById(itemId);
        } else {
            toDrop = new Item(itemId, (short) 0, quantity);
        }

        toDrop.setOwner(player.getName());
        if (player.gmLevel() < 3) {
            short f = toDrop.getFlag();
            f |= ItemConstants.ACCOUNT_SHARING;
            f |= ItemConstants.UNTRADEABLE;
            f |= ItemConstants.SANDBOX;

            toDrop.setFlag(f);
            toDrop.setOwner("TRIAL-MODE");
        }

        c.getPlayer().getMap().spawnItemDrop(c.getPlayer(), c.getPlayer(), toDrop, c.getPlayer().getPosition(), true, true);
    }
}
