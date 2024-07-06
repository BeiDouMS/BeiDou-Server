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

/* Oasis near Ariant Castle
 */


function isTigunMorphed(ch) {
    const BuffStat = Java.type('org.gms.client.BuffStat');
    return ch.getBuffSource(BuffStat.MORPH) == 2210005;
}

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0) {
        cm.dispose();
    } else {
        if (mode == 1) {
            status++;
        } else {
            status--;
        }
        if (status == 0 && mode == 1) {
            if (cm.isQuestStarted(3900) && cm.getQuestProgressInt(3900) != 5) {
                cm.sendOk("#b(你喝了绿洲中的水，感到精神焕发。)");
                cm.setQuestProgress(3900, 5);
            } else if (cm.isQuestCompleted(3938)) {
                if (cm.canHold(2210005)) {
                    if (!cm.haveItem(2210005) && !isTigunMorphed(cm.getPlayer())) {
                        cm.gainItem(2210005, 1);
                        cm.sendOk("你找到了一缕头发（可能是Tigun的）在水中漂浮着，你捉住了它。记得上次#bJano#k是怎么做的，你制作了一个新的#t2210005#。");
                    }
                } else {
                    cm.sendOk("你没有可用的使用槽位。");
                }
            } else if (cm.isQuestStarted(3934) || (cm.isQuestCompleted(3934) && !cm.isQuestCompleted(3935))) {
                if (cm.canHold(2210005)) {
                    if (!cm.haveItem(2210005) && !isTigunMorphed(cm.getPlayer())) {
                        cm.gainItem(2210005, 1);
                        cm.sendOk("你成功找到了一只在河流上漂浮的奇怪瓶子。它看起来像是一个模仿城堡卫兵之一的变身瓶，也许你可以用它自由地在城堡内漫游。");
                    }
                } else {
                    cm.sendOk("你发现了一只在河流上漂浮的奇怪烧瓶。但你决定忽略它，因为你没有可用的使用槽。");
                }
            }

            cm.dispose();
        }
    }
}