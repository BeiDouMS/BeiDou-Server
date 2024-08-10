/*
	QUEST: James's Whereabouts (3)
	NPC: James
	Why tf does this quest exist?!
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
        return;
    } else if (mode == 0 && status == 0) {
        qm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }

    if (status == 0) {
        qm.sendNext("嘿！谢谢你给我带来了#b#t4001317##k.");
    } else if (status == 1) {
        qm.sendNextPrev("我打算穿着#b#t4001317##k回去.给我一分钟穿上它。然后再跟你聊。。。");
    } else if (status == 2) {
        qm.forceStartQuest();
        qm.dispose();
    }
}
