/*
	QUEST: 消灭余党
	NPC: 维奥莉塔
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        qm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }


    if (status == 0) {
        qm.sendNext("这还不是结束，#b#h ##k。 #b首相#k 的手下仍然散布在城堡中。");
    } else if (status == 1) {
        qm.sendAcceptDecline("据我所知，靠近#b天空高楼 3#k附近有一群首相的手下。前几天我捡到了首相掉落的一把钥匙。拿去吧，用这把钥匙。");
    } else if (status == 2) {
        if (qm.canHold(4032405)) {
            qm.gainItem(4032405, 1);
            qm.sendNext("最后一次，祝你好运。");
        } else {
            qm.sendOk("请确保你的其他栏有空位。");
            qm.dispose();
        }
    } else if (status == 3) {
        qm.forceStartQuest();
        qm.dispose();
    }
}

function end(mode, type, selection) {}
