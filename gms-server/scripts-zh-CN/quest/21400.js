var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode == 0 && type == 0) {
        status -= 2;
    } else if (mode != 1) {
        //if (mode == 0)
            qm.sendNext("#b(你得考虑一下...)#k");
        qm.dispose();
        return;
    }
	
	if (status == 0) {
        qm.sendAcceptDecline("训练进展如何？我知道你很忙，但是请立即过来#b里恩#k.摩诃开始变得怪异了.和以前不一样了。是。。。比平时更黑。");
    } else if (status == 1) {
            qm.startQuest();
            qm.sendOk("我对此有种不好的感觉。请回来。我从来没有见过这样的马哈，但我能感觉到它所经历的痛苦。#b只有你，摩诃的主人，才能做点什么!");
    } else if (status == 2) {
        qm.dispose();
    }
}