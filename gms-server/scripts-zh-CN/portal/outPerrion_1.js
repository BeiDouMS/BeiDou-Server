function enter(pi) {
    pi.message("你发现了通往地下神殿入口的捷径");
    pi.playPortalSound();
    pi.warp(105100000, 2);
    return true;
}