/*
	Author: BubblesDev
	Description: Quest - Veteran Hunter (29400)
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
            qm.sendNext("Hunt #r100,000#k monsters that are higher level than you within 30 days! Anyone who completes this task will receive the title, #e#bVeteran Hunter#k#n. The person with the highest score will receive the #e#bLegendary Hunter (Special Title)#k#n.");
        } else if (status == 1) {
            qm.sendYesNo("Note that monsters must be higher level than your character. Characters level 120 or above must hunt monsters level 120 or higher. Do you want to accept this challenge?");
        } else if (status == 2) {
            qm.forceStartQuest();
            qm.setQuestProgress(29400, 0);
            qm.showInfoText("You have accepted the Veteran Hunter medal challenge!");
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
                qm.sendNext("You have hunted #r" + getProgress() + "#k / #b" + requiredKills + "#k eligible monsters. Keep hunting monsters that are higher level than you.");
                qm.dispose();
                return;
            }
            qm.sendNext("Congratulations on earning your honorable #b<Veteran Hunter>#k title. Keep up the good work!\r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0#\r\n #v1142004:# #t1142004# 1");
        } else if (status == 1) {
            if (qm.canHold(1142004)) {
                qm.gainItem(1142004);
                qm.forceCompleteQuest();
                qm.dispose();
            } else {
                qm.sendNext("Please make room in your medal/necklace inventory.");
            }
        } else if (status == 2) {
            qm.dispose();
        }
    }
}
