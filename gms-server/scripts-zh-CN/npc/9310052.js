/*
	NPC Name: 		虚竹
	Map(s): 		Mount Song: Mahavira Hall (702100000)
	Description: 	Quest NPC - 寻找师兄 chain
	Quest: 			8538 (寻找师兄1), 8539 (寻找师兄2)
*/

var status = 0;
var questAction = "";

function start() {
    status = -1;
    questAction = "";
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }
    if (mode == 0) {
        cm.dispose();
        return;
    }
    status++;

    if (status == 0) {
        if (cm.isQuestCompleted(8538) && cm.isQuestStarted(8539) && cm.haveItem(4031787, 1)) {
            questAction = "complete8539";
            cm.sendNext("啊，师兄的信！原来师兄正在精修禅宗，多谢施主带来师兄的音信。小僧无以为报，些许物件，还请施主笑纳。");
        } else if (cm.isQuestCompleted(8539)) {
            cm.sendNext("多谢施主带来师兄的音信。");
            cm.dispose();
        } else if (cm.isQuestStarted(8538)) {
            cm.sendNext("施主找到我师兄了吗？他在山腰苦修，就在#b#m702030000##k。");
            cm.dispose();
        } else if (!cm.isQuestStarted(8538) && !cm.isQuestCompleted(8538)) {
            questAction = "start8538";
            cm.sendYesNo("这位施主，小僧有事相求，万望施主不吝相助。");
        } else {
            cm.sendNext("今天也要好好修炼啊");
            cm.dispose();
        }
    } else if (status == 1) {
        if (questAction == "complete8539") {
            cm.completeQuest(8539);
            cm.sendNext("小僧无以为报，些许物件，还请施主笑纳。");
            cm.dispose();
        } else {
            cm.startQuest(8538);
            cm.sendNext("小僧有个师兄早年外出苦修，时日已久却不见音信，小僧好不挂念，还请施主代为寻找。小僧的师兄法号#b#p9310040##k。");
        }
    } else if (status == 2) {
        cm.sendNextPrev("请施主去#b#m702030000##k寻找我的师兄。");
    } else {
        cm.dispose();
    }
}
