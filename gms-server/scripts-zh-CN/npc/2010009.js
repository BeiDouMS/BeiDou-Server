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
var status;
var choice;
var guildName;

var allianceCost = 2000000;
var increaseCost = 1000000;
var allianceLimit = 5;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
    } else {
        cm.dispose();
        return;
    }
    if (status == 0) {
        if (cm.getPlayer().getGuildId() < 1 || cm.getPlayer().getGuildRank() != 1) {
            cm.sendNext("你好！我是#b#p2010009##k。只有家族会长才能尝试组建家族联盟。");
            cm.dispose();
            return;
        }

        cm.sendSimple("你好！我是#bLenario#k。\r\n#b#L0#你能告诉我家族联盟是什么吗？#l\r\n#L1#我如何创建家族联盟？#l\r\n#L2#我想创建一个家族联盟。#l\r\n#L3#我想为家族联盟添加更多公会。#l\r\n#L4#我想解散家族联盟。#l");
    } else if (status == 1) {
        choice = selection;
        if (selection == 0) {
            cm.sendNext("家族联盟就像它的名字一样，是由多个公会组成的超级团体。我负责管理这些家族联盟。");
            cm.dispose();
        } else if (selection == 1) {
            cm.sendNext("要创建家族联盟，需要两位且仅限两位公会会长组成一个队伍，并且必须在同一个频道的房间内。该队伍的队长将被指定为家族联盟的主人。\r\n最初，只有两个公会可以加入新的联盟，但随着时间的推移，你可以通过在特定时机与我交谈并投资一定费用来扩大联盟的容量。");
            cm.dispose();
        } else if (selection == 2) {
            if (!cm.isLeader()) {
                cm.sendNext("如果你想组建家族联盟，请告诉你的队伍领袖与我交谈。他/她将被指定为家族联盟的领袖。");
                cm.dispose();
                return;
            }
            if (cm.getPlayer().getGuild().getAllianceId() > 0) {
                cm.sendOk("当你的公会已经注册在其他家族联盟中时，你无法创建家族联盟。");
                cm.dispose();
                return;
            }

            cm.sendYesNo("哦，你对组建家族联盟感兴趣吗？目前这项操作的费用是 #b" + allianceCost + " 冒险币#k。");
        } else if (selection == 3) {
            if (cm.getPlayer().getMGC() == null) {
                cm.sendOk("如果你没有公会，就无法扩展家族联盟。");
                cm.dispose();
                return;
            }

            var rank = cm.getPlayer().getMGC().getAllianceRank();
            if (rank == 1) {
                cm.sendYesNo("你想要增加你的家族联盟 #rone guild#k 的位置吗？这个手续的费用是 #b" + increaseCost + " 冒险币#k。");
            } else {
                cm.sendNext("只有家族联盟会长才能扩大家族联盟中的公会数量。");
                cm.dispose();
            }
        } else if (selection == 4) {
            if (cm.getPlayer().getMGC() == null) {
                cm.sendOk("如果你没有家族联盟，你就无法解散家族联盟。");
                cm.dispose();
                return;
            }

            var rank = cm.getPlayer().getMGC().getAllianceRank();
            if (rank == 1) {
                cm.sendYesNo("你确定要解散你的家族联盟吗？");
            } else {
                cm.sendNext("只有家族联盟的会长才能解散家族联盟。");
                cm.dispose();
            }
        }
    } else if (status == 2) {
        if (choice == 2) {
            if (cm.getMeso() < allianceCost) {
                cm.sendOk("你没有足够的金币来完成这个请求。");
                cm.dispose();
                return;
            }
            cm.sendGetText("Now please enter the name of your new Guild Union. (max. 12 letters)");
        } else if (choice == 3) {
            if (cm.getAllianceCapacity() == allianceLimit) {
                cm.sendOk("你的联盟已经达到了公会的最大容量。");
                cm.dispose();
                return;
            }
            if (cm.getMeso() < increaseCost) {
                cm.sendOk("你没有足够的金币来完成这个请求。");
                cm.dispose();
                return;
            }

            cm.upgradeAlliance();
            cm.gainMeso(-increaseCost);
            cm.sendOk("你的联盟现在可以接受一个额外的公会。");
            cm.dispose();
        } else if (choice == 4) {
            if (cm.getPlayer().getGuild() == null || cm.getPlayer().getGuild().getAllianceId() <= 0) {
                cm.sendNext("你无法解散一个不存在的家族联盟。");
                cm.dispose();
            } else {
                cm.disbandAlliance(cm.getClient(), cm.getPlayer().getGuild().getAllianceId());
                cm.sendOk("你的家族联盟已经解散。");
                cm.dispose();
            }
        }
    } else if (status == 3) {
        guildName = cm.getText();
        cm.sendYesNo("'"+ guildName + "'会成为你的家族联盟的名字吗？");
    } else if (status == 4) {
        if (!cm.canBeUsedAllianceName(guildName)) {
            cm.sendNext("此名称不可用，请选择另一个。"); //Not real text
            status = 1;
            choice = 2;
        } else {
            if (cm.createAlliance(guildName) == null) {
                cm.sendOk("请检查一下你和另一个公会领袖是否都在这个房间里，确保两个公会目前都没有在联盟中注册。在这个过程中，除了你们两个，不应该有其他公会领袖在场。");
            } else {
                cm.gainMeso(-allianceCost);
                cm.sendOk("你已成功组建了家族联盟。");
            }
            cm.dispose();
        }
    }
}