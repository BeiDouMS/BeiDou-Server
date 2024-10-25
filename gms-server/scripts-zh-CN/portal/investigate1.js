function enter(pi) {
    if (pi.isQuestActive(2314) || pi.isQuestCompleted(2314)) {
        pi.openNpc(1300014);
        return true;
    }
    return false;
}