/*
 * Cygnus 1st Job advancement - Night Walker
 */

var status = -1;
var jobType = 4;
var canTryFirstJob = true;

function end(mode, type, selection) {
    if (mode == 0) {
	if (status == 0) {
	    qm.sendNext("这个决定..非常重要.");
	    qm.dispose();
	    return;
	}
		status--;
    } else {
        status++;
    }
    if (status == 0) {
    	qm.sendYesNo("你决定好了嘛? 这会是你最后的决定唷, 所以想清楚你要做什么. 你想要成为 夜行者嘛?");
    } else if (status == 1) {
        if (canTryFirstJob) {
            canTryFirstJob = false;
            if (qm.getPlayer().getJob().getId() != 1400) {
                if(!qm.canGetFirstJob(jobType)) {
                    qm.sendOk("请先将等级提升到 #b10级, " + qm.getFirstJobStatRequirement(jobType) + "#k 我会告诉你 #r夜行者#k在哪.");
                    qm.dispose();
                    return;
                }

                if (!(qm.canHoldAll([1472061, 1142066]) && qm.canHold(2070000))) {
                    qm.sendOk("请先给背包空出些空间.");
                    qm.dispose();
                    return;
                }

                qm.gainItem(1472061, 1);
                qm.gainItem(2070000, 800);
                qm.gainItem(1142066, 1);
                const Job = Java.type('client.Job');
                qm.changeJob(Job.NIGHTWALKER1);
                qm.getPlayer().resetStats();
            }
            qm.forceCompleteQuest();
        }
        qm.sendNext("恭喜成功转职.");
    } else if (status == 2) {
    	qm.sendNextPrev("我还扩大了你的库存量.");
    } else if (status == 3) {
    	qm.sendNextPrev("打开技能栏，看看获得的新技能.");
    } else if (status == 4) {
    	qm.sendNextPrev("从现在开始，你死亡的时候会损失一部分经验值.");
    } else if (status == 5) {
    	qm.sendNextPrev("现在。。。我要你出去向全世界展示天鹅座的骑士们是如何成长的.");
    } else if (status == 6) {
        qm.dispose();
    }
}