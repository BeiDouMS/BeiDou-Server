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

            cm.sendOk("我只想和你们的领导谈话！");
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

                    cm.sendNext("请救救米内尔娃，她被波波皮希困在封印中，他是我们塔楼的恐怖存在！他把米内尔娃雕像的所有部分都弄丢了，我们必须把它们全部找回来！哦，请原谅我，我是塔楼的管家伊克。我是米内尔娃的皇家仆人。");
                } else {
                    cm.warp(920010000, 2);
                }
                cm.dispose();
                break;
            case 920010100:
                if (isStatueComplete()) {
                    if (eim.getIntProperty("statusStg7") == -1) {
                        eim.warpEventTeam(920010800);
                    } else if (eim.getIntProperty("statusStg8") == -1) {
                        cm.sendOk("哦！你带来了#t4001055#！请把它放在雕像的底座上，让米涅瓦重生！");
                    } else {
                        cm.sendOk("谢谢你救了米内尔瓦！请和她交谈…");
                    }
                } else {
                    cm.sendOk("请拯救米内尔娃！收集她雕像的六块碎片，然后与我交谈以取回最后一块碎片！");
                }
                break;
            case 920010200: //walkway
                if (!cm.haveItem(4001050, 30)) {
                    cm.sendOk("收集这个阶段怪物身上的30个雕像碎片，然后请把它们带给我，这样我就可以把它们拼在一起！");
                } else {
                    cm.sendOk("你已经找到了它们！这里是第一块雕像碎片。");
                    cm.removeAll(4001050);
                    cm.gainItem(4001044, 1); //first piece
                    eim.giveEventPlayersExp(3500);
                    clearStage(1, eim);
                }
                break;
            case 920010300: //storage
                if (eim.getIntProperty("statusStg2") != 1) {
                    if (cm.getMap().countMonsters() == 0 && cm.getMap().countItems() == 0) {
                        if (cm.canHold(4001045)) {
                            cm.sendOk("哦，我找到了第二块雕像碎片。拿去吧。");
                            cm.gainItem(4001045, 1);
                            eim.giveEventPlayersExp(3500);
                            clearStage(2, eim);
                            eim.setProperty("statusStg2", "1");
                        } else {
                            cm.sendOk("我已经找到了第二块雕像碎片。在你的背包中腾出一个空位来拿它。");
                        }
                    } else {
                        cm.sendOk("在这个房间里找到隐藏的第二块雕像碎片。");
                    }
                } else {
                    cm.sendOk("干得好。去找其他雕像碎片。");
                }

                break;
            case 920010400: //lobby
                if (eim.getIntProperty("statusStg3") == -1) {
                    cm.sendOk("请找到本周的LP，并将其放在音乐播放器上。\r\n#v4001056# 星期日\r\n#v4001057# 星期一\r\n#v4001058# 星期二\r\n#v4001059# 星期三\r\n#v4001060# 星期四\r\n#v4001061# 星期五\r\n#v4001062# 星期六");
                } else if (eim.getIntProperty("statusStg3") == 0) {
                    cm.getMap().getReactorByName("stone3").forceHitReactor(1);
                    cm.sendOk("哦，这音乐... 它和环境非常搭配。做得好，一个箱子出现在场地上。从中取出雕像的一部分！");
                    eim.giveEventPlayersExp(3500);
                    clearStage(3, eim);
                    eim.setProperty("statusStg3", "2");

                } else {
                    cm.sendOk("非常感谢你！");
                }
                break;
            case 920010500: //sealed
                if (eim.getIntProperty("statusStg4") == -1) {
                    var total = 3;
                    for (var i = 0; i < 2; i++) {
                        var rnd = Math.round(Math.random() * total);
                        total -= rnd;

                        eim.setProperty("stage4_" + i, rnd);
                    }
                    eim.setProperty("stage4_2", "" + total);

                    eim.setProperty("statusStg4", "0");
                }
                if (eim.getIntProperty("statusStg4") == 0) {
                    var players = Array();
                    var total = 0;
                    for (var i = 0; i < 3; i++) {
                        var z = cm.getMap().getNumPlayersInArea(i);
                        players.push(z);
                        total += z;
                    }
                    if (total != 3) {
                        cm.sendOk("这些平台上需要有确切的3名玩家。");
                    } else {
                        var num_correct = 0;
                        for (var i = 0; i < 3; i++) {
                            if (eim.getProperty("stage4_" + i) === ("" + players[i])) {
                                num_correct++;
                            }
                        }
                        if (num_correct == 3) {
                            cm.sendOk("你找到了正确的组合！地图顶部出现了一个宝箱，去拿取里面的雕像碎片吧！");
                            cm.getMap().getReactorByName("stone4").forceHitReactor(1);
                            eim.giveEventPlayersExp(3500);
                            clearStage(4, eim);
                        } else {
                            eim.showWrongEffect();
                            if (num_correct > 0) {
                                cm.sendOk("一个平台上有正确数量的玩家。");
                            } else {
                                cm.sendOk("所有的平台上都有错误的玩家数量。");
                            }
                        }
                    }
                } else {
                    cm.sendOk("干得好！请去找其他碎片，拯救米内尔瓦！");
                }
                cm.dispose();
                break;
            case 920010600: //lounge
                if (eim.getIntProperty("statusStg5") == -1) {
                    if (!cm.haveItem(4001052, 40)) {
                        cm.sendOk("在这个阶段从怪物身上收集40个雕像碎片，然后请把它们带给我，这样我就可以把它们拼在一起！");
                    } else {
                        cm.sendOk("你已经找到了它们！这里是第五块雕像碎片。");
                        cm.removeAll(4001052);
                        cm.gainItem(4001048, 1); //fifth piece
                        eim.giveEventPlayersExp(3500);
                        clearStage(5, eim);
                        eim.setIntProperty("statusStg5", 1);
                    }
                } else {
                    cm.sendOk("你已经找到了所有的东西。去搜索塔的其他房间吧。");
                }
                break;
            case 920010700: //on the way up
                if (eim.getIntProperty("statusStg6") == -1) {
                    var rnd1 = Math.floor(Math.random() * 5);

                    var rnd2 = Math.floor(Math.random() * 5);
                    while (rnd2 == rnd1) {
                        rnd2 = Math.floor(Math.random() * 5);
                    }

                    if (rnd1 > rnd2) {
                        rnd1 = rnd1 ^ rnd2;
                        rnd2 = rnd1 ^ rnd2;
                        rnd1 = rnd1 ^ rnd2;
                    }

                    var comb = "";
                    for (var i = 0; i < rnd1; i++) {
                        comb += "0";
                    }
                    comb += "1";
                    for (var i = rnd1 + 1; i < rnd2; i++) {
                        comb += "0";
                    }
                    comb += "1";
                    for (var i = rnd2 + 1; i < 5; i++) {
                        comb += "0";
                    }

                    eim.setProperty("stage6_c", "" + comb);

                    eim.setProperty("statusStg6", "0");
                }

                var comb = eim.getProperty("stage6_c");

                if (eim.getIntProperty("statusStg6") == 0) {
                    var react = "";
                    var total = 0;
                    for (var i = 1; i <= 5; i++) {
                        if (cm.getMap().getReactorByName("" + i).getState() > 0) {
                            react += "1";
                            total += 1;
                        } else {
                            react += "0";
                        }
                    }

                    if (total != 2) {
                        cm.sendOk("地图顶部需要精确地推动两个杠杆。");
                    } else {
                        var num_correct = 0;
                        var psh_correct = 0;
                        for (var i = 0; i < 5; i++) {
                            if (react.charCodeAt(i) == comb.charCodeAt(i)) {
                                num_correct++;
                                if (react.charAt(i) == '1') {
                                    psh_correct++;
                                }
                            }
                        }
                        if (num_correct == 5) {
                            cm.sendOk("你找到了正确的组合！从里面取出雕像碎片！");
                            cm.getMap().getReactorByName("stone6").forceHitReactor(1);
                            eim.giveEventPlayersExp(3500);
                            clearStage(6, eim);
                        } else {
                            eim.showWrongEffect();
                            if (psh_correct >= 1) {
                                cm.sendOk("其中一个推动的杠杆是正确的。");
                            } else {
                                cm.sendOk("两个推杆都是错误的。");
                            }
                        }
                    }
                } else {
                    cm.sendOk("干得漂亮！去看看其他的部分吧。");
                }
                break;
            case 920010800:
                cm.sendNext("请找到一种方法来打败波波精灵！一旦你通过种植种子找到了黑暗尼芬死亡，你就找到了波波精灵！打败它，拿到生命之根来拯救米内尔瓦！！");
                break;
            case 920010900:
                if (eim.getProperty("statusStg8") == "1") {
                    cm.sendNext("这是塔的监狱。你可能会在这里找到一些好东西，只要确保尽快解决前面的谜题。");
                } else {
                    cm.sendNext("在那里你找不到任何雕像碎片。爬上梯子返回中心塔，然后到其他地方去搜索。一旦你救了米涅瓦，你可以回到这里拿下面的好东西。");
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