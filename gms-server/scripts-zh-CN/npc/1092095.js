/*

 脚本功能：疯狂挤奶
 
 */
 
var status = 0;
var ca = java.util.Calendar.getInstance();
var year = ca.get(java.util.Calendar.YEAR); //获得年份
var month = ca.get(java.util.Calendar.MONTH) + 1; //获得月份
var day = ca.get(java.util.Calendar.DATE); //获取日
var hour = ca.get(java.util.Calendar.HOUR_OF_DAY); //获得小时
var minute = ca.get(java.util.Calendar.MINUTE); //获得分钟
var second = ca.get(java.util.Calendar.SECOND); //获得秒
var chance4 = Math.floor(Math.random() * 90 + 10);//点卷随机变量 *20为每次最小数字20 +10是浮动数字 最大为30 可以自行设定大小
var chance5 = Math.floor(Math.random() * 100 + 50);//信用点随机变量
var chance6 = Math.floor(Math.random() * 100000 + 50000);//金币随机变量

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (status == 0 && mode == 0) {
  cm.dispose();
  return;
    }
    if (mode == 1) {
  status++;
    } else {
  status--;
    }
    if (cm.getMapId() == 180000001) {
  cm.sendOk("很遗憾，您因为违反用户守则被禁止游戏活动，如有异议请联系管理员.");//挤奶对应喇叭脚步【event , laba1】
  cm.dispose();
    } else if (status == 0) {
  var selStr = "当前点券：" + cm.getPlayer().getCashShop().getCash(1) + "\r\n当前信用券：" + cm.getPlayer().getCashShop().getCash(4) + "\r\n当前金币：" + cm.getPlayer().getMeso() + "\r\n#d当前服务器时间: #r" + hour + " #b点 #r" + minute + " #b分\r\n\r\n#e#r疯狂挤奶活动暂未开启\r\n\r\n#r     活动将在每小时28分-30分开始#n\r\n\r\n#b开启活动后请疯狂点击我。这样您将随机获得一些：点卷 信用点与游戏币的奖励。#k\r\n\r\n";
	selStr += "#r#L1#挤奶说明#l    #L2#开始挤奶#l\r\n\r\n\r\n ";

  cm.sendSimple(selStr);
    } else if (status == 1) {
  switch (selection) {
  case 1:
cm.sendOk("#r活动将在每小时28分-30分开始#n\r\n\r\n#b开启活动后请疯狂点击我。这样您将随机获得一些点卷，信用点，金币的奖励。#k");
cm.dispose();
break;
  case 2:
if ((minute >= 28 && minute <= 30)) {//开启时间每小时23-24分钟之间
				cm.getPlayer().getCashShop().gainCash(1, chance4);
				cm.getPlayer().getCashShop().gainCash(4, chance5);
    cm.gainMeso(chance6);//给与玩家随机变量游戏币
    cm.sendOk("你挤出 #r" + chance4 + " 点卷.\r\n" + chance5 + "#k 信用点\r\n#r" + chance6 + "#k 游戏币\r\n");
				cm.dropMessage(0,"『疯狂挤奶』 : 恭喜 " + cm.getPlayer() + "在自由市场奶牛挤出 " + chance4 + " 点卷. " + chance5 + " 信用点 . " + chance6 + " 游戏币. "); 
    cm.dispose();
} else {
    cm.sendOk("#e#r疯狂挤奶活动暂未开启#n\r\n\r\n#r活动将在每小时28分-30分开始#n\r\n\r\n#b开启活动后请疯狂点击我。这样您将随机获得一些点卷，信用点，金币的奖励。#k");
    cm.dispose();
}
break;
  }
    }
}