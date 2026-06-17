var medalId = 1142115;
var medalName = "Undersea Explorer Medal";

function start(mode, type, selection) {
    qm.forceStartQuest();
    qm.sendOk("Explore the main regions of Aqua Road. Come back after completing the exploration to receive #b#t" + medalId + "##k.");
    qm.dispose();
}

function end(mode, type, selection) {
    if (!qm.haveItem(medalId) && !qm.canHold(medalId)) {
        qm.sendOk("Please make 1 free slot in your Equip inventory before receiving the #b" + medalName + "#k.");
        qm.dispose();
        return;
    }

    qm.forceCompleteQuest();
    if (!qm.haveItem(medalId)) {
        qm.gainItem(medalId, 1);
    }
    qm.sendOk("Congratulations on completing the Aqua Road exploration!\r\nMedal received: #b#v" + medalId + "##t" + medalId + "##k");
    qm.dispose();
}
