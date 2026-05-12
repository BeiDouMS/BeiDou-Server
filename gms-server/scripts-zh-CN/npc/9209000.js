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
 * @npc: Abdula
 * @map: Multiple towns on Maplestory
 * @func: Job Skill / Mastery Book Drop Announcer
 */

var status;
var selected = 0;
var skillbook = [], masterybook = [], table = [];

function start() {
    status = -1;
    selected = 0;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            var greeting = "你好，我是#p9209000#，是技能书和能手册的解说员！";

            if (cm.getPlayer().isCygnus()) {
                cm.sendOk(greeting + "骑士团没有可用的技能书或能手册。");
                cm.dispose();
                return;
            }

            var jobrank = cm.getJob().getId() % 10;
            if (jobrank < 2) {
                cm.sendOk(greeting + "继续锻炼自己，直到你达到当前职业#r四转#k。当你达到这一成就时，新的提升机会将会到来！");
                cm.dispose();
                return;
            }

            skillbook = cm.getAvailableSkillBooks();
            masterybook = cm.getAvailableMasteryBooks();

            if (skillbook.length == 0 && masterybook.length == 0) {
                cm.sendOk(greeting + "目前没有可用的技能书来进一步提升你的技能。要么你已经#b将所有技能都学满了#k，要么#b你还没有达到使用某些技能书籍的最低要求#k。");
                cm.dispose();
            } else if (skillbook.length > 0 && masterybook.length > 0) {
                var sendStr = greeting + "我们为你找到了提升技能的新机会，快来提升你的技能吧！选择一个类型来查看。\r\n\r\n#b";

                sendStr += "#L1# 技能书#l\r\n";
                sendStr += "#L2# 能手册#l\r\n";

                cm.sendSimple(sendStr);
            } else if (skillbook.length > 0) {
                selected = 1;
                cm.sendNext(greeting + "New opportunities for skill improvement have been located for you to improve your skills! Only skill learns available for now.");
            } else {
                selected = 2;
                cm.sendNext(greeting + "New opportunities for skill improvement have been located for you to improve your skills! Only skill upgrades available.");
            }

        } else if (status == 1) {
            var sendStr = "The following books are currently available:\r\n\r\n";
            if (selected == 0) {
                selected = selection;
            }

            if (selected == 1) {
                table = skillbook;
                for (var i = 0; i < table.length; i++) {
                    if (table[i] > 0) {
                        var itemid = table[i];
                        sendStr += "  #L" + i + "# #i" + itemid + "#  #t" + itemid + "##l\r\n";
                    } else {
                        var skillid = -table[i];
                        sendStr += "  #L" + i + "# #s" + skillid + "#  #q" + skillid + "##l\r\n";
                    }
                }
            } else {
                table = masterybook;
                for (var i = 0; i < table.length; i++) {
                    var itemid = table[i];
                    sendStr += "  #L" + i + "# #i" + itemid + "#  #t" + itemid + "##l\r\n";
                }
            }

            cm.sendSimple(sendStr);

        } else if (status == 2) {
            selected = selection;

            var sendStr;
            if (table[selected] > 0) {
                var mobList = cm.getNamesWhoDropsItem(table[selected]);

                if (mobList.length == 0) {
                    sendStr = "No mobs drop '#b#t" + table[selected] + "##k'.\r\n\r\n";
                } else {
                    sendStr = "The following mobs drop '#b#t" + table[selected] + "##k':\r\n\r\n";

                    for (var i = 0; i < mobList.length; i++) {
                        sendStr += "  #L" + i + "# " + mobList[i] + "#l\r\n";
                    }

                    sendStr += "\r\n\r\n";
                }
            } else {
                sendStr = "\r\n\r\n";
            }

            sendStr += cm.getSkillBookInfo(table[selected]);

            cm.sendNext(sendStr);
            cm.dispose();
        }
    }
}