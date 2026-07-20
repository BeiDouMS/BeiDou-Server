var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function finishIfAlreadyAwarded(medalId) {
    if (qm.isQuestCompleted(qm.getQuest())) {
        qm.sendOk("You have already received the #b#t" + medalId + "##k. This challenge is already recorded in your adventure history.");
        qm.dispose();
        return true;
    }
    if (qm.haveItemWithId(medalId, true)) {
        qm.forceCompleteQuest();
        qm.earnTitle(qm.getMedalName());
        qm.sendOk("You have already received the #b#t" + medalId + "##k. This challenge is already recorded in your adventure history.");
        qm.dispose();
        return true;
    }
    return false;
}

function start(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.PET_OWNER_MEDAL_ID)) {
        return;
    }
    qm.forceStartQuest();
    qm.sendOk("Raise a pet's closeness to #b" + Medal.PET_REQUIRED_TAMENESS + "#k, then return to receive the #b#t" + Medal.PET_OWNER_MEDAL_ID + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (finishIfAlreadyAwarded(Medal.PET_OWNER_MEDAL_ID)) {
        return;
    }
    var tameness = Medal.getMaxPetTameness(qm.getPlayer());
    if (tameness < Medal.PET_REQUIRED_TAMENESS) {
        qm.sendOk("Your pet is not close enough yet.\r\nBest closeness: #r" + tameness + "#k / " + Medal.PET_REQUIRED_TAMENESS);
        qm.dispose();
        return;
    }

    awardMedal(Medal.PET_OWNER_MEDAL_ID);
}

function awardMedal(medalId) {
    if (!qm.haveItem(medalId)) {
        if (!qm.canHold(medalId)) {
            qm.sendOk("Please make room in your equip inventory.");
            qm.dispose();
            return;
        }
        qm.gainItem(medalId, 1);
    }

    qm.forceCompleteQuest();
    qm.earnTitle(qm.getMedalName());
    qm.sendOk("Your pet follows you not because of orders, but because of trust. That bond is more precious than any trophy.\r\n\r\nPlease accept the #b#t" + medalId + "##k. May everyone see what a wonderful pet owner you are.");
    qm.dispose();
}
