var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            const InventoryType = Java.type('org.gms.client.inventory.InventoryType');
            if (cm.getPlayer().getInventory(InventoryType.ETC).getNumFreeSlot() < 1) {
                cm.sendNext("检查你的杂项物品栏是否有可用的空位。");
                cm.dispose();
                return;
            }

            const PacketCreator = Java.type('org.gms.util.PacketCreator');
            cm.getClient().sendPacket(PacketCreator.openRPSNPC());
            cm.dispose();
        }
    }
}