package org.gms.client.command.commands.gm0;

import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.EquipmentAffix;
import org.gms.client.inventory.InventoryType;
import org.gms.util.I18nUtil;

public class LockAffixCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("LockAffixCommand.message1"));
    }

    @Override
    public void execute(Client client, String[] params) {
        if (params.length != 2) {
            client.getPlayer().dropMessage(5, I18nUtil.getMessage("LockAffixCommand.message2"));
            return;
        }
        try {
            short slot = Short.parseShort(params[0]);
            int affixSlot = Integer.parseInt(params[1]);
            if (!(client.getPlayer().getInventory(InventoryType.EQUIP).getItem(slot) instanceof Equip equip)
                    || affixSlot < 0 || affixSlot >= equip.getAffixes().size()) {
                client.getPlayer().dropMessage(5, I18nUtil.getMessage("LockAffixCommand.message3"));
                return;
            }
            EquipmentAffix affix = equip.getAffixes().get(affixSlot);
            affix.setLocked(!affix.isLocked());
            client.getPlayer().forceUpdateItem(equip);
            client.getPlayer().dropMessage(5, I18nUtil.getMessage(
                    affix.isLocked() ? "LockAffixCommand.message4" : "LockAffixCommand.message5",
                    affixSlot
            ));
        } catch (NumberFormatException exception) {
            client.getPlayer().dropMessage(5, I18nUtil.getMessage("LockAffixCommand.message2"));
        }
    }
}
