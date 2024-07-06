/**
 * @author: Ronan
 * @npc: Ellin
 * @map: Ellin PQ
 * @func: Ellin PQ Coordinator
 */

var status = 0;
var mapid;

function start() {
    mapid = cm.getPlayer().getMapId();

    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            var ellinStr = ellinMapMessage(mapid);

            if (mapid == 930000000) {
                cm.sendNext(ellinStr);
            } else if (mapid == 930000300) {
                var eim = cm.getEventInstance();

                if (eim.getIntProperty("statusStg4") == 0) {
                    eim.showClearEffect(cm.getMap().getId());
                    eim.setIntProperty("statusStg4", 1);
                }

                cm.sendNext(ellinStr);
            } else if (mapid == 930000400) {
                if (cm.haveItem(4001169, 20)) {
                    if (cm.isEventLeader()) {
                        cm.sendNext("哦，你带来了它们！我们现在可以继续了，我们要继续吗？");
                    } else {
                        cm.sendOk("你已经带来了他们，但你不是队长！请让队长把弹珠给我……");
                        cm.dispose();

                    }
                } else {
                    if (cm.getEventInstance().gridCheck(cm.getPlayer()) != 1) {
                        cm.sendNext(ellinStr);

                        cm.getEventInstance().gridInsert(cm.getPlayer(), 1);
                        status = -1;
                    } else {
                        var mobs = cm.getMap().countMonsters();

                        if (mobs > 0) {
                            if (!cm.haveItem(2270004)) {
                                if (cm.canHold(2270004, 10)) {
                                    cm.gainItem(2270004, 10);
                                    cm.sendOk("拿10个#t2270004#。首先，#r削弱#o9300174#的力量，一旦它的生命值降低，使用我给你的物品来捕捉它们。");
                                    cm.dispose();

                                } else {
                                    cm.sendOk("在领取净化器之前，请确保你的使用物品栏有足够的空间！");
                                    cm.dispose();

                                }
                            } else {
                                cm.sendYesNo(ellinStr + "\r\n\r\nIt may be you are #rwilling to quit#k? Please double-think it, maybe your partners are still trying this instance.");
                            }
                        } else {
                            cm.sendYesNo("你们已经捕捉到了所有的 #o9300174#。让队长把所有的 #b20 #t4001169##k 给我，然后我们继续。" + "\r\n\r\n也许你是 #rwilling to quit#k？请三思，也许你的队友还在努力尝试这个副本。");
                        }
                    }
                }
            } else {
                cm.sendYesNo(ellinStr + "\r\n\r\nIt may be you are #rwilling to quit#k? Please double-think it, maybe your partners are still trying this instance.");
            }
        } else if (status == 1) {
            if (mapid == 930000000) {
            } else if (mapid == 930000300) {
                cm.getEventInstance().warpEventTeam(930000400);
            } else if (mapid == 930000400) {
                if (cm.haveItem(4001169, 20) && cm.isEventLeader()) {
                    cm.gainItem(4001169, -20);
                    cm.getEventInstance().warpEventTeam(930000500);
                } else {
                    cm.warp(930000800, 0);
                }
            } else {
                cm.warp(930000800, 0);
            }

            cm.dispose();
        }
    }
}

function ellinMapMessage(mapid) {
    switch (mapid) {
        case 930000000:
            return "Welcome to the Forest of Poison Haze. Proceed by entering the portal.";

        case 930000100:
            return "The #b#o9300172##k have taken the area. We have to eliminate all these contaminated monsters to proceed further.";

        case 930000200:
            return "A great spine has blocked the way ahead. To remove this barrier we must retrieve the poison the #b#o9300173##k carries to deter the overgrown spine. However, the poison in natural state can't be handled, as it is way too concentrated. Use the #bfountain#k over there to dilute it.";

        case 930000300:
            return "Oh great, you have reached me. We can now proceed further inside the forest.";

        case 930000400:
            return "The #b#o9300175##k took over this area. However they are not ordinary monsters, then regrow pretty fast, #rnormal weapon and magic does no harm to it#k at all. We have to purify all these contaminated monsters, using #b#t2270004##k! Let your group leader get me 20 Monster Marbles from them.";

        case 930000600:
            return "The root of all problems of the forest! Place the obtained Magic Stone on the Altar and prepare yourselves!";

        case 930000700:
            return "This is it, you guys did it! Thank you so much for purifying the forest!!";

    }
}