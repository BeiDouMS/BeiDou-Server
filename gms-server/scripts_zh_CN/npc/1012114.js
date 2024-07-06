/*
  Growlie (that fatass uhh.. hungry lion or whatever)

  @author FightDesign (RageZONE)
  @author Ronan
  */

var status = 0;
var chosen = -1;

function clearStage(stage, eim) {
    eim.setProperty(stage + "stageclear", "true");
    eim.showClearEffect(true);

    eim.giveEventPlayersStageReward(stage);
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();

    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 0) {
            status += ((chosen == 2) ? 1 : -1);
        } else {
            status++;
        }

        if (status == 0) {
            if (cm.isEventLeader()) {
                cm.sendSimple("咆哮！我是格劳利，随时准备保护这个地方。你是怎么来到这里的？\r\n#b#L0# 请告诉我这个地方是怎么回事。#l\r\n#L1# 我带来了#t4001101#。#l\r\n#L2# 我想离开这个地方。#l");
            } else {
                cm.sendSimple("咆哮！我是格劳利，随时准备保护这个地方。你是怎么来到这里的？\r\n#b#L0# 请告诉我这个地方是怎么回事。#l\r\n#L2# 我想离开这个地方。#l");
            }
        } else if (status == 1) {
            if (chosen == -1) {
                chosen = selection;
            }
            if (chosen == 0) {
                cm.sendNext("这个地方最好的描述是，你可以在每个满月时品尝到由月兔制作的美味年糕。");
            } else if (chosen == 1) {
                if (cm.haveItem(4001101, 10)) {
                    cm.sendNext("哦...这不是月兔做的年糕吗？请把年糕给我。嗯...这些看起来很美味。下次再来找我拿更多#b#t4001101##k。一路平安！");
                } else {
                    cm.sendOk("我建议你检查并确保你确实收集了#b10 #t4001101#s#k。");
                    cm.dispose();
                }
            } else if (chosen == 2) {
                cm.sendYesNo("你确定要离开吗？");
            } else {
                cm.dispose();

            }
        } else if (status == 2) {
            if (chosen == 0) {
                cm.sendNextPrev("收集这个区域所有紫菀叶上的紫菀种子，并将种子种植在新月附近的基座上，看着紫菀绽放。有6种类型的紫菀，它们都需要不同的基座。基座必须与花的种子相匹配是至关重要的。");
            } else if (chosen == 1) {
                cm.gainItem(4001101, -10);

                var eim = cm.getEventInstance();
                clearStage(1, eim);

                var map = eim.getMapInstance(cm.getPlayer().getMapId());
                map.killAllMonstersNotFriendly();

                eim.clearPQ();
                cm.dispose();
            } else {
                if (mode == 1) {
                    cm.warp(910010300);
                } else {
                    cm.sendOk("你最好为我收集一些美味的年糕，因为时间不等人，咆哮！");
                }
                cm.dispose();
            }
        } else if (status == 3) {
            if (chosen == 0) {
                cm.sendNextPrev("当报春花盛开时，满月将升起，这时月兔将出现并开始磨米。你的任务是击退怪物，确保月兔能专心制作最好的年糕。");
            }
        } else if (status == 4) {
            if (chosen == 0) {
                cm.sendNextPrev("我希望你和你的队友合作，帮我弄到10个年糕。我强烈建议你在规定的时间内给我年糕。");
            }
        } else {
            cm.dispose();
        }
    }
}