/*
	NPC Name: 		Guoqing
	Map(s): 		Mount Song: Mountainside (702030000)
	Description: 	Quest NPC - Searching for Senior Brother chain
	Quest: 			8538, 8539
*/

var status = 0;

function start() {
    status = -1;
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
        if (cm.isQuestStarted(8538) && cm.haveItem(4031786, 1)) {
            cm.sendYesNo("Amitabha. Greetings, traveler. What brings you to seek me out?");
        } else if (cm.isQuestStarted(8539)) {
            cm.sendNext("Please deliver the letter to my junior as soon as possible, so he may cease worrying.");
            cm.dispose();
        } else {
            cm.sendNext("Practice, practice...");
            cm.dispose();
        }
    } else if (status == 1) {
        cm.completeQuest(8538);
        cm.sendNext("Thank you very much. I've been out so long that my junior must have been worried. Come find me again later.");
    } else if (status == 2) {
        cm.startQuest(8539);
        cm.sendNextPrev("My junior sent you? I see... I have not yet completed my ascetic training, so I cannot return to the temple. May I trouble you to deliver this letter to my junior?");
    } else if (status == 3) {
        cm.sendNextPrev("Thank you kindly.");
    } else {
        cm.dispose();
    }
}
