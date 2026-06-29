var questId = 29916;
var requiredMedal = 1142014;
var titleName = "Henesys Donor";
var medalName = "Henesys Donor Medal";
var praiseMessage = "Your kindness has taken root in Henesys like sunlight over a quiet field. The people will remember the warmth you brought to their town.";
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