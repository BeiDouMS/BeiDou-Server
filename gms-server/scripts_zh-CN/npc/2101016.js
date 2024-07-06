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
            copns = arena.getAriantScore(cm.getPlayer());
            if (copns < 1 && !cm.getPlayer().isGM()) {
                cm.sendOk("太糟糕了，你没有得到任何珠宝！");
                cm.dispose();
            } else {
                cm.sendNext("好的，让我看看……你做得非常好，而且你带来了我喜欢的#b" + copns + "#k珠宝。由于你完成了比赛，我将奖励你#b" + arena.getAriantRewardTier(cm.getPlayer()) + "点#k战斗竞技场分数。如果你想了解更多关于战斗竞技场分数的信息，那就去找#b#p2101015##k谈谈吧。");
            }
        } else if (status == 1) {
            //cm.warp(980010020, 0);
            copns = arena.getAriantRewardTier(cm.getPlayer());
            arena.clearAriantRewardTier(cm.getPlayer());
            arena.clearAriantScore(cm.getPlayer());
            cm.removeAll(4031868);

            cm.getPlayer().gainExp(92.7 * cm.getPlayer().getExpRate() * copns, true, true);
            cm.getPlayer().gainAriantPoints(copns);
            cm.sendOk("好的！下次再给我更多的宝石！啊哈哈哈哈哈！");
            cm.dispose();
        }
    }
}