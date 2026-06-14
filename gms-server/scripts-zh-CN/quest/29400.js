var VeteranHunterMedal = Java.type('org.gms.server.quest.medal.VeteranHunterMedal');
var medalId = VeteranHunterMedal.MEDAL_ID;
var requiredKills = VeteranHunterMedal.REQUIRED_KILLS;

function progressText() {
    return VeteranHunterMedal.getProgress(qm.getPlayer()) + " / " + requiredKills;
}

function start(mode, type, selection) {
    var alreadyHasMedal = qm.haveItem(medalId);
    qm.forceStartQuest();

    var message = "挑战已经开始！请在#r30天#k内狩猎#r100000只#k符合条件的怪物。";
    if (alreadyHasMedal) {
        message += "\r\n\r\n你已经拥有#b勤奋冒险家勋章#k，本次挑战会继续计数，但完成时不会重复发放勋章。";
    }
    message += "\r\n\r\n#b计数规则#k\r\n- 120级以下角色：只统计等级#r高于自身等级#k的怪物。\r\n- 120级及以上角色：只统计等级#r120级及以上#k的怪物。\r\n\r\n当前狩猎数：#r" + progressText() + "#k";
    qm.sendOk(message);
    qm.dispose();
}

function end(mode, type, selection) {
    if (!VeteranHunterMedal.isComplete(qm.getPlayer())) {
        qm.sendOk("你还没有达到#r100000只#k的狩猎目标。\r\n\r\n当前狩猎数：#r" + progressText() + "#k");
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
    qm.dispose();
}
