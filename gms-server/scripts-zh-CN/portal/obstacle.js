/*
 	Author: GitHub:@Magical-H
 	Map: Mushroom Castle - Deep Inside Mushroom Forest (106020300)  蘑菇森林深处
 	Right Portal    右传送点
 */

function enter(pi) {
    if (pi.isQuestStarted(100202)) {    //使用过奇拉蘑菇孢子后允许直接通过
        pi.playPortalSound();
        pi.warp(106020400, 2);
        return true;
    } else if (pi.hasItem(4000507)) {
        pi.gainItem(4000507, -1);
        pi.playPortalSound();
        pi.warp(106020400, 2);
        pi.message(`消耗一个 蘑菇的毒孢子 通过了结界。`);
        return true;
    } else {
        pi.showInfo("Effect/OnUserEff/normalEffect/mushroomcastle/chatBalloon1");
        pi.message("似乎有一个魔力强大的结界阻止你进入。");
    }
    return false;
}