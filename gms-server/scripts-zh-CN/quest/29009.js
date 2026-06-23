var medalId = 1142116;
var medalName = "武陵道场探险家勋章";

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("请探索武陵和百草堂的主要区域。完成探索后，再来领取#b#t" + medalId + "##k。");
    qm.dispose();
}

function end(mode, type, selection) {
    if (!qm.haveItem(medalId) && !qm.canHold(medalId)) {
        qm.sendOk("请在装备栏中空出 1 个位置后再来领取#b" + medalName + "#k。");
        qm.dispose();
        return;
    }

    qm.forceCompleteQuest();
    if (!qm.haveItem(medalId)) {
        qm.gainItem(medalId, 1);
    }
    qm.sendOk("恭喜你完成武陵和百草堂探索！\r\n获得勋章：#b#v" + medalId + "##t" + medalId + "##k");
    qm.dispose();
}
