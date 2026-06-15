var OutstandingCitizenMedal = Java.type('org.gms.server.quest.medal.OutstandingCitizenMedal');
var medalId = OutstandingCitizenMedal.MEDAL_ID;

function getMissingRequirements() {
    var missing = [];
    var player = qm.getPlayer();
    var familyEntry = player.getFamilyEntry();

    if (!player.isMarried()) {
        missing.push("get married: complete a wedding with another character and wear a valid wedding ring");
    }
    if (player.getGuildId() <= 0) {
        missing.push("join a guild: the character must currently belong to a guild");
    }
    if (familyEntry == null || familyEntry.getJuniorCount() < 1) {
        missing.push("register a Junior: have at least 1 Junior in the Family system");
    }

    return missing;
}

function formatMissingRequirements(missing) {
    var text = "You have not met all requirements yet. The #bOutstanding Citizen#k medal requires all 3 conditions:\r\n";
    text += "#b- Get married#k: complete a wedding with another character and wear a valid wedding ring.\r\n";
    text += "#b- Join a guild#k: this character must currently belong to a guild.\r\n";
    text += "#b- Register a Junior#k: have at least 1 Junior in the Family system.\r\n\r\n";
    text += "Still missing:\r\n#r- " + missing.join("\r\n- ") + "#k\r\n\r\n";
    text += "Please come back after all missing conditions are complete.";
    return text;
}

function start(mode, type, selection) {
    qm.forceStartQuest();
    OutstandingCitizenMedal.refreshEligibility(qm.getPlayer());

    var missing = getMissingRequirements();
    if (missing.length > 0) {
        qm.sendOk(formatMissingRequirements(missing));
    } else {
        qm.sendOk("You meet the marriage, guild, and Junior requirements. Talk to me again to receive the #bOutstanding Citizen#k medal.");
    }
    qm.dispose();
}

function end(mode, type, selection) {
    OutstandingCitizenMedal.refreshEligibility(qm.getPlayer());

    var missing = getMissingRequirements();
    if (missing.length > 0) {
        qm.sendOk(formatMissingRequirements(missing));
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
    OutstandingCitizenMedal.clearEligibility(qm.getPlayer());
    qm.dispose();
}
