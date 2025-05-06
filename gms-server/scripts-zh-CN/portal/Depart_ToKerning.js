function enter(pi) {
    var em = pi.getEventManager("KerningTrain");
    if (!em.startInstance(pi.getPlayer())) {
        pi.message("本次班车已满客，请等候下一班。");
        return false;
    }

    pi.playPortalSound();
    return true;
}