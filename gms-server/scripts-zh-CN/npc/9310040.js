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
        cm.sendOk("些许小事，就不麻烦施主了。");
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
            cm.sendNext("阿弥陀佛，贫僧有礼了。不知施主找贫僧，所为何事。");
            return;
        }

        if (!cm.isQuestStarted(QUEST_FIND_SENIOR_2) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_2) &&
            Quest.getInstance(QUEST_FIND_SENIOR_2).canStart(player, npcId)) {
            flow = "start8539";
            cm.sendAcceptDecline("原来是#b#p9310052##k师弟所托，贫僧入世修行未满，还不能回寺。这里有书信一封，请施主带给我的小师弟，不知可否？");
            return;
        }

        if (cm.isQuestStarted(QUEST_FIND_SENIOR_2) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_2)) {
            cm.sendOk("请施主早日将书信送与我那小师弟，免得他挂念，于修行不利。");
            cm.dispose();
            return;
        }

        if (cm.isQuestStarted(QUEST_FIND_SENIOR_1) && !cm.isQuestCompleted(QUEST_FIND_SENIOR_1)) {
            cm.sendOk("阿弥陀佛，贫僧有礼了。不知施主找贫僧，所为何事。");
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
                cm.sendOk("太谢谢你了，出来这么久让师弟担心了啊~等会再来找我。");
            } else {
                cm.sendOk("似乎暂时无法完成任务（请确认携带了要交付的物品，并确保背包空间充足）。");
            }
            cm.dispose();
            return;
        }

        if (flow == "start8539") {
            Quest.getInstance(QUEST_FIND_SENIOR_2).start(player, npcId);
            if (cm.isQuestStarted(QUEST_FIND_SENIOR_2)) {
                cm.gainItem(ITEM_LETTER_FROM_SENIOR, 1);
                cm.sendOk("如此多谢施主了。");
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
