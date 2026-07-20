var questId = 29915;
var requiredMedal = 1142064;
var titleName = "武陵道场征服者";
var medalName = "武陵道场征服者勋章";
var praiseMessage = "你的意志已经回响在武陵道场的每一层。你直面所有大师怪物，从未退缩；从今天起，武陵道场征服者的荣耀将与你的名字并列。";
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