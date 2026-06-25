var questId = 29933;
var requiredMedal = 1142030;
var titleName = "明珠港爱心使者";
var medalName = "明珠港爱心使者勋章";
var praiseMessage = "在明珠港，旅程从潮声与告别中开始。而你的慷慨，已经成为送给每一位旅人的祝福。";
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