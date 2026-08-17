package soloMapling.ArtificialPlayer.GCMoveSystem;

import org.gms.provider.Data;
import org.gms.provider.DataProvider;
import org.gms.provider.DataProviderFactory;
import org.gms.provider.DataTool;
import org.gms.provider.wz.WZFiles;
import org.gms.server.maps.Foothold;
import org.gms.server.maps.FootholdTree;
import org.gms.server.maps.MapleMap;
import org.gms.server.maps.Portal;
import org.gms.server.maps.PortalFactory;
import org.gms.server.maps.Rope;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

// Ported from GreenCatMS. Credit: NutNNut.
final class BotNavigationMapLoader {
    private BotNavigationMapLoader() {
    }

    static MapleMap loadMapGeometry(int mapId) {
        DataProvider mapSource = DataProviderFactory.getDataProvider(WZFiles.MAP);
        Data mapData = mapSource.getData(getMapName(mapId));
        if (mapData == null) {
            throw new IllegalArgumentException("Map data not found for " + mapId);
        }

        Data infoData = mapData.getChildByPath("info");
        String link = DataTool.getString(infoData.getChildByPath("link"), "");
        if (!link.isEmpty()) {
            mapData = mapSource.getData(getMapName(Integer.parseInt(link)));
            infoData = mapData.getChildByPath("info");
        }

        float monsterRate = 0.0f;
        Data mobRate = infoData.getChildByPath("mobRate");
        if (mobRate != null) {
            monsterRate = (Float) mobRate.getData();
        }

        MapleMap map = new MapleMap(mapId, 0, 0, DataTool.getInt("returnMap", infoData, mapId), monsterRate);
        map.setFieldLimit(DataTool.getInt(infoData.getChildByPath("fieldLimit"), 0));
        map.setSwim(DataTool.getInt(infoData.getChildByPath("swim"), 0) != 0);
        loadBounds(map, mapData, infoData);
        loadPortals(map, mapData);
        loadFootholds(map, mapData);
        loadRopes(map, mapData);

        Data footholdSpeedData = infoData.getChildByPath("fs");
        if (footholdSpeedData != null) {
            map.setFootholdSpeed(DataTool.getFloat(footholdSpeedData));
        }
        return map;
    }

    private static void loadBounds(MapleMap map, Data mapData, Data infoData) {
        int top = DataTool.getInt(infoData.getChildByPath("VRTop"));
        int bottom = DataTool.getInt(infoData.getChildByPath("VRBottom"));
        if (top == bottom) {
            Data minimapData = mapData.getChildByPath("miniMap");
            if (minimapData != null) {
                int px = DataTool.getInt(minimapData.getChildByPath("centerX")) * -1;
                int py = DataTool.getInt(minimapData.getChildByPath("centerY")) * -1;
                int height = DataTool.getInt(minimapData.getChildByPath("height"));
                int width = DataTool.getInt(minimapData.getChildByPath("width"));
                map.setMapPointBoundings(px, py, height, width);
                return;
            }

            int dist = 1 << 18;
            map.setMapPointBoundings(-dist / 2, -dist / 2, dist, dist);
            return;
        }

        int left = DataTool.getInt(infoData.getChildByPath("VRLeft"));
        int right = DataTool.getInt(infoData.getChildByPath("VRRight"));
        map.setMapLineBoundings(top, bottom, left, right);
    }

    private static void loadPortals(MapleMap map, Data mapData) {
        Data portalData = mapData.getChildByPath("portal");
        if (portalData == null) {
            return;
        }

        PortalFactory portalFactory = new PortalFactory();
        for (Data portal : portalData) {
            Portal created = portalFactory.makePortal(DataTool.getInt(portal.getChildByPath("pt")), portal);
            map.addPortal(created);
        }
    }

    private static void loadFootholds(MapleMap map, Data mapData) {
        List<Foothold> footholds = new LinkedList<>();
        Point lowerBound = new Point();
        Point upperBound = new Point();

        Data footholdData = mapData.getChildByPath("foothold");
        if (footholdData == null) {
            map.setFootholds(new FootholdTree(new Point(), new Point()));
            return;
        }

        for (Data footRoot : footholdData) {
            for (Data footCategory : footRoot) {
                for (Data footHold : footCategory) {
                    int x1 = DataTool.getInt(footHold.getChildByPath("x1"));
                    int y1 = DataTool.getInt(footHold.getChildByPath("y1"));
                    int x2 = DataTool.getInt(footHold.getChildByPath("x2"));
                    int y2 = DataTool.getInt(footHold.getChildByPath("y2"));
                    Foothold foothold = new Foothold(new Point(x1, y1), new Point(x2, y2), Integer.parseInt(footHold.getName()));
                    foothold.setPrev(DataTool.getInt(footHold.getChildByPath("prev")));
                    foothold.setNext(DataTool.getInt(footHold.getChildByPath("next")));
                    foothold.setForbidFallDown(DataTool.getInt(footHold.getChildByPath("forbidFallDown"), 0) != 0);
                    footholds.add(foothold);
                    lowerBound.x = Math.min(lowerBound.x, Math.min(x1, x2));
                    lowerBound.y = Math.min(lowerBound.y, Math.min(y1, y2));
                    upperBound.x = Math.max(upperBound.x, Math.max(x1, x2));
                    upperBound.y = Math.max(upperBound.y, Math.max(y1, y2));
                }
            }
        }

        FootholdTree tree = new FootholdTree(lowerBound, upperBound);
        for (Foothold foothold : footholds) {
            tree.insert(foothold);
        }
        map.setFootholds(tree);
    }

    private static void loadRopes(MapleMap map, Data mapData) {
        Data ropeData = mapData.getChildByPath("ladderRope");
        if (ropeData == null) {
            return;
        }

        for (Data rope : ropeData) {
            int x = DataTool.getInt(rope.getChildByPath("x"));
            int y1 = DataTool.getInt(rope.getChildByPath("y1"));
            int y2 = DataTool.getInt(rope.getChildByPath("y2"));
            boolean ladder = DataTool.getInt(rope.getChildByPath("l"), 0) == 1;
            map.addRope(new Rope(x, y1, y2, ladder));
        }
    }

    private static String getMapName(int mapId) {
        return "Map/Map" + (mapId / 100000000) + "/" + String.format("%09d", mapId) + ".img";
    }
}
