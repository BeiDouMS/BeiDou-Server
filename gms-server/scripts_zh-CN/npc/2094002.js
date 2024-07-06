var status = -1;
var level = 1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        status--;
    }
    if (cm.getPlayer().getMapId() == 925100700) {
        cm.warp(251010404, 0);
        cm.dispose();
        return;
    }

    if (status == 1) {   // leaders cant withdraw
        cm.warp(251010404, 0);
        return;
    }

    if (!cm.isEventLeader()) {
        // Player chose "No" or "End Chat"
        if (mode <= 0) {
            cm.dispose();
        } else {
            cm.sendYesNo("我希望你们的领导和我谈谈。或者，你可能想要退出。你打算放弃这次活动吗？");
        }
    } else {
        var eim = cm.getEventInstance();
        if (eim == null) {
            cm.warp(251010404, 0);
            cm.sendNext("你怎么可能在这里，而没有在实例上注册呢？");
            cm.dispose();
            return;
        }

        level = eim.getProperty("level");

        switch (cm.getPlayer().getMapId()) {
            case 925100000:
                cm.sendNext("我们现在要前往海盗船！要进入船内，我们必须消灭所有守卫的怪物。");
                cm.dispose();
                break;
            case 925100100:
                var emp = eim.getProperty("stage2");
                if (emp === "0") {
                    if (cm.haveItem(4001120, 20)) {
                        cm.sendNext("太棒了！现在去收集20个升华勋章给我。");
                        cm.gainItem(4001120, -20);
                        cm.getMap().killAllMonsters();
                        eim.setProperty("stage2", "1");
                    } else {
                        cm.sendNext("我们现在要前往海盗船！为了进入，我们必须证明自己是高贵的海盗。给我猎取20个新手勋章。");
                    }
                } else if (emp === "1") {
                    if (cm.haveItem(4001121, 20)) {
                        cm.sendNext("太棒了！现在去收集20个资深勋章给我。");
                        cm.gainItem(4001121, -20);
                        cm.getMap().killAllMonsters();
                        eim.setProperty("stage2", "2");
                    } else {
                        cm.sendNext("我们现在要前往海盗船！为了进入，我们必须证明自己是高贵的海盗。给我猎取20个升龙勋章。");
                    }
                } else if (emp === "2") {
                    if (cm.haveItem(4001122, 20)) {
                        cm.sendNext("太棒了！现在让我们走吧。");
                        cm.gainItem(4001122, -20);
                        cm.getMap().killAllMonsters();
                        eim.setProperty("stage2", "3");
                        eim.showClearEffect(cm.getMapId());
                    } else {
                        cm.sendNext("我们现在要前往海盗船！为了进入，我们必须证明自己是高贵的海盗。给我猎取20个资深勋章。");
                    }
                } else {
                    cm.sendNext("下一个阶段已经开放。前进！");
                }
                cm.dispose();
                break;
            case 925100200:
            case 925100300:
"To assault the pirate ship, we must destroy the guards first."
                cm.dispose();
                break;
            case 925100201:
                if (cm.getMap().getMonsters().size() == 0) {
                    cm.sendNext("海盗之王的宝箱出现了！如果你碰巧有一把钥匙，就把它放在宝箱旁边，揭示宝藏。这肯定会让他很生气。");
                    if (eim.getProperty("stage2a") == "0") {
                        cm.getMap().setReactorState();
                        eim.setProperty("stage2a", "1");
                    }
                } else {
                    cm.sendNext("这些风铃花藏匿起来了。我们必须解救它们。");
                }
                cm.dispose();
                break;
            case 925100301:
                if (cm.getMap().getMonsters().size() == 0) {
                    cm.sendNext("海盗之王的宝箱出现了！如果你碰巧有一把钥匙，就把它放在宝箱旁边，揭示宝藏。这肯定会让他很生气。");
                    if (eim.getProperty("stage3a") === "0") {
                        cm.getMap().setReactorState();
                        eim.setProperty("stage3a", "1");
                    }
                } else {
                    cm.sendNext("这些风铃花藏匿起来了。我们必须解救它们。");
                }
                cm.dispose();
                break;
            case 925100202:
            case 925100302:
"These are the Captains and Krus that devote their lives to the Lord Pirate. Kill them as you see fit."
                cm.dispose();
                break;
            case 925100400:
                cm.sendNext("这些是船舶动力的来源。我们必须使用旧金属钥匙封闭门上的动力源！");
                cm.dispose();
                break;
            case 925100500:
                if (cm.getMap().getMonsters().size() == 0) {
                    cm.sendNext("谢谢你救了我们的领袖！我们欠你一份人情。");
                } else {
                    cm.sendNext("打败所有的怪物！甚至是海盗王的手下！");
                }
                cm.dispose();
                break;
        }
    }


}