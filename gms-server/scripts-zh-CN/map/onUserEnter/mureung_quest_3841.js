function start(ms) {
    if (ms.getQuestStatus(3840) == 2 && ms.getQuestStatus(3841) == 0 && ms.getPlayer().getLevel() >= 77 && ms.getPlayer().getMapId() == 250010503) {
        ms.openNpc(2091004, "mureung_quest_3841");
    }
}
