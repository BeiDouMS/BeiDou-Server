var questId = 29917;
var requiredMedal = 1142015;
var titleName = "Ellinia Donor";
var medalName = "Ellinia Donor Medal";
var praiseMessage = "Your generosity now flows through Ellinia like clear magic among the trees. Even the oldest branches seem to whisper thanks for what you have given.";
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