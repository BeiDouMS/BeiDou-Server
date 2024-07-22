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
var status = 0;
var selected = -1;
var party = 0;

function start() {
    cm.sendOk("金字塔任务（PyramidPQ）目前不可用。");
    cm.dispose();
}

/*function start() {
	status = -1;
	var text = "You should NOT talk to this NPC in this map.";
	if (cm.getMapId() == 926020001)
		text = "Stop! You've succesfully passed Nett's test. By Nett's grace, you will now be given the opportunity to enter Pharaoh Yeti's Tomb. Do you wish to enter it now?\r\n\r\n#b#L0# Yes, I will go now.#l\r\n#L1# No, I will go later.#l";
	else if (cm.getMapId() == 926010000)
		text = "I am Duarte.\r\n\r\n#b#L0# Ask about the Pyramid.#l\r\n#e#L1# Enter the Pyramid.#l#n\r\n\r\n#L2# Find a Party.#l\r\n\r\n#L3# Enter Pharaoh Yeti's Tomb.#l\r\n#L4# Ask about Pharaoh Yeti's treasures.#l\r\n#L5# Receive the <Protector of Pharaoh> Medal.#l";
	else 
		text = "Do you want to forfeit the challenge and leave?\r\n\r\n#b#L0# Leave#l";
		
	cm.sendSimple(text);
}
*/

function action(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode < 0 || (type == 4 && mode == 0)) {
        cm.dispose();
        return;
    } else {
        status++;
    }

    if (cm.getMapId() == 926010000) {
        if (status == 0) {
            if (selection > -1) {
                selected = selection;
            }
            if (selection == 0 || selected == 0) {
                cm.sendNext("这是Nett金字塔，混沌和复仇之神。长期以来，它一直被埋在沙漠深处，但Nett已命令它升起地面。如果你不惧混乱和可能的死亡，你可以挑战沉睡在金字塔内的Yeti法老。无论结果如何，选择权在你手中。");
            } else if (selection == 1) {
                cm.sendSimple("你这些不知道尼特之怒的愚蠢家伙，现在是选择你们命运的时刻！\r\n\r\n#b#L0# 独自进入。#l\r\n#L1# 与2人或更多的队伍一起进入。#l");
            } else if (selection == 2) {
                cm.openUI(0x16);
                cm.showInfoText("Use the Party Search (Hotkey O) window to search for a party to join anytime and anywhere!");
                cm.dispose();
            } else if (selection == 3) {
                cm.sendSimple("你带来了什么宝石？\r\n\r\n#L0##i4001322# #t4001322##l\r\n#L1##i4001323# #t4001323##l\r\n#L2##i4001324# #t4001324##l\r\n#L3##i4001325# #t4001325##l");
            } else if (selection == 4) {
                cm.sendNext("在法老雪人的墓穴内，你可以通过证明自己有能力击败法老雪人的克隆体——法老雪人小弟，获得#e#b#t2022613##k#n。在那个盒子里藏着一份非常特别的宝藏。那就是#e#b#t1132012##k#n。\r\n#i1132012:# #t1132012#\r\n\r\n而且，如果你以某种方式能够在地狱模式中生存下来，你将获得#e#b#t1132013##k#n。\r\n\r\n#i1132013:# #t1132013#\r\n\r\n当然，Nett是不会允许这种事情发生的。");
            } else if (selection == 5) {
                var progress = cm.getQuestProgressInt(29932);
                if (progress >= 50000) {
                    cm.dispose();
                } else {
                    cm.sendNext("抱歉，我无法完成你的要求。");
                }

            }
        } else if (status == 1) {
            if (selected == 0) {
                cm.sendNextPrev("一旦你进入金字塔，你将面对内特的愤怒。由于你看起来不太聪明，我会给你一些建议和规则。记住它们。#b\r\n1. 小心你的#e#r行动槽#b#n不要减少。保持你的槽水平的唯一方法是不停地与怪物战斗。\r\n2. 无法做到的人将付出昂贵的代价。小心不要造成任何#r失误#b。\r\n3. 小心带有#v04032424#标记的法老少年雪人。如果错误地攻击他，你会后悔的。\r\n4. 明智地使用赋予你的技能来完成击杀成就。");
            } else if (selected == 1) {
                party = selection;
                cm.sendSimple("你这个不怕死亡残酷的人，做出你的决定！\r\n#L0##i3994115##l#L1##i3994116##l#L2##i3994117##l#L3##i3994118##l");
            } else if (selected == 3) {
                if (selection == 0) {
                    if (cm.haveItem(4001322)) {
                        return;
                    }
                } else if (selection == 1) {
                    if (cm.haveItem(4001323)) {
                        return;
                    }
                } else if (selection == 2) {
                    if (cm.haveItem(4001324)) {
                        return;
                    }
                } else if (selection == 3) {
                    if (cm.haveItem(4001325)) {
                        return;
                    }
                }
                cm.sendOk("你需要一颗宝石才能进入法老雪人的墓室。你确定你有吗？");
                cm.dispose();
            } else if (selected == 5) {
            } else {
                cm.dispose();
            }
        } else if (status == 2) {
            if (selected == 0) {
                cm.sendNextPrev("那些能够经受住尼特的愤怒的人将受到尊敬，但那些失败的人将面临毁灭。这就是我能给你的所有建议。剩下的就看你们了。");
            } else if (selected == 1) {
                var mode = "EASY";
                //Finish this
                var pqparty = cm.getPlayer().getParty();
                if (party == 1) {
                    if (pqparty == null) {
                        cm.sendOk("请先创建队伍");
                        cm.dispose();
                        return;
                    } else {
                        if (pqparty.getMembers().size() < 2) {
                            cm.sendOk("获得更多成员...");
                            cm.dispose();
                            return;
                        } else {
                            var i = 0;
                            for (var a = 0; a < pq.getMembers().size(); a++) {
                                var pqchar = pq.getMembers().get(a);
                                if (i > 1) {
                                    break;
                                }
                                if (pqchar != null && pqchar.getMapId() == 926010000) {
                                    i++;
                                }
                            }
                            if (i < 2) {
                                cm.sendOk("确保你的地图上有2名或更多队员。");
                                cm.dispose();
                                return;
                            }
                        }
                    }
                }

                if (cm.getPlayer().getLevel() < 40) {
                    cm.sendOk("你必须达到40级以上才能进入这个组队任务。");
                    cm.dispose();
                    return;
                }
                if (selection < 3 && cm.getPlayer().getLevel() > 60) {
                    cm.sendOk("只有等级超过60级的玩家才能进入地狱模式。");
                    cm.dispose();
                    return;
                }
                if (selection == 1) {
                    mode = "NORMAL";
                } else if (selection == 2) {
                    mode = "HARD";
                } else if (selection == 3) {
                    mode = "HELL";
                }

                if (!cm.createPyramid(mode, party == 1)) {
                    cm.sendOk("所有房间都已满，请稍后再试或者换一个频道:)");
                }
                cm.dispose();
            }
        } else if (status == 3) {
            cm.dispose();
        }
    } else if (cm.getMapId() == 926020001) {
        if (status == 0) {
            if (selection == 0) {
                cm.dispose();
            }//:(
            else if (selection == 1) {
                cm.sendNext("我会给你法老雪人的宝石。有了这个宝石，你随时可以进入法老雪人的墓穴。检查一下你的杂项窗口里是否至少有一个空位。");
            }

        } else if (status == 1) {
            var itemid = 4001325;
            if (cm.getPlayer().getLevel() >= 60) {
                itemid = 4001325;
            }
            if (cm.canHold(itemid)) {
                cm.gainItem(itemid);
                cm.warp(926010000);
            } else {
                cm.showInfoText("You must have at least 1 empty slot in your Etc window to receive the reward.");
            }

            cm.dispose();
        }
    } else {
        cm.warp(926010000);
        cm.getPlayer().setPartyQuest(null);
        cm.dispose();
    }
}/*Do you want to forfeit the challenge and leave?

Your allotted time has passed. Do you want to leave now?



*/