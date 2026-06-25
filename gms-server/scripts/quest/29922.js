var questId = 29922;
var requiredMedal = 1142004;
var titleName = "Veteran Hunter";
var medalName = "Veteran Hunter Medal";
var praiseMessage = "Countless battles have shaped your steps, and every hunt has sharpened your resolve. You have earned a hunter's name that others will speak with respect.";
var status = -1;
var readyToAward = false;

function hasRequiredMedal() {
    return qm.haveItemWithId(requiredMedal, true);
}

function isCompleted() {
    return qm.getQuestStatus(questId) == 2;
}

function awardTitle() {
    qm.forceStartQuest();
    qm.forceCompleteQuest();
    qm.message("<" + titleName + "> title has been awarded.");
    qm.earnTitle(titleName);
}

function handle(mode) {
    if (mode == -1) {
        qm.dispose();
        return;
    }

    status++;
    if (status == 0) {
        if (isCompleted()) {
            qm.sendOk("You have already earned the #b<" + titleName + ">#k title.");
        } else if (!hasRequiredMedal()) {
            qm.sendOk("Please earn the #b" + medalName + "#k first, then claim this title.");
        } else {
            readyToAward = true;
            qm.sendOk(praiseMessage);
        }
        return;
    }

    if (readyToAward && !isCompleted() && hasRequiredMedal()) {
        awardTitle();
    }
    qm.dispose();
}

function start(mode, type, selection) {
    handle(mode);
}

function end(mode, type, selection) {
    handle(mode);
}