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
/*
*	Author : Raz
*	Author : Ronan
*
*	NPC = 9103001 - Rolly
*	Map =  Ludibrium - <Ludibrium>
*	NPC MapId = 220000000
*	Function = Start LMPQ
*
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
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            em = cm.getEventManager("LudiMazePQ");
            if (em == null) {
                cm.sendOk("鲁迪布里安迷宫组队任务遇到了一个错误。");
                cm.dispose();
                return;
            } else if (cm.isUsingOldPqNpcStyle()) {
                action(1, 0, 0);
                return;
            }

            cm.sendSimple("#e#b<组队任务：鲁塔比迷宫>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n这是通往鲁塔比迷宫的入口。尽情享受吧！\r\n#b#L0#进入鲁塔比迷宫#l\r\n#L1#我想要" + (cm.getPlayer().isRecvPartySearchInviteEnabled() ? "禁用" : "启用") + "组队搜索。\r\n#L2#什么是鲁塔比迷宫？");
        } else if (status == 1) {
            if (selection == 0) {
                if (cm.getParty() == null) {
                    cm.sendOk("尝试与你的队伍一起挑战迷宫任务。");
                    cm.dispose();
                } else if (!cm.isLeader()) {
                    cm.sendOk("如果你决定去挑战它，请让你的队长通知我！");
                    cm.dispose();
                } else {
                    var eli = em.getEligibleParty(cm.getParty());
                    if (eli.size() > 0) {
                        if (!em.startInstance(cm.getParty(), cm.getPlayer().getMap(), 1)) {
                            cm.sendOk("另一个队伍已经进入了该频道的#r组队任务#k。请尝试其他频道，或者等待当前队伍完成。");
                        }
                    } else {
                        cm.sendOk("你的队伍至少需要有3名成员才能应对这个迷宫。");
                    }

                    cm.dispose();
                }
            } else if (selection == 1) {
                var psState = cm.getPlayer().toggleRecvPartySearchInvite();
                cm.sendOk("你的组队搜索状态现在是：#b" + (psState ? "enabled" : "disabled") + "#k。想要改变状态时随时找我。");
                cm.dispose();
            } else {
                cm.sendOk("#e#b<组队任务：鲁塔比迷宫>#k#n\r\n这个迷宫适用于所有3人或更多成员的队伍，所有参与者必须在51级至70级之间。你将有15分钟的时间来逃离迷宫。在房间的中心，将设置一个传送门，将你传送到另一个房间。这些传送门将把你传送到其他房间，你会（希望）找到出口。皮埃特里将在出口处等待，所以你只需要和他交谈，他就会放你出去。打破房间里的所有箱子，箱子里的怪物会掉落一个优惠券。逃离迷宫后，你将根据收集的优惠券获得经验奖励。此外，如果队长拥有至少30张优惠券，那么将为队伍提供特别的礼物。如果你无法在规定的15分钟内逃离迷宫，你将在迷宫中获得0经验。如果你决定在迷宫中下线，你将被自动踢出迷宫。即使队伍成员在任务中途离开，剩下的成员仍然可以继续进行任务，除非他们在迷宫中没有达到最低队伍成员数量。如果你处于危急状态，无法猎杀怪物，你可以避开它们来保护自己。你的战斗精神和智慧将受到考验！祝你好运！");
                cm.dispose();
            }
        }
    }
}