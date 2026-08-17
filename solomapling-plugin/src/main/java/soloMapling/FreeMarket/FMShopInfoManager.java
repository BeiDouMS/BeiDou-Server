package soloMapling.FreeMarket;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import static soloMapling.FreeMarket.ArtificialShopGenerator.random;

public class FMShopInfoManager {

    public static List<Integer> henesysRegionFM = new ArrayList<>(List.of(910000001, 910000002, 910000003, 910000004, 910000005, 910000006));
    public static List<Integer> ludiRegionFM = new ArrayList<>(List.of(910000007, 910000008, 910000009, 910000010, 910000011, 910000012));
    public static List<Integer> perionRegionFM = new ArrayList<>(List.of(910000013, 910000014, 910000015, 910000016, 910000017));
    public static List<Integer> elnathRegionFM = new ArrayList<>(List.of(910000018, 910000019, 910000020, 910000021, 910000022));

    public static List<Point> henesysShopCoordinates = new ArrayList<>(List.of(
            // 24 Shops
            // Top Row
            new Point(-262, -416), new Point(-129, -416), new Point(19, -416), new Point(175, -416),
            new Point(328, -416), new Point(482, -416), new Point(638, -416), new Point(801, -416),
            // Mid Row
            new Point(-300, -206), new Point(-133, -206), new Point(18, -206),
            new Point(166, -206), new Point(319, -206), new Point(477, -206),
            // Stairs
            new Point(608, -86), new Point(723, -146), new Point(854, -146),
            // Bot row
            new Point(-271, 34), new Point(-124, 34), new Point(21, 34),
            new Point(169, 34), new Point(328, 34), new Point(481, 34) //, new Point(640, 34) // Clear for Visibility
    ));

    public static Point henesysHacker = new Point(908, 34);

    public static List<Point> ludiShopCoordinates = new ArrayList<>(List.of(
            // 28 Spots
            // Top
            new Point(-1769, -318), new Point(-1595, -318), new Point(-1409, -318), new Point(-1236, -318),
            new Point(-1081, -318), new Point(-916, -318), new Point(-751, -318), new Point(-580, -318), new Point(-421, -318),
            // Mid
            new Point(-1775, -108), new Point(-1608, -108), new Point(-1455, -108), new Point(-1290, -108),
            new Point(-1146, -108), new Point(-984, -108), new Point(-836, -108), new Point(-682, -108), new Point(-506, -108),
            // Bot
            new Point(-1893, 102), new Point(-1718, 102), new Point(-1561, 102), new Point(-1400, 102),
            new Point(-1230, 102), new Point(-1052, 102), new Point(-881, 102), new Point(-709, 102), new Point(-529, 102), new Point(-209, 102)
    ));

    public static List<Point> perionShopCoordinates = new ArrayList<>(List.of(
            // 26 spots
            // Top
            new Point(-418, 975), new Point(-276, 975), new Point(-119, 975), new Point(35, 975), new Point(170, 975),
            new Point(313, 975), new Point(477, 975),
            // Mid
            new Point(-465, 1185), new Point(-322, 1185), new Point(-169, 1185), new Point(-24, 1185), new Point(120, 1185),
            new Point(255, 1185), new Point(374, 1185), new Point(533, 1185),
            // Door
            new Point(-110, 1425), new Point(199, 1425),
            // Bot
            new Point(-508, 1515), new Point(-352, 1515), new Point(-203, 1515), new Point(-63, 1515), new Point(68, 1515),
            new Point(213, 1515), new Point(359, 1515), new Point(494, 1515), new Point(636, 1515)
    ));

    public static List<Point> elnathShopCoordinates = new ArrayList<>(List.of(
            // 27 Spots
            // Top
            new Point(159, -386), new Point(317, -386), new Point(458, -386), new Point(587, -386), new Point(730, -386),
            new Point(875, -386), new Point(1006, -386), new Point(1148, -386),
            // Mid
            new Point(60, -146), new Point(208, -146), new Point(356, -146), new Point(498, -146), new Point(639, -146),
            new Point(787, -146), new Point(933, -146), new Point(1086, -146), new Point(1244, -146),
            // Bot
            new Point(-190, 94), new Point(124, 94), new Point(283, 94), new Point(433, 94), new Point(574, 94),
            new Point(727, 94), new Point(876, 94), new Point(1024, 94), new Point(1158, 94), new Point(1319, 94)
    ));




    public Point modifyXCoordVariance(Point point) {
        int offset = random.nextInt(11) - 5;
        return new Point(point.x + offset, point.y);
    }

    public List<Integer> getRegionFMMapId(String regionName) {
        return switch (regionName.toLowerCase()) {
            case "henesys" -> henesysRegionFM;
            case "ludi" -> ludiRegionFM;
            case "perion" -> perionRegionFM;
            case "elnath" -> elnathRegionFM;
            default -> throw new IllegalArgumentException("Invalid region name: " + regionName);
        };
    }

    public static String getRegionByMapId(int mapId) {
        if (henesysRegionFM.contains(mapId)) {
            return "henesys";
        } else if (ludiRegionFM.contains(mapId)) {
            return "ludi";
        } else if (perionRegionFM.contains(mapId)) {
            return "perion";
        } else if (elnathRegionFM.contains(mapId)) {
            return "elnath";
        }
        return "unknown"; // Default if mapId doesn't belong to any list
    }

    public List<Point> getRegionFMSpots(String regionName) {
        return switch (regionName.toLowerCase()) {
            case "henesys" -> henesysShopCoordinates;
            case "ludi" -> ludiShopCoordinates;
            case "perion" -> perionShopCoordinates;
            case "elnath" -> elnathShopCoordinates;
            default -> throw new IllegalArgumentException("Invalid region name: " + regionName);
        };
    }






}
