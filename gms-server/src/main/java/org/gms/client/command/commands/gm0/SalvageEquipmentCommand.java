package org.gms.client.command.commands.gm0;

import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.EquipmentValueCalculator;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.manipulator.InventoryManipulator;
import org.gms.util.I18nUtil;

public class SalvageEquipmentCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("SalvageEquipmentCommand.message1"));
    }

    @Override
    public void execute(Client client, String[] params) {
        if (params.length != 1) {
            client.getPlayer().dropMessage(5, I18nUtil.getMessage("SalvageEquipmentCommand.message2"));
            return;
        }
        try {
            short slot = Short.parseShort(params[0]);
            if (!(client.getPlayer().getInventory(InventoryType.EQUIP).getItem(slot) instanceof Equip equip)) {
                client.getPlayer().dropMessage(5, I18nUtil.getMessage("SalvageEquipmentCommand.message3"));
                return;
            }
            int reward = EquipmentValueCalculator.getSalvagePrice(equip);
            InventoryManipulator.removeFromSlot(client, InventoryType.EQUIP, slot, (short) 1, false);
            client.getPlayer().gainMeso(reward, true, false, true);
            client.getPlayer().dropMessage(5, I18nUtil.getMessage("SalvageEquipmentCommand.message4", reward));
        } catch (NumberFormatException exception) {
            client.getPlayer().dropMessage(5, I18nUtil.getMessage("SalvageEquipmentCommand.message2"));
        }
    }
}
