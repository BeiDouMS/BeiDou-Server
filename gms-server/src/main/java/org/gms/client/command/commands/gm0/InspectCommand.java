package org.gms.client.command.commands.gm0;

import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.EquipmentAffixFormatter;
import org.gms.client.inventory.Inventory;
import org.gms.client.inventory.InventoryType;
import org.gms.client.inventory.Item;
import org.gms.server.ItemInformationProvider;
import org.gms.util.I18nUtil;

import java.util.Comparator;

public class InspectCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("InspectCommand.message1"));
    }

    @Override
    public void execute(Client client, String[] params) {
        if (params.length == 0) {
            listEquipment(client);
            return;
        }
        if (params.length != 1) {
            showUsage(client);
            return;
        }

        try {
            short slot = Short.parseShort(params[0]);
            if (slot <= 0) {
                showUsage(client);
                return;
            }

            var item = client.getPlayer().getInventory(InventoryType.EQUIP).getItem(slot);
            if (!(item instanceof Equip equip)) {
                client.getPlayer().dropMessage(5, I18nUtil.getMessage("InspectCommand.message3"));
                return;
            }
            client.getPlayer().dropMessage(5, EquipmentAffixFormatter.format(equip));
        } catch (NumberFormatException exception) {
            showUsage(client);
        }
    }

    private void listEquipment(Client client) {
        Inventory inventory = client.getPlayer().getInventory(InventoryType.EQUIP);
        client.getPlayer().dropMessage(5, I18nUtil.getMessage("InspectCommand.list.title"));
        inventory.list().stream()
                .filter(Equip.class::isInstance)
                .map(Equip.class::cast)
                .sorted(Comparator.comparingInt(Item::getPosition))
                .forEach(equip -> client.getPlayer().dropMessage(
                        5,
                        I18nUtil.getMessage(
                                "InspectCommand.list.item",
                                equip.getPosition(),
                                ItemInformationProvider.getInstance().getName(equip.getItemId()),
                                equip.getAffixes().size()
                        )
                ));
    }

    private void showUsage(Client client) {
        client.getPlayer().dropMessage(5, I18nUtil.getMessage("InspectCommand.message2"));
    }
}
