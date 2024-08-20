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
			qm.sendNext("嗨，您是新来的冒险家吗？正如你所见，冒险岛世界存在一个着常年被冰雪覆盖的村子 #b冰封雪域#k。关于这里有个小故事，你感兴趣吗？");
	    }
		else if (status == 1 )
		{
			//第二层对话
			var text = "据说这里的某深处存在着一个恐怖的存在，它的诅咒导致这里常年下雪。曾经有一个小队前去讨伐这传说中的怪物。";
            text += "这个队伍集结了冒险岛世界最强大的4个职业，在到达这个怪物的藏身之处的洞穴迷宫前中了陷阱，战士队长为了队友，永远留在了那里。";
			text += "他们唯一的牧师更是最后感染了那个怪物的火毒而走火入魔。纵使这般，虽然他们获胜了，但也是惨胜。其中的弓箭手和飞侠是一对夫妻。";
			text += "飞侠怀有身孕，殊不知这怪物竟然在死前将自身最后的邪念注入到了这个飞侠体内，想借此重生。";
			text += "他们回到村里后，村长阿尔卡特听闻此事，竟欲处死弓箭手的妻子,他施展魔法禁锢了她，村民将她绑上祭台。";
			text += "最后这二人也终于死了，讨伐怪物的远征队，无一活口。。。据说这个怪物还是重生了，最后见过它的人，只隐约看到它有8只手。。。";
			qm.sendNextPrev(text);
		}
		else if (status == 2)
		{
			qm.sendAcceptDecline("怎么样，故事有趣吗？不过这对你来说还是有难度的，怎么样，想热热身吗？");
		}
		else if (status == 3)
		{
			//最后一层对话完继续循环至此，退出结束
			qm.sendOk("好的，你去狩猎5只#b#o9409000##k！");
			qm.forceStartQuest();
            qm.dispose();
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
            qm.sendOk("不错嘛,任务完成!这是给你的奖励,希望你能用到它\r\n\r\n#fUI/CashShop.img/CSDiscount/bonus# 金币x50000");		
			qm.forceCompleteQuest();
			qm.gainMeso(50000);
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