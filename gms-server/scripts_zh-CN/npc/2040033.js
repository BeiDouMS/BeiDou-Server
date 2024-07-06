/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc> 
                       Matthias Butz <matze@odinms.de>
                       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation. You may not use, modify
    or distribute this program under any other version of the
    GNU Affero General Public License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 -- Odin JavaScript --------------------------------------------------------------------------------
 Neru - Ludibrium : Ludibrium Pet Walkway (220000006)
 -- By ---------------------------------------------------------------------------------------------
 Xterminator
 -- Version Info -----------------------------------------------------------------------------------
 1.1 - Second Version by Moogra
 1.0 - First Version by Xterminator
 ---------------------------------------------------------------------------------------------------
 **/

function start() {
    if (cm.haveItem(4031128)) {
        cm.sendNext("嗯，那是我哥的信！可能是在责备我觉得自己没在工作之类的事情...嗯？啊...你听从我哥的建议，训练了你的宠物并且到了这里，是吧？太棒了！既然你为了到这里努力了，我会提高你和宠物的亲密度等级。");
    } else {
        cm.sendOk("我哥叫我照顾宠物障碍赛道，但是……因为我离他太远了，我忍不住想要四处闲逛……嘿嘿，既然看不见他，那就随便放松几分钟吧。");
        cm.dispose();
    }
}

function action(mode, type, selection) {
    if (mode < 1) {
    } else if (cm.getPlayer().getNoPets() == 0) {
        cm.sendNextPrev("“嗯... 你真的带着你的宠物来到这里吗？这些障碍是为宠物准备的。你没有宠物来这里干什么？滚出去！”");
    } else {
        cm.gainItem(4031128, -1);
        cm.gainTameness(4);
        cm.sendNextPrev("你觉得呢？你不觉得你和你的宠物已经更亲近了吗？如果你有时间，再次训练你的宠物通过这个障碍课程……当然，要得到我哥哥的许可。");
    }
    cm.dispose();
}