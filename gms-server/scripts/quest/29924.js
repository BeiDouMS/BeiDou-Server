var status = -1;

function start(mode, type, selection) {
	if (qm.getPlayer().getLevel() >= 10 && ((qm.getPlayer().getJob().getId() / 100) | 0) == 21) {
                if(!qm.haveItem(1142129)) {
                        if(qm.canHold(1142129)) {
                                qm.gainItem(1142129,1);
                        } else {
                                qm.dispose();
                                return;
                        }
                }
                
                var medalname = qm.getMedalName();
                qm.message("<" + medalname + "> 奖励已获取.");
                qm.earnTitle("<" + medalname + "> 奖励已获取.");
                
		qm.forceStartQuest();
		qm.forceCompleteQuest();
	}
        
	qm.dispose();
}

function end(mode, type, selection) {
	if (qm.getPlayer().getLevel() >= 10 && ((qm.getPlayer().getJob().getId() / 100) | 0) == 21) {
                if(!qm.haveItem(1142129)) {
                        if(qm.canHold(1142129)) {
                                qm.gainItem(1142129,1);
                        } else {
                                qm.dispose();
                                return;
                        }
                }
            
                var medalname = qm.getMedalName();
                qm.message("<" + medalname + "> 奖励已获取.");
                qm.earnTitle("<" + medalname + "> 奖励已获取.");
            
		qm.forceStartQuest();
		qm.forceCompleteQuest();
	}
        
	qm.dispose();
}