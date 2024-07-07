/*
    Author: BubblesDev
    Quest: Abel Glasses Quest
*/

var status = -1;    // thanks IxianMace for noticing missing status declaration

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
            if (!qm.isQuestCompleted(2186)) {
                if (qm.haveItem(4031853)) {
                    if (qm.canHold(2030019)) {
                        qm.gainItem(4031853, -1);
                        qm.gainExp(1700);
                        qm.gainItem(2030019, 10);

                        qm.sendOk("你找到了我的眼镜，太感谢你了！现在我又能看见东西了。");
                        qm.forceCompleteQuest();
                    } else {
                        qm.sendOk("#r消耗栏满了。");
                    }
                } else if (qm.haveItem(4031854) || qm.haveItem(4031855)) { //When I figure out how to make a completance with just a pickup xD
                    if (qm.canHold(2030019)) {
                        if (qm.haveItem(4031854)) {
                            qm.gainItem(4031854, -1);
                        } else {
                            qm.gainItem(4031855, -1);
                        }

                        qm.gainExp(1000);
                        qm.gainItem(2030019, 5);

                        qm.sendOk("这不是我的眼镜。。。不过还能凑合着用，总之谢谢你了。");
                        qm.forceCompleteQuest();
                    } else {
                        qm.sendOk("#r消耗栏满了。");
                    }
                }
            }
        } else if (status == 1) {
            qm.dispose();
        }
    }
}