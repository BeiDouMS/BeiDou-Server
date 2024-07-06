/**
 * @author: Eric
 * @npc: Sgt. Anderson
 * @maps: Ludibrium PQ Maps
 * @func: Ludi PQ (Warps you out)
 */

var status = -1;

function start() {
    if (cm.getMapId() != 922010000 && cm.getMapId() != 922010800) {
        cm.sendYesNo("如果你想在离开这个阶段后再次尝试这个任务，你将不得不从头开始。你确定要离开这张地图吗？");
    } else if (cm.getMapId() == 922010800) {
        cm.sendSimple("你需要帮助吗？#b\r\n#L0#我需要平台傀儡。#l\r\n#L1#我想离开这里。#l#k");
    } else {
        cm.removeAll(4001022); // pass of dimension
        cm.removeAll(4001023);
        cm.removeAll(4001454); // platform puppet
        cm.warp(221024500, 0);
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else if (mode == 0 && (status == 0 || status == -1)) {
        cm.sendNext("我明白了。集合你的队友的力量，再努力一点！");
        cm.dispose();
        return;
    } else {
        status--;
    }
    if (status == 0) {
        if (cm.getMapId() == 922010800) {
            if (selection == 0) {
                cm.sendNext("你已经获得了一个平台傀儡。如果你把它放在平台上，它将产生与某人站在那里相同的效果。记住，这是一个只能在这里使用的物品。");
                cm.gainItem(4001454, 1);
                cm.dispose();
            } else {
                cm.sendYesNo("如果你想在离开这个阶段后再次尝试这个任务，你将不得不从头开始。你确定要离开这张地图吗？");
            }
        } else {
            var eim = cm.getPlayer().getEventInstance();
            if (eim != null) {
                eim.removePlayer(cm.getPlayer());
            } else {
                cm.warp(922010000, 0);
            }
            cm.dispose();
        }
    } else if (status == 1) {
        var eim = cm.getPlayer().getEventInstance();
        if (eim != null) {
            eim.removePlayer(cm.getPlayer());
        } else {
            cm.warp(922010000, 0);
        }
        cm.dispose();
    }
}