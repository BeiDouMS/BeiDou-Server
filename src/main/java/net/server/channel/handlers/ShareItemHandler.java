package net.server.channel.handlers;

import client.Character;
import client.Client;
import client.inventory.InventoryType;
import client.inventory.Item;
import net.AbstractPacketHandler;
import net.packet.InPacket;
import net.server.Server;
import server.ItemInformationProvider;
import tools.PacketCreator;

/**
 * @author lee
 */
public class ShareItemHandler extends AbstractPacketHandler {

    @Override
    public void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();
        byte type = p.readByte();
        short pos = p.readShort();
        
        if (Server.getInstance().getCurrentTime() - chr.getLastShareTime() < 15000 ) {
            chr.message("请不要频繁分享。");
            return;
        }
        chr.setLastShareTime(Server.getInstance().getCurrentTime());

        InventoryType it = InventoryType.getByType(type);
        if (it != null) {
            Item item = chr.getInventory(it).getItem(pos);
            if (item != null) {
                String medal = "";
                Item medalItem = chr.getInventory(InventoryType.EQUIPPED).getItem((short) -49);
                if (medalItem != null) {
                    ItemInformationProvider ii = ItemInformationProvider.getInstance();
                    medal = "<" + ii.getName(medalItem.getItemId()) + "> ";
                }
                String msg = medal + chr.getName() + " : （点击查看）";
                chr.getMap().broadcastMessage(PacketCreator.itemMegaphone(msg, true, c.getChannel(), item));
            }
        }
    }
}
