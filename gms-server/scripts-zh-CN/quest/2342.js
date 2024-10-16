/*
	QUEST: Recovered Royal Seal.
	NPC: Violetta
*/

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
            if (qm.isQuestStarted(2331) && !qm.isQuestCompleted(2331)) {
                if (!qm.hasItem(4001318)) {
                    if (qm.canHold(4001318)) {
                        qm.forceStartQuest();
                        qm.gainItem(4001318, 1);
                        qm.sendOk("看起来你在与蘑菇大臣战斗时忘记拿起#b#t4001318##k了。给你，这对我们王国非常重要，请尽快把它交给我的父亲。");
                    } else {
                        qm.sendOk("请确保你的其他栏有一个空位");
                        cm.dispose();
                    }
                } else {
                    qm.forceStartQuest();
                    qm.sendOk("你手中的#b#t4001318##k对我们王国非常重要，请尽快把它交给我的父亲把。");
                }
            } else {
                qm.sendOk("你需要找回被盗的#r#t4001318##k");
                qm.dispose();
            }
        } else if (status == 1) {
            qm.forceCompleteQuest();
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    if (qm.isQuestCompleted(2336) && qm.hasItem(4001318)) {
        qm.gainItem(4001318, -1);
        qm.forceCompleteQuest();
        qm.dispose();
    } else {
        qm.sendOk("请尽快把#b#t4032387##k交给我的父亲。再回来找我.");
        qm.dispose();
    }
}
