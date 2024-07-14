/* Author: aaroncsn <MapleSea Like>
	NPC Name: 		Karcasa
	Map(s): 		The Burning Sands: Tents of the Entertainers(260010600)
	Description: 		Warps to Victoria Island
*/
var towns = [100000000, 101000000, 102000000, 103000000, 104000000];

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0) {
            cm.sendNext("嗯...你是害怕速度还是高度？你不相信我的飞行技能？相信我，我已经解决了所有的问题！");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendNext("我不知道你是怎么发现这个的，但你来对地方了！对于那些在尼哈尔沙漠徘徊并开始想家的人，我提供直飞金银岛的航班，不停歇！别担心飞船——它只摔过一两次！你在那艘小飞船上长时间飞行时不觉得幽闭恐惧吗？");
        } else if (status == 1) {
            cm.sendYesNo("请记住两件事。一，这条线路实际上是用于海外运输，所以 #r我不能保证你会降落在哪个城镇#k。二，由于我要安排你乘坐这个特殊航班，费用会有点高。服务费是 #e#b10,000 枚金币#n#k。有一架航班即将起飞。你对这个直达航班感兴趣吗？");
        } else if (status == 2) {
            cm.sendNext("好的，准备起飞~");
        } else if (status == 3) {
            if (cm.getMeso() >= 10000) {
                cm.gainMeso(-10000);
                cm.warp(towns[Math.floor(Math.random() * towns.length)]);
            } else {
                cm.sendNextPrev("嘿，你手头紧吗？我告诉过你，你需要 #b10,000#k 金币才能参与这个活动。");
                cm.dispose();
            }
        }
    }
}