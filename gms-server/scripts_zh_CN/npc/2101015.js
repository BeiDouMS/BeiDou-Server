var arena;
var status = 0;

function start() {
    arena = cm.getPlayer().getAriantColiseum();
    if (arena == null) {
        cm.sendOk("嘿，我在竞技场的战斗中没看到你！你在这里做什么？");
        cm.dispose();
        return;
    }

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
            menuStr = generateSelectionMenu(["I would like to check my battle points! / I would like to exchange (1) Palm Tree Beach Chair", "I would like to know more about the points of the Battle Arena."]);
            cm.sendSimple("你好，我能为你做些什么？");
        } else if (status == 1) {
            if (selection == 0) {
                apqpoints = cm.getPlayer().getAriantPoints();
                if (apqpoints >= 100) {
                    cm.sendNext("哇，看起来你已经准备好要交易的#b100#k点数了，我们来交易吧！");
                } else if (apqpoints + arena.getAriantRewardTier(cm.getPlayer()) >= 100) {
                    cm.sendOk("你的战斗竞技场分数：#b" + apqpoints + "#k 点，你几乎已经达到了这个分数！和我的妻子#p2101016#交谈，获取这些分数，然后再和我交谈！");
                    cm.dispose();
                } else {
                    cm.sendOk("你的战斗竞技场分数：#b" + apqpoints + "#k 点。你需要超过#b100点#k，这样我才能给你#b棕榈树沙滩椅#k。当你有足够的分数时再和我交谈。");
                    cm.dispose();
                }
            } else if (selection == 1) {
                cm.sendOk("主要目标是让玩家在战斗竞技场中积累点数，以便兑换最高奖品：#b椰树沙滩椅#k。在战斗中积累点数，当时机成熟时与我交谈以获取奖品。在每场战斗中，玩家有机会根据最终拥有的珠宝数量来获得积分。但要小心！如果你的积分与其他玩家的差距#ris too high#k，那一切都将化为乌有，你只能获得微薄的#r1 point#k。");
                cm.dispose();
            }
        } else if (status == 2) {
            cm.getPlayer().gainAriantPoints(-100);
            cm.gainItem(3010018, 1);
            cm.dispose();
        }
    }
}

function generateSelectionMenu(array) {     // nice tool for generating a string for the sendSimple functionality
    var menu = "";
    for (var i = 0; i < array.length; i++) {
        menu += "#L" + i + "##b" + array[i] + "#l#k\r\n";
    }
    return menu;
}