var medalId = 1142081;

function getMissingRequirements() {
    var missing = [];
    var player = qm.getPlayer();
    var familyEntry = player.getFamilyEntry();

    if (!player.isMarried()) {
        missing.push("get married");
    }
    if (player.getGuildId() <= 0) {
        missing.push("join a guild");
    }
    if (familyEntry == null || familyEntry.getJuniorCount() < 1) {
        missing.push("register at least 1 Junior");
    }

    return missing;
}

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("Join a marriage, a guild, and register at least 1 Junior. Come back once you meet all three requirements.");
    qm.dispose();
}

function end(mode, type, selection) {
    var missing = getMissingRequirements();
    if (missing.length > 0) {
        qm.sendOk("You have not met all the requirements yet. Please " + missing.join(", ") + ".");
        qm.dispose();
        return;
    }

    if (!qm.haveItem(medalId)) {
        if (!qm.canHold(medalId)) {
            qm.sendOk("Please make room in your Equip inventory before receiving the medal.");
            qm.dispose();
            return;
        }
        qm.gainItem(medalId, 1);
    }

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> has been awarded.");
    qm.earnTitle(medalname);
    qm.forceCompleteQuest();
    qm.dispose();
}
