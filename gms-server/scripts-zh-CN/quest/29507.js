var Medal = Java.type('org.gms.server.quest.medal.SpecialChallengeMedal');

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("将宠物亲密度提升到 #b" + Medal.PET_REQUIRED_TAMENESS + "#k 后，再来领取#b#t" + Medal.PET_OWNER_MEDAL_ID + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    var tameness = Medal.getMaxPetTameness(qm.getPlayer());
    if (tameness < Medal.PET_REQUIRED_TAMENESS) {
        qm.sendOk("你的宠物亲密度还不够。\r\n最高亲密度：#r" + tameness + "#k / " + Medal.PET_REQUIRED_TAMENESS);
        qm.dispose();
        return;
    }
    awardMedal(Medal.PET_OWNER_MEDAL_ID);
}

function awardMedal(medalId) {
    if (!qm.haveItem(medalId)) {
        if (!qm.canHold(medalId)) {
            qm.sendOk("请在装备栏空出 1 个位置。");
            qm.dispose();
            return;
        }
        qm.gainItem(medalId, 1);
    }
    qm.forceCompleteQuest();
    qm.earnTitle(qm.getMedalName());
    qm.sendOk("宠物跟随你，并不是因为命令，而是因为信任。能把这份羁绊培养到如此深厚，本身就是了不起的冒险。\r\n\r\n请收下#b#t" + medalId + "##k。愿所有人都看见你是一位真正可爱的宠物主人。");
    qm.dispose();
}
