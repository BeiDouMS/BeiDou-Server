/**
 * @author: Ronan
 * @npc: Chamberlain Eak
 * @map: Orbis - Tower of Goddess
 * @func: Orbis PQ
 */

var status = 0;
var em = null;

function isStatueComplete() {
    for (var i = 1; i <= 6; i++) {
        if (cm.getMap().getReactorByName("scar" + i).getState() < 1) {
            return false;
        }
    }

    return true;
}

function clearStage(stage, eim) {
    eim.setProperty("statusStg" + stage, "1");
    eim.showClearEffect(true);
}

function start() {
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

        if (cm.getPlayer().getMapId() == 920011200) { //exit
            cm.warp(200080101);
            cm.dispose();
            return;
        }
        if (!cm.isEventLeader()) {
            if (cm.getPlayer().getMapId() == 920010000) {
                cm.warp(920010000, 2);
                cm.dispose();
                return;
            }

            cm.sendOk("我只想和你们的队长谈话！");
            cm.dispose();
            return;
        }

        var eim = cm.getEventInstance();

        switch (cm.getPlayer().getMapId()) {
            case 920010000:
                if (eim.getIntProperty("statusStg0") != 1) {
                    eim.warpEventTeamToMapSpawnPoint(920010000, 2);
                    eim.giveEventPlayersExp(3500);
                    clearStage(0, eim);

                    cm.sendNext("请救救雅典娜，她被远古精灵困在封印中，远古精灵是我们塔楼的恐怖存在！他把雅典娜雕像的所有部分都弄丢了，我们必须把它们全部找回来！哦，请原谅我，我是塔楼的管家易克。我是雅典娜的皇家仆人。");
                } else {
                    cm.warp(920010000, 2);
                }
                cm.dispose();
                break;
            case 920010100:
                if (eim.getIntProperty("statusStg1") != 1) {
                    cm.sendOk("第二阶段已完成！现在将带大家进入下一个阶段。");
                    cm.removeAll(4001050);
                    cm.gainItem(4001044, 1); //first piece
                    eim.giveEventPlayersExp(3500);
                    clearStage(1, eim);
                    eim.warpEventTeam(920010300);
                } else if (cm.haveItem(4001055, 1)) {
                    cm.sendOk("生命草已经带回！我会直接召唤雅典娜女神，请和她对话领取奖励。");
                    cm.removeAll(4001055);
                    eim.clearPQ();
                    eim.setProperty("statusStg8", "1");
                    eim.giveEventPlayersExp(3500);
                    eim.showClearEffect(true);
                    eim.startEventTimer(5 * 60000);
                    if (cm.getMap().getNPCById(2013002) == null) {
                        cm.spawnNpc(2013002, new java.awt.Point(-48, -916), cm.getMap());
                    }
                } else if (eim.getIntProperty("statusStg6") == 1 && eim.getIntProperty("statusStg7") == -1) {
                    cm.sendOk("女神像修复流程已简化！现在将带大家进入花园阶段。");
                    cm.removeAll(4001044);
                    cm.removeAll(4001045);
                    cm.removeAll(4001046);
                    cm.removeAll(4001047);
                    cm.removeAll(4001048);
                    cm.removeAll(4001049);
                    eim.warpEventTeam(920010800);
                } else if (isStatueComplete()) {
                    if (eim.getIntProperty("statusStg7") == -1) {
                        eim.warpEventTeam(920010800);
                    } else if (eim.getIntProperty("statusStg8") == -1) {
                        cm.sendOk("哦！你带来了#t4001055#！请把它放在雕像的底座上，让雅典娜重生！");
                    } else {
                        cm.sendOk("谢谢你救了雅典娜！请和她交谈。");
                    }
                } else {
                    cm.sendOk("请继续完成前面的阶段，然后再和我对话进入花园阶段。");
                }
                break;
            case 920010200: //walkway
                if (eim.getIntProperty("statusStg1") != 1) {
                    cm.sendOk("第二阶段已简化。请回到中央塔，让队长直接和易克对话完成这一阶段。");
                } else {
                    cm.sendOk("这个阶段已经完成了。去寻找其他雕像碎片吧。");
                }
                break;
            case 920010300: //storage
                if (eim.getIntProperty("statusStg2") != 1) {
                    if (cm.canHold(4001045)) {
                        cm.sendOk("第三阶段已完成！现在将带大家进入下一个阶段。");
                        cm.gainItem(4001045, 1);
                        eim.giveEventPlayersExp(3500);
                        clearStage(2, eim);
                        eim.setProperty("statusStg2", "1");
                        eim.warpEventTeam(920010400);
                    } else {
                        cm.sendOk("我已经找到了第二块雕像碎片。在你的背包中腾出一个空位来拿它。");
                    }
                } else {
                    cm.sendOk("干得好。去找其他雕像碎片。");
                }

                break;
            case 920010400: //lobby
                if (eim.getIntProperty("statusStg3") != 2) {
                    cm.sendOk("第四阶段已完成！现在将带大家进入下一个阶段。");
                    cm.gainItem(4001046, 1); //third piece
                    eim.giveEventPlayersExp(3500);
                    clearStage(3, eim);
                    eim.setProperty("statusStg3", "2");
                    eim.warpEventTeam(920010500);

                } else {
                    cm.sendOk("这个阶段已经完成了。去寻找其他雕像碎片吧。");
                }
                break;
            case 920010500: //sealed
                if (eim.getIntProperty("statusStg4") != 1) {
                    cm.sendOk("第五阶段已完成！现在将带大家进入下一个阶段。");
                    cm.gainItem(4001047, 1); //fourth piece
                    eim.giveEventPlayersExp(3500);
                    clearStage(4, eim);
                    eim.setProperty("statusStg4", "1");
                    eim.warpEventTeam(920010600);
                } else {
                    cm.sendOk("这个阶段已经完成了。去寻找其他雕像碎片吧。");
                }
                cm.dispose();
                break;
            case 920010600: //lounge
                if (eim.getIntProperty("statusStg5") != 1) {
                    cm.sendOk("第六阶段已完成！现在将带大家进入下一个阶段。");
                    cm.removeAll(4001052);
                    cm.gainItem(4001048, 1); //fifth piece
                    eim.giveEventPlayersExp(3500);
                    clearStage(5, eim);
                    eim.setIntProperty("statusStg5", 1);
                    eim.warpEventTeam(920010700);
                } else {
                    cm.sendOk("这个阶段已经完成了。去寻找其他雕像碎片吧。");
                }
                break;
            case 920010700: //on the way up
                if (eim.getIntProperty("statusStg6") != 1) {
                    cm.sendOk("第七阶段已完成！现在将带大家回到中央塔。");
                    cm.gainItem(4001049, 1); //sixth piece
                    eim.giveEventPlayersExp(3500);
                    clearStage(6, eim);
                    eim.setProperty("statusStg6", "1");
                    eim.warpEventTeam(920010100);
                } else {
                    cm.sendOk("这个阶段已经完成了。请回到中央塔继续前进。");
                }
                break;
            case 920010800:
                if (eim.getIntProperty("statusStg7") == -1) {
                    cm.getMap().killAllMonsters();
                    cm.getMap().allowSummonState(false);
                    cm.spawnMonster(9300039, 260, 490);
                    eim.setProperty("statusStg7", "0");
                    cm.sendNext("远古精灵已经出现！请击败它，取得特别奇怪的种子后再和我对话。");
                } else if (cm.haveItem(4001054, 1)) {
                    cm.sendOk("你带来了特别奇怪的种子！我会帮你取得生命草，并带大家回到中央塔。");
                    cm.removeAll(4001054);
                    cm.gainItem(4001055, 1);
                    eim.setProperty("statusStg7", "1");
                    eim.warpEventTeam(920010100);
                } else {
                    cm.sendNext("请先击败远古精灵，并带着它掉落的#b#t4001054##k再来找我。我会帮你进入下一个阶段。");
                }
                break;
            case 920010900:
                if (eim.getProperty("statusStg8") == "1") {
                    cm.sendNext("这是塔的监狱。你可能会在这里找到一些好东西，只要确保尽快解决前面的谜题。");
                } else {
                    cm.sendNext("在那里你找不到任何雕像碎片。爬上梯子返回中心塔，然后到其他地方去搜索。一旦你救了雅典娜，你可以回到这里拿下面的好东西。");
                }
                break;
            case 920011000:
                if (cm.getMap().countMonsters() > 0) {
                    cm.sendNext("这是塔楼的隐藏房间。在清除了这个房间上的所有怪物之后，与我交谈以获得进入宝藏房间的权限，留下中央塔楼的通道。");
                } else {
                    cm.warp(920011100, "st00");
                }
                break;
        }
        cm.dispose();
    }
}

function clear() {
    cm.showEffect(true, "quest/party/clear");
    cm.playSound(true, "Party1/Clear");
}