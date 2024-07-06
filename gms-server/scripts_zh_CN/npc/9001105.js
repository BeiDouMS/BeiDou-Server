var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            if (cm.getPlayer().getMapId() == 922240200) {
                cm.sendOk("真遗憾，等你准备好了再回来吧。");
            }

            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (cm.getMapId() == 922240200) {
                cm.sendSimple("你有什么要说的吗...? #b\b\r\n#L0#我想要救出嘎嘎。#l\r\n");    //#L1#I want to go to the Space Mine.#l
            } else if (cm.getMapId() >= 922240000 && cm.getMapId() <= 922240019) {
                cm.sendYesNo("如果你失败了也不要担心。你还有3次机会。你还想放弃吗？");
            } else if (cm.getMapId() >= 922240100 && cm.getMapId() <= 922240119) {
                var text = "You went through so much trouble to rescue Gaga, but it looks like we're back to square one. ";
                var rgaga = cm.getPlayer().getEvents().get("rescueGaga");
                if (rgaga.getCompleted() > 10) {
                    text += "Please don't give up until Gaga is rescued. To show you my appreciation for what you've accomplished thus far, I've given you a Spaceship. It's rather worn out, but it should still be operational. Check your #bSkill Window#k.";
                    rgaga.giveSkill(cm.getPlayer());
                } else {
                    text += "Let's go back now.";
                }

                cm.sendNext(text);
            }
        } else {
            if (cm.getPlayer().getMapId() == 922240200) {
                if (status == 1) {
                    if (selection == 0) {
                        selected = 1;
                        cm.sendNext("欢迎！我从小月兔那里听说了发生的事情，很高兴你来了，因为我正打算请求一些帮助。Gaga是我的朋友，以前帮过我，经常过来打个招呼。不幸的是，他被外星人绑架了。");
                    } else {
                        selected = 2;
                        cm.sendYesNo("在太空矿场，你可以找到一种特殊的矿石，叫做#bKrypto Crystals#k，它含有太空的神秘力量。#bKrypto Crystals#l通常呈翡翠绿色，但如果被太空飞船的#bSpace Beam#k击中，就会变成褐色。记住，为了阻止外星人的阴谋，需要#b10个褐色Krypto Crystal#和10个翡翠Krypto Crystal#。但即使#b1个Krypto Crystal#也能帮上忙，尽可能多地带给我。哦，还有一件事！太空矿场受到太空Mateons的保护。由于#Krypto Crystals#k的力量，它们非常强大，所以不要试图打败它们。只需专注于迅速收集水晶。");
                    }
                } else if (status == 2) {
                    if (selected == 1) {
                        cm.sendYesNo("如果我们把嘎嘎留给外星人，他会遭遇可怕的事情！我会让你借用一艘月兔用来旅行的太空飞船，这样你就可以救出嘎嘎。虽然他有时看起来有点犹豫不决、慢吞吞和不成熟，但他其实是一个很好的年轻人。你现在想去救他吗？");
                    } else if (selected == 2) {
                        cm.sendOk("尚未编码，f4。");
                        cm.dispose();
                    }
                } else if (status == 3) {
                    var em = cm.getEventManager("RescueGaga");
                    if (em == null) {
                        cm.sendOk("此活动目前不可用。");
                    } else if (!em.startInstance(cm.getPlayer())) {
                        cm.sendOk("当前地图上有其他玩家，稍后再来吧。");
                    }

                    cm.dispose();
                }
            } else if (cm.getPlayer().getMapId() >= 922240000 && cm.getPlayer().getMapId() <= 922240019) {
                cm.warp(922240200, 0);
                cm.dispose();
            } else if (cm.getPlayer().getMapId() >= 922240100 && cm.getPlayer().getMapId() <= 922240119) {
                cm.warp(922240200, 0);
                cm.dispose();
            }
        }
    }
}