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
            qm.sendSimple("墙上写了什么？\r\n#L0##b如果我有一把铁锤和一把匕首，一把弓和一支箭……#l\r\n#L1#拜伦爱西琳#l\r\n#L2#啊，我忘了。");
            }else{
            qm.sendOk("我猜你还没有找到#p2103001#。无论如何，想找到沙子图团可没那么容易……");
            qm.dispose();
            }
        } else if (status == 1) {
            if (selection == 0) {
                qm.sendSimple("你找到墙了吗？\r\n#L0##b找到了，但是……我不知道它在说什么。#l");
            }else{
                qm.sendOk("我猜你还没有找到#p2103001#。无论如何，想找到沙子图团可没那么容易……");
                qm.dispose();
            }
        } else if (status == 2) {
            if (selection == 0) {
            qm.sendOk("如果我有一把铁锤和一把匕首……一张弓和一支箭……这是什么意思？你想让我告诉你吗？其实我也不知道。这得靠你自己思考。如果一定要线索的话……武器本身只是物品，只有被人拿起时才有意义。");
            }
            qm.gainExp(28843);
            qm.forceCompleteQuest();
            qm.dispose();
        }
         }
}
