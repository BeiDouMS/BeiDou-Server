/*
	NPC Name: 		Xuzhu
	Map(s): 		Mount Song: Mahavira Hall (702100000)
	Description: 	Quest NPC - Searching for Senior Brother chain
	Quest: 			8538, 8539
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
            cm.sendNext("Ah, a letter from my senior! He is in deep meditation. Thank you for bringing me his news. I have little to offer, but please accept these humble gifts.");
        } else if (cm.isQuestCompleted(8539)) {
            cm.sendNext("Thank you for bringing word from my senior.");
            cm.dispose();
        } else if (cm.isQuestStarted(8538)) {
            cm.sendNext("Have you found my senior brother? He is cultivating on the mountainside at #b#m702030000##k.");
            cm.dispose();
        } else if (!cm.isQuestStarted(8538) && !cm.isQuestCompleted(8538)) {
            questAction = "start8538";
            cm.sendYesNo("Greetings, traveler. This humble monk has a favor to ask. Would you be willing to help?");
        } else {
            cm.sendNext("I must train hard today too.");
            cm.dispose();
        }
    } else if (status == 1) {
        if (questAction == "complete8539") {
            cm.completeQuest(8539);
            cm.sendNext("I have little to offer, but please accept these humble gifts.");
            cm.dispose();
        } else {
            cm.startQuest(8538);
            cm.sendNext("My senior brother left long ago for ascetic training, but has not returned for a long time. I am worried about him. Please help me find him. His dharma name is #b#p9310040##k.");
        }
    } else if (status == 2) {
        cm.sendNextPrev("Please look for my senior brother at #b#m702030000##k.");
    } else {
        cm.dispose();
    }
}
