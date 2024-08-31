function enter(pi) {
    if (pi.isQuestCompleted(2331)) {
        pi.openNpc(1300013);
        return false;
    }

    if (pi.isQuestCompleted(2333) && pi.isQuestStarted(2331) && !pi.hasItem(4001318)) {
        pi.getPlayer().message("玉玺丢失了？嗯，不用担心！凯文会帮您保密。");
        if (pi.canHold(4001318)) {
            pi.gainItem(4001318, 1);
        } else {
            pi.getPlayer().message("嘿，你背包空间已经满了，如何拿取蘑菇王国玉玺？");
        }
    }

    if (pi.isQuestCompleted(2333)) {
        pi.playPortalSound();
        pi.warp(106021600, 1);
        return true;
    } else if (pi.isQuestStarted(2332) && pi.hasItem(4032388)) {
        pi.forceCompleteQuest(2332, 1300002);
        pi.getPlayer().message("找到了公主！");
        pi.giveCharacterExp(4400, pi.getPlayer());

        var em = pi.getEventManager("MK_PrimeMinister");
        var party = pi.getPlayer().getParty();
        if (party != null) {
            var eli = em.getEligibleParty(pi.getParty());   // thanks Conrad for pointing out missing eligible party declaration here
            if (eli.size() > 0) {
                if (em.startInstance(party, pi.getMap(), 1)) {
                    pi.playPortalSound();
                    return true;
                } else {
                    pi.message("有其它团队正在此频道挑战BOSS。");
                    return false;
                }
            }
        } else {
            if (em.startInstance(pi.getPlayer())) { // thanks RedHat for noticing an issue here
                pi.playPortalSound();
                return true;
            } else {
                pi.message("有其它团队正在此频道挑战BOSS。");
                return false;
            }
        }
    } else if (pi.isQuestStarted(2333) || (pi.isQuestCompleted(2332) && !pi.isQuestStarted(2333))) {
        var em = pi.getEventManager("MK_PrimeMinister");

        var party = pi.getPlayer().getParty();
        if (party != null) {
            var eli = em.getEligibleParty(pi.getParty());
            if (eli.size() > 0) {
                if (em.startInstance(party, pi.getMap(), 1)) {
                    pi.playPortalSound();
                    return true;
                } else {
                    pi.message("有其它团队正在此频道挑战BOSS。");
                    return false;
                }
            }
        } else {
            if (em.startInstance(pi.getPlayer())) {
                pi.playPortalSound();
                return true;
            } else {
                pi.message("有其它团队正在此频道挑战BOSS。");
                return false;
            }
        }
    } else {
        pi.getPlayer().message("门似乎已经被锁住了，需要找到开启门的钥匙……");
        return false;
    }
}
