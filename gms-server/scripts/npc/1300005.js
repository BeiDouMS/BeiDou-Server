var status = -1;
var introQuestIds = [2300, 2301, 2302, 2303, 2304, 2305, 2306, 2307, 2308, 2309, 2310];

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    }

    if (mode == 0 && type > 0) {
        cm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (status == 0) {
        if (!hasMushroomIntroCompleted()) {
            cm.sendOk("Please speak with the Maple Administrator first.");
            cm.dispose();
        } else if (!cm.isQuestStarted(2312) && !cm.isQuestCompleted(2312)) {
            cm.sendAcceptDecline("We need your help, noble explorer. Our kingdom is currently facing a big threat, and we need to test your skills before placing our faith in you. Will you help us?");
        } else if (cm.isQuestStarted(2312)) {
            if (!cm.haveItem(4000499, 50)) {
                cm.sendOk("Please bring me 50 Mutated Spores from the Renegade Spores.");
                cm.dispose();
            } else {
                cm.sendNext("Did you teach those Renegade Spores a lesson?");
            }
        } else if (!cm.isQuestStarted(2313) && !cm.isQuestCompleted(2313)) {
            cm.sendAcceptDecline("I have told our #bMinister of Home Affairs#k of your abilities. Please go pay a visit to him immediately.");
        } else if (cm.isQuestStarted(2313)) {
            cm.sendNext("Save our kingdom! We believe in you!");
        } else {
            cm.sendOk("Please hurry and meet the Minister of Home Affairs.");
            cm.dispose();
        }
    } else if (status == 1) {
        if (!cm.isQuestStarted(2312) && !cm.isQuestCompleted(2312)) {
            cm.startQuest(2312);
            cm.sendOk("Keep moving forward, and you'll see #bRenegade Spores#k. Please teach them a lesson and bring back #b50 Mutated Spores#k.");
            cm.dispose();
        } else if (cm.isQuestStarted(2312)) {
            cm.completeQuest(2312);
            cm.gainExp(11500);
            cm.gainItem(4000499, -50);
            cm.sendOk("That was amazing. I apologize for doubting your abilities. Please save our Kingdom of Mushroom from this crisis!");
            cm.dispose();
        } else if (!cm.isQuestStarted(2313) && !cm.isQuestCompleted(2313)) {
            cm.startQuest(2313);
            cm.sendOk("Save our kingdom! We believe in you!");
            cm.dispose();
        } else if (cm.isQuestStarted(2313)) {
            cm.completeQuest(2313);
            cm.gainExp(4000);
            cm.dispose();
        } else {
            cm.dispose();
        }
    }
}

function hasMushroomIntroCompleted() {
    for (var i = 0; i < introQuestIds.length; i++) {
        if (cm.isQuestCompleted(introQuestIds[i])) {
            return true;
        }
    }
    return false;
}
