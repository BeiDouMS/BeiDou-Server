/**北斗刷道具



---By hanmburger*/
var status;

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
			cm.sendGetNumber("请输入数字，我可以刷任何道具",0,0,99999999);
	    }
		else if (status == 1 )
		{
			//第二层对话
		    if (1)
		    {
		    	cm.gainItem(selection,1);
		    	var text = "恭喜你，Get到了！" + "#i" + selection + "#";
		        cm.sendOk(text);
		        cm.dispose();	
		    
		    }
		    else
		    {
		    	cm.sendOk("道具不存在！");
		    	cm.dispose();	
		    }
		}
		else
		{
			//最后一层对话完继续循环至此，退出结束
			cm.dispose();
		}
	}
			
}

function CheckStatus(mode)
{
	if (mode == -1)
	{
		cm.dispose();//点击了取消，停止，结束
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
		cm.dispose();//防止第一层对话带有上一项或者取消按钮而产生bug。
		return false;
	}	
	return true;
}