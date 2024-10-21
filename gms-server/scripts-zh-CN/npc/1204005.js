/*
 * NPC : Tru
 * Map : 910400000
 */

var questID = 21733;
var questInfoNumber = 21762;
var puppeteerMobID = 9300345;
var normalInfoShopMapID = 104000004;

var dialogDepth = -1;

function start() {
    dialogDepth = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) { // END CHAT
        cm.dispose();
        return;
    }

    if (type == 0 && mode == 0) { // PREV
        dialogDepth--;
    } else {
        dialogDepth++;
    }

    if (cm.getQuestProgressInt(questID, puppeteerMobID) < 1) {
        cm.sendDefault();
        cm.dispose();
        return;
    }

    switch (dialogDepth) {
        case 0:
            cm.sendNext('啊……那些家伙全都消灭了？哈哈……真不愧是英雄大人！呃，先整理整理再说。');
            break;
        case 1:
            cm.setQuestProgress(questID, questInfoNumber, 2);
            cm.warp(normalInfoShopMapID);
            cm.dispose();
            break;
        default:
            cm.dispose();
            break;
    }
}