var item;
var stance;
var status = -1;
var vecItem;

function end(mode, type, selection) {
    if (mode == 0) {
        qm.dispose();
        return;
    }
    status++;

    if (status == 0) {
        qm.sendNext("呵呵呵…！就是这个！只要有这个样本，现在正在地球防卫总部进行的研究，就更加活跃了！不过，没想到还会有比我更出色的人啊…我还需要更努力呀！不管怎么说，这样帮了我大忙，应该给点酬劳才行。");
    } else if (status == 1) {
        const InventoryType = Java.type('org.gms.client.inventory.InventoryType');
        if (qm.getPlayer().getInventory(InventoryType.USE).getNumFreeSlot() < 1) {
            qm.getPlayer().dropMessage(1, "USE inventory full.");
            qm.dispose();
            return;
        }

        var talkStr = "来…请选择感兴趣的卷轴吧！成功的机率都是10%。\r\n\r\n#r选择卷轴\r\n#b"
        stance = qm.getPlayer().getJobStyle();

        const Job = Java.type('org.gms.client.Job');
        if (stance == Job.WARRIOR || stance == Job.BEGINNER) {
            vecItem = [2043002, 2043102, 2043202, 2044002, 2044102, 2044202, 2044402, 2044302];
        } else if (stance == Job.MAGICIAN) {
            vecItem = [2043702, 2043802];
        } else if (stance == Job.BOWMAN || stance == Job.CROSSBOWMAN) {
            vecItem = [2044502, 2044602];
        } else if (stance == Job.THIEF) {
            vecItem = [2043302, 2044702];
        } else {
            vecItem = [2044802, 2044902];
        }

        for (var i = 0; i < vecItem.length; i++) {
            talkStr += "\r\n#L" + i + "# #i" + vecItem[i] + "# #t" + vecItem[i] + "#";
        }
        qm.sendSimple(talkStr);
    } else if (status == 2) {
        item = vecItem[selection];
        qm.gainItem(item, 1);
        qm.gainItem(4031103, -1);
        qm.gainItem(4031104, -1);
        qm.gainItem(4031105, -1);
        qm.gainItem(4031106, -1);
        qm.gainExp(12000);
        qm.completeQuest();

        qm.dispose();
    }
}
