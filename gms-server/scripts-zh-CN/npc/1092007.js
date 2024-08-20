/*
NPC:        Muirhat - Nautilus' Port
Created By: Kevin
Function:   When on the quest, he warps player to Black Magician's Disciple
*/

var status;

function start() {

    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.getQuestStatus(2175) == 1) {
                if (cm.getPlayer().canHold(2030019)) {
                    cm.sendOk("请拿着这个 #b#t2030019##k，它会让你的生活变得轻松很多。 #i2030019#");
                } else {
                    cm.sendOk("没有可用的免费物品栏位。请先在您的使用物品栏中腾出空间。");
                    cm.dispose();
                }
            } else {
                cm.sendOk("黑魔法师及其追随者。凯琳和诺提勒斯船员。他们会互相追逐，直到其中一个消失，这是肯定的。");
                cm.dispose();
            }
        } else if (status == 1) {
            cm.warp(912000000, 0);
            cm.dispose();
        }
    }
}