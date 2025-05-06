function enter(pi) {
    var nex = pi.getEventManager("GuardianNex");
    if (nex == null) {
        pi.message("守护者布索 特挑战系统发生错误，当前不可用");  // 保留原意同时符合中文语序‌:ml-citation{ref="1,8" data="citationList"}
        return false;
    }

    var quests = [3719, 3724, 3730, 3736, 3742, 3748];
    var mobs = [7120100, 7120101, 7120102, 8120100, 8120101, 8140510];

    for (var i = 0; i < quests.length; i++) {
        if (pi.isQuestActive(quests[i])) {
            if (pi.getQuestProgressInt(quests[i], mobs[i]) != 0) {
                pi.message("你已挑战过布索，请先完成当前任务");
                return false;
            }

            if (!nex.startInstance(i, pi.getPlayer())) {
                pi.message("布索 正在被挑战，请等待其他玩家完成挑战");
                return false;
            } else {
                pi.playPortalSound();
                return true;
            }
        }
    }

    pi.message("一股神秘的力量阻止你进入。");
    return false;
}