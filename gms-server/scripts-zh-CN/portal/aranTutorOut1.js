/*
	@Author: kevintjuh93
*/
function enter(pi) {
    if (pi.isQuestStarted(21000)) {
        //lol nexon does this xD
        pi.teachSkill(20000017, 0, -1, -1);
        pi.teachSkill(20000018, 0, -1, -1);
        //nexon sends updatePlayerStats Stat.AVAILABLESP 0
        pi.teachSkill(20000017, 1, 0, -1);
        pi.teachSkill(20000018, 1, 0, -1);
        //actually nexon does enableActions here :P
        pi.playPortalSound();
        pi.warp(914000200, 1);
        return true;
    } else {
        pi.message("你只有在接受你右边的雅典娜·皮尔斯的任务后才能退出。");
        return false;
    }
}