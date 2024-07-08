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
/* Author: Xterminator
	NPC Name: 		Mr. Goldstein
	Map(s): 		Victoria Road : Lith Harbour (104000000)
	Description:		Extends Buddy List
*/
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            cm.sendNext("我明白了...你的朋友似乎没有我想象的那么多。哈哈哈，开玩笑的！无论如何，如果你改变主意了，随时可以回来找我谈生意。如果你交了很多朋友，你就知道了... 嘿嘿...");
            cm.dispose();
            return;
        } else if (status >= 1 && mode == 0) {
            cm.sendNext("我明白了...我觉得你的朋友可能没有我想象的那么多。如果不是这样，你现在手头上就没有24万金币？不管怎样，如果你改变主意了，回来找我，我们可以谈生意。当然，前提是你得解决一些财务问题... 嘿嘿...");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0) {
            cm.sendYesNo("我希望我能赚到像昨天一样多的钱...嗨，你好！你不想扩展你的好友列表吗？你看起来像是有很多朋友的人...那么，你觉得呢？只要有一些钱，我就可以帮你实现。不过要记住，这只适用于一个角色，不会影响你账号上的其他角色。你想扩展你的好友列表吗？");
        } else if (status == 1) {
            cm.sendYesNo("好的，不错的选择！其实并不是很贵。#b24万金币，我就可以给你的好友列表增加5个名额#k。不，我不会单独出售它们。一旦你购买了，它就会永久地出现在你的好友列表上。所以如果你是那些需要更多空间的人，那么你最好去做。你觉得呢？你会花24万金币吗？");
        } else if (status == 2) {
            var capacity = cm.getPlayer().getBuddylist().getCapacity();
            if (capacity >= 50 || cm.getMeso() < 240000) {
                cm.sendNext("嘿... 你确定你有 #b240,000金币#k 吗？如果是的话，那么检查一下你是否已经将好友列表扩展到了最大。即使你付了钱，你的好友列表上限也只能有 #b50#k 个。");
                cm.dispose();
            } else {
                var newcapacity = capacity + 5;
                cm.gainMeso(-240000);
                cm.getPlayer().setBuddyCapacity(newcapacity)
                cm.sendOk("好的！你的好友列表现在应该有5个额外的槽位。自己去检查一下。如果你还需要更多的好友列表空间，你知道该找谁。当然，这并不是免费的……好了，再见……");
                cm.dispose();
            }
        }
    }
}