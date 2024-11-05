/*
脚本：新人福利礼包
作者：SpicyBurgerKing
日期：2024-10-31
备注：北斗开发组
 */

var status;
var textMsg;
//Start
function start() 
{
  status = -1;
  action(1, 0, 0);
}

function action(mode, type, selection) 
{
	if (CheckStatus(mode))
	{
	    if (status == 0)
	    {
			//第一层对话
			var strGetText = cm.getCharacterExtendValue("新人福利礼包");
			if ( strGetText == "已领取" )
			{
				cm.sendOk("您已经领取了新手奖励了。每个角色#r限领一次。#k");
				cm.dispose();
			}
			else
			{
				cm.sendAcceptDecline("您确定要领取新手礼包吗？一个角色#r限领一次。#k");	
			}
	    }
		else if (status == 1 )
		{
			//第二层对话
		    cm.saveOrUpdateCharacterExtendValue("新人福利礼包", "已领取");
		    cm.gainItem(2430033,10);
		    cm.sendOk("恭喜您获得新手奖励，祝您游戏愉快！");
		    cm.dispose();			
		}
		else
		{
			//最后一层对话完继续循环至此，推出结束
			cm.dispose();
		}
	}
			
}

function CheckStatus(mode)
{
	if (mode == -1)
	{
		cm.dispose();
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
		cm.dispose();
		return false;
	}	
	return true;
}