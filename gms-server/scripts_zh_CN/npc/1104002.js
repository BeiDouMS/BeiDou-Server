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

var status;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        var mapobj = cm.getMap();

        if (mode == 0 && type > 0) {
            cm.getPlayer().dropMessage(5, "Eleanor: Oh, lost the Empress and still challenging us? Now you've done it! Prepare yourself!!!");

            const LifeFactory = Java.type('org.gms.server.life.LifeFactory');
            const Point = Java.type('java.awt.Point');
            mapobj.spawnMonsterOnGroundBelow(LifeFactory.getMonster(9001010), new Point(850, 0));
            mapobj.destroyNPC(1104002);

            cm.dispose();
            return;
        }
        if (mode == 1) {
            status++;
        } else {
            status--;
        }

        if (status == 0) {
            if (!cm.isQuestStarted(20407)) {
                cm.sendOk("“... 骑士，你似乎还不确定要面对这场战斗，是吗？挑战一个人的时候，如果他们还没有心理准备进行战斗，那就没有什么优雅可言。和你那只笨拙的大鸟说说你的想法，也许它会给你一些勇气。”");
                cm.dispose();
                return;
            }

            cm.sendAcceptDecline("Hahahahaha! This place's Empress is already under my domain, that's surely a great advance on the #bBlack Wings#k' overthrow towards Maple World... And you, there? Still wants to face us? Or, better yet, since you seem strong enough to be quite a supplementary reinforcement at our service, #rwill you meet our expectations and fancy joining us#k since there's nothing more you can do?");
        } else if (status == 1) {
            cm.sendOk("“哈，懦夫在#r黑魔法师#k的军队中没有立足之地。滚吧！”");
            cm.dispose();
        }
    }
}