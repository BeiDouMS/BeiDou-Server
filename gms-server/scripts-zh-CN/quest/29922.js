var questId = 29922;
var requiredMedal = 1142004;
var titleName = "勤奋冒险家";
var medalName = "勤奋冒险家勋章";
var praiseMessage = "无数次狩猎磨炼了你的脚步，也见证了你的坚持。你已经配得上勤奋冒险家的称号，这份荣誉属于真正不懈前行的人。";
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