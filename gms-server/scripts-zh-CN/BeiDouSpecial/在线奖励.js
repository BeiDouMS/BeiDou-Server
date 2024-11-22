/*
脚本：在线奖励
作者：SpicyBurgerKing
日期：2024-10-31
备注：北斗开发组
 */


var status = 0;
var Eventid = "站街奖励";
var OnlineLevel = [10, 30, 60, 120, 240, 360, 480];//, 600, 720
var textMsg = ["恭喜领取成功！", "您未达到领取条件", "您已经领取过了。"];
var getStatus = 0;//"0000000"
var msg;
var giftContent = [5, 10, 15, 20, 25, 30, 35];
var index = [0x01,0x10,0x100,0x1000,0x10000,0x100000,0x1000000];
//Start
function start() 
{
	var limitDt = new Date();
	limitDt.setHours(0, 0, 5, 0);
	if (new Date() <= limitDt) {
		cm.sendOk("在线奖励正在初始化中，请稍后再试...");
		cm.dispose();
		return;
	}
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
	        if (cm.getOnlineTime() < 3600)
			{
	             time = "今日在线时间：#e#r"+ Math.floor(cm.getOnlineTime() / 60) +"#k#n 分钟\r\n\r\n";
		    } 
			else 
			{
				let hour = Math.floor(cm.getOnlineTime() / 3600);
				let min = Math.floor((cm.getOnlineTime() - hour * 3600) / 60);
		         time = "今日在线时间：#e#r"+ hour +"#k#n 小时 #e#r"+ min +"#k#n 分钟\r\n\r\n";
		    }
			var getTmpStatus = cm.getAccountExtendValue("每日在线奖励领取状态",true);
			
			if (getTmpStatus == null)
			{
				cm.saveOrUpdateAccountExtendValue("每日在线奖励领取状态", "0", true);
				getStatus = 0;
			}
			else
			{
				getStatus = parseInt(getTmpStatus, 10);
			}
			//cm.saveOrUpdateAccountExtendValue("每日在线奖励领取状态", "0", true);
			time += "#b#L0#领取【10】分钟在线奖励#l\r\n#k";
			time += "#b#L1#领取【30】分钟在线奖励#l\r\n#k";
			time += "#b#L2#领取【60】分钟在线奖励#l\r\n#k";
			time += "#b#L3#领取【120】分钟在线奖励#l\r\n#k";
			time += "#b#L4#领取【240】分钟在线奖励#l\r\n#k";
			time += "#b#L5#领取【360】分钟在线奖励#l\r\n#k";
			time += "#b#L6#领取【480】分钟在线奖励#l\r\n#k";
			cm.sendSimple(time);
	    }
		else if (status == 1 )
		{
			//第二层对话
			var currentOnlineTime = Math.floor(cm.getOnlineTime() / 60);
			AwardItem(selection, getStatus, currentOnlineTime, OnlineLevel[selection], giftContent[selection]);
			cm.sendOk(msg);
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

/**  
 * 获取奖励 
 *   
 * @param {number} selection - 玩家选择的奖励索引  
 * @param {number} acquire - 当前奖励领取状态 
 * @param {number} currentOnlineTime - 当前在线时间（分钟）  
 * @param {number} scalar - 在线奖励的时间梯度（分钟）  
 * @param {number} gaincount - 获取物品的数量  
 */  
function AwardItem(selection, acquire, currentOnlineTime, scalar, gaincount) {  
    if (currentOnlineTime >= scalar) 
	{  
        // 检查是否已经领取过该奖励  
        var rewardBit = acquire & index[selection]; 
        var isRewardClaimed = (rewardBit !== 0); 
        if (!isRewardClaimed) 
		{  
            // 颁发奖励  
            cm.gainItem(2430033, gaincount);
            var newAcquireStatus = acquire | index[selection];
            cm.saveOrUpdateAccountExtendValue("每日在线奖励领取状态", String(newAcquireStatus), true);  
            msg = textMsg[0]; 
        } 
		else 
		{  
            // 设置已领取消息  
            msg = textMsg[2];  
        }  
    } 
	else 
	{  
        msg = textMsg[1]; 
    }  
}