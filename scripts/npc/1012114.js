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
                cm.sendSimple("嗷哦！~我是#p1012114#是这里的守护者，你来这里做什么？\r\n" +
                    "#b#L0#请告诉我这里的事情#l\r\n" +
                    "#L1#我带来了10个 #t4001101##l\r\n" +
                    "#L2#我想离开这里#l");
            } else {
                cm.sendSimple("嗷哦！~我是#p1012114#是这里的守护者，你来这里做什么？\r\n" +
                    "#b#L0#请告诉我这里的事情#l\r\n" +
                    "#L2#我想离开这里#l");
            }
        } else if (status == 1) {
            if (chosen == -1) {
                chosen = selection;
            }
            if (chosen == 0) {
                cm.sendNext("在每个满月的夜晚，这座山丘是品尝月妙年糕的最佳地点！");
            } else if (chosen == 1) {
                if (cm.haveItem(4001101, 10)) {
                    cm.sendNext("这正是月妙制作的年糕！请快点给我！谢谢你们的帮助！我已经吃饱了，现在送你们回去吧。");
                } else {
                    cm.sendOk("你确定你有 #b10 #t4001101#s#k？");
                    cm.dispose();
                }
            } else if (chosen == 2) {
                cm.sendYesNo("你确定要离开吗？");
            } else {
                cm.dispose();

            }
        } else if (status == 2) {
            if (chosen == 0) {
                cm.sendNextPrev("普通下面的草，收获六种颜色的种子，把它们种在月亮周围的六个平台上，如果是正确的平台，种子就会开出迎月花。");
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
                    cm.sendOk("请快点收集年糕给我，时间不多了！");
                }
                cm.dispose();
            }
        } else if (status == 3) {
            if (chosen == 0) {
                cm.sendNextPrev("当平台上开满迎月花（提示：褐黄蓝绿紫浅紫），满月就会召唤月妙出来了，你们需要确保月妙专心捣年糕不被打扰。");
            }
        } else if (status == 4) {
            if (chosen == 0) {
                cm.sendNextPrev("我会在这里等待你们给我送来#b10个#k年糕，我现在很饿，请你们抓紧时间！");
            }
        } else {
            cm.dispose();
        }
    }
}