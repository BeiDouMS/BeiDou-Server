var status = -1;

function start(mode, type, selection) {
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
            qm.sendNext("据说每当有什么邪恶的东西扰乱这片土地时，一棵鬼树就会出现在这里。。。我们需要一个能保护我们村子的英雄！");
            //最近聽說東部岩山時常發生奇怪的襲擊事件，也許你可以留意一下…
            qm.forceCompleteQuest();
        } else if (status == 1) {
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    qm.dispose();
}
