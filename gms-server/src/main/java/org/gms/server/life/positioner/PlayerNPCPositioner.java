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
package org.gms.server.life.positioner;

import org.gms.config.GameConfig;
import org.gms.net.server.Server;
import org.gms.net.server.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.gms.server.life.PlayerNPC;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.server.maps.MapleMap;
import org.gms.util.PacketCreator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author RonanLana
 */
public class PlayerNPCPositioner {
    private static final Logger log = LoggerFactory.getLogger(PlayerNPCPositioner.class);

    private static boolean isPlayerNpcNearby(List<Point> otherPos, Point searchPos, int xLimit, int yLimit) {
        int xLimit2 = xLimit / 2, yLimit2 = yLimit / 2;

        Rectangle searchRect = new Rectangle(searchPos.x - xLimit2, searchPos.y - yLimit2, xLimit, yLimit);
        for (Point pos : otherPos) {
            Rectangle otherRect = new Rectangle(pos.x - xLimit2, pos.y - yLimit2, xLimit, yLimit);

            if (otherRect.intersects(searchRect)) {
                return true;
            }
        }

        return false;
    }

    private static int calcDx(int newStep) {
        return GameConfig.getServerInt("playernpc_area_x") / (newStep + 1);
    }

    private static int calcDy(int newStep) {
        return (GameConfig.getServerInt("playernpc_area_y") / 2) + (GameConfig.getServerInt("playernpc_area_y") / (1 << (newStep + 1)));
    }

    private static List<Point> rearrangePlayerNpcPositions(MapleMap map, int newStep, int pnpcsSize) {
        Rectangle mapArea = map.getMapArea();

        int leftPx = mapArea.x + GameConfig.getServerInt("playernpc_initial_x"), px, py = mapArea.y + GameConfig.getServerInt("playernpc_initial_y");
        int outx = mapArea.x + mapArea.width - GameConfig.getServerInt("playernpc_initial_x"), outy = mapArea.y + mapArea.height;
        int cx = calcDx(newStep), cy = calcDy(newStep);

        List<Point> otherPlayerNpcs = new LinkedList<>();
        while (py < outy) {
            px = leftPx;

            while (px < outx) {
                Point searchPos = map.getPointBelow(new Point(px, py));
                if (searchPos != null) {
                    if (!isPlayerNpcNearby(otherPlayerNpcs, searchPos, cx, cy)) {
                        otherPlayerNpcs.add(searchPos);

                        if (otherPlayerNpcs.size() == pnpcsSize) {
                            return otherPlayerNpcs;
                        }
                    }
                }

                px += cx;
            }

            py += cy;
        }

        return null;
    }

    private static Point rearrangePlayerNpcs(MapleMap map, int newStep, List<PlayerNPC> pnpcs) {
        Rectangle mapArea = map.getMapArea();

        int leftPx = mapArea.x + GameConfig.getServerInt("playernpc_initial_x"), px, py = mapArea.y + GameConfig.getServerInt("playernpc_initial_y");
        int outx = mapArea.x + mapArea.width - GameConfig.getServerInt("playernpc_initial_x"), outy = mapArea.y + mapArea.height;
        int cx = calcDx(newStep), cy = calcDy(newStep);

        List<Point> otherPlayerNpcs = new LinkedList<>();
        int i = 0;

        while (py < outy) {
            px = leftPx;

            while (px < outx) {
                Point searchPos = map.getPointBelow(new Point(px, py));
                if (searchPos != null) {
                    if (!isPlayerNpcNearby(otherPlayerNpcs, searchPos, cx, cy)) {
                        if (i == pnpcs.size()) {
                            return searchPos;
                        }

                        PlayerNPC pn = pnpcs.get(i);
                        i++;

                        pn.updatePlayerNPCPosition(map, searchPos);
                        otherPlayerNpcs.add(searchPos);
                    }
                }

                px += cx;
            }

            py += cy;
        }

        return null;    // this area should not be reached under any scenario
    }

    private static Point reorganizePlayerNpcs(MapleMap map, int newStep, List<MapObject> mmoList) {
        if (!mmoList.isEmpty()) {
            if (GameConfig.getServerBoolean("use_debug")) {
                log.info("Re-organizing pnpc map, step {}", newStep);
            }

            List<PlayerNPC> playerNpcs = new ArrayList<>(mmoList.size());
            for (MapObject mmo : mmoList) {
                playerNpcs.add((PlayerNPC) mmo);
            }

            playerNpcs.sort((p1, p2) -> {
                return p1.getScriptId() - p2.getScriptId(); // scriptid as playernpc history
            });

            for (Channel ch : Server.getInstance().getChannelsFromWorld(map.getWorld())) {
                MapleMap m = ch.getMapFactory().getMap(map.getId());

                for (PlayerNPC pn : playerNpcs) {
                    m.removeMapObject(pn);
                    m.broadcastMessage(PacketCreator.removeNPCController(pn.getObjectId()));
                    m.broadcastMessage(PacketCreator.removePlayerNPC(pn.getObjectId()));
                }
            }

            Point ret = rearrangePlayerNpcs(map, newStep, playerNpcs);

            for (Channel ch : Server.getInstance().getChannelsFromWorld(map.getWorld())) {
                MapleMap m = ch.getMapFactory().getMap(map.getId());

                for (PlayerNPC pn : playerNpcs) {
                    m.addPlayerNPCMapObject(pn);
                    m.broadcastMessage(PacketCreator.spawnPlayerNPC(pn));
                    m.broadcastMessage(PacketCreator.getPlayerNPC(pn));
                }
            }

            return ret;
        }

        return null;
    }

    private static Point getNextPlayerNpcPosition(MapleMap map, int initStep) {   // automated playernpc position thanks to Ronan
        List<MapObject> mmoList = map.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapObjectType.PLAYER_NPC));
        List<Point> otherPlayerNpcs = new LinkedList<>();
        for (MapObject mmo : mmoList) {
            otherPlayerNpcs.add(mmo.getPosition());
        }

        int cx = calcDx(initStep), cy = calcDy(initStep);
        Rectangle mapArea = map.getMapArea();
        int outx = mapArea.x + mapArea.width - GameConfig.getServerInt("playernpc_initial_x"), outy = mapArea.y + mapArea.height;
        boolean reorganize = false;

        int i = initStep;
        while (i < GameConfig.getServerInt("playernpc_area_steps")) {
            int leftPx = mapArea.x + GameConfig.getServerInt("playernpc_initial_x"), px, py = mapArea.y + GameConfig.getServerInt("playernpc_initial_y");

            while (py < outy) {
                px = leftPx;

                while (px < outx) {
                    Point searchPos = map.getPointBelow(new Point(px, py));
                    if (searchPos != null) {
                        if (!isPlayerNpcNearby(otherPlayerNpcs, searchPos, cx, cy)) {
                            if (i > initStep) {
                                map.getWorldServer().setPlayerNpcMapStep(map.getId(), i);
                            }

                            if (reorganize && GameConfig.getServerBoolean("playernpc_organize_area")) {
                                return reorganizePlayerNpcs(map, i, mmoList);
                            }

                            return searchPos;
                        }
                    }

                    px += cx;
                }

                py += cy;
            }

            reorganize = true;
            i++;

            cx = calcDx(i);
            cy = calcDy(i);
            if (GameConfig.getServerBoolean("playernpc_organize_area")) {
                otherPlayerNpcs = rearrangePlayerNpcPositions(map, i, mmoList.size());
            }
        }

        if (i > initStep) {
            map.getWorldServer().setPlayerNpcMapStep(map.getId(), GameConfig.getServerInt("playernpc_area_steps") - 1);
        }
        return null;
    }

    public static Point getNextPlayerNpcPosition(MapleMap map) {
        return getNextPlayerNpcPosition(map, map.getWorldServer().getPlayerNpcMapStep(map.getId()));
    }
}
