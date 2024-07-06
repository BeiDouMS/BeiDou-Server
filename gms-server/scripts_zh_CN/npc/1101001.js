/*
   NPC Name: 		Divine Bird
   Map(s): 		Erev
   Description: 		3rd job KoC Buff
*/

function start() {
    const GameConstants = Java.type('org.gms.constants.game.GameConstants');
    if (cm.getPlayer().isCygnus() && GameConstants.getJobBranch(cm.getJob()) > 2) {
        cm.useItem(2022458);
        cm.sendOk("让我为你祈福，我的骑士。请保护冒险岛的世界……");
    } else {
        cm.sendOk("不要停止训练。你的每一分能量都需要用来保护冒险岛的世界……");
    }

    cm.dispose();
}