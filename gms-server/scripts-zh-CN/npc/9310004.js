/**
 * 东方神州系列地图脚本
 * 北斗项目组	https://github.com/BeiDouMS/BeiDou-Server
 * 作者：@Magical-H
 * 2025-01-02 17:33:23
 */

/*是否允许重复进入*/
const isRepeat = true;	//true = 允许完成任务后重复进入。；false = 不允许完成任务后重复进入。
const isUseItem = false;	//true = 进入时扣除资格证明（需要放弃任务重新领取，降低刷刷效率）； false = 进入时不扣除资格证明（允许玩家原地进入重复刷刷，提高效率）
const itemID = 4031289;		//资格证明
const mapID = 701010321;	//进入的地图
function start() {
	//4103和8512均为寻找赤珠的任务，似乎是不同版本的任务。
	if (isRepeat && (cm.isQuestCompleted(4103) || cm.isQuestCompleted(8512))) {
		cm.sendNextLevel('Sure','最近的家畜又突然变得非常暴躁，我们推测是#b#e#o9300188##k#n又又又卷土重来了，麻烦你去处理一下吧。');
	} else if (cm.haveItem(itemID ,1)) {//资格证明道具
		cm.sendNextLevel('Sure',`嗯？\r\n看不出来你还有些本事哦，连#b#e#v4031289##t4031289##k#n都能搞到手。\r\n既然如此，那你就先去#b#e#m${mapID}##k#n调查看看。`);
	} else {
		cm.sendOkLevel('',`敬礼！\r\n你好，#b#h ##k，我是#e#b#p${cm.getNpc()}##k#n，在这片区域进行巡逻。\r\n如果你没什么事的话，还是快点回去吧！`);
	}
}

function level() {
	leveldispose();
}
function levelnull() {
	leveldispose();
}
function leveldispose() {
	cm.dispose();
}
function levelEnter() {
	if (!isRepeat && (isUseItem && cm.haveItem(itemID ,1))) cm.gainItem(itemID,-1);	//仅有资格证明进入时是否扣除
	cm.warp(mapID);
	cm.dispose();
}
function levelSure() {
	cm.sendYesNoLevel('Coward','Enter',`我现在送你进入#b#e#m${mapID}##k#n，准备好了吗？`);
}
function levelCoward() {
	cm.sendOkLevel('',`当你准备好去#b#e#m${mapID}##k#n的时候再来找我。`);
}