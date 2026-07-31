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
var status = -1;
var selected = -1;
var party = 0;
var questComplete = false;

function start() {
    status = -1;
    selected = -1;
    party = 0;
    questComplete = false;

    var text = "You should NOT talk to this NPC in this map.";
    if (cm.getMapId() == 926020001 || cm.getMapId() == 926010001) {
        text = "Stop! You've successfully passed Nett's test. By Nett's grace, you will now be given the opportunity to enter Pharaoh Yeti's Tomb. Do you wish to enter it now?\r\n\r\n#b#L0# Yes, I will go now.#l\r\n#L1# No, I will go later.#l";
    } else if (cm.getMapId() == 926010000) {
        if (cm.isQuestStarted(3955)) {
            questComplete = true;
            cm.sendNext("Fools. A sea of fools. Are all humans this foolish? Even one who called himself a scholar dared to venture here. But is not death something to avoid?\r\n\r\n#b#L0# I am here on behalf of that scholar, Byron.#l#k");
            return;
        }
        text = "I am Duarte.\r\n\r\n#b#L0# Ask about the Pyramid.#l\r\n#e#L1# Enter the Pyramid.#l#n\r\n\r\n#L2# Find a Party.#l\r\n\r\n#L3# Enter Pharaoh Yeti's Tomb.#l\r\n#L4# Ask about Pharaoh Yeti's treasures.#l\r\n#L5# Receive the <Protector of Pharaoh> Medal.#l";
    } else {
        text = "Do you want to forfeit the challenge and leave?\r\n\r\n#b#L0# Leave#l";
    }

    cm.sendSimple(text);
}

function action(mode, type, selection) {
    if (mode == 0 && type == 0) {
        status--;
    } else if (mode < 0 || (type == 4 && mode == 0)) {
        cm.dispose();
        return;
    } else {
        status++;
    }

    if (questComplete) {
        if (status == 0) {
            cm.sendNext("I see. That fool. I was merciful enough to save him from the throngs of death, and instead he drags himself to another one of its doorsteps. So be it. Let us see if he can escape the breath of Anubis.");
        } else if (status == 1) {
            cm.forceCompleteQuest(3955);
            cm.sendNext("I will permit your entrance, but it remains to be seen if the Pyramid will accept you as well. If it does, then you will acquire the Pharaoh Yeti's Gem. If you bring that to me, I will lead you directly to a spot where you can acquire many other rare gems.");
        } else {
            cm.dispose();
        }
        return;
    }

    if (cm.getMapId() == 926010000) {
        if (status == 0) {
            if (selection > -1) {
                selected = selection;
            }
            if (selection == 0 || selected == 0) {
                cm.sendNext("This is the pyramid of Nett, the god of chaos and revenge. For a long time, it was buried deep in the desert, but Nett has ordered it to rise above ground. If you are unafraid of chaos and possible death, you may challenge Pharaoh Yeti, who lies asleep inside the Pyramid. Whatever the outcome, the choice is yours to make.");
            } else if (selection == 1) {
                cm.sendSimple("You fools who know no fear of Nett's wrath, it is now time to choose your destiny! \r\n\r\n#b#L0# Enter alone.#l\r\n#L1# Enter with a party of 2 or more.#l");
            } else if (selection == 2) {
                cm.openUI(0x16);
                cm.showInfoText("Use the Party Search (Hotkey O) window to search for a party to join anytime and anywhere!");
                cm.dispose();
            } else if (selection == 3) {
                cm.sendSimple("What gem have you brought?\r\n\r\n#L0##i4001322# #t4001322##l\r\n#L1##i4001323# #t4001323##l\r\n#L2##i4001324# #t4001324##l\r\n#L3##i4001325# #t4001325##l");
            } else if (selection == 4) {
                cm.sendNext("Inside Pharaoh Yeti's Tomb, you can acquire a #e#b#t2022613##k#n by proving yourself capable of defeating the #bPharaoh Jr. Yeti#k, the Pharaoh's clone. Inside that box lies a very special treasure. It is the #e#b#t1132012##k#n.\r\n#i1132012:# #t1132012#\r\n\r\nIf you survive Hell Mode, the stronger Pharaoh Jr. Yeti can drop #e#b#t2022618##k#n, which also contains the #e#b#t1132013##k#n.\r\n\r\n#i1132013:# #t1132013#\r\n\r\nThough, of course, Nett won't allow that to happen.");
            } else if (selection == 5) {
                var progress = cm.getQuestProgressInt(29932, 7760);
                if (cm.isQuestCompleted(29932) || cm.haveItem(1142142)) {
                    cm.sendOk("You have already received the <Protector of Pharaoh> Medal.");
                } else if (progress >= 50000) {
                    if (!cm.canHold(1142142)) {
                        cm.sendOk("Please make sure your Equip inventory has enough space before receiving the medal.");
                    } else {
                        cm.gainItem(1142142, 1);
                        cm.forceCompleteQuest(29932);
                        cm.sendOk("You have proven yourself as the Protector of Pharaoh. Please accept the medal.");
                    }
                } else {
                    cm.sendNext("You have not yet met the requirement. Defeat 50,000 monsters in Nett's Pyramid, then return to me.\r\nCurrent defeated monsters: " + progress + ".");
                }
            }
        } else if (status == 1) {
            if (selected == 0) {
                cm.sendNextPrev("Once you enter the Pyramid, you will be faced with the wrath of Nett. Since you don't look too sharp, I will offer you some advice and rules to follow. Remember them well.#b\r\n\r\n1. Be careful that your #e#rAct Gauge#b#n does not decrease. The only way to maintain your Gauge level is to battle the monsters without stopping.\r\n2. Those who are unable will pay dearly. Be careful to not cause any #rMiss#b.\r\n3. Be wary of the Pharaoh Jr. Yeti with the #v04032424# mark. Make the mistake of attacking him and you will regret it.\r\n4. Be wise about using the skill that is given to you for Kill accomplishments.");
            } else if (selected == 1) {
                party = selection;
                cm.sendSimple("You who lack fear of death's cruelty, make your decision!\r\n#L0##i3994115##l#L1##i3994116##l#L2##i3994117##l#L3##i3994118##l");
            } else if (selected == 3) {
                var gems = [4001322, 4001323, 4001324, 4001325];
                if (selection >= 0 && selection < gems.length && cm.haveItem(gems[selection])) {
                    cm.gainItem(gems[selection], -1);
                    cm.warp(926010010 + selection);
                } else {
                    cm.sendOk("You'll need a gem to enter Pharaoh Yeti's Tomb. Are you sure you have one?");
                }
                cm.dispose();
            } else {
                cm.dispose();
            }
        } else if (status == 2) {
            if (selected == 0) {
                cm.sendNextPrev("Those who are able to withstand Nett's wrath will be honored, but those who fail will face destruction. This is all the advice I can give you. The rest is in your hands.");
            } else if (selected == 1) {
                var mode = "EASY";
                if (party == 1) {
                    if (cm.getParty() == null) {
                        cm.sendOk("Create a party first.");
                        cm.dispose();
                        return;
                    }
                    if (!cm.isLeader()) {
                        cm.sendOk("Only the party leader may apply for the party challenge.");
                        cm.dispose();
                        return;
                    }
                    if (cm.partyMembersInMap() < 2) {
                        cm.sendOk("Make sure that 2 or more party members are in your map.");
                        cm.dispose();
                        return;
                    }
                }

                if (cm.getPlayer().getLevel() < 40) {
                    cm.sendOk("You must be Lv. 40+ to enter this PQ.");
                    cm.dispose();
                    return;
                }
                if (selection < 3 && cm.getPlayer().getLevel() > 60) {
                    cm.sendOk("Only Hell mode is available for players that are over Lv. 60.");
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
                    cm.sendOk("All rooms are full for this mode, please try it again later or on another channel.");
                }
                cm.dispose();
            }
        } else if (status == 3) {
            cm.dispose();
        }
    } else if (cm.getMapId() == 926020001 || cm.getMapId() == 926010001) {
        if (status == 0) {
            if (selection == 0) {
                cm.warp(926010010);
                cm.dispose();
            } else if (selection == 1) {
                cm.sendNext("I will give you Pharaoh Yeti's Gem. You will be able to enter Pharaoh Yeti's Tomb anytime with this Gem. Check to see if you have at least 1 empty slot in your Etc window.");
            }
        } else if (status == 1) {
            var itemid = 4001325;
            if (cm.canHold(itemid)) {
                cm.gainItem(itemid);
                cm.warp(926010000);
            } else {
                cm.showInfoText("You must have at least 1 empty slot in your Etc window to receive the reward.");
            }
            cm.dispose();
        }
    } else {
        var pyramid = cm.getPyramid();
        if (pyramid != null) {
            pyramid.leave(926010000);
        } else {
            cm.warp(926010000);
        }
        cm.dispose();
    }
}
