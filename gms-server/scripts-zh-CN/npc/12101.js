/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Author: Xterminator,repair by hamburger
	NPC Name: 		Rain
	Map(s): 		Maple Road : Amherst (1010000)
	Description: 		Talks about Amherst
*/
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
			cm.sendNext("这是位于冒险岛东北部的名为#b彩虹岛#k的小村子。你知道这里可是新手的天堂哦，因为这个地方周围只有一些弱小的怪物~。");
	    }
		else if (status == 1 )
		{
			//第二层对话
            cm.sendNextPrev("如果你想变得更强大，那就去#b南港#k吧，那里有一个港口。你可以乘坐一搜巨大的游轮前往一个叫做#b金银岛#k的地方。与这个小岛相比，它的面积可是一望无际。");
		}
		else if (status == 2)
		{
			cm.sendPrev("在金银岛上，你可以游历并选择你的职业。我听说有一个荒凉的村落，有许多战士住在那里。还有悬崖、高地……那会是什么样的地方？");
			//cm.dispose();
		}
		else
			cm.dispose();
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