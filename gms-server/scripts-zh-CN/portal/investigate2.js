function enter(pi) {
    if (pi.isQuestActive(2322) || pi.isQuestCompleted(2322)) {
        pi.openNpc(1300014);
        return true;
    }
    return false;
}