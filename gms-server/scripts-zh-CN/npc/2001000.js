/*
 *  Cliff - Happy Ville NPC
 */

var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        if (status > 0) {
            status--;
        } else {
            cm.dispose();
            return;
        }
    }
    if (status == 0) {
        cm.sendNext("看到那边的雪人了吗？和他们对话，就能前往幸福村著名的巨大圣诞树房间。你可以把各种装饰品挂在树上，亲手布置属于自己的圣诞树。");
    } else if (status == 1) {
        cm.sendNextPrev("每个圣诞树房间最多容纳6个人，而且不能交易或开设商店。你丢下的装饰品只有你自己能捡回，所以不用担心被别人拿走。");
    } else if (status == 2) {
        cm.sendNextPrev("掉在圣诞树房间里的装饰品不会消失。离开房间时，系统会把你放下的物品归还给你，不需要临走前一件件捡起来。");
    } else if (status == 3) {
        cm.sendPrev("想装饰圣诞树的话，先去找#p2002001#购买装饰品吧。对了，最大也最漂亮的装饰品可买不到，听说它被怪物拿走了……");
        cm.dispose();
    }
}
