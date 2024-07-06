var status = -1;

function start() {
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 1 && mode == 0) {
            cm.dispose();
            return;
        }

        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        var eim = cm.getEventInstance();
        if (eim == null) {
            cm.sendNext("活动还没有开始...");
            cm.dispose();
            return;
        }
        switch (cm.getPlayer().getMapId()) {
            case 610030100:
                if (status == 0) {
                    cm.sendNext("啊，你成功进来了。让我快告诉你：他们已经抓住我们了。守护大师们大约一分钟后就要来这里了。我们最好赶紧。");
                } else if (status == 1) {
                    cm.sendNext("通往扭曲大师的传送门已经坏了。我们必须找到另一条路，一条会让我们经历许多死亡陷阱的路。");
                } else if (status == 2) {
                    cm.sendNext("你可以在这附近找到传送门……你最好快点找到它。我会赶上来的。");
                    cm.dispose();
                }
                break;
            case 610030200:
                if (status == 0) {
                    cm.sendNext("这太成功了！现在，对于这条路，我相信我们需要每个冒险家职业的人才能通过。");
                } else if (status == 1) {
                    cm.sendNext("他们需要运用他们的技能在被称为"Sigils"的东西上。一旦这五个都完成了，我们就可以继续前进。");
                    cm.dispose();
                }
                break;
            case 610030300:
                if (status == 0) {
                    cm.sendNext("现在我们这里有更多的封印。至少需要五名冒险家爬到最顶端并穿过传送门。但要注意：这张地图上并非所有的墙壁或地面都是看起来的那样，所以要小心行事！");
                } else if (status == 1) {
                    cm.sendNext("哦，还要小心这些致命陷阱：巨石。它们真的很厉害。祝你好运。");
                    cm.dispose();
                }
                break;
            case 610030400:
                if (status == 0) {
                    cm.sendNext("现在我们这里有更多的封印。然而，其中一些并不起作用。在这里，所有职业都必须发挥自己的作用，因为至少有一个封印是由他们的职业技能激活的，但是每个职业可能有多个封印，所以一定要测试它们全部。");
                } else if (status == 1) {
                    cm.sendNext("这些吸血蝙蝠会挡住你的路，但它们只是一种干扰。要摆脱它们，让五名冒险者同时站在中左平台上。要通过，尝试每一个符文，直到它们生效。");
                    cm.dispose();
                }
                break;
            case 610030500:
                if (status == 0) {
                    cm.sendNext("惊讶你能走到这一步！你在这里看到的是红木城堡的雕像，但是没有任何武器。");
                } else if (status == 1) {
                    cm.sendNext("有五个房间，每个房间附近都有一个雕像标记。");
                } else if (status == 2) {
                    cm.sendNext("我怀疑这些房间中每一个都有雕像的五把武器之一。");
                } else if (status == 3) {
                    cm.sendNext("把武器带回来，把它们恢复到掌握之遗物中！");
                    cm.dispose();
                }
                break;
            case 610030700:
                cm.sendNext("那真是一次出色的表现！这条路通向扭曲大师的军械库。");
                cm.dispose();
                break;
        }
    }
}