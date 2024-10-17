/*
	NPC Name: 		Cygnus
	Description: 		Quest - Encounter with the Young Queen
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || mode == 0 && type > 0) {
        qm.dispose();
        return;
    }

    if (mode == 1) {
        status++;
    } else {
	if (status == 2) {
	    qm.sendNext("嗯，没什么好担心的。对你这样的人来说，这将是轻而易举的事。鼓起勇气，准备好了就告诉我.");
	    qm.dispose();
	    return;
	}
        status--;
    }
    if (status == 0) {
    	qm.sendNext("喔? #p1101002# 派你来的, 呵呵？你一定是最近加入皇家骑士团的新手。欢迎，很高兴见到你！我是 #p1102000#. 我是培训老师，培训所有像你这样的贵族。当然，我不是你能说的那种人.");
    } else if (status == 1) {
    	qm.sendNextPrev("我们叫皮约斯。你已经看到了 #p1101001# 她一直在皇后身边，不是吗? 小精灵和 #p1101001#, 但我们属于不同的类型. 当然，你没见过我们，因为我们只住在埃雷夫。你很快就会习惯吃比萨饼的.");
    } else if (status == 2) {
    	qm.sendNextPrev("哦，你知不知道埃雷夫里面没有怪物？一点邪恶都不敢进入地狱。但别担心。你将能够训练由叫做咪咪的幻觉怪物.");
    } else if (status == 3) {
    	qm.sendAcceptDecline("你好像准备好了！看看你所取得的成就，我认为你应该马上开始寻找更高级的咪咪。. 现在需要你去猎杀 #b15个 #r#o100122# 在 #m130010100##k? 通过左边的路口可以到达 #b #m130010100# #k.");
    } else if (status == 4) {
        qm.guideHint(12);
        qm.forceStartQuest(20020);
        qm.forceCompleteQuest(20100);
        qm.forceStartQuest();
        qm.dispose();
    }
}

// function end(mode, type, selection) {

// }