/*
	Author: DietStory v1.02 dev team
	NPC: Matthias
	Quest: Hidden Inside the Trash Can
*/


var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
    } else if (mode == 0 && status == 0) {
		qm.sendOk("什么？你拒绝了任务吗？好吧，就这样做。我就直接报告给 #p1101002#.");
        qm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }


    if (status == 0) {
		qm.sendAcceptDecline("你不是真的给我灌输信心，但既然你是皇家骑士。。。既然现在没有其他人可以搜索。。。好吧，让我向你解释这次任务的目的.");
    } else if (status == 1) {
        qm.forceStartQuest();
        qm.dispose();
    }
}