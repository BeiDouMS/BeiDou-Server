/**
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version by Drago (MapleStorySA)
 2.0 - Second Version by Ronan (HeavenMS)
 3.0 - Third Version by Jayd - translated CPQ contents to English and added Pirate items
 ---------------------------------------------------------------------------------------------------
 **/

var status = 0;
var rnk = -1;
var n1 = 50; //???
var n2 = 40; //??? ???
var n3 = 7; //35
var n4 = 10; //40
var n5 = 20; //50

var cpqMap = 980000000;
var cpqMinLvl = 30;
var cpqMaxLvl = 50;
var cpqMinAmt = 2;
var cpqMaxAmt = 6;

// Ronan's custom ore refiner NPC
var refineRocks = true;     // enables moon rock, star rock
var refineCrystals = true;  // enables common crystals
var refineSpecials = true;  // enables lithium, special crystals
var feeMultiplier = 7.0;

function start() {
    status = -1;

    const GameConfig = Java.type('org.gms.config.GameConfig');
    if (!GameConfig.getServerBoolean("use_cpq")) {
        if (GameConfig.getServerBoolean("use_enable_custom_npc_script")) {
            status = 0;
            action(1, 0, 4);
        } else {
            cm.sendOk("怪物嘉年华目前不可用。");
            cm.dispose();
        }

        return;
    }

    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status >= 0 && mode == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        const GameConfig = Java.type('org.gms.config.GameConfig');
        if (cm.getPlayer().getMapId() == 980000010) {
            if (status == 0) {
                cm.sendNext("希望你在怪物嘉年华上玩得开心！");
            } else if (status > 0) {
                cm.warp(980000000, 0);
                cm.dispose();
            }
        } else if (cm.getChar().getMap().isCPQLoserMap()) {
            if (status == 0) {
                if (cm.getChar().getParty() != null) {
                    var shiu = "";
                    if (cm.getPlayer().getFestivalPoints() >= 300) {
                        shiu += "#rA#k";
                        cm.sendOk("很遗憾，尽管你表现出色，但你要么平局要么输掉了这场战斗。下次胜利就属于你了！\r\n\r\n#b你的结果：" + shiu);
                        rnk = 10;
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shiu += "#rB#k";
                        rnk = 20;
                        cm.sendOk("很遗憾，即使你表现出色，你要么平局要么失败了。只差一点点，胜利就可能属于你了！\r\n\r\n#b你的结果：" + shiu);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shiu += "#rC#k";
                        rnk = 30;
                        cm.sendOk("很遗憾，你要么平局要么失败了。胜利属于那些努力奋斗的人。我看到了你的努力，所以胜利离你并不遥远。继续努力吧！\r\n#b你的结果：" + shiu);
                    } else {
                        shiu += "#rD#k";
                        rnk = 40;
                        cm.sendOk("很遗憾，你要么打成了平局，要么输掉了战斗，你的表现清楚地反映了这一点。我希望你下次能做得更好。\r\n\r\n#b你的结果：" + shiu);
                    }
                } else {
                    cm.warp(980000000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 10:
                        cm.warp(980000000, 0);
                        cm.gainExp(17500);
                        cm.dispose();
                        break;
                    case 20:
                        cm.warp(980000000, 0);
                        cm.gainExp(1200);
                        cm.dispose();
                        break;
                    case 30:
                        cm.warp(980000000, 0);
                        cm.gainExp(5000);
                        cm.dispose();
                        break;
                    case 40:
                        cm.warp(980000000, 0);
                        cm.gainExp(2500);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980000000, 0);
                        cm.dispose();
                        break;
                }
            }
        } else if (cm.getChar().getMap().isCPQWinnerMap()) {
            if (status == 0) {
                if (cm.getChar().getParty() != null) {
                    var shi = "";
                    if (cm.getPlayer().getFestivalPoints() >= 300) {
                        shi += "#rA#k";
                        rnk = 1;
                        cm.sendOk("恭喜你的胜利！表现太棒了！对方队伍毫无还手之力！希望下次也能有同样出色的表现！\r\n\r\n#b你的成绩：" + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 100) {
                        shi += "#rB#k";
                        rnk = 2;
                        cm.sendOk("恭喜你的胜利！太棒了！你对抗对方团队做得很好！再坚持一会儿，下次你肯定能拿到A！\r\n\r\n#b你的成绩：" + shi);
                    } else if (cm.getPlayer().getFestivalPoints() >= 50) {
                        shi += "#rC#k";
                        rnk = 3;
                        cm.sendOk("恭喜你的胜利。你做了一些事情，但这不能算是一个好的胜利。我期待你下次能做得更好。\r\n\r\n#b你的结果：" + shi);
                    } else {
                        shi += "#rD#k";
                        rnk = 4;
                        cm.sendOk("恭喜你的胜利，尽管你的表现并没有完全体现出来。在下一次怪物嘉年华中更加积极参与吧！\r\n\r\n#b你的结果：" + shi);
                    }
                } else {
                    cm.warp(980000000, 0);
                    cm.dispose();
                }
            } else if (status == 1) {
                switch (rnk) {
                    case 1:
                        cm.warp(980000000, 0);
                        cm.gainExp(50000);
                        cm.dispose();
                        break;
                    case 2:
                        cm.warp(980000000, 0);
                        cm.gainExp(25500);
                        cm.dispose();
                        break;
                    case 3:
                        cm.warp(980000000, 0);
                        cm.gainExp(21000);
                        cm.dispose();
                        break;
                    case 4:
                        cm.warp(980000000, 0);
                        cm.gainExp(19505);
                        cm.dispose();
                        break;
                    default:
                        cm.warp(980000000, 0);
                        cm.dispose();
                        break;
                }
            }
        } else if (cm.getMapId() == cpqMap) {   // only CPQ1
            if (status == 0) {
                if (cm.getParty() == null) {
                    status = 10;
                    cm.sendOk("在你加入战斗之前，你需要先创建一个队伍！");
                } else if (!cm.isLeader()) {
                    status = 10;
                    cm.sendOk("如果你想开始战斗，让#b队长#k和我交谈。");
                } else {
                    var party = cm.getParty().getMembers();
                    var inMap = cm.partyMembersInMap();
                    var lvlOk = 0;
                    var isOutMap = 0;
                    for (var i = 0; i < party.size(); i++) {
                        if (party.get(i).getLevel() >= cpqMinLvl && party.get(i).getLevel() <= cpqMaxLvl) {
                            lvlOk++;

                            if (party.get(i).getPlayer().getMapId() != cpqMap) {
                                isOutMap++;
                            }
                        }
                    }

                    if (party >= 1) {
                        status = 10;
                        cm.sendOk("你的队伍人数不够。你需要一个有 #b" + cpqMinAmt + "#k - #r" + cpqMaxAmt + "#k 名成员的队伍，并且他们应该和你在同一地图上。");
                    } else if (lvlOk != inMap) {
                        status = 10;
                        cm.sendOk("确保你的队伍中的每个人都处于正确的等级范围内（" + cpqMinLvl + "~" + cpqMaxLvl + "）！");
                    } else if (isOutMap > 0) {
                        status = 10;
                        cm.sendOk("有一些队伍成员不在地图上！");
                    } else {
                        if (!cm.sendCPQMapLists()) {
                            cm.sendOk("所有的怪物嘉年华场地目前都在使用中！请稍后再试。");
                            cm.dispose();
                        }
                    }
                }
            } else if (status == 1) {
                if (cm.fieldTaken(selection)) {
                    if (cm.fieldLobbied(selection)) {
                        cm.challengeParty(selection);
                        cm.dispose();
                    } else {
                        cm.sendOk("房间目前已满。");
                        cm.dispose();
                    }
                } else {
                    var party = cm.getParty().getMembers();
                    if ((selection >= 0 && selection <= 3) && party.size() < (GameConfig.getServerBoolean("use_enable_solo_expeditions") ? 1 : 2)) {
                        cm.sendOk("你至少需要2名玩家才能参与战斗！");
                    } else if ((selection >= 4 && selection <= 5) && party.size() < (GameConfig.getServerBoolean("use_enable_solo_expeditions") ? 1 : 3)) {
                        cm.sendOk("你至少需要3名玩家参与战斗！");
                    } else {
                        cm.cpqLobby(selection);
                    }
                    cm.dispose();
                }
            } else if (status == 11) {
                cm.dispose();
            }
        } else {
            if (status == 0) {
                var talk = "你想做什么呢？ 如果你没有参加过怪物嘉年华, 在参加之前，你需要知道一些事情! \r\n#b#L0# 前往怪物嘉年华地图 1.#l \r\n#L3# 前往怪物嘉年华地图 2.#l \r\n#L1# 了解怪物嘉年华.#l\r\n#L2# 交易 #t4001129#.#l";
                if (GameConfig.getServerBoolean("use_enable_custom_npc_script")) {
                    talk += "\r\n#L4# ... 我可以精炼我的矿石吗#l";
                }
                cm.sendSimple(talk);
            } else if (status == 1) {
                if (selection == 0) {
                    if ((cm.getLevel() > 29 && cm.getLevel() < 51) || cm.getPlayer().isGM()) {
                        cm.getChar().saveLocation("MONSTER_CARNIVAL");
                        cm.warp(980000000, 0);
                        cm.dispose();

                    } else if (cm.getLevel() < 30) {
                        cm.sendOk("你必须至少达到30级才能参加怪物嘉年华。当你足够强大时，和我交谈。");
                        cm.dispose();

                    } else {
                        cm.sendOk("很抱歉，只有等级在30到50级之间的玩家才能参加怪物嘉年华活动。");
                        cm.dispose();

                    }
                } else if (selection == 1) {
                    status = 60;
                    cm.sendSimple("你想做什么？\r\n#b#L0# 什么是怪物嘉年华？#l\r\n#L1# 怪物嘉年华概述。#l\r\n#L2# 怪物嘉年华的详细信息。#l\r\n#L3# 其实什么都不想做了，我改变主意了。#l");
                } else if (selection == 2) {
                    cm.sendSimple("记住，如果你有#t4001129#，你可以用它来兑换物品。选择你想要兑换的物品！\r\n#b#L0# #t1122007#（" + n1 + " 纪念币）#l\r\n#L1# #t2041211#（" + n2 + " 纪念币）#l\r\n#L2# 战士武器#l\r\n#L3# 魔法师武器#l\r\n#L4# 弓箭手武器#l\r\n#L5# 盗贼武器#l\r\n#L6# 海盗武器#l");
                } else if (selection == 3) {
                    cm.getChar().saveLocation("MONSTER_CARNIVAL");
                    cm.warp(980030000, 0);
                    cm.dispose();

                } else if (selection == 4) {
                    var selStr = "Very well, instead I offer a steadfast #bore refining#k service for you, taxing #r" + ((feeMultiplier * 100) | 0) + "%#k over the usual fee to synthetize them. What will you do?#b";

                    var options = ["Refine mineral ores", "Refine jewel ores"];
                    if (refineCrystals) {
                        options.push("Refine crystal ores");
                    }
                    if (refineRocks) {
                        options.push("Refine plates/jewels");
                    }

                    for (var i = 0; i < options.length; i++) {
                        selStr += "\r\n#L" + i + "# " + options[i] + "#l";
                    }

                    cm.sendSimple(selStr);

                    status = 76;
                }
            } else if (status == 2) {
                select = selection;
                if (select == 0) {
                    if (cm.haveItem(4001129, n1) && cm.canHold(4001129)) {
                        cm.gainItem(1122007, 1);
                        cm.gainItem(4001129, -n1);
                        cm.dispose();
                    } else {
                        cm.sendOk("检查一下你是否缺少#b#t4001129##k，或者你的装备栏是否已满。");
                        cm.dispose();
                    }
                } else if (select == 1) {
                    if (cm.haveItem(4001129, n2) && cm.canHold(2041211)) {
                        cm.gainItem(2041211, 1);
                        cm.gainItem(4001129, -n2);
                        cm.dispose();
                    } else {
                        cm.sendOk("检查一下你是否缺少#b#t4001129##k，或者你的使用物品栏是否已满。");
                        cm.dispose();
                    }
                } else if (select == 2) {//S2 Warrior 26 S3 Magician 6 S4 Bowman 6 S5 Thief 8
                    status = 10;
                    cm.sendSimple("请确保您拥有 # t4001129 # 用于您想要的武器。选择您想要交易的武器 # t4001129 #。我有的选择都非常好，而且我不是那些说话的人！\r\n#b#L0# #z1302004#（" + n3 + " 纪念币）#l\r\n#L1# #z1402006#（" + n3 + " 纪念币）#l\r\n#L2# #z1302009#（" + n4 + " 纪念币）#l\r\n#L3# #z1402007#（" + n4 + " 纪念币）#l\r\n#L4# #z1302010#（" + n5 + " 纪念币）#l\r\n#L5# #z1402003#（" + n5 + " 纪念币）#l\r\n#L6# #z1312006#（" + n3 + " 纪念币）#l\r\n#L7# #z1412004#（" + n3 + " 纪念币）#l\r\n#L8# #z1312007#（" + n4 + " 纪念币）#l\r\n#L9# #z1412005#（" + n4 + " 纪念币）#l\r\n#L10# #z1312008#（" + n5 + " 纪念币）#l\r\n#L11# #z1412003#（" + n5 + " 纪念币）#l\r\n#L12# 继续到下一页（1/2）#l");
                } else if (select == 3) {
                    status = 20;
                    cm.sendSimple("选择您想要交易的武器。我这里的武器非常吸引人。亲自看看吧！\r\n#b#L0# #z1372001#（" + n3 + " 纪念币）#l\r\n#L1# #z1382018#（" + n3 + " 纪念币）#l\r\n#L2# #z1372012#（" + n4 + " 纪念币）#l\r\n#L3# #z1382019#（" + n4 + " 纪念币）#l\r\n#L4# #z1382001#（" + n5 + " 纪念币）#l\r\n#L5# #z1372007#（" + n5 + " 纪念币）#l");
                } else if (select == 4) {
                    status = 30;
                    cm.sendSimple("选择您想要交易的武器。我这里的武器非常吸引人。亲自看看吧！\r\n#b#L0# #z1452006#（" + n3 + " 纪念币）#l\r\n#L1# #z1452007#（" + n4 + " 纪念币）#l\r\n#L2# #z1452008#（" + n5 + " 纪念币）#l\r\n#L3# #z1462005#（" + n3 + " 纪念币）#l\r\n#L4# #z1462006#（" + n4 + " 纪念币）#l\r\n#L5# #z1462007#（" + n5 + " 纪念币）#l");
                } else if (select == 5) {
                    status = 40;
                    cm.sendSimple("选择您想要交易的武器。我所拥有的武器质量最高。选择对您最有吸引力的那一件！\r\n#b#L0# #z1472013#（" + n3 + " 纪念币）#l\r\n#L1# #z1472017#（" + n4 + " 纪念币）#l\r\n#L2# #z1472021#（" + n5 + " 纪念币）#l\r\n#L3# #z1332014#（" + n3 + " 纪念币）#l\r\n#L4# #z1332031#（" + n4 + " 纪念币）#l\r\n#L5# #z1332011#（" + n4 + " 纪念币）#l\r\n#L6# #z1332016#（" + n5 + " 纪念币）#l\r\n#L7# #z1332003#（" + n5 + " 纪念币）#l");
                } else if (select == 6) {
                    status = 50; //pirate rewards
                    cm.sendSimple("选择您想要交易的武器。我所拥有的武器质量最高。选择对您最有吸引力的那一件吧！\r\n#b#L0# #z1482005#（" + n3 + " 纪念币）#l \r\n#b#L1# #z1482006#（" + n4 + " 纪念币）#l \r\n#b#L2# #z1482007#（" + n5 + " 纪念币）#l \r\n#b#L3# #z1492005#（" + n3 + " 纪念币）#l \r\n#b#L4# #z1492006#（" + n4 + " 纪念币）#l \r\n#b#L5# #z1492007#（" + n5 + " 纪念币）#l");
                }
            } else if (status == 11) {
                if (selection == 12) {
                    cm.sendSimple("选择您想要交易的武器。我这里的武器非常有用。看一看！\r\n#b#L0# #z1322015#（" + n3 + " 纪念币）#l\r\n#L1# #z1422008#（" + n3 + " 纪念币）#l\r\n#L2# #z1322016#（" + n4 + " 纪念币）#l\r\n#L3# #z1422007#（" + n4 + " 纪念币）#l\r\n#L4# #z1322017#（" + n5 + " 纪念币）#l\r\n#L5# #z1422005#（" + n5 + " 纪念币）#l\r\n#L6# #z1432003#（" + n3 + " 纪念币）#l\r\n#L7# #z1442003#（" + n3 + " 纪念币）#l\r\n#L8# #z1432005#（" + n4 + " 纪念币）#l\r\n#L9# #z1442009#（" + n4 + " 纪念币）#l\r\n#L10# #z1442005#（" + n5 + " 纪念币）#l\r\n#L11# #z1432004#（" + n5 + " 纪念币）#l\r\n#L12# 返回第一页（2/2）#l");
                } else {
                    var item = [1302004, 1402006, 1302009, 1402007, 1302010, 1402003, 1312006, 1412004, 1312007, 1412005, 1312008, 1412003];
                    var cost = [n3, n3, n4, n4, n5, n5, n3, n3, n4, n4, n5];
                    if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                        cm.gainItem(item[selection], 1);
                        cm.gainItem(4001129, -cost[selection]);
                        cm.dispose();
                    } else {
                        cm.sendOk("您没有足够的#b#t4001129##k，或者您的背包已满。请再次检查。");
                        cm.dispose();
                    }
                }
            } else if (status == 12) {
                if (selection == 12) {
                    status = 10;
                    cm.sendSimple("请确保你有#b#t4001129##k用于你想要的武器。选择你想要交换的武器#t4001129#。我这里的选择都非常不错，而且我不是那些说话空话的人！\r\n#b#L0# #z1302004#（" + n3 + " 纪念币）#l\r\n#L1# #z1402006#（" + n3 + " 纪念币）#l\r\n#L2# #z1302009#（" + n4 + " 纪念币）#l\r\n#L3# #z1402007#（" + n4 + " 纪念币）#l\r\n#L4# #z1302010#（" + n5 + " 纪念币）#l\r\n#L5# #z1402003#（" + n5 + " 纪念币）#l\r\n#L6# #z1312006#（" + n3 + " 纪念币）#l\r\n#L7# #z1412004#（" + n3 + " 纪念币）#l\r\n#L8# #z1312007#（" + n4 + " 纪念币）#l\r\n#L9# #z1412005#（" + n4 + " 纪念币）#l\r\n#L10# #z1312008#（" + n5 + " 纪念币）#l\r\n#L11# #z1412003#（" + n5 + " 纪念币）#l\r\n#L12# 继续到下一页（1/2）#l");
                } else {
                    var item = [1322015, 1422008, 1322016, 1422007, 1322017, 1422005, 1432003, 1442003, 1432005, 1442009, 1442005, 1432004];
                    var cost = [n3, n3, n4, n4, n5, n5, n3, n3, n4, n4, n5, n5];
                    if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                        cm.gainItem(item[selection], 1);
                        cm.gainItem(4001129, -cost[selection]);
                        cm.dispose();
                    } else {
                        cm.sendOk("您没有足够的#b#t4001129##k，或者您的背包已满。请再次检查。");
                        cm.dispose();
                    }
                }
            } else if (status == 21) {
                var item = [1372001, 1382018, 1372012, 1382019, 1382001, 1372007];
                var cost = [n3, n3, n4, n4, n5, n5];
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("您没有足够的#b#t4001129##k，或者您的背包已满。请再次检查。");
                    cm.dispose();
                }
            } else if (status == 31) {
                var item = [1452006, 1452007, 1452008, 1462005, 1462006, 1462007];
                var cost = [n3, n4, n5, n3, n4, n5];
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("您没有足够的#b#t4001129##k，或者您的背包已满。请再次检查。");
                    cm.dispose();
                }
            } else if (status == 41) {
                var item = [1472013, 1472017, 1472021, 1332014, 1332031, 1332011, 1332016, 1332003];
                var cost = [n3, n4, n5, n3, n4, n4, n5, n5];
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("您没有足够的 #b#t4001129##k，或者您的背包已满。请再次检查。");
                    cm.dispose();
                }
            } else if (status == 51) {
                var item = [1482005, 1482006, 1482007, 1492005, 1492006, 1492007];
                var cost = [n3, n4, n5, n3, n4, n5];
                if (cm.haveItem(4001129, cost[selection]) && cm.canHold(item[selection])) {
                    cm.gainItem(item[selection], 1);
                    cm.gainItem(4001129, -cost[selection]);
                    cm.dispose();
                } else {
                    cm.sendOk("您没有足够的#b#t4001129##k，或者您的背包已满。请再次检查。");
                    cm.dispose();
                }
            } else if (status == 61) {
                select = selection;
                if (selection == 0) {
                    cm.sendNext("哈哈！我是斯皮格曼，这个怪物嘉年华的领袖。我在这里举办了第一届#b怪物嘉年华#k，等待像你这样的旅行者参加这场盛会！");
                } else if (selection == 1) {
                    cm.sendNext("“#b怪物嘉年华#k 由两组玩家进入战场，并释放对方召唤的怪物。 #b通过获得的嘉年华点数（CP）来决定胜利者的战斗团队#k。”");
                } else if (selection == 2) {
                    cm.sendNext("当你进入嘉年华场地时，你会看到怪物列表窗口出现。你只需要#b选择你想要使用的东西，然后按下确定#k。非常简单，对吧？");
                } else {
                    cm.dispose();
                }
            } else if (status == 62) {
                if (select == 0) {
                    cm.sendNext("什么是#b怪物嘉年华#k？哈哈哈！可以说这是一次你永远不会忘记的经历！这是与其他像你一样的冒险者进行的战斗！#k");
                } else if (select == 1) {
                    cm.sendNext("进入嘉年华场地时，你的任务是通过击败来自对立阵营的怪物来获得CP，并使用这些CP来分散对立阵营的注意，防止他们攻击怪物。");
                } else if (select == 2) {
                    cm.sendNext("一旦你熟悉了指令，试着使用 #bTAB 和 F1 ~ F12#k。#bTAB 可以在召唤怪物/使用技能/保护者之间切换#k，而 #bF1 ~ F12 可以直接访问窗口之一#k。");
                }
            } else if (status == 63) {
                if (select == 0) {
                    cm.sendNext("我知道你们用真正的武器互相战斗太危险了；我不会建议这样野蛮的行为。我的朋友，我所提供的是竞争的乐趣。战斗的激动和与如此强大和有动力的人竞争的激动。我提议你们的团队和对立团队都召唤怪物，并打败对方团队召唤的怪物。这就是怪物嘉年华的精髓。此外，你可以使用在怪物嘉年华期间赚取的枫币来获得新物品和武器！");
                } else if (select == 1) {
                    cm.sendNext("有三种方法可以分散对方的注意力：召唤怪物、能力和守护者。如果你想了解更多关于“详细说明”的内容，我可以给你更深入的了解！");
                } else if (select == 2) {
                    cm.sendNext("召唤怪物会召唤一个攻击对方队伍的怪物，并将其控制在其下。使用CP召唤一个怪物，它将出现在同一区域，攻击对方队伍。");
                }
            } else if (status == 64) {
                if (select == 0) {
                    cm.sendNext("当然，事情并不那么简单。还有其他方法可以阻止另一组放怪物，这取决于你如何解决。你觉得呢？对友好竞争感兴趣吗？");
                    cm.dispose();
                } else if (select == 1) {
                    cm.sendNext("请记住，把你的CP留着从来都不是一个好主意。#b你使用的CP将决定怪物嘉年华的胜负。");
                } else if (select == 2) {
                    cm.sendNext("#b能力#k 是一种选项，可以使用黑暗、虚弱等能力来阻止对方小组杀死其他怪物。不需要太多的CP，但是非常值得。唯一的问题是它们持续时间不长。明智地使用这种策略！");
                }
            } else if (status == 65) {
                if (select == 1) {
                    cm.sendNext("哦，不用担心变成幽灵。在怪物嘉年华中，你死后不会损失经验值。所以这真的是一种独特的体验！");
                    cm.dispose();
                } else if (select == 2) {
                    cm.sendNext("“#b守护者#k基本上是一个被召唤的物品，可以大幅增加你的小队召唤的怪物的能力。守护者会一直起作用，直到被对方小队摧毁，所以我希望你先召唤几个怪物，然后再带上守护者。”");
                }
            } else if (status == 66) {
                cm.sendNext("最后，在怪物嘉年华中，你不能使用随身携带的物品/恢复药水。与此同时，怪物会让这些物品掉落。当你拾取这些物品时，它们会立即激活。因此，知道何时获取这些物品非常重要。");
                cm.dispose();
            } else if (status == 77) {
                var allDone;

                if (selection == 0) {
                    allDone = refineItems(0); // minerals
                } else if (selection == 1) {
                    allDone = refineItems(1); // jewels
                } else if (selection == 2 && refineCrystals) {
                    allDone = refineItems(2); // crystals
                } else if (selection == 2 && !refineCrystals || selection == 3) {
                    allDone = refineRockItems(); // moon/star rock
                }

                if (allDone) {
                    cm.sendOk("完成了。谢谢你的出现~。");
                } else {
                    cm.sendOk("完成。请注意，一些物品无法合成，可能是因为您的杂项物品栏空间不足，或者没有足够的纪念币来支付费用。");
                }
                cm.dispose();
            }
        }
    }
}

function getRefineFee(fee) {
    return ((feeMultiplier * fee) | 0);
}

function isRefineTarget(refineType, refineItemid) {
    if (refineType == 0) { //mineral refine
        return refineItemid >= 4010000 && refineItemid <= 4010007 && !(refineItemid == 4010007 && !refineSpecials);
    } else if (refineType == 1) { //jewel refine
        return refineItemid >= 4020000 && refineItemid <= 4020008 && !(refineItemid == 4020008 && !refineSpecials);
    } else if (refineType == 2) { //crystal refine
        return refineItemid >= 4004000 && refineItemid <= 4004004 && !(refineItemid == 4004004 && !refineSpecials);
    }

    return false;
}

function getRockRefineTarget(refineItemid) {
    if (refineItemid >= 4011000 && refineItemid <= 4011006) {
        return 0;
    } else if (refineItemid >= 4021000 && refineItemid <= 4021008) {
        return 1;
    }

    return -1;
}

function refineItems(refineType) {
    var allDone = true;

    var refineFees = [[300, 300, 300, 500, 500, 500, 800, 270], [500, 500, 500, 500, 500, 500, 500, 1000, 3000], [5000, 5000, 5000, 5000, 1000000]];
    var itemCount = {};

    const InventoryType = Java.type('org.gms.client.inventory.InventoryType');
    var iter = cm.getPlayer().getInventory(InventoryType.ETC).iterator();
    while (iter.hasNext()) {
        var it = iter.next();
        var itemid = it.getItemId();

        if (isRefineTarget(refineType, itemid)) {
            var ic = itemCount[itemid];

            if (ic != undefined) {
                itemCount[itemid] += it.getQuantity();
            } else {
                itemCount[itemid] = it.getQuantity();
            }
        }
    }

    for (var key in itemCount) {
        var itemqty = itemCount[key];
        var itemid = parseInt(key);

        var refineQty = ((itemqty / 10) | 0);
        if (refineQty <= 0) {
            continue;
        }

        while (true) {
            itemqty = refineQty * 10;

            var fee = getRefineFee(refineFees[refineType][(itemid % 100) | 0] * refineQty);
            if (cm.canHold(itemid + 1000, refineQty, itemid, itemqty) && cm.getMeso() >= fee) {
                cm.gainMeso(-fee);
                cm.gainItem(itemid, -itemqty);
                cm.gainItem(itemid + (itemid != 4010007 ? 1000 : 1001), refineQty);

                break;
            } else if (refineQty <= 1) {
                allDone = false;
                break;
            } else {
                refineQty--;
            }
        }
    }

    return allDone;
}

function refineRockItems() {
    var allDone = true;
    var minItems = [[0, 0, 0, 0, 0, 0, 0], [0, 0, 0, 0, 0, 0, 0, 0, 0]];
    var minRocks = [2147483647, 2147483647];

    var rockItems = [4011007, 4021009];
    var rockFees = [10000, 15000];

    const InventoryType = Java.type('org.gms.client.inventory.InventoryType');
    var iter = cm.getPlayer().getInventory(InventoryType.ETC).iterator();
    while (iter.hasNext()) {
        var it = iter.next();
        var itemid = it.getItemId();
        var rockRefine = getRockRefineTarget(itemid);
        if (rockRefine >= 0) {
            var rockItem = ((itemid % 100) | 0);
            var itemqty = it.getQuantity();

            minItems[rockRefine][rockItem] += itemqty;
        }
    }

    for (var i = 0; i < minRocks.length; i++) {
        for (var j = 0; j < minItems[i].length; j++) {
            if (minRocks[i] > minItems[i][j]) {
                minRocks[i] = minItems[i][j];
            }
        }
        if (minRocks[i] <= 0 || minRocks[i] == 2147483647) {
            continue;
        }

        var refineQty = minRocks[i];
        while (true) {
            var fee = getRefineFee(rockFees[i] * refineQty);
            if (cm.canHold(rockItems[i], refineQty) && cm.getMeso() >= fee) {
                cm.gainMeso(-fee);

                var j;
                if (i == 0) {
                    for (j = 4011000; j < 4011007; j++) {
                        cm.gainItem(j, -refineQty);
                    }
                    cm.gainItem(j, refineQty);
                } else {
                    for (j = 4021000; j < 4021009; j++) {
                        cm.gainItem(j, -refineQty);
                    }
                    cm.gainItem(j, refineQty);
                }

                break;
            } else if (refineQty <= 1) {
                allDone = false;
                break;
            } else {
                refineQty--;
            }
        }
    }

    return allDone;
}