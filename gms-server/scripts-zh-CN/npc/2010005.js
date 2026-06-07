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
 Shuri the Tour Guide - Orbis (200000000)
 -- By ---------------------------------------------------------------------------------------------
 Information & Xterminator
 -- Version Info -----------------------------------------------------------------------------------
 1.0 - First Version
 ---------------------------------------------------------------------------------------------------
 **/

var pay = 2000;
var ticket = 4031134;
var msg;
var check;

var status = 0;

function start() {
    cm.sendSimple("你听说过风景优美的#b#m110000000##k吗？那是一片离#m" + cm.getPlayer().getMapId() + "#稍远的海滩。我可以现在带你过去，费用是#b" + pay + "金币#k；如果你带着#b#t" + ticket + "##k，就可以免费前往。\r\n\r\n#L0##b支付 " + pay + " 金币。#k#l\r\n#L1##b使用 #t" + ticket + "#。#k#l\r\n#L2##b#t" + ticket + "# 是什么？#k#l");
}

function action(mode, type, selection) {
    if (mode == -1 || (mode == 0 && status == 0)) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 1) {
            cm.sendNext("看起来你还有别的事情要处理。旅行和狩猎都很辛苦，先去休息一下吧；如果改变主意了，随时再来找我。");
            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 1) {
            if (selection == 0 || selection == 1) {
                check = selection;
                if (selection == 0) {
                    msg = "你要支付#b" + pay + "金币#k前往#m110000000#吗？";
                } else if (selection == 1) {
                    msg = "你带着#b#t" + ticket + "##k吗？有了它，就可以随时前往#m110000000#。";
                }
                cm.sendYesNo(msg + "不过那里也有怪物出没，请不要掉以轻心。要现在前往#m110000000#吗？");
            } else if (selection == 2) {
                cm.sendNext("#t" + ticket + "# 是一种特别的旅行券，只要带在身上，就能免费前往#m110000000#。它非常稀有，就连我们导游也要花钱才能得到。可惜我几周前休假时不小心把自己的那张弄丢了。");
                status = 3;
            }
        } else if (status == 2) {
            if (check == 0) {
                if (cm.getMeso() < pay) {
                    cm.sendOk("你的金币似乎不够。赚钱的方法很多，比如出售装备、打倒怪物或完成任务。等准备好费用后再来找我吧。");
                    cm.dispose();
                } else {
                    cm.gainMeso(-pay);
                    access = true;
                }
            } else if (check == 1) {
                if (!cm.haveItem(ticket)) {
                    cm.sendOk("嗯？#b#t" + ticket + "##k在哪里呢？你确定带在身上吗？请再确认一下。");
                    cm.dispose();
                } else {
                    access = true;
                }
            }
            if (access) {
                cm.getPlayer().saveLocation("FLORINA");
                cm.warp(110000000, "st00");
                cm.dispose();
            }
        } else if (status == 3) {
            cm.sendNext("#t" + ticket + "# 是一种特别的旅行券，只要带在身上，就能免费前往#m110000000#。它非常稀有，就连我们导游也要花钱才能得到。可惜我几周前休假时不小心把自己的那张弄丢了。");
        } else if (status == 4) {
            cm.sendPrev("没把那张券带回来，想起来还真让人难受。希望有人把它捡到后放在安全的地方。这就是我的故事。如果你找到它，也许能好好利用起来。还有问题的话，随时来问我。");
        } else if (status == 5) {
            cm.dispose();
        }

    }
}
