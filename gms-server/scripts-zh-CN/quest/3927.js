var status = -1;
var firstSelection;
function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
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
            if(qm.getQuestProgress(3927) == "1"){
            qm.sendSimple("墙壁说什么了？\r\n#L0##b如果我有一把铁锤和一把匕首，一把弓和一支箭...#l\r\n#L1# 拜伦爱西林 #l\r\n#L2# 啊，我忘了。");
            }else{
            qm.sendOk("我猜你还没有找到#p2103001#。无论如何，要找到沙盗对你来说都会很困难...");
            qm.dispose();
            }
        } else if (status == 1) {
            if (selection == 0) {
                qm.sendSimple("你找到墙了吗？\r\n#L0##b我有，但是…我不知道它在说什么。#l");
            }else{
                qm.sendOk("我猜你还没有找到#p2103001#。无论如何，要找到沙盗对你来说都会很困难...");
                qm.dispose();
            }
        } else if (status == 2) {
            if (selection == 0) {
            qm.sendOk("如果我有一把铁锤和一把匕首... 一支弓箭... 这是什么意思？你想让我告诉你吗？我自己也不知道。这是你应该考虑的事情。如果你需要线索... 它会是这样的... 一件武器只是一件物品... 直到有人使用它?");
            }
            qm.gainExp(28843);
            qm.forceCompleteQuest();
            qm.dispose();
        }
         }
}
