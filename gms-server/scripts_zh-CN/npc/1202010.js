var status = 0;

var spawnPnpc = false;
var spawnPnpcFee = 7000000;
var jobType = 21;

function start() {
    const GameConstants = Java.type('org.gms.constants.game.GameConstants');
    if (parseInt(cm.getJobId() / 100) == jobType && cm.canSpawnPlayerNpc(GameConstants.getHallOfFameMapid(cm.getJob()))) {
        spawnPnpc = true;

        var sendStr = "You have walked a long way to reach the power, wisdom and courage you hold today, haven't you? What do you say about having right now #ra NPC on the Hall of Fame holding the current image of your character#k? Do you like it?";
        if (spawnPnpcFee > 0) {
            sendStr += " I can do it for you, for the fee of #b " + cm.numberWithCommas(spawnPnpcFee) + " mesos.#k";
        }

        cm.sendYesNo(sendStr);
    } else {
        cm.sendOk("瞧，这里是利恩的杰出勇敢英雄！那些坚定的心灵一直以来一直在保护我们的人民，他们是我们勇敢的战友。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    status++;
    if (mode == 0 && type != 1) {
        status -= 2;
    }
    if (status == -1) {
        start();

    } else {
        if (spawnPnpc) {
            if (mode > 0) {
                if (cm.getMeso() < spawnPnpcFee) {
                    cm.sendOk("抱歉，您没有足够的金币购买在名人堂上的位置。");
                    cm.dispose();
                    return;
                }

                const PlayerNPC = Java.type('org.gms.server.life.PlayerNPC');
                const GameConstants = Java.type('org.gms.constants.game.GameConstants');
                if (PlayerNPC.spawnPlayerNPC(GameConstants.getHallOfFameMapid(cm.getJob()), cm.getPlayer())) {
                    cm.sendOk("给你了！希望你会喜欢它。");
                    cm.gainMeso(-spawnPnpcFee);
                } else {
                    cm.sendOk("抱歉，名人堂目前已满...");
                }
            }

            cm.dispose();

        } else {
            // do nothing
        }
    }
}