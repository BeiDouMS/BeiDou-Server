var item;
var status = -1;
var item;

function end(mode, type, selection) {
    if (mode == 0) {
        qm.dispose();
        return;
    }
    status++;

    if (status == 0) {
        qm.sendNext("果然是你。我早就知道你很快可以完成~ 上次也是做得不错~ 真是了不起阿！作为谢礼题，我应该送你礼物。#b#p1051000##k送了你一双鞋，希望对的你冒险之旅有帮助，赶快收下吧。");
    } else if (status == 1) {
        const InventoryType = Java.type('org.gms.client.inventory.InventoryType');
        if (qm.getPlayer().getInventory(InventoryType.EQUIP).getNumFreeSlot() < 1) {
            qm.sendOk("请清理一下装备栏以获得奖励。");
            qm.dispose();
            return;
        }

        var stance = qm.getPlayer().getJobStyle();

        const Job = Java.type('org.gms.client.Job');
        if (stance == Job.WARRIOR) {
            item = 1072003;
        } else if (stance == Job.MAGICIAN) {
            item = 1072077;
        } else if (stance == Job.BOWMAN || stance == Job.CROSSBOWMAN) {
            item = 1072081;
        } else if (stance == Job.THIEF) {
            item = 1072035;
        } else if (stance == Job.BRAWLER || stance == Job.GUNSLINGER) {
            item = 1072294;
        } else {
            item = 1072018;
        }

        qm.gainItem(item, 1);
        qm.gainItem(4000007, -150);
        qm.gainExp(2200);
        qm.completeQuest();

        qm.sendOk("那么如果有一天你想帮助别人的话，就来找我吧。这里有很多人需要帮助啊~~");
    } else if (status == 2) {
        qm.dispose();
    }
}
