/*
	NPC Name: 		Nineheart
	Description: 		Quest - Do you know the black Magician?
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
	if (status == 8) {
	    qm.sendNext("哦，你还有什么问题吗？再跟我说话，我一开始就跟你解释。");
	    qm.dispose();
	    return;
	}
        status--;
    }
    if (status == 0) {
    	qm.sendNext("嗨, #h0#. 来迎来到 #p1101000# 骑士团. 我的名字是 #p1101002# 而我目前作为年轻慈禧的战术家。哈哈！");
    } else if (status == 1) {
    	qm.sendNextPrev("我敢肯定，你有很多的问题，因为一切都发生得太快。我会解释这一切，一个接一个，从那里你是你在这里做什么。");
    } else if (status == 2) {
    	qm.sendNextPrev("这个岛叫做耶雷弗。多亏了皇后的魔法，这座岛通常像空中的小船一样漂浮在空中，在枫树世界周围巡逻。不过，现在我们停在这里是有原因的。");
    } else if (status == 3) {
    	qm.sendNextPrev("T这位年轻的女皇是枫之谷世界的统治者。什么？这是你听说过她的第一次？啊，是的。嗯，她是枫之谷世界的统治者，但她不喜欢来控制它。她从远处观看，以确保一切都很好。好吧，至少这是她一贯的作用。");
    } else if (status == 4) {
    	qm.sendNextPrev("但现在不是这样。我们已经在整个枫树世界找到了预示着黑法师复活的迹象。我们不能让黑法师回来恐吓枫树世界就像他过去一样！");
    } else if (status == 5) {
    	qm.sendNextPrev("但那是很久以前的事了，今天的人们还没意识到黑法师有多可怕。我们都被我们今天所享受的和平的枫树世界宠坏了，也忘记了枫树世界曾经是多么的混乱和可怕。如果我们不做点什么，黑暗魔法师将再次统治枫树世界！");
    } else if (status == 6) {
    	qm.sendNextPrev("这就是为什么年轻的皇后决定自己动手。她正在组建一个勇敢的法师骑士的头衔，以一劳永逸地打败黑法师。你知道你需要做什么，对吗？我相信你有个主意，因为你，你自己，报名成为一名骑士。");
    } else if (status == 7) {
    	qm.sendNextPrev("我们必须变得更强这样我们就能击败黑法师如果他复活。我们的首要目标是防止他破坏枫树世界，而你将在其中扮演一个突出的角色。");
    } else if (status == 8) {
    	qm.sendAcceptDecline("我的解释到此结束。我回答了你所有的问题吗？ \r\n\r\n#fUI/UIWindow.img/QuestIcon/4/0# \r\n#fUI/UIWindow.img/QuestIcon/8/0# 380 exp");
    } else if (status == 9) {
        if (!qm.isQuestStarted(20016)) {
            qm.forceStartQuest();
            qm.gainExp(380);
        }
        qm.sendNext("我很高兴你清楚我们目前的状况但你知道在你目前的水平上你连面对Black Mage的手下都不够坚强更别说面对黑法师本人了事实上连他手下的手下都没有你将如何以你目前的水平保护枫树世界？");
    } else if (status == 10) {
    	qm.sendNextPrev("虽然你已经被接受为骑士，但你还不能被承认为骑士。你不是一个正式的骑士，因为你甚至不是一个受训的骑士。如果你保持目前的水平，你将只不过是一个勤杂工。 #p1101000# 骑士.");
    } else if (status == 11) {
    	qm.sendNextPrev("但从一开始就没人是个强壮的骑士。皇后不希望有人强大。她需要一个有勇气的人，经过严格的训练，她能把他培养成一个强壮的骑士。所以，你应该先成为一名受过训练的骑士。我们会讨论你的任务，当你到达这一点。");
    } else if (status == 12) {
    	qm.forceCompleteQuest();
        qm.sendPrev("走左边的入口到达训练森林。在那里，你会发现#p1102000#，培训老师，谁将教你如何变得更强。我可不想看到你漫无目的的四处游荡直到你到达LV。10，你听到了吗？");
    } else if (status == 13) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
}