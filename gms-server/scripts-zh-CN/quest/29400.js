/*
	Author: BubblesDev
	Description: Quest - Veteran Hunter (29400) - 勤奋冒险家勋章
	Quest ID: 29400
*/

var status = -1;
var requiredKills = 100000;

function getProgress() {
    return Math.min(qm.getQuestProgressInt(29400), requiredKills);
}

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            qm.dispose();
            return;
        }

        if (status == 0) {
            qm.sendNext("在30天内狩猎高于自己等级的#r100,000只#k怪物！完成任务即可获得#e#b勤奋冒险家勋章#k#n称号。其中实力最为出众的勇士将获得#e#b传说中的猎人(特级)#k#n……");
        } else if (status == 1) {
            qm.sendYesNo("请注意：必须为高于自身等级的怪物（120级以上的角色必须狩猎120级以上的怪物）。你要接受这个挑战吗？");
        } else if (status == 2) {
            qm.forceStartQuest();
            qm.setQuestProgress(29400, 0);
            qm.showInfoText("你已接受勤奋冒险家勋章挑战！");
            qm.dispose();
        }
    }
}

function end(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode != 1 && type > 0) {
            qm.dispose();
            return;
        }

        status++;
        if (status == 0) {
            if (getProgress() < requiredKills) {
                qm.sendNext("你目前已狩猎#r" + getProgress() + "#k / #b" + requiredKills + "#k只符合条件的怪物。请继续狩猎高于自身等级的怪物。");
                qm.dispose();
                return;
            }
            qm.sendNext("恭喜你获得#b<勤奋冒险家>勋章#k！\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n #v1142004:# #t1142004# 1");
        } else if (status == 1) {
            if (qm.canHold(1142004)) {
                qm.gainItem(1142004);
                qm.forceCompleteQuest();
                qm.dispose();
            } else {
                qm.sendNext("请在勋章栏位中腾出空间。");
            }
        } else if (status == 2) {
            qm.dispose();
        }
    }
}
