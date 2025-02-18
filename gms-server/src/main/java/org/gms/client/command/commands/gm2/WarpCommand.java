/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package org.gms.client.command.commands.gm2;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.server.maps.FieldLimit;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.MiniDungeonInfo;
import org.gms.util.I18nUtil;
import org.gms.server.maps.Portal;

public class WarpCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("WarpCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("WarpCommand.message2"));
            return;
        }

        try {
            MapleMap target = c.getChannelServer().getMapFactory().getMap(Integer.parseInt(params[0]));
            if (target == null) {
                player.yellowMessage(I18nUtil.getMessage("WarpCommand.message3", params[0]));
                return;
            }

            if (!player.isAlive()) {
                player.dropMessage(1, I18nUtil.getMessage("WarpCommand.message4"));
                return;
            }

            if (!player.isGM()) {
                if (player.getEventInstance() != null || MiniDungeonInfo.isDungeonMap(player.getMapId()) || FieldLimit.CANNOTMIGRATE.check(player.getMap().getFieldLimit())) {
                    player.dropMessage(1, I18nUtil.getMessage("WarpCommand.message5"));
                    return;
                }
            }

            // expedition issue with this command detected thanks to Masterrulax
            player.saveLocationOnWarp();
            Portal portal = null;
            if (params.length >= 2) {
                portal = target.getPortal(params[1]);   // 首先尝试使用String作为参数获取Portal
                if (portal == null && params[1].matches("\\d+")) {// 检查params[1]是否全由数字组成，如果是，则尝试使用int方式获取Portal
                    portal = target.getPortal(Integer.parseInt(params[1]));
                }
            }
            if (portal == null) {
                portal = target.getRandomPlayerSpawnpoint(); // 随机落脚点
            }
            player.changeMap(target, portal);
        } catch (Exception ex) {
            player.yellowMessage(I18nUtil.getMessage("WarpCommand.message3", params[0]));
        }
    }
}
