/*
 	Author: GitHub:@Magical-H
 	Map: Mushroom Castle - Deep Inside Mushroom Forest (106020300)
 	Right Portal
 */

function enter(pi) {
    if (pi.isQuestStarted(100202) || pi.isQuestCompleted(100202)) {
        pi.playPortalSound();
        pi.warp(106020400, 2);
        return true;
    }

    pi.showInfo("Effect/OnUserEff/normalEffect/mushroomcastle/chatBalloon1");
    pi.message("\u4f3c\u4e4e\u6709\u4e00\u4e2a\u9b54\u529b\u5f3a\u5927\u7684\u7ed3\u754c\u963b\u6b62\u4f60\u8fdb\u5165\u3002");
    return false;
}
