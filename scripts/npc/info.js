/* @Author vcLee
use for command @info
*/

let status, expMsg = "", mesoMsg = "", dropMsg = "";
let showExtra = false;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode === -1) {
        cm.dispose();
    } else {
        if (mode === 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode === 1) {
            status++;
        } else {
            status--;
        }

        let player = cm.getPlayer();

        if (status === 0) {
            let msg = "";
            let lv = player.getLevel();
            msg += "角色名: #r" + player.getName() + "#k  等级 #b" + lv + "#k\r\n";
            msg += "点券: #b" + player.getCashShop().getCash(1) + "#k 抵用券: #b" + player.getCashShop().getCash(2) + "#k PQ积分: #b" + player.getPQPoint() + "\r\n\r\n";
            msg += "#b#L0#查询倍率#l  #L1#传送市场#l  #L2#超级脸型#l  #L3#超级发型#l\r\n\r\n"
            cm.sendSimple(msg);
        } else if (status === 1) {
            if (selection === 0) {
                let msg = "";
                let world = player.getWorldServer();
                msg += "玩家信息\r\n"
                expMsg = player.hasNoviceExpRate() ? " - 新手限制" : "";
                if (expMsg === "" && showExtra) expMsg = " (基础: #e#b" + player.getRawExpRate() + "x#n#k" + " 加成卡: #e#b" + player.getCouponExpRate() + "x#n#k)"
                msg += "经验加成: #e#b" + player.getExpRate() + "x#n#k" + expMsg + "\r\n";
                msg += "怪物经验: #e#b" + player.getMobExpRate().toFixed(2) + "x#n#k\r\n";
                if (showExtra) mesoMsg = " (基础: #e#b" + player.getRawMesoRate() + "x#n#k" + " 加成卡: #e#b" + player.getCouponMesoRate() + "x#n#k)"
                msg += "金币爆率: #e#b" + player.getMesoRate() + "x#k#n" + mesoMsg + "\r\n";
                if (showExtra) dropMsg = " (基础: #e#b" + player.getRawDropRate() + "x#n#k" + " 加成卡: #e#b" + player.getCouponDropRate() + "x#n#k)"
                msg += "物品爆率: #e#b" + player.getDropRate() + "x#k#n" + dropMsg + "\r\n";
                msg += "BOSS爆率: #e#b" + player.getBossDropRate() + "x#k#n" + dropMsg + "\r\n\r\n";
                msg += "GMSE 服务器信息\r\n"
                msg += "经验加成: #e#b" + world.getExpRate() + "x#n#k  ";
                msg += "金币爆率: #e#b" + world.getMesoRate() + "x#k#n  ";
                msg += "物品爆率: #e#b" + world.getDropRate() + "x#k#n  ";
                msg += "BOSS爆率: #e#b" + world.getBossDropRate() + "x#k#n" + "\r\n";
                msg += "任务奖励: #e#b" + world.getQuestRate() + "x#k#n\r\n";
                cm.sendOk(msg);
                cm.dispose();
            } else if (selection === 1) {
                let cmd = Java.type("client.command.CommandsExecutor");
                cmd.getInstance().handle(cm.getClient(), "@fm");
                cm.dispose();
            } else if (selection === 2) {
                cm.dispose();
                cm.openNpc(9201088, "脸型");
            } else if (selection === 3) {
                cm.dispose();
                cm.openNpc(9201088, "发型");
            }
        }
    }
}

function generateSelectionMenu(array) {     // nice tool for generating a string for the sendSimple functionality
    let menu = "";
    for (let i = 0; i < array.length; i++) {
        menu += "#L" + i + "#" + array[i] + "#l\r\n";
    }
    return menu;
}