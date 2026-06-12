var VeteranHunterMedal = Java.type('org.gms.server.quest.medal.VeteranHunterMedal');
var medalId = VeteranHunterMedal.MEDAL_ID;
var requiredKills = VeteranHunterMedal.REQUIRED_KILLS;

function progressText() {
    return VeteranHunterMedal.getProgress(qm.getPlayer()) + " / " + requiredKills;
}

function start(mode, type, selection) {
    var alreadyHasMedal = qm.haveItem(medalId);
    qm.forceStartQuest();

    var message = "The challenge has begun! Hunt #r100,000#k eligible monsters within #r30 days#k.";
    if (alreadyHasMedal) {
        message += "\r\n\r\nYou already have the #bVeteran Hunter#k medal. This challenge will keep counting, but the medal will not be awarded again.";
    }
    message += "\r\n\r\n#bCounting Rules#k\r\n- Characters below level 120: only monsters #rhigher than your level#k count.\r\n- Characters level 120 or higher: only monsters #rlevel 120 or higher#k count.\r\n\r\nCurrent hunt count: #r" + progressText() + "#k";
    qm.sendOk(message);
    qm.dispose();
}

function end(mode, type, selection) {
    if (!VeteranHunterMedal.isComplete(qm.getPlayer())) {
        qm.sendOk("You have not reached the #r100,000#k monster hunting goal yet.\r\n\r\nCurrent hunt count: #r" + progressText() + "#k");
        qm.dispose();
        return;
    }

    if (!qm.haveItem(medalId)) {
        if (!qm.canHold(medalId)) {
            qm.sendOk("Please free 1 slot in your Equip inventory, then claim the medal again.");
            qm.dispose();
            return;
        }
        qm.gainItem(medalId, 1);
    }

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> Reward received.");
    qm.earnTitle(medalname);
    qm.forceCompleteQuest();
    qm.dispose();
}
