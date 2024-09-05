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
var text;
//Start
function start(mode, type, selection)
{
	if (CheckStatus(mode))
	{
	    if (status == 0)
	    {
			//第一层对话
			qm.sendNext("大事不好了，邪恶的气息正在侵袭冒险岛世界，勇士，请听我说...");
	    }
		else if (status == 1 )
		{
			//第二层对话
			if (mode == 0)
			{
				qm.sendOk("不要这么冷漠嘛，冒险岛世界的安危也需要你的力量！");
				qm.dispose();
			}
			else
			{
			    text = "本来冒险岛世界的怪物都非常祥和，可是不知道发生了什么事，最近北斗气象台检测到冒险世界正在被一股强大的黑暗力量吞噬，最先发现异常的地区是位于#b魔法密林郊区#k的猴子森林！";
			    text += "猴子们受到黑暗力量的影响一个接一个的死去，并且产生变异。有人甚至看到死去的猴子竟复活过来，对人类进行无差别的攻击！这可能还只是个开始。";
			    qm.sendNextPrev(text);				
			}
		}
		else if (status == 2 )
		{
			qm.sendAcceptDecline("勇士，我知道您很强大，还请#r帮帮我们，我可以送您过去！！#k");
		}
		else if (status == 3)
		{
			if (qm.getLevel() < 40)
			{
				qm.sendOk("还是等你40级以后再去吧，你现在去会死翘翘的。");
				qm.dispose();
			}
			else
			{
				qm.warp(100040103);
			    qm.sendOk("谢谢您，请帮我消灭200只，但愿这样可以让冒险岛世界的黑暗气息能有效地被遏制一些。");
			    qm.forceStartQuest(); 
                qm.dispose();				
			}

		}
	}
}

function end(mode, type, selection)
{
	if (CheckStatus(mode))
	{
	    if (status == 0)
	    {
			//第一层对话
            qm.sendOk("天呐您这么快就消灭了200只，冒险岛世界有救了！谢谢您~！");		
			qm.forceCompleteQuest();
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