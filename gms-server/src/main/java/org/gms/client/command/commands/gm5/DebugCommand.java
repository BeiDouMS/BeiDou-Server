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
package org.gms.client.command.commands.gm5;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.command.Command;
import org.gms.client.inventory.Item;
import org.gms.constants.id.NpcId;
import org.gms.net.server.Server;
import org.gms.server.ItemInformationProvider;
import org.gms.server.TimerManager;
import org.gms.server.life.Monster;
import org.gms.server.life.SpawnPoint;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.maps.Portal;
import org.gms.server.maps.Reactor;
import org.gms.util.I18nUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class DebugCommand extends Command {
    private final static String[] debugTypes = {"monster", "packet", "portal", "spawnpoint", "pos", "map", "mobsp", "event", "areas", "reactors", "servercoupons", "playercoupons", "timer", "marriage", "buff", ""};
    private final static ItemInformationProvider ii = ItemInformationProvider.getInstance();

    {
        setDescription(I18nUtil.getMessage("DebugCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();

        if (params.length < 1) {
            player.yellowMessage(I18nUtil.getMessage("DebugCommand.message2"));
            return;
        }

        switch (params[0]) {
            case "type":
            case "help":
                String msgTypes = I18nUtil.getMessage("DebugCommand.message3") + "\r\n\r\n";
                for (int i = 0; i < debugTypes.length; i++) {
                    msgTypes += ("#L" + i + "#" + debugTypes[i] + "#l\r\n");
                }

                c.getAbstractPlayerInteraction().npcTalk(NpcId.STEWARD, msgTypes);
                break;

            case "monster":
                List<MapObject> monsters = player.getMap().getMapObjectsInRange(player.getPosition(), Double.POSITIVE_INFINITY, Arrays.asList(MapObjectType.MONSTER));
                for (MapObject monstermo : monsters) {
                    Monster monster = (Monster) monstermo;
                    Character controller = monster.getController();
                    player.message(I18nUtil.getMessage("DebugCommand.message4",monster.getName(), monster.getId(), controller != null ? I18nUtil.getMessage("DebugCommand.message5", controller.getName(), monster.isControllerHasAggro(), monster.isControllerKnowsAboutAggro()) : "<none>"));
                }
                break;

            case "packet":
                //player.getMap().broadcastMessage(PacketCreator.customPacket(joinStringFrom(params, 1)));
                break;

            case "portal":
                Portal portal = player.getMap().findClosestPortal(player.getPosition());
                if (portal != null) {
                    player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message6", portal.getId(), portal.getName(), portal.getType(), portal.getTargetMapId(), portal.getScriptName(), portal.getPortalState() ? 1 : 0));
                } else {
                    player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message7"));
                }
                break;

            case "spawnpoint":
                SpawnPoint sp = player.getMap().findClosestSpawnpoint(player.getPosition());
                if (sp != null) {
                    player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message8", sp.getPosition().getX(), sp.getPosition().getY(), sp.getMonsterId(), !sp.getDenySpawn(), sp.shouldSpawn()));
                } else {
                    player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message9"));
                }
                break;

            case "pos":
                player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message10", (int) player.getPosition().getX(), (int) player.getPosition().getY()));
                break;

            case "map":
                player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message11", player.getMap().getId(), player.getMap().getEventInstance() != null ? player.getMap().getEventInstance().getName() : "null", player.getMap().getAllPlayers().size(), player.getMap().countMonsters(), player.getMap().countReactors(), player.getMap().countItems(), player.getMap().getMapObjects().size()));
                break;

            case "mobsp":
                player.getMap().reportMonsterSpawnPoints(player);
                break;

            case "event":
                if (player.getEventInstance() == null) {
                    player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message12"));
                } else {
                    player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message13", player.getEventInstance().getName()));
                }
                break;

            case "areas":
                player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message14", player.getMapId()));

                byte index = 0;
                for (Rectangle rect : player.getMap().getAreas()) {
                    player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message15", index, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
                    index++;
                }
                break;

            case "reactors":
                player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message16", player.getMapId()));

                for (MapObject mmo : player.getMap().getReactors()) {
                    Reactor mr = (Reactor) mmo;
                    player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message17", mr.getId(), mr.getObjectId(), mr.getName(), mr.getReactorType(), mr.getState(), mr.getEventState(), mr.getPosition().getX(), mr.getPosition().getY()));
                }
                break;

            case "servercoupons":
            case "coupons":
                String s = I18nUtil.getMessage("DebugCommand.message18");
                player.dropMessage(6, s);
                for (Integer i : Server.getInstance().getActiveCoupons()) {
                    s = ii.getName(i) + "  (" + i + ");";
                    player.dropMessage(6, s);
                }
                break;

            case "playercoupons":
                s = I18nUtil.getMessage("DebugCommand.message19");
                player.dropMessage(6, s);
                for (Integer i : player.getActiveCoupons()) {
                    s = ii.getName(i) + "  (" + i + ");";
                    player.dropMessage(6, s);
                }
                break;

            case "timer":
                TimerManager tMan = TimerManager.getInstance();
                player.dropMessage(6, I18nUtil.getMessage("DebugCommand.message20", tMan.getTaskCount(), tMan.getQueuedTasks(), tMan.getActiveCount(), tMan.getCompletedTaskCount()));
                break;

            case "marriage":
                c.getChannelServer().debugMarriageStatus();
                break;

            case "buff":
                c.getPlayer().debugListAllBuffs();
                break;
            default:
                player.dropMessage(5, I18nUtil.getMessage("DebugCommand.message21"));
        }
    }
}
