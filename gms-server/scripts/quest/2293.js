/*
	This file is part of the DietStory Maple Story Server
    Copyright (C) 2017
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
/* Author: Benji 
	NPC Name: 		Maestro Rho
	Map(s): 		Kerning Square Lobby
	Description: 	The Last Song
*/

var status = -1;

function start(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        qm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }

    if (status == 0) {
        qm.sendNext("你还记得摇滚精神演奏的最后一首歌吗？我能想到他的几首歌，接下来模仿给你，仔细听完后告诉我是哪首歌。#b你只有一次机会#k，所以请谨慎做出选择。");
        qm.forceStartQuest();
    } else if (status == 1) {
        qm.dispose();
    }
}

function end(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        qm.dispose();
        return;
    } else if (mode == 0) {
        status--;
    } else {
        status++;
    }

    // TODO: there are 10 different riffs; quest2288/0 through quest2288/9.
    // One of the riffs should play randomly upon the death of Spirit of Rock, but there is currently no system in place to achieve that in a reasonable way.
    // Spirit of Rock (4300013) spawns an invisible mob on death (Spirit of Rock's Soul, 4300017) which was likely used in some clever way in GMS.
    // The map (103040430) has two scripts which could be useful: onFirstUserEnter=Depart_Boss_F_Enter and onUserEnter=Depart_BossEnter
    // Currently, the best hypothesis is that one of the map scripts registers some form of "mob spawn" action/script that runs once the invisible mob spawns.
    // The script would randomly pick one of the 10 riffs and then register it with all chrs on the map (to later be used by this quest 2293) and play it.
    if (status == 0) {
        qm.sendSimple("我给你一些样品试听。请选一个。在做你的选择之前请仔细听。\r\n\\t#b#L1#听第一首歌#l \r\n\\t#L2#听第二首歌#l \r\n\\t#L3#听第三首歌#l \r\n\\r\n\\t#e#L4#输入正确的歌曲。#l");
    } else if (status == 1) {
        if (selection == 1) {
            qm.playSound("Party1/Failed");
            qm.sendOk("令人尴尬的熟悉。。。");
            status = -1;
        } else if (selection == 2) {
            qm.playSound("Coconut/Failed");
            qm.sendOk("是这个吗？");
            status = -1;
        } else if (selection == 3) {
            qm.playSound("quest2288/6");
            qm.sendOk("你听到了吗?");
            status = -1;
        } else if (selection == 4) {
            qm.sendGetNumber("现在，请告诉我答案。你只有#b一次机会#k，所以请明智地选择。请在聊天窗口输入#b1，2，或者3#k。\r\n", 1, 1, 3);
        }
    } else if (status == 2) {
        if (selection == 1) {
            qm.sendOk("显然你不喜欢音乐。");
        } else if (selection == 2) {
            qm.sendOk("我想你可以再试一次。");
        } else if (selection == 3) {
            qm.sendOk("所以这就是他演奏的那首歌。。。好吧，这毕竟不是我的歌，但我很高兴我现在可以肯定地知道。非常感谢。");
            qm.forceCompleteQuest();
            qm.gainExp(32500);
        } else {
            qm.dispose();
        }
    } else if (status == 3) {
        qm.dispose();
    }
}
