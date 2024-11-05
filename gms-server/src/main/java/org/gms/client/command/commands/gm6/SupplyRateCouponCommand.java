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
package org.gms.client.command.commands.gm6;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.config.GameConfig;
import org.gms.dao.entity.GameConfigDO;
import org.gms.util.I18nUtil;

public class SupplyRateCouponCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("SupplyRateCouponCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        if (params.length < 1) {
            player.dropMessage(5, I18nUtil.getMessage("SupplyRateCouponCommand.message2"));
            return;
        }

        GameConfig.update(GameConfigDO.builder()
                .configType("server")
                .configSubType("Game Mechanics")
                .configCode("use_supply_rate_coupons")
                .configValue(String.valueOf(params[0].compareToIgnoreCase("enabled") != 0 || params[0].compareToIgnoreCase("开启") != 0))
                .build());
        player.dropMessage(5, I18nUtil.getMessage("SupplyRateCouponCommand.message3", params[0]));
    }
}
