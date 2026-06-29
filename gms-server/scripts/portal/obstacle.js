/*
 	Author: Ronan
 	Map: Mushroom Castle - Deep Inside Mushroom Forest (106020300)
 	Right Portal
 */

function enter(pi) {
    if (pi.isQuestStarted(100202) || pi.isQuestCompleted(100202)) {
        pi.playPortalSound();
        pi.warp(106020400, 2);
        return true;
    }

    pi.message("A powerful magic barrier is blocking the way.");
    return false;
}
