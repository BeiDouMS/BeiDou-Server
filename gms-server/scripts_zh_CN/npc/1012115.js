function start() {
    var status = cm.getQuestStatus(20706);

    if (status == 0) {
        cm.sendNext("看起来这个地区没有什么可疑的东西。");
    } else if (status == 1) {
        cm.forceCompleteQuest(20706);
        cm.sendNext("你已经发现了影子！最好向#p1103001#报告。");
    } else if (status == 2) {
        cm.sendNext("影子已经被发现了。最好向#p1103001#报告一下。");
    }
    cm.dispose();
}