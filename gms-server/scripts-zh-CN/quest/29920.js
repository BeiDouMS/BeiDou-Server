var questId = 29920;
var requiredMedal = 1142018;
var titleName = "林中之城爱心使者";
var medalName = "林中之城爱心使者勋章";
var praiseMessage = "在林中之城深邃的树影之下，你的慷慨成为了一盏安稳的灯。它会照亮这里的人们，也照亮后来者前行的路。";
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
    qm.message("<" + titleName + "> 称号已获得。");
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
            qm.sendOk("你已经获得过#b<" + titleName + ">#k称号。");
        } else if (!hasRequiredMedal()) {
            qm.sendOk("请先获得#b" + medalName + "#k后，再来领取这个称号。");
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