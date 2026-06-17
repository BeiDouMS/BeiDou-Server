var status = -1;
var flow = null;

var Quest = Java.type('org.gms.server.quest.Quest');

var QUEST_FIND_SENIOR_1 = 8538;
var QUEST_FIND_SENIOR_2 = 8539;

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

    if (flow == "start8538" && status == 1 && mode == 0) {
        cm.sendOk("Amitabha. Since you have important matters to attend to, I shall not impose. Please come help me when you have time...");
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
        if (cm.isQuestStarted(QUEST_FIND_SENIOR_2) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_2) &&
            Quest.getInstance(QUEST_FIND_SENIOR_2).canComplete(player, npcId)) {
            flow = "complete8539";
            cm.sendNext("Ah, a letter from my senior! He is in deep meditation. Thank you for bringing me his news. I have little to offer, but please accept these humble gifts.");
            return;
        }

        if (!cm.isQuestStarted(QUEST_FIND_SENIOR_1) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_1) &&
            Quest.getInstance(QUEST_FIND_SENIOR_1).canStart(player, npcId)) {
            flow = "start8538";
            cm.sendNext("Greetings, traveler. This humble monk has a favor to ask. Would you be willing to help?");
            return;
        }

        if (cm.isQuestStarted(QUEST_FIND_SENIOR_1) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_1)) {
            cm.sendOk("My senior brother's dharma name is #b#p9310040##k. Please help me find him.");
            cm.dispose();
            return;
        }

        if (cm.isQuestStarted(QUEST_FIND_SENIOR_2) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_2)) {
            cm.sendOk("Has my senior's letter not yet arrived? Please bring back word from him when you meet.");
            cm.dispose();
            return;
        }

        cm.sendDefault();
        cm.dispose();
        return;
    }

    if (status == 1) {
        if (flow == "start8538") {
            cm.sendAcceptDecline("My senior brother left long ago for ascetic training, but has not returned for a long time. I am worried about him. Please help me find him. His dharma name is #b#p9310040##k.");
            return;
        }

        if (flow == "complete8539") {
            Quest.getInstance(QUEST_FIND_SENIOR_2).complete(player, npcId);
            if (!cm.isQuestCompleted(QUEST_FIND_SENIOR_2)) {
                cm.sendOk("Unable to complete the quest at this time. (Please ensure you have my senior's letter and sufficient inventory space.)");
            }
            cm.dispose();
            return;
        }

        cm.dispose();
        return;
    }

    if (status == 2) {
        if (flow == "start8538") {
            Quest.getInstance(QUEST_FIND_SENIOR_1).start(player, npcId);
            if (cm.isQuestStarted(QUEST_FIND_SENIOR_1)) {
                cm.sendOk("My senior brother's dharma name is #b#p9310040##k. Please help me find him.");
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
