/*
	NPC Name: 		The Forgotten Temple Manager
	Map(s): 		Deep in the Shrine - Twilight of the gods
	Description: 		Pink Bean
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        var eim = cm.getEventInstance();
        if (!eim.isEventCleared()) {
            if (status == 0) {
                cm.sendYesNo("你现在想出去吗？");
            } else if (status == 1) {
                cm.warp(270050000, 0);
                cm.dispose();
            }

        } else {
            if (status == 0) {
                cm.sendYesNo("粉色豆豆已经被打败了！你们真是这片土地上真正的英雄！很快，时间神殿将再次闪耀如初，这都归功于你们的努力！向我们的英雄们欢呼！！你们准备好走了吗？");
            } else if (status == 1) {
                if (eim.giveEventReward(cm.getPlayer(), 1)) {
                    cm.warp(270050000);
                } else {
                    cm.sendOk("如果你的装备、使用、设置和其他物品栏中没有空位，你将无法获得实例奖励。");
                }

                cm.dispose();
            }
        }
    }
}