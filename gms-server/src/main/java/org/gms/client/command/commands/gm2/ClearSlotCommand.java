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
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.constants.game.GameConstants;
import org.gms.util.I18nUtil;

public class ClearSlotCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("ClearSlotCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("ClearSlotCommand.message2"));
            return;
        }
        String type = params[0];
        switch (type) {
            case "all":
            case "全部":
                for (int i = 0; i < GameConstants.MAX_CLEAN_PACK_SIZE; i++) {
                    removeSlot(c, InventoryType.EQUIP, i);
                    removeSlot(c, InventoryType.USE, i);
                    removeSlot(c, InventoryType.ETC, i);
                    removeSlot(c, InventoryType.SETUP, i);
                    removeSlot(c, InventoryType.CASH, i);
                }
                player.yellowMessage(I18nUtil.getMessage("ClearSlotCommand.message3"));
                break;
            case "equip":
            case "装备":
                for (int i = 0; i < GameConstants.MAX_CLEAN_PACK_SIZE; i++) {
                    removeSlot(c, InventoryType.EQUIP, i);
                }
                player.yellowMessage(I18nUtil.getMessage("ClearSlotCommand.message4"));
                break;
            case "use":
            case "消耗":
                for (int i = 0; i < GameConstants.MAX_CLEAN_PACK_SIZE; i++) {
                    removeSlot(c, InventoryType.USE, i);
                }
                player.yellowMessage(I18nUtil.getMessage("ClearSlotCommand.message5"));
                break;
            case "setup":
            case "设置":
                for (int i = 0; i < GameConstants.MAX_CLEAN_PACK_SIZE; i++) {
                    removeSlot(c, InventoryType.SETUP, i);
                }
                player.yellowMessage(I18nUtil.getMessage("ClearSlotCommand.message6"));
                break;
            case "etc":
            case "其他":
                for (int i = 0; i < GameConstants.MAX_CLEAN_PACK_SIZE; i++) {
                    removeSlot(c, InventoryType.ETC, i);
                }
                player.yellowMessage(I18nUtil.getMessage("ClearSlotCommand.message7"));
                break;
            case "cash":
            case "现金":
                for (int i = 0; i < GameConstants.MAX_CLEAN_PACK_SIZE; i++) {
                    removeSlot(c, InventoryType.CASH, i);
                }
                player.yellowMessage(I18nUtil.getMessage("ClearSlotCommand.message8"));
                break;
            default:
                player.yellowMessage(I18nUtil.getMessage("ClearSlotCommand.message9", type));
                break;
        }
    }

    private void removeSlot(Client c, InventoryType type, int slot) {
        Item tempItem = c.getPlayer().getInventory(type).getItem((byte) slot);
        if (tempItem == null) {
            return;
        }
        InventoryManipulator.removeFromSlot(c, type, (byte) slot, tempItem.getQuantity(), false, false);
    }
}
