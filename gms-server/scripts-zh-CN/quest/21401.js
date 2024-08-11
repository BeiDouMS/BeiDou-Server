var status = -1;

function start(mode, type, selection) {
    status++;
    if (mode == 0 && type == 0) {
        status -= 2;
    } else if (mode != 1) {
        //if (mode == 0)
            qm.sendNext("#b(你需要考虑一下。。。)#k");
        qm.dispose();
        return;
    }
	
	if (status == 0) {
        qm.sendNext("你问我，为什么我看起来像这样？我不想谈这个，但我想我不能对你隐瞒，因为你是我的主人.");
    } else if (status == 1) {
		qm.sendNextPrev("当你被困在冰里几百年的时候，我也被冻住了。离开你很久了。就在那时，黑暗的种子种在了我的心里.");
	} else if (status == 2) {
		qm.sendNextPrev("但自从你醒来，我以为黑暗已经消失了。我以为事情会回到原来的样子。但我错了。。。");
	} else if (status == 3) {
		qm.sendAcceptDecline("拜托，战神。请阻止我发火。只有你能控制我。它现在从我手中消失了。请不惜一切代价#r别让我发疯#k!");
	} else if (status == 4) {
		var em = qm.getEventManager("MahaBattle");
                if (!em.startInstance(qm.getPlayer())) {
                    qm.sendOk("地图上有人，请稍后再来.");
                } else {
                    qm.startQuest();
                }
                
		qm.dispose();
	}
}

function end(mode, type, selection) {
    status++;
    if (mode == 0 && type == 0) {
        status -= 2;
    } else if (mode != 1) {
        //if (mode == 0)
            qm.sendNext("#b(你需要考虑一下……)#k");
        qm.dispose();
        return;
    }
	
	if (status == 0) {
		qm.sendNext("谢谢你，战神。如果不是你，我会很生气，谁知道会发生什么。谢谢你，不要！这只是你作为我主人的职责。。。");
	} else if(status == 1) {
		qm.sendYesNo("不管怎样，我只是注意到你达到了一个多么高的水平。如果你能控制我的愤怒，我想你已经准备好处理更多的能力了.");
	} else if(status == 2) {
		if(!qm.isQuestCompleted(21401)) {
			if(!qm.canHold(1142132)) {
				qm.sendOk("哇，你的#b装备栏#k已满. 请留出多余的空间来获取物品.");
				qm.dispose();
				return;
			}
                        if (!qm.canHold(2280003, 1)) {
                                qm.sendOk("你的#b物品栏#k已满. 请留出多余的空间来获取物品.");
				qm.dispose();
				return;
                        }
			
			qm.gainItem(1142132, true);
                        qm.gainItem(2280003, 1);
			qm.changeJobById(2112);
			
			qm.completeQuest();
		}
		qm.sendNext("你的技能已经恢复。这些技能已经沉睡了很长时间，你必须重新训练自己，但一旦你完成训练，你就会和以前一样.");
	} else if(status == 3) {
                qm.dispose();
        }
}

function spawnMob(x, y, id, map) {
    if (map.getMonsterById(id) != null) {
        return;
    }

    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
    const Point = Java.type('java.awt.Point');
    var mob = LifeFactory.getMonster(id);
    map.spawnMonsterOnGroundBelow(mob, new Point(x, y));
}