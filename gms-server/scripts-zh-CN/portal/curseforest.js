function enter(pi) {
    // 任务状态检测
    if (pi.isQuestStarted(2224) || pi.isQuestStarted(2226) || pi.isQuestCompleted(2227)) {
        var hourDay = pi.getHourOfDay();

        // 时间限制检测（0-7点或17-24点）
        if (!((hourDay >= 0 && hourDay < 7) || hourDay >= 17)) {
            pi.getPlayer().dropMessage(5, "当前时段禁止进入该区域");  // 时间限制提示
            return false;
        } else {
            // 传送逻辑
            pi.playPortalSound();
            pi.warp(pi.isQuestCompleted(2227) ? 910100001 : 910100000, "out00");
            return true;
        }
    }

    // 默认拒绝提示
    pi.getPlayer().dropMessage(5, "尚未满足进入条件");  // 任务限制提示‌
    return false;
}