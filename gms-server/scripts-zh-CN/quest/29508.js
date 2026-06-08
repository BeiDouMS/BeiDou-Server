var OutstandingCitizenMedal = Java.type('org.gms.server.quest.medal.OutstandingCitizenMedal');
var questId = OutstandingCitizenMedal.QUEST_ID;
var medalId = OutstandingCitizenMedal.MEDAL_ID;

function getMissingRequirements() {
    var missing = [];
    var player = qm.getPlayer();
    var familyEntry = player.getFamilyEntry();

    if (!player.isMarried()) {
        missing.push("????");
    }
    if (player.getGuildId() <= 0) {
        missing.push("????");
    }
    if (familyEntry == null || familyEntry.getJuniorCount() < 1) {
        missing.push("???? 1 ???");
    }

    return missing;
}

function start(mode, type, selection) {
    if (qm.getQuestStatus(questId) == 2) {
        qm.sendOk("?????????????");
        qm.dispose();
        return;
    }

    qm.forceStartQuest();
    OutstandingCitizenMedal.refreshEligibility(qm.getPlayer());

    var missing = getMissingRequirements();
    if (missing.length > 0) {
        qm.sendOk("???????????????? 1 ????????????????????");
    } else {
        qm.sendOk("??????????????????????");
    }
    qm.dispose();
}

function end(mode, type, selection) {
    if (qm.getQuestStatus(questId) == 2) {
        OutstandingCitizenMedal.clearEligibility(qm.getPlayer());
        qm.sendOk("?????????????");
        qm.dispose();
        return;
    }

    OutstandingCitizenMedal.refreshEligibility(qm.getPlayer());

    var missing = getMissingRequirements();
    if (missing.length > 0) {
        qm.sendOk("??????????????" + missing.join("?") + "?");
        qm.dispose();
        return;
    }

    if (!qm.haveItem(medalId)) {
        if (!qm.canHold(medalId)) {
            qm.sendOk("???????? 1 ???????????");
            qm.dispose();
            return;
        }
        qm.gainItem(medalId, 1);
    }

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> ??????");
    qm.earnTitle(medalname);
    qm.forceCompleteQuest();
    OutstandingCitizenMedal.clearEligibility(qm.getPlayer());
    qm.dispose();
}
