/*
	NPC: Blocked Entrance (portal?)
	MAP: Mushroom Castle - East Castle Tower (106021400)
*/

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
        return;
    } else if (mode == 0 && status == 0) {
        cm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }


    if (cm.getMapId() == 106021402) {
        if (!(cm.isQuestCompleted(2331))) {
            cm.dispose();
            return;
        }

        if (status == 0) {
            cm.sendSimple("#L0#进入战斗 #b帕比国王#k 和 #b雪人兄弟#k。#l\r\n#L1#进入战斗 #b总理#k。#l");
        } else if (status == 1) {
            if (selection == 0) {
                var pepe = cm.getEventManager("KingPepeAndYetis");
                pepe.setProperty("player", cm.getPlayer().getName());
                pepe.startInstance(cm.getPlayer());
                cm.dispose();

            } else if (selection == 1) {
                var em = cm.getEventManager("MK_PrimeMinister2");

                var party = cm.getPlayer().getParty();
                if (party != null) {
                    if (!em.startInstance(party, cm.getMap(), 1)) {
                        cm.sendOk("另一个队伍已经在这个频道挑战boss了。");
                    }
                } else {
                    if (!em.startInstance(cm.getPlayer())) {
                        cm.sendOk("另一个队伍已经在这个频道挑战boss了。");
                    }
                }

                cm.dispose();

            }
        }
    } else {
        var questProgress = cm.getQuestProgressInt(2330, 3300005) + cm.getQuestProgressInt(2330, 3300006) + cm.getQuestProgressInt(2330, 3300007); //3 Yetis
        if (!(cm.isQuestStarted(2330) && questProgress < 3)) {  // thanks Vcoc for finding an exploit with boss entry through NPC
            cm.dispose();
            return;
        }

        if (status == 0) {
            cm.sendSimple("#L1#进入挑战 #b帕普王#k 和 #b雪人兄弟#k。#l");
        } else if (status == 1) {
            if (selection == 1) {
                var pepe = cm.getEventManager("KingPepeAndYetis");
                pepe.setProperty("player", cm.getPlayer().getName());
                pepe.startInstance(cm.getPlayer());
                cm.dispose();

            }
        }
    }
}