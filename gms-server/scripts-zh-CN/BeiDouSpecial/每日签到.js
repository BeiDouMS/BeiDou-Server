/**北斗脚本

签到脚本

---By hanmburger*/
var status = -1;
var text;
var BeiDouUI ="#fMap/MapHelper.img/BeiDou/logo#";

// 每个礼包所需的在线时长
var condition = new Array(30, 60, 120, 180, 240, 300, 360);

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
			text = cm.getCharacterExtendValue("每日签到",true);
			if (text == "TRUE")
			{
			    cm.sendOk("您已经签到过了，请明天再来");
			    cm.dispose();				
			}
			else
			{
			    cm.saveOrUpdateCharacterExtendValue("每日签到", "TRUE",true);
			    cm.sendOk("签到成功");
				cm.gainItem(2430033,1);
			    cm.dispose();				
			}
	    }
		else
		{
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

//获取当前时间
function getNowTime() {
    var date = new Date();
    //年 getFullYear()：四位数字返回年份
    var year = date.getFullYear();  //getFullYear()代替getYear()
    //月 getMonth()：0 ~ 11
    var month = date.getMonth() + 1;
    //日 getDate()：(1 ~ 31)
    var day = date.getDate();
    //时 getHours()：(0 ~ 23)
    var hour = date.getHours();
    //分 getMinutes()： (0 ~ 59)
    var minute = date.getMinutes();
    //秒 getSeconds()：(0 ~ 59)
    var second = date.getSeconds();

    var time = '当前时间是：' + year + '-' + addZero(month) + '-' + addZero(day) + ' ' + addZero(hour) + ':' + addZero(minute) + ':' + addZero(second);
    return time;
}


//小于10的拼接上0字符串
function addZero(s) {
    return s < 10 ? ('0' + s) : s;
}
