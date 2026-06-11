/*
	QUEST: Re-acquiring Alcadno Cape
	NPC: Maed
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1) {
        qm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            qm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            qm.sendNext("你把#b卡帕莱特披风#k弄丢了。我可以再给你做一件，但需要一些材料。");
        } else if (status == 1) {
            qm.sendAcceptDecline("要制作新披风，请给我带来#b5个#t4021006##k、#b10个#t4000021##k和#b10000金币#k。");
        } else if (status == 2) {
            qm.sendOk("等你收集到了所有材料再回来找我。");
            qm.forceStartQuest();
        } else if (status == 3) {
            qm.dispose();
        }
    }
}
