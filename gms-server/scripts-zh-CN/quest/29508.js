var medalId = 1142081;
var Quest = Java.type('org.gms.server.quest.Quest');

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

function tryAwardMedal() {
    var missing = getMissingRequirements();
    if (missing.length > 0) {
        qm.sendOk("你还没有满足全部条件，请先：" + missing.join("、") + "。");
        Quest.getInstance(29508).reset(qm.getPlayer());
        qm.dispose();
        return false;
    }

    if (!qm.haveItem(medalId)) {
        if (!qm.canHold(medalId)) {
            qm.sendOk("请在装备栏中空出 1 个位置后再来领取勋章。");
            qm.dispose();
            return false;
        }
        qm.gainItem(medalId, 1);
    }

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> 奖励已获得。");
    qm.earnTitle(medalname);
    qm.forceCompleteQuest();
    qm.dispose();
    return true;
}

function start(mode, type, selection) {
    tryAwardMedal();
}

function end(mode, type, selection) {
    tryAwardMedal();
}
