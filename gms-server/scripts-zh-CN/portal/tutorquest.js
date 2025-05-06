function enter(pi) {
    const player = pi.getPlayer();
    const currentMap = player.getMapId();

    // 根据不同地图ID进行传送判断
    switch(currentMap) {
        case 130030001: // 新手起始地图
            if (pi.isQuestStarted(20010)) {
                pi.playPortalSound();
                pi.warp(130030002, 0); // 传送到下一地图
                return true;
            } else {
                pi.message("请先与NPC对话接取任务");
            }
            break;

        case 130030002: // 初级任务地图
            if (pi.isQuestCompleted(20011)) {
                pi.playPortalSound();
                pi.warp(130030003, 0);
                return true;
            } else {
                pi.message("请先完成当前任务再继续前进");
            }
            break;

        case 130030003: // 中级任务地图
            if (pi.isQuestCompleted(20012)) {
                pi.playPortalSound();
                pi.warp(130030004, 0);
                return true;
            } else {
                pi.message("请先通过本关试炼任务");
            }
            break;

        case 130030004: // 高级任务地图
            if (pi.isQuestCompleted(20013)) {
                pi.playPortalSound();
                pi.warp(130030005, 0);
                return true;
            } else {
                pi.message("需完成最终试炼才能进入圣地");
            }
            break;
    }

    return false;
}