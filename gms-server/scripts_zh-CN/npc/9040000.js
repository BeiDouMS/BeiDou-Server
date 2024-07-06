/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2019 RonanLana

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

/**
 * @author: Ronan
 * @npc: Shuang
 * @map: Victoria Road: Excavation Site<Camp> (101030104)
 * @func: Start Guild PQ
 */

var status = 0;
var sel;
var em = null;

function findLobby(guild) {
    for (var iterator = em.getInstances().iterator(); iterator.hasNext();) {
        var lobby = iterator.next();

        if (lobby.getIntProperty("guild") == guild) {
            if (lobby.getIntProperty("canJoin") == 1) {
                return lobby;
            } else {
                return null;
            }
        }
    }

    return null;
}

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
            em = cm.getEventManager("GuildQuest");
            if (em == null) {
                cm.sendOk("公会任务遇到了一个错误。");
                cm.dispose();
                return;
            }

            cm.sendSimple("#e#b<公会任务：沙连尼安遗迹>\r\n#k#n" + em.getProperty("party") + "\r\n\r\n通往沙连尼安的道路就在这里。你想做什么？#b\r\n#L0#注册公会进行公会任务#l\r\n#L1#加入你的公会的公会任务#l\r\n#L2#我想了解更多细节。#l");
        } else if (status == 1) {
            sel = selection;
            if (selection == 0) {
                if (!cm.isGuildLeader()) {
                    cm.sendOk("你的公会会长/副会长必须与我交谈，以注册公会参加公会任务。");
                    cm.dispose();
                } else {
                    if (em.isQueueFull()) {
                        cm.sendOk("这个频道的队列已经满了。请耐心等待一会儿，然后再试一次，或者尝试在另一个频道。");
                        cm.dispose();
                    } else {
                        var qsize = em.getQueueSize();
                        cm.sendYesNo(((qsize > 0) ? "There is currently #r" + qsize + "#k guilds queued on. " : "") + "Do you wish for your guild to join this queue?");
                    }
                }
            } else if (selection == 1) {
                if (cm.getPlayer().getGuildId() > 0) {
                    var eim = findLobby(cm.getPlayer().getGuildId());
                    if (eim == null) {
                        cm.sendOk("你的公会目前不在此频道上进行战略时间。请再次检查你的公会是否正在计划公会任务，如果是的话，请确认他们分配的频道。");
                    } else {
                        if (cm.isLeader()) {
                            em.getEligibleParty(cm.getParty());
                            eim.registerParty(cm.getPlayer());
                        } else {
                            eim.registerPlayer(cm.getPlayer());
                        }
                    }
                } else {
                    cm.sendOk("如果你没有加入公会，就无法参与公会任务！");
                }

                cm.dispose();
            } else {
                var reqStr = "";
                reqStr += "\r\n\r\n    Team requirements:\r\n\r\n";
                reqStr += "     - 1 team member #rbelow or equal level 30#k.\r\n";
                reqStr += "     - 1 team member who is a #rThief with Dark Sight#k skill and #rmaxed Haste#k.\r\n";
                reqStr += "     - 1 team member who is a Magician with #rmaxed Teleport#k.\r\n";
                reqStr += "     - 1 team member who is a #rlong ranged attacker#k like Bowman, Assassin, or Gunslinger.\r\n";
                reqStr += "     - 1 team member with #rgood jumping skills#k like Assassin with maxed Flash Jump or Gunslinger with Wings.\r\n";

                cm.sendOk("#e#b<公会任务：沙连尼安遗迹>#k#n\r\n与你的公会成员一起合作，试图从骷髅的掌控中夺回鲁比安，通过团队合作克服沙连尼安墓穴内等待的许多谜题和挑战。完成任务实例后可以获得丰厚的奖励，并为你的公会积累公会点数。");
                cm.dispose();
            }
        } else if (status == 2) {
            if (sel == 0) {
                var entry = em.addGuildToQueue(cm.getPlayer().getGuildId(), cm.getPlayer().getId());
                if (entry > 0) {
                    cm.sendOk("您的公会已成功注册。一条消息将在您的聊天窗口中弹出，让您的公会了解注册状态。\r\n现在，#r重要#k：作为这个实例的领导者，#r在公会被召集进行策略时，您必须已经在该频道上#k。#b未能完成此操作将使#k您的公会注册作废，并立即召集下一个公会。还必须注意的是，如果您，作为这个实例的领导者，从策略时间结束到实例持续时间的任何时刻都不在场，将使任务中断，并立即将您的公会移出队列。");
                } else if (entry == 0) {
                    cm.sendOk("这个频道的队列已经满了。请耐心等待一会儿，然后再试一次，或者尝试在另一个频道。");
                } else {
                    cm.sendOk("您的公会已经在一个频道排队了。请等待您的公会轮到。");
                }
            }

            cm.dispose();
        }
    }
}