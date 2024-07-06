// NPC - Wall
// Location: Magatia - Home of the Missing Alchemist
// Used to handle quest 3311 - Clue

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

        if (!cm.isQuestStarted(3311)) {
            cm.dispose();
            return;
        }

        if (status == 0) {
            cm.sendYesNo("在一片蜘蛛网的拥挤中，有一堵墙后面似乎写着什么东西。也许你应该仔细看看墙？");
        }
        else if (status == 1) {
            cm.setQuestProgress(3311, 5);
            cm.sendOk("在一面满是涂鸦的墙上，似乎有一句话格外显眼。#b它是以一种吊坠的形式出现……#k 这是什么意思？");
        }
        else {
            cm.dispose();
        }
    }
}