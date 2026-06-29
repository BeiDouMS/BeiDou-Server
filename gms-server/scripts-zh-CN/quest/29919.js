var questId = 29919;
var requiredMedal = 1142017;
var titleName = "废弃都市爱心使者";
var medalName = "废弃都市爱心使者勋章";
var praiseMessage = "因为你的慷慨，废弃都市的街巷也多了一束光。即使人们步履匆匆，你留下的善意仍会被长久记住。";
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