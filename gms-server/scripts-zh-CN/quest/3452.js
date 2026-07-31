var status = -1;

function end(mode, type, selection) {
    status++;
    if (mode != 1) {
        qm.dispose();
    } else {
        if (status == 0) {
            qm.sendNext("请收下这些#b魔力药水#k，就当作我的一点谢礼。");
        } else if (status == 1) {
            const InventoryType = Java.type('org.gms.client.inventory.InventoryType');
            if (qm.getPlayer().getInventory(InventoryType.USE).getNumFreeSlot() >= 1) {
                qm.gainItem(4000099, -1);
                qm.gainItem(2000011, 50);
                qm.gainExp(8000);
                qm.forceCompleteQuest();
                qm.dispose();
            } else {
                qm.sendNext("嗯？看起来你的背包已经满了。");
            }
        } else if (status == 2) {
            qm.dispose();
        }
    }
}

