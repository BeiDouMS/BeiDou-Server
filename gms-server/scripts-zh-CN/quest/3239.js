var status = -1;

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (qm.haveItem(4031092, 10)) {
                const InventoryType = Java.type('org.gms.client.inventory.InventoryType');
                if (qm.getPlayer().getInventory(InventoryType.USE).getNumFreeSlot() >= 1) {
                    qm.sendOk("干得好！你带回了所有丢失的#t4031092#。在这里，拿着这卷轴作为我的谢意……");
                } else {
                    qm.sendOk("在领取奖品之前，请确保消耗栏有一个空位.");
                    qm.dispose();

                }
            } else {
                qm.sendOk("请归还我丢失在这个房间的10个#t4031092#.");
                qm.dispose();

            }
        } else if (status == 1) {
            qm.gainItem(4031092, -10);

            rnd = Math.floor(Math.random() * 4);
            if (rnd == 0) {
                qm.gainItem(2040704, 1);
            } else if (rnd == 1) {
                qm.gainItem(2040705, 1);
            } else if (rnd == 2) {
                qm.gainItem(2040707, 1);
            } else {
                qm.gainItem(2040708, 1);
            }

            qm.gainExp(2700);
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}
