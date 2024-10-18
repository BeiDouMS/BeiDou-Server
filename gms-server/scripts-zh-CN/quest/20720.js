/*
	QUEST: Before the Mission in Perion Begins
	NPC: Neinheart
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        qm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }

    if (status == 0) {
		qm.sendAcceptDecline("到目前为止情况如何？此时，您可能可以在 #m103000000# 参加派对任务。升级是很重要的，是的，但我们现在需要你以皇家骑士的身份执行任务。我们刚收到一个新消息，可能会有帮助.");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.dispose();
    }
}

function end(mode, type, selection) {}