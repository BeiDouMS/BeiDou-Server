var OutstandingCitizenMedal = Java.type('org.gms.server.quest.medal.OutstandingCitizenMedal');
var medalId = OutstandingCitizenMedal.MEDAL_ID;

function getMissingRequirements() {
    var missing = [];
    var player = qm.getPlayer();
    var familyEntry = player.getFamilyEntry();

    if (!player.isMarried()) {
        missing.push("完成结婚：与另一名角色完成婚礼，并佩戴有效婚戒");
    }
    if (player.getGuildId() <= 0) {
        missing.push("加入公会：角色当前必须属于任意公会");
    }
    if (familyEntry == null || familyEntry.getJuniorCount() < 1) {
        missing.push("拥有后辈：家族系统中至少登记 1 名后辈");
    }

    return missing;
}

function formatMissingRequirements(missing) {
    var text = "你还没有满足全部条件。领取#b最佳公民勋章#k需要同时完成以下 3 项：\r\n";
    text += "#b- 完成结婚#k：与另一名角色完成婚礼，并佩戴有效婚戒。\r\n";
    text += "#b- 加入公会#k：角色当前必须属于任意公会。\r\n";
    text += "#b- 拥有后辈#k：家族系统中至少登记 1 名后辈。\r\n\r\n";
    text += "目前还缺少：\r\n#r- " + missing.join("\r\n- ") + "#k\r\n\r\n";
    text += "请补齐缺少的条件后再来找我领取勋章。";
    return text;
}

function start(mode, type, selection) {
    qm.forceStartQuest();
    OutstandingCitizenMedal.refreshEligibility(qm.getPlayer());

    var missing = getMissingRequirements();
    if (missing.length > 0) {
        qm.sendOk(formatMissingRequirements(missing));
    } else {
        qm.sendOk("你已经同时满足结婚、公会和后辈条件。请再次和我对话领取#b最佳公民勋章#k。");
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
            qm.sendOk("请在装备栏中空出 1 个位置后再来领取勋章。");
            qm.dispose();
            return;
        }
        qm.gainItem(medalId, 1);
    }

    var medalname = qm.getMedalName();
    qm.message("<" + medalname + "> 奖励已获得。");
    qm.earnTitle(medalname);
    qm.forceCompleteQuest();
    OutstandingCitizenMedal.clearEligibility(qm.getPlayer());
    qm.dispose();
}
