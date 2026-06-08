var medalId = 1142081;

function getMissingRequirements() {
    var missing = [];
    var player = qm.getPlayer();
    var familyEntry = player.getFamilyEntry();

    if (!player.isMarried()) {
        missing.push("完成结婚");
    }
    if (player.getGuildId() <= 0) {
        missing.push("加入公会");
    }
    if (familyEntry == null || familyEntry.getJuniorCount() < 1) {
        missing.push("拥有至少 1 名后辈");
    }

    return missing;
}

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("请完成结婚、加入公会，并拥有至少 1 名后辈。满足全部条件后再来找我领取勋章。");
    qm.dispose();
}

function end(mode, type, selection) {
    var missing = getMissingRequirements();
    if (missing.length > 0) {
        qm.sendOk("你还没有满足全部条件，请先：" + missing.join("、") + "。");
        qm.dispose();
        return;
    }

    if (!qm.haveItem(medalId)) {
        if (!qm.canHold(medalId)) {
            qm.sendOk("请在装备栏中空出 1 个位置后再来领取勋章。");
            qm.dispose();
            return;
        }
        qm.gainItem(medalId, 1);
    }

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> 奖励已获得。");
    qm.earnTitle("<" + medalname + "> 奖励已获得。");
    qm.forceCompleteQuest();
    qm.dispose();
}
