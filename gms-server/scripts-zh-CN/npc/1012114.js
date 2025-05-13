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
              cm.sendSimple("吼！我是兴儿，时刻准备守护这片地方。你来这儿干什么？\r\n#b#L0# 请告诉我这片地方是怎么回事。#l\r\n#L1# 我带来了 #t4001101#。#l\r\n#L2# 我想离开这片地方。#l");
          } else {
              cm.sendSimple("吼！我是兴儿，时刻准备守护这片地方。你来这儿干什么？\r\n#b#L0# 请告诉我这片地方是怎么回事。#l\r\n#L2# 我想离开这片地方。#l");
          }
      } else if (status == 1) {
          if (chosen == -1) {
              chosen = selection;
          }
          if (chosen == 0) {
              cm.sendNext("这片地方可以说是绝佳之处，每逢满月你都能品尝到月兔制作的美味年糕。");
          } else if (chosen == 1) {
              if (cm.haveItem(4001101, 10)) {
                  cm.sendNext("哦……这不是月兔做的年糕吗？请把年糕给我。嗯……这些看起来很美味。下次再带更多的 #b#t4001101##k 来找我哦。一路平安！");
              } else {
                  cm.sendOk("我建议你检查一下，确保你确实收集了 #b10 个 #t4001101##k。");
                  cm.dispose();
              }
          } else if (chosen == 2) {
              cm.sendYesNo("你确定要离开吗？");
          } else {
              cm.dispose();

          }
      } else if (status == 2) {
          if (chosen == 0) {
              cm.sendNextPrev("在这片区域收集遍布各处的迎月花种子，然后把种子种在月牙附近的土壤里，就能看到迎月花盛开。迎月花有六种，每种都需要不同的土壤。土壤必须适合花的种子，这一点至关重要。");
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
                  cm.sendOk("那你最好给我收集些美味的年糕，因为时间不多了，吼！");
              }
              cm.dispose();
          }
      } else if (status == 3) {
          if (chosen == 0) {
              cm.sendNextPrev("当迎月花盛开时，满月就会升起，那时月兔就会出现并开始舂米。你的任务是击退怪物，确保月兔能专心制作出最美味的年糕。");
          }
      } else if (status == 4) {
          if (chosen == 0) {
              cm.sendNextPrev("我希望你和你的队员们合作，给我弄来 10 个年糕。我强烈建议你在规定时间内把年糕给我。");
          }
      } else {
          cm.dispose();
      }
  }
}