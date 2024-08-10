var status = -1;

function start(mode, type, selection) {
	status++;
	if (status == 0) {
		qm.sendNext("嘿！你能帮我个忙吗？最近 #p20000# 有点奇怪。。。");
	} else if (status == 1) {
		qm.sendNext("就在最近，他还常常愁眉苦脸地抱怨自己的关节炎，但他突然变得满面笑容!!");
	} else if (status == 2) {
		qm.sendNext("我觉得那个木箱后面有个秘密。你能偷偷地看一下旁边的木箱吗 #p20000#?");
	} else if (status == 3) {
		qm.sendNext("你知道 #p20000# 在哪, 是吗？他在右边。一直往前走，直到你看到维京在哪里，然后穿过那条悬挂的鲨鱼和章鱼，你就会看到约翰。盒子应该就在他旁边.");
	} else if (status == 4) {
                qm.forceStartQuest();
		qm.dispose();
        }
}

function end(mode, type, selection) {
    qm.forceCompleteQuest();
    qm.gainExp(200);
    qm.dispose();
}