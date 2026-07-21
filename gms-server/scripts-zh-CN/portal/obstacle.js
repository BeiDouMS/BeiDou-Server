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
    pi.message("似乎有一个魔力强大的结界阻止你进入。");
    return false;
}
