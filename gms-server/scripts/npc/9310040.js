var status = -1;
var flow = null;

var Quest = Java.type('org.gms.server.quest.Quest');

var QUEST_FIND_SENIOR_1 = 8538;
var QUEST_FIND_SENIOR_2 = 8539;

var ITEM_LETTER_TO_SENIOR = 4031786;
var ITEM_LETTER_FROM_SENIOR = 4031787;

function start() {
    status = -1;
    flow = null;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }

    if (flow == "start8539" && status == 0 && mode == 0) {
        cm.sendOk("It is a small matter. I shall not trouble you further.");
        cm.dispose();
        return;
    }

    if (mode == 0 && status >= 0) {
        cm.dispose();
        return;
    }

    status += (mode == 1 ? 1 : -1);

    var player = cm.getPlayer();
    var npcId = cm.getNpc();

    if (status == 0) {
        if (cm.isQuestStarted(QUEST_FIND_SENIOR_1) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_1) &&
            cm.haveItem(ITEM_LETTER_TO_SENIOR, 1) &&
            Quest.getInstance(QUEST_FIND_SENIOR_1).canComplete(player, npcId)) {
            flow = "complete8538";
            cm.sendNext("Amitabha. Greetings, traveler. What brings you to seek me out?");
            return;
        }

        if (!cm.isQuestStarted(QUEST_FIND_SENIOR_2) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_2) &&
            Quest.getInstance(QUEST_FIND_SENIOR_2).canStart(player, npcId)) {
            flow = "start8539";
            cm.sendAcceptDecline("Ah, so it is my junior #b#p9310052##k who sent you. I have not yet completed my ascetic training, so I cannot return to the temple. May I trouble you to deliver this letter to my junior?");
            return;
        }

        if (cm.isQuestStarted(QUEST_FIND_SENIOR_2) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_2)) {
            cm.sendOk("Please deliver the letter to my junior as soon as possible, so he may cease worrying, lest it hinder his cultivation.");
            cm.dispose();
            return;
        }

        if (cm.isQuestStarted(QUEST_FIND_SENIOR_1) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_1)) {
            cm.sendOk("Amitabha. Greetings, traveler. What brings you to seek me out?");
            cm.dispose();
            return;
        }

        cm.sendDefault();
        cm.dispose();
        return;
    }

    if (status == 1) {
        if (flow == "complete8538") {
            Quest.getInstance(QUEST_FIND_SENIOR_1).complete(player, npcId);
            if (cm.isQuestCompleted(QUEST_FIND_SENIOR_1)) {
                cm.removeItem(ITEM_LETTER_TO_SENIOR, 1);
                cm.sendOk("Thank you very much. I've been out so long that my junior must have been quite worried. Come find me again later.");
            } else {
                cm.sendOk("Unable to complete the quest at this time. (Please ensure you have the required item and sufficient inventory space.)");
            }
            cm.dispose();
            return;
        }

        if (flow == "start8539") {
            Quest.getInstance(QUEST_FIND_SENIOR_2).start(player, npcId);
            if (cm.isQuestStarted(QUEST_FIND_SENIOR_2)) {
                cm.gainItem(ITEM_LETTER_FROM_SENIOR, 1);
                cm.sendOk("Thank you kindly.");
            } else {
                cm.sendOk("Unable to start the quest at this time. (Please check level/job requirements and ensure you have inventory space.)");
            }
            cm.dispose();
            return;
        }

        cm.dispose();
        return;
    }

    cm.dispose();
}
