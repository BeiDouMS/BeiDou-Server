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
import org.gms.server.life.Monster;
import org.gms.server.life.NPC;
import org.gms.server.life.PlayerNPC;
import org.gms.server.maps.MapObject;
import org.gms.util.I18nUtil;

import java.util.HashSet;

public class WhereaMiCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("WhereaMiCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        HashSet<Character> chars = new HashSet<>();
        HashSet<NPC> npcs = new HashSet<>();
        HashSet<PlayerNPC> playernpcs = new HashSet<>();
        HashSet<Monster> mobs = new HashSet<>();

        for (MapObject mmo : player.getMap().getMapObjects()) {
            if (mmo instanceof NPC npc) {
                npcs.add(npc);
            } else if (mmo instanceof Character mc) {
                chars.add(mc);
            } else if (mmo instanceof Monster mob) {
                if (mob.isAlive()) {
                    mobs.add(mob);
                }
            } else if (mmo instanceof PlayerNPC npc) {
                playernpcs.add(npc);
            }
        }

        player.yellowMessage(I18nUtil.getMessage("WhereaMiCommand.message2") + player.getMap().getId());

        player.yellowMessage(I18nUtil.getMessage("WhereaMiCommand.message3"));
        for (Character chr : chars) {
            player.dropMessage(5, ">> " + chr.getName() + " - " + chr.getId() + " - " + I18nUtil.getMessage("WhereaMiCommand.message8") + chr.getObjectId());
        }

        if (!playernpcs.isEmpty()) {
            player.yellowMessage(I18nUtil.getMessage("WhereaMiCommand.message4"));
            for (PlayerNPC pnpc : playernpcs) {
                player.dropMessage(5, ">> " + pnpc.getName() + I18nUtil.getMessage("WhereaMiCommand.message7") + pnpc.getScriptId() + " - " + I18nUtil.getMessage("WhereaMiCommand.message8") + pnpc.getObjectId());
            }
        }

        if (!npcs.isEmpty()) {
            player.yellowMessage(I18nUtil.getMessage("WhereaMiCommand.message5"));
            for (NPC npc : npcs) {
                player.dropMessage(5, ">> " + npc.getName() + " - " + npc.getId() + " - " + I18nUtil.getMessage("WhereaMiCommand.message8") + npc.getObjectId());
            }
        }

        if (!mobs.isEmpty()) {
            player.yellowMessage(I18nUtil.getMessage("WhereaMiCommand.message6"));
            for (Monster mob : mobs) {
                if (mob.isAlive()) {
                    player.dropMessage(5, ">> " + mob.getName() + " - " + mob.getId() + " - " + I18nUtil.getMessage("WhereaMiCommand.message8") + mob.getObjectId());
                }
            }
        }
    }
}
