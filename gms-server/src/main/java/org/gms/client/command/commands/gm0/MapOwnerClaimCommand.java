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
   @Author: Ronan
*/
package org.gms.client.command.commands.gm0;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.config.GameConfig;
import org.gms.server.maps.MapleMap;
import org.gms.util.I18nUtil;

public class MapOwnerClaimCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("MapOwnerClaimCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        if (c.tryacquireClient()) {
            try {
                Character chr = c.getPlayer();

                if (GameConfig.getServerBoolean("use_map_ownership_system")) {
                    if (chr.getEventInstance() == null) {
                        MapleMap map = chr.getMap();
                        if (map.countBosses() == 0) {   // thanks Conrad for suggesting bosses prevent map leasing
                            MapleMap ownedMap = chr.getOwnedMap();  // thanks Conrad for suggesting not unlease a map as soon as player exits it
                            if (ownedMap != null) {
                                ownedMap.unclaimOwnership(chr);

                                if (map == ownedMap) {
                                    chr.dropMessage(5, I18nUtil.getMessage("MapOwnerClaimCommand.message2"));
                                    return;
                                }
                            }

                            if (map.claimOwnership(chr)) {
                                chr.dropMessage(5, I18nUtil.getMessage("MapOwnerClaimCommand.message3"));
                            } else {
                                chr.dropMessage(5, I18nUtil.getMessage("MapOwnerClaimCommand.message4"));
                            }
                        } else {
                            chr.dropMessage(5, I18nUtil.getMessage("MapOwnerClaimCommand.message5"));
                        }
                    } else {
                        chr.dropMessage(5, I18nUtil.getMessage("MapOwnerClaimCommand.message6"));
                    }
                } else {
                    chr.dropMessage(5, I18nUtil.getMessage("MapOwnerClaimCommand.message7"));
                }
            } finally {
                c.releaseClient();
            }
        }
    }
}
