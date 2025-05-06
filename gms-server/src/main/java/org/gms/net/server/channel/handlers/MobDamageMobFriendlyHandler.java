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
package org.gms.net.server.channel.handlers;

import org.gms.client.Client;
import org.gms.constants.id.MobId;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.scripting.event.EventInstanceManager;
import org.gms.server.life.Monster;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;
import org.gms.util.Randomizer;

/**
 * @author Xotic (XoticStory) & BubblesDev
 */

public final class MobDamageMobFriendlyHandler extends AbstractPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        int attacker = p.readInt();
        p.readInt();
        int damaged = p.readInt();

        MapleMap map = c.getPlayer().getMap();
        Monster monster = map.getMonsterByOid(damaged);

        if (monster == null || map.getMonsterByOid(attacker) == null) {
            return;
        }

        int damage = Randomizer.nextInt(((monster.getMaxHp() / 13 + monster.getPADamage() * 10)) * 2 + 500) / 10; // Formula planned by Beng.
        String mopName = monster.getName();
        if (monster.getHp() - damage < 1) {     // friendly dies
            switch (monster.getId()) {
                case MobId.WATCH_HOG ->
                        map.broadcastMessage(PacketCreator.serverNotice(5, mopName + " 被外星人殴打致重伤，已被兽医紧急拉走抢救。"));    //护卫用小浣猪
                case MobId.MOON_BUNNY -> //moon bunny
                        map.broadcastMessage(PacketCreator.serverNotice(5, mopName + " 因为重伤被抬回去休养了。")); //月妙
                case MobId.TYLUS -> //tylus
                        map.broadcastMessage(PacketCreator.serverNotice(5, mopName + " 在伏击部队的强大攻势下倒下了。"));  //冒牌泰勒斯
                case MobId.JULIET -> //juliet
                        map.broadcastMessage(PacketCreator.serverNotice(5, mopName + " 在战斗中晕倒了。")); //朱丽叶
                case MobId.ROMEO -> //romeo
                        map.broadcastMessage(PacketCreator.serverNotice(5, mopName + " 在战斗中晕倒了。")); //罗密欧
                case MobId.GIANT_SNOWMAN_LV1_EASY, MobId.GIANT_SNOWMAN_LV1_MEDIUM, MobId.GIANT_SNOWMAN_LV1_HARD ->
                        map.broadcastMessage(PacketCreator.serverNotice(5, "雪人在战斗的热浪中融化了。"));
                case MobId.DELLI -> //delli
                        map.broadcastMessage(PacketCreator.serverNotice(5, mopName + " 在伏击后消失了，只留下地上的被单..."));  //德里
            }
            
            map.killFriendlies(monster);
        } else {
            EventInstanceManager eim = map.getEventInstance();
            if (eim != null) {
                eim.friendlyDamaged(monster);
            }
        }

        monster.applyAndGetHpDamage(damage, false);
        int remainingHp = monster.getHp();
        if (remainingHp <= 0) {
            remainingHp = 0;
            map.removeMapObject(monster);
        }

        map.broadcastMessage(PacketCreator.MobDamageMobFriendly(monster, damage, remainingHp), monster.getPosition());
        c.sendPacket(PacketCreator.enableActions());
    }
}