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

function start(){
    if (im.getMapId() == 106020300) {
        var portal = im.getMap().getPortal("obstacle");
        if (portal != null && portal.getPosition().distance(im.getPlayer().getPosition()) < 240) {
            if (!(im.isQuestStarted(100202) || im.isQuestCompleted(100202))) {
                im.startQuest(100202);
            }
            im.removeAll(2430014);
            im.showInfo("Effect/OnUserEff/normalEffect/mushroomcastle/chatBalloon2");
            im.dropMessage(6, '蘑菇的毒孢子与结界产生了反应，结界消散了。');
        } else {
            im.message('请靠近魔法结界后再使用这个道具。');
        }
    } else {
        im.message('这里没有会对这个道具产生反应的魔法结界。');
    }

    im.dispose();
}

