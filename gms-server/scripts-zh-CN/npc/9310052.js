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

    if (flow == "start8538" && status == 1 && mode == 0) {
        cm.sendOk("阿弥陀佛，施主既有要事在身，小僧不敢叨扰。施主若得闲暇时，再来帮小僧便可...");
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
            cm.haveItem(ITEM_LETTER_FROM_SENIOR, 1) &&
            Quest.getInstance(QUEST_FIND_SENIOR_2).canComplete(player, npcId)) {
            flow = "complete8539";
            cm.sendNext("啊，师兄的信。原来师兄正在精修禅宗，多谢施主带来师兄的音信。小僧无以为报，些许物件，还请施主笑纳。");
            return;
        }

        if (!cm.isQuestStarted(QUEST_FIND_SENIOR_1) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_1) &&
            Quest.getInstance(QUEST_FIND_SENIOR_1).canStart(player, npcId)) {
            flow = "start8538";
            cm.sendNext("这位施主，小僧有事相求，万望施主不吝相助。");
            return;
        }

        if (cm.isQuestStarted(QUEST_FIND_SENIOR_1) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_1)) {
            cm.sendOk("小僧的师兄法号#b#p9310040##k，还请施主代为寻找。");
            cm.dispose();
            return;
        }

        if (cm.isQuestStarted(QUEST_FIND_SENIOR_2) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_2)) {
            cm.sendOk("师兄的书信还未送到吗？请施主见到师兄后替小僧带回音信。");
            cm.dispose();
            return;
        }

        cm.sendDefault();
        cm.dispose();
        return;
    }

    if (status == 1) {
        if (flow == "start8538") {
            cm.sendAcceptDecline("小僧有个师兄早年外出苦修，时日已久却不见音信，小僧好不挂念，还请施主代为寻找。小僧的师兄法号#b#p9310040##k。");
            return;
        }

        if (flow == "complete8539") {
            Quest.getInstance(QUEST_FIND_SENIOR_2).complete(player, npcId);
            if (cm.isQuestCompleted(QUEST_FIND_SENIOR_2)) {
                cm.removeItem(ITEM_LETTER_FROM_SENIOR, 1);
                cm.sendOk("小僧无以为报，些许物件，还请施主笑纳。");
            } else {
                cm.sendOk("似乎暂时无法完成任务（请确认携带了师兄的信，并确保背包有空位）。");
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
                cm.gainItem(ITEM_LETTER_TO_SENIOR, 1);
                cm.sendOk("小僧的师兄法号#b#p9310040##k，还请施主代为寻找。");
            } else {
                cm.sendOk("似乎暂时无法接取任务（请确认等级/职业条件，并确保背包有空位）。");
            }
            cm.dispose();
            return;
        }

        cm.dispose();
        return;
    }

    cm.dispose();
}
