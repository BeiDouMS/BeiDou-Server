/*
StudyJS-By HaiLong

BaseFunction:                                                             type |          mode               |  section
0:  sendYesNo(str) -[弹出Yes/No对话框]                                     1   |  是=1,否=0,结束=-1          |     \
1:  sendNext(str)  -[弹出带有下一个的对话框]                               0   |  下一项=1,结束=-1           |     \
2:  sendPrev(str)  -[弹出带有上一个的对话框]                               0   |  上一项=0,结束=-1           |     \
3:  sendOk(str)    -[弹出带有确定的对话框]                                 0   |  确定=1,结束=-1             |     \
4:  sendNextPrev(str)      -[弹出上&下一个的对话框]                        0   |  上一项=0,下一项=1,停止=-1  |     \
5:  sendAcceptDecline(str) -[弹出接受&拒绝的对话框]                        12  |  接受=1,拒绝=0,结束=-1      |     \  
6:  sendSimple(str) -[弹出带有选项(#L索引# xxx #l)的对话框]                4   |  选择=1,结束=0              |  选择的索引       
7:  sendStyle(str,int styles[]) -[弹出选择造型的对话框]                    0   |  确定=1,取消=0,结束=-1      |  选择的索引
8:  sendGetNumber(str,int def, int min, int max) -[弹出输入数字的对话框]   0   |  确定=1,结束=0              |  输入的数字
9:  setGetText(str) -[保存指定的字符串]                                    \   |          \                  |     \
10: sendGetText(str) -[弹出带有输入字符串的对话框]                         0   |  确定=1,结束=0              |     \
11: getText(str) -[返回sendGetText(str)/setGetText(str)寫入的字符串]       \   |          \                  |     \

AllowFunction-could use directly
-gainMeso获取金币(int gain);
 */


var status = -1;

//Start
function start(mode, type, selection)
{
	if (CheckStatus(mode))
	{
	    if (status == 0)
	    {
			//第一层对话
			qm.sendNext("哦，尊贵的冒险者#r#h ##k，欢迎莅临#b《北斗冒险岛》#k这方被神秘与奇迹所笼罩的奇幻之地！吾乃您的引导者，#r小睡#k，一个集智慧与优雅于一身的存在，特此恭候您的到来。")
	    }
		else if (status == 1 )
		{
			//第二层对话
			qm.sendNextPrev("哈哈，恭喜您，被命运的骰子扔进了这场荒诞不经的大冒险！您准备好了吗？前方是无尽的搞笑、离奇和不可思议！从玛加提亚城的狂欢派对，直接跳入冰封雪域的冰雪奇缘，再一头扎进森林迷宫的魔幻蘑菇圈，最后飘向星空岛屿的银河漂流瓶大会！")
		}
		else if (status == 2)
		{
			//最后一层对话完继续循环至此，退出结束
		    if (mode == 0)
		    {
		    	qm.forceStartQuest();
                qm.gainMeso(100000);			
		    	qm.forceCompleteQuest();
				qm.dispose();
		    }
			else
			{
				qm.sendNextPrev("记住，冒险岛不只是冒险，它是一场对常规的无情嘲弄，是对逻辑的彻底颠覆！所以，请放开您的笑点，拥抱这份疯狂，与我们一起，在荒诞与奇幻中，创造属于我们的神经质传奇吧！哈哈，让我们一起，笑对人生，疯癫到底！");
			}
			
		}
		else if (status == 3)
		{
			if (qm.getPlayer().getMapId() != 4)
			{
				qm.sendAcceptDecline("你想重新回到#b青苹果乐园#k吗？");
			}
			else
			{
			    qm.sendOk("请跟你面前的昨日酣睡对话吧！祝您游戏开心。");
			    qm.forceStartQuest();
                qm.dispose();				
			}

		}
		else if (status == 4)
		{
			if (qm.getJobId() == 0)
			{
			    qm.warp(4);
			    qm.sendOk("请跟你面前的昨日酣睡对话吧！祝您游戏开心。");
			    qm.forceStartQuest();			
			    qm.dispose();				
			}
			else
			{
				qm.sendOk("咦，你竟然不是新手。很遗憾我不能带你前往青苹果乐园了，不过我依然可以给你一部分启动资金：\r\n #fUI/CashShop.img/CashItem/0# x100000");
				qm.forceStartQuest();
                qm.gainMeso(100000);			
		    	qm.forceCompleteQuest();
				qm.dispose();
			}
		}
		else
			qm.dispose();
	}
}

function end(mode, type, selection)
{
	if (CheckStatus(mode))
	{
	    if (status == 0)
	    {
			//第一层对话
            qm.sendOk("看来你已经见过小睡了，欢迎来到北斗，这是给你的启动资金，希望对您的冒险有所帮助\r\n\r\n#fUI/CashShop.img/CSDiscount/bonus# 金币: 1000000");
            qm.gainMeso(100000);			
			qm.forceCompleteQuest();
            qm.dispose();			
	    }
		else if (status == 1 )
		{
			//第二层对话			
		}
		else
		{
			//最后一层对话完继续循环至此，退出结束
			qm.dispose();
		}
	}
			
}

function CheckStatus(mode)
{
	if (mode == -1)
	{
		qm.dispose();
		return false;
	}
	
	if (mode == 1)
	{
		status++;
	}
	else
	{
		status--;
	}
	
	if (status == -1)
	{
		qm.dispose();
		return false;
	}	
	return true;
}