var item;
var stance;
var status = -1;
var item;

function end(mode, type, selection) {
    if (mode == 0) {
        qm.dispose();
        return;
    }
    status++;

    if (status == 0) {
        qm.sendNext("什么？你是在告诉我你已经消灭了150只 #o4230120#？而且这些... 是的，这确实是120个 #t4000122#。我一直在想你是怎么一个人完成这个任务的，但你做得很好。好吧，这是一个对我来说非常重要的物品，但请收下它。");
    } else if (status == 1) {
        const InventoryType = Java.type('org.gms.client.inventory.InventoryType');
        if (qm.getPlayer().getInventory(InventoryType.EQUIP).getNumFreeSlot() < 1) {
            qm.sendOk("请在装备栏中腾出一个空位来领取奖励。");
            qm.dispose();
            return;
        }

        var talkStr = "你喜欢这个手套吗？我已经保存了一段时间，本来打算有一天用它，但看起来你戴起来更好看。请好好利用它；此外，我从部门那里得到了很多东西，我不再需要它了。";
        stance = qm.getPlayer().getJobStyle();

        const Job = Java.type('org.gms.client.Job');
        if (stance == Job.WARRIOR) {
            item = 1082024;
        } else if (stance == Job.MAGICIAN) {
            item = 1082063;
        } else if (stance == Job.BOWMAN || stance == Job.CROSSBOWMAN) {
            item = 1082072;
        } else if (stance == Job.THIEF) {
            item = 1082076;
        } else if (stance == Job.BRAWLER || stance == Job.GUNSLINGER) {
            item = 1082195;
        } else {
            item = 1082149;
        }

        qm.sendNext(talkStr);
    } else if (status == 2) {
        qm.completeQuest();
        qm.gainItem(item, 1);
        qm.gainItem(4000122, -120);
        qm.gainExp(6100);
        qm.sendOk("非常感谢你作为Mesorangers之一完成任务。我已经告诉部门你成功的故事，部门似乎对你也很满意。希望你继续和我们合作。再见~");
    } else if (status == 3) {
        qm.dispose();
    }
}

