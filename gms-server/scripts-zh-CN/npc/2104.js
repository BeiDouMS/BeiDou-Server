

var status = -1;

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
		    cm.sendNext("好的，那么我们出发吧。冒险旅途开始了！");			
	    }
		else
		{
			cm.warp(1);
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


