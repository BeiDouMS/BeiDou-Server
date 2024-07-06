var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    var eim = cm.getEventInstance();
    if (eim != null && eim.getIntProperty("glpq6") == 3) {
        cm.sendOk("干得漂亮。你超越了扭曲大师。通过那扇门领取你的奖品。");
        cm.dispose();
        return;
    }

    if (!cm.isEventLeader()) {
        cm.sendNext("我希望你们的领导和我谈谈。");
        cm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
        status--;
    }

    if (eim != null) {
        if (eim.getIntProperty("glpq6") == 0) {
            if (status == 0) {
                cm.sendNext("欢迎来到扭曲大师的堡垒。我将是今晚的主持人…");
            } else if (status == 1) {
                cm.sendNext("今晚，我们有一群冒险岛玩家的盛宴.. 哈哈哈...");
            } else if (status == 2) {
                cm.sendNext("让我们经过特别训练的守护大师护送你！");
                cm.mapMessage(6, "Engarde! Master Guardians approach!");
                for (var i = 0; i < 10; i++) {
                    var mob = eim.getMonster(9400594);
                    const xPos = Math.floor(-1337 + (Math.random() * 1337))
                    cm.getMap().spawnMonsterOnGroundBelow(mob, new java.awt.Point(xPos, 276));
                }
                for (var i = 0; i < 20; i++) {
                    var mob = eim.getMonster(9400582);
                    const xPos = Math.floor(-1337 + (Math.random() * 1337))
                    cm.getMap().spawnMonsterOnGroundBelow(mob, new java.awt.Point(xPos, 276));
                }
                eim.setIntProperty("glpq6", 1);
                cm.dispose();
            }
        } else if (eim.getIntProperty("glpq6") == 1) {
            if (cm.getMap().countMonsters() == 0) {
                if (status == 0) {
                    cm.sendOk("嗯，这是什么？你打败了它们？");
                } else if (status == 1) {
                    cm.sendNext("好吧，无论如何！扭曲之主将很高兴欢迎你。");
                    cm.mapMessage(6, "Twisted Masters approach!");

                    //Margana
                    var mob = eim.getMonster(9400590);
                    cm.getMap().spawnMonsterOnGroundBelow(mob, new java.awt.Point(-22, 1));

                    //Red Nirg
                    var mob2 = eim.getMonster(9400591);
                    cm.getMap().spawnMonsterOnGroundBelow(mob2, new java.awt.Point(-22, 276));

                    //Hsalf
                    var mob4 = eim.getMonster(9400593);
                    cm.getMap().spawnMonsterOnGroundBelow(mob4, new java.awt.Point(496, 276));

                    //Rellik
                    var mob3 = eim.getMonster(9400592);
                    cm.getMap().spawnMonsterOnGroundBelow(mob3, new java.awt.Point(-496, 276));

                    eim.setIntProperty("glpq6", 2);
                    cm.dispose();
                }
            } else {
                cm.sendOk("不要理我。主守护者会护送你！");
                cm.dispose();
            }
        } else if (eim.getIntProperty("glpq6") == 2) {
            if (cm.getMap().countMonsters() == 0) {
                cm.sendOk("什么？呃...这不可能发生。");
                cm.mapMessage(5, "The portal to the next stage has opened!");
                eim.setIntProperty("glpq6", 3);

                eim.showClearEffect(true);
                eim.giveEventPlayersStageReward(6);

                eim.clearPQ();
                cm.dispose();
            } else {
                cm.sendOk("不要理会我。扭曲之主会护送你！");
                cm.dispose();
            }
        } else {
            cm.sendOk("干得漂亮。你超越了扭曲大师。通过那扇门领取你的奖品。");
            cm.dispose();
        }
    } else {
        cm.dispose();
    }
}