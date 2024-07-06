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
/* Yulete
	Yulete's Office (926100203)
	Magatia NPC
 */

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function playersTooClose() {
    var npcpos = cm.getMap().getMapObject(cm.getNpcObjectId()).getPosition();
    var listchr = cm.getMap().getPlayers();

    for (var iterator = listchr.iterator(); iterator.hasNext();) {
        var chr = iterator.next();

        var chrpos = chr.getPosition();
        if (Math.sqrt(Math.pow((npcpos.getX() - chrpos.getX()), 2) + Math.pow((npcpos.getY() - chrpos.getY()), 2)) < 310) {
            return true;
        }
    }

    return false;
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

        var eim = cm.getEventInstance();

        if (cm.getMapId() == 926100203) {
            if (status == 0) {
                var state = eim.getIntProperty("yuleteTalked");

                if (state == -1) {
                    cm.sendOk("“嘿，看来你们有了新伙伴。祝你们和他们玩得开心，我先礼貌地告辞了。”");

                } else if (playersTooClose()) {
                    cm.sendOk("哦，你好。自从你们进入这个范围以来，我一直在监视你们的行动。能够到达这里真是了不起，我对你们表示赞赏。现在看看时间，我现在有一个约会，恐怕我需要离开了。但不用担心，我的助手们会处理你们所有人。现在，如果你们允许的话，我就走了。");

                    eim.setIntProperty("yuleteTalked", -1);
                } else if (eim.getIntProperty("npcShocked") == 0) {
                    cm.sendOk("嗯~ 你可真是个狡猾的家伙？不过，这并不重要。自从你们进入这个区域以来，我一直在#b监视你们的行动#k。能够到达这里真是了不起，我对你们都表示赞赏。现在，看看时间，我现在有个约会，恐怕我得离开了。但不用担心，我的#r助手们#k会处理你们所有人。现在，如果你允许的话，我就走了。");

                    eim.setIntProperty("yuleteTalked", -1);
                } else {
                    cm.sendOk("“哈哈！什么，怎么--你是怎么到这里来的？！我以为我已经封闭了所有的路径！不要紧，这种情况很快就会解决。伙计们：部署#rmaster武器#k！！你！是的，就是你。你难道不觉得这就此结束了吗，回头看看你的同伴，他们需要一些帮助！我现在就撤退。”");

                    eim.setIntProperty("yuleteTalked", 1);
                }
            }

            cm.dispose();
        } else {
            if (status == 0) {
                if (eim.isEventCleared()) {
                    cm.sendOk("不要啊... 我被打败了？但是为什么？我所做的一切都是为了更伟大的炼金术的发展！你们不能把我关起来，我所做的只是站在我这样的位置上的每个人都会做的事情！但是不，他们就决定阻碍科学的进步，只是因为它被认为是危险的？哦，得了吧！");
                } else {
                    var state = eim.getIntProperty("yuletePassed");

                    const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
                    const Point = Java.type('java.awt.Point');
                    if (state == -1) {
                        cm.sendOk("瞧！这就是马加提亚炼金术研究的顶峰！哈哈哈哈哈哈……");
                    } else if (state == 0) {
                        cm.sendOk("你们真是一群讨厌的家伙，哎。好吧，我向你们展示我的最新武器，由最优秀的炼金术带来，#r弗兰肯罗伊德#k。");
                        eim.dropMessage(5, "Yulete: I present you my newest weapon, brought by the finest alchemy, Frankenroid!");

                        var mapobj = eim.getMapInstance(926100401);
                        var bossobj = LifeFactory.getMonster(9300139);

                        //mapobj.spawnMonsterWithEffect(bossobj, 13, new Packages.java.awt.Point(250, 100));
                        mapobj.spawnMonsterOnGroundBelow(bossobj, new Point(250, 100));

                        eim.setIntProperty("statusStg7", 1);
                        eim.setIntProperty("yuletePassed", -1);
                    } else {
                        cm.sendOk("你们这些家伙真是让人头疼，哎。好吧，我向你们展示我的最新武器，由阿尔卡德诺和泽纳米斯最精湛的炼金术结合而成，那些令马加提亚社会中无聊的人们禁止携带的东西，#rmighty Frankenroid#k！");
                        eim.dropMessage(5, "Yulete: I present you my newest weapon, brought by the finest combined alchemy of Alcadno's and Zenumist's, those that the boring people of Magatia societies have banned to bring along, the mighty Frankenroid!!");

                        var mapobj = eim.getMapInstance(926100401);
                        var bossobj = LifeFactory.getMonster(9300140);

                        //mapobj.spawnMonsterWithEffect(bossobj, 14, new Packages.java.awt.Point(250, 100));
                        mapobj.spawnMonsterOnGroundBelow(bossobj, new Point(250, 100));

                        eim.setIntProperty("statusStg7", 2);
                        eim.setIntProperty("yuletePassed", -1);
                    }
                }
            }

            cm.dispose();
        }
    }
}