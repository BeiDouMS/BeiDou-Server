package org.gms.client.command.commands.gm0;

import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.client.inventory.Equip;
import org.gms.client.inventory.EquipmentAffix;
import org.gms.client.inventory.EquipmentAffixGenerator;
import org.gms.client.inventory.InventoryType;
import org.gms.util.I18nUtil;

public class RerollAffixCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("RerollAffixCommand.message1"));
    }

    @Override
    public void execute(Client client, String[] params) {
        if (params.length != 1) {
            client.getPlayer().dropMessage(5, I18nUtil.getMessage("RerollAffixCommand.message2"));
            return;
        }
        try {
            short slot = Short.parseShort(params[0]);
            if (!(client.getPlayer().getInventory(InventoryType.EQUIP).getItem(slot) instanceof Equip equip)) {
                client.getPlayer().dropMessage(5, I18nUtil.getMessage("RerollAffixCommand.message3"));
                return;
            }
            int cost = calculateRerollCost(equip);
            if (client.getPlayer().getMeso() < cost) {
                client.getPlayer().dropMessage(5, I18nUtil.getMessage("RerollAffixCommand.message4", cost));
                return;
            }
            client.getPlayer().gainMeso(-cost, true, false, true);
            EquipmentAffixGenerator.reroll(equip, true);
            client.getPlayer().forceUpdateItem(equip);
            client.getPlayer().dropMessage(5, I18nUtil.getMessage("RerollAffixCommand.message5", cost));
        } catch (NumberFormatException exception) {
            client.getPlayer().dropMessage(5, I18nUtil.getMessage("RerollAffixCommand.message2"));
        }
    }

    private int calculateRerollCost(Equip equip) {
        double rarityMultiplier = Math.pow(1.8, Math.max(0, equip.getRarity()));
        long lockedCount = equip.getAffixes().stream().filter(EquipmentAffix::isLocked).count();
        double lockMultiplier = Math.pow(1.6, lockedCount);
        double multiplier = rarityMultiplier * lockMultiplier;
        return (int) (Math.round(10_000 * multiplier / 1_000) * 1_000);
    }
}
