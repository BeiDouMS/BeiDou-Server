var status = -1;

function start(mode, type, selection) {
    var familyEntry = qm.getPlayer().getFamilyEntry();
    if (familyEntry != null && familyEntry.getJuniorCount() > 0) {
        qm.forceCompleteQuest();
        qm.gainExp(3000);
        qm.sendNext("做得很好！");
    } else {
        qm.sendNext("你还没有找到一个同学吗？");
    }
    qm.dispose();
}

function end(mode, type, selection) {
    var familyEntry = qm.getPlayer().getFamilyEntry();
    if (familyEntry != null && familyEntry.getJuniorCount() > 0) {  // script found thanks to kvmba
        qm.forceCompleteQuest();
        qm.gainExp(3000);
        qm.sendNext("做得很好！");
    } else {
        qm.sendNext("你还没有找到一个同学吗？");
    }
    qm.dispose();
}
