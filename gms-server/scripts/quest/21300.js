var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode == 0 && type == 0) {
        status -= 2;
    } else if (mode != 1) {
        //if (mode == 0)
            qm.sendNext("#b(你需要考虑一下。。。)#k");
        qm.dispose();
        return;
    }

    if (status == 0) {
            qm.sendNext("训练进行得如何？ 嗯... 70级...还不算什么，但是自从我第一次结识冰鲜面世以来，您确实取得了长足的进步。 继续训练，我相信有一天您将能够重新获得战斗前的状态。");
    } else if (status == 1) {
            qm.sendAcceptDecline("但在那之前，我需要你再控制一下。#我们的杆臂又一次做出奇怪的反应。好像有什么事要告诉你。#基特也许能唤醒你隐藏的力量，所以请马上来。");
    } else if (status == 2) {
            qm.forceStartQuest();
            qm.sendOk("不管怎么说，我以为武器有自己的身份，但说真的。。。这武器不停地说话。它先是不停地哭，因为我没有真正注意到它的需要，然后。。。啊，请保守这个秘密。我不认为再打乱武器是个好主意。");
    } else if (status == 3) {
        qm.dispose();
    }
}